locals {
  instance_subnet_map = {
    prod       = module.vpc.public_subnet_ids[0]
    monitoring = module.vpc.public_subnet_ids[1]
    nat        = module.vpc.public_subnet_ids[0]
  }
}