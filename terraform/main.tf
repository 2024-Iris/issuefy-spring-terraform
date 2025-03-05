module "vpc" {
  source      = "./modules/vpc"
  name_prefix = var.name_prefix

  tags = {
    Environment = "prod"
  }
}

module "ec2" {
  source              = "./modules/ec2"
  instance_subnet_map = local.instance_subnet_map
  ec2_sg_id           = module.ec2-sg.ec2_sg_id
  name_prefix         = var.name_prefix
}

module "ec2-sg" {
  source      = "./modules/security"
  name_prefix = var.name_prefix
  vpc_id      = module.vpc.vpc_id

  tags = {
    Environment = "prod"
  }
}