locals {
  instance_subnet_map = {
    prod       = module.vpc.public_subnet_ids[0]
    monitoring = module.vpc.public_subnet_ids[1]
    nat        = module.vpc.public_subnet_ids[0]
  }
}

locals {
  iam_roles = {
    "ec2-to-ecs" = {
      assume_role_services = ["ec2.amazonaws.com"]
      policy_arns = [
        "arn:aws:iam::aws:policy/service-role/AmazonEC2ContainerServiceforEC2Role",
        "arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess",
        "arn:aws:iam::aws:policy/AmazonECS_FullAccess"
      ]
      tags = {
        Purpose = "ECS EC2 Registration"
      }
    }

    "ecsTaskExecutionRole" = {
      assume_role_services = ["ecs-tasks.amazonaws.com"]
      policy_arns = [
        "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
      ]
      tags = {
        Purpose = "ECS Task Execution"
      }
    }

    "ec2-monitoring" = {
      assume_role_services = ["ec2.amazonaws.com"]
      policy_arns = [
        "arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess",
        "arn:aws:iam::aws:policy/AmazonS3FullAccess"
      ]
      tags = {
        Purpose = "EC2 Instance Monitoring"
      }
    }
  }
}

locals {
  instance_profiles = {
    for name, mod in module.iam_roles :
    name => mod.instance_profile_name
  }

  enriched_instance_definitions = {
    for name, def in var.instance_definitions :
    name => merge(def, {
      iam_instance_profile = (
        can(def.iam_instance_profile) ?
        try(local.instance_profiles[def.iam_instance_profile], null) :
        null
      )
    })
  }
}

