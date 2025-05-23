module "vpc" {
  source      = "./modules/vpc"
  name_prefix = var.name_prefix

  tags = {
    Environment = "prod"
  }
}

module "ec2" {
  source               = "./modules/ec2"
  instance_subnet_map  = local.instance_subnet_map
  ec2_sg_id            = module.security_group.ec2_sg_id
  name_prefix          = var.name_prefix
  instance_definitions = local.enriched_instance_definitions
}

module "security_group" {
  source      = "./modules/security"
  name_prefix = var.name_prefix
  vpc_id      = module.vpc.vpc_id

  tags = {
    Environment = "prod"
  }
}

data "aws_ssm_parameter" "rds_user_name" {
  name            = "/rds-user-name"
  with_decryption = true
}

data "aws_ssm_parameter" "rds_password" {
  name            = "/rds-password"
  with_decryption = true
}

module "rds" {
  source                  = "./modules/rds"
  identifier              = "issuefy-db"
  instance_class          = "db.t3.micro"
  allocated_storage       = 20
  username                = data.aws_ssm_parameter.rds_user_name.value
  password                = data.aws_ssm_parameter.rds_password.value
  private_subnet_ids      = module.vpc.private_subnet_ids
  vpc_security_group_ids = [module.security_group.rds_sg_id]
  multi_az                = false
  backup_retention_period = 0
  tags = {
    Environment = "prod"
    Service     = "issuefy"
  }
}

module "iam" {
  source     = "./modules/iam"
  group_name = "issuefy_power"
  user_name  = "roy_power"

  policy_arns = [
    "arn:aws:iam::aws:policy/AdministratorAccess",
    "arn:aws:iam::aws:policy/AmazonElasticContainerRegistryPublicPowerUser",
    "arn:aws:iam::aws:policy/AmazonS3FullAccess",
    "arn:aws:iam::aws:policy/ElasticLoadBalancingReadOnly"
  ]

  enable_console_access  = true
  enable_mfa_enforcement = true

  tags = {
    Department = "issuefy"
    Role       = "power"
  }
}

module "iam_roles" {
  source   = "./modules/iamrole"
  for_each = local.iam_roles

  name                 = each.key
  assume_role_services = each.value.assume_role_services
  policy_arns          = each.value.policy_arns
  tags                 = each.value.tags
}

module "alb" {
  source             = "./modules/alb"
  alb_security_group = module.security_group.alb_sg_id
  name_prefix        = var.name_prefix
  subnets            = module.vpc.public_subnet_ids
}

module "alb_listener" {
  source    = "./modules/alb/listener"
  alb_arn   = module.alb.alb_arn
  listeners = local.listeners
}

module "alb_target_group" {
  source        = "./modules/alb/targetgroup"
  target_groups = local.target_groups
  vpc_id        = module.vpc.vpc_id
}

module "cloud_map" {
  source = "./modules/cloudmap"
  vpc_id = module.vpc.vpc_id
}

module "ecs_cluster" {
  source       = "./modules/ecs/cluster"
  namespace_id = module.cloud_map.namespace_id
  cluster_name = "${var.name_prefix}-cluster"
}

module "ecs_service" {
  source       = "./modules/ecs/service"
  cluster_id   = module.ecs_cluster.cluster_id
  ecs_services = local.ecs_services
}

module "ecs_task" {
  source               = "./modules/ecs/task"
  ecs_task_definitions = local.ecs_task_definitions
}

data "aws_ecr_repository" "issuefy_was" {
  name = "issuefy-was"
}

data "aws_ecr_repository" "issuefy_prometheus" {
  name = "issuefy-prometheus"
}

data "aws_ecr_repository" "issuefy_web" {
  name = "issuefy-web"
}
