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
  ec2_sg_id            = module.ec2-sg.ec2_sg_id
  name_prefix          = var.name_prefix
  instance_definitions = local.enriched_instance_definitions
}

module "ec2-sg" {
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
  vpc_security_group_ids = [module.ec2-sg.rds_sg_id]
  multi_az                = false
  backup_retention_period = 0
  tags = {
    Environment = "prod"
    Service     = "issuefy"
  }
}


module "ecr" {
  source = "./modules/ecr"

  for_each = var.ecr_repositories

  repository_name      = each.key
  scan_on_push         = each.value.scan_on_push
  image_tag_mutability = each.value.image_tag_mutability
  tags                 = each.value.tags
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

resource "aws_iam_instance_profile" "profiles" {
  for_each = local.iam_roles

  name = each.key
  role = module.iam_roles[each.key].role_name
}



