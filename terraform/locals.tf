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
        def.iam_instance_profile != null
        ? (
        contains(keys(local.instance_profiles), def.iam_instance_profile)
        ? local.instance_profiles[def.iam_instance_profile]
        : null
      )
        : null
      )
    })
  }
}


locals {
  listeners = {
    web = {
      port             = 3000
      protocol         = "HTTP"
      target_group_arn = module.alb_target_group.target_group_arns["monitor"]
    }

    prometheus = {
      port             = 9090
      protocol         = "HTTP"
      target_group_arn = module.alb_target_group.target_group_arns["prometheus"]
    }

    loki = {
      port             = 3100
      protocol         = "HTTP"
      target_group_arn = module.alb_target_group.target_group_arns["loki"]
    }

    http = {
      port             = 80
      protocol         = "HTTP"
      target_group_arn = module.alb_target_group.target_group_arns["web"]
    }

    # except HTTPS listener
    # https = {
    #   port             = 443
    #   protocol         = "HTTPS"
    #   target_group_arn = module.alb_target_group.target_group_arns["web"]
    # }
  }
}

locals {
  target_groups = {
    "web" = {
      name     = "web"
      port     = 80
      protocol = "HTTP"
      path     = "/"
      health_check = {
        path                = "/"
        interval            = 30
        timeout             = 5
        healthy_threshold   = 3
        unhealthy_threshold = 2
      }
    }

    "monitor" = {
      name     = "monitor"
      port     = 3000
      protocol = "HTTP"
      path     = "/metrics"
      health_check = {
        path = "/login"
      }
    }

    "prometheus" = {
      name     = "prometheus"
      port     = 9090
      protocol = "HTTP"
      path     = "/"
      health_check = {
        path = "/graph"
      }
    }

    "loki" = {
      name     = "loki"
      port     = 3100
      protocol = "HTTP"
      path     = "/"
      health_check = {
        path = "/ready"
      }
    }

    "was" = {
      name     = "was"
      port     = 8080
      protocol = "HTTP"
      path     = "/"
      health_check = {
        path                = "/api/health"
        interval            = 30
        timeout             = 5
        healthy_threshold   = 3
        unhealthy_threshold = 2
      }
    }
  }
}
locals {
  ecr_repo_urls = {
    for name, repo in module.ecr :
    name => repo.repository_url
  }
}

locals {
  ecs_services = {
    "issuefy-was" = {
      name            = "issuefy-was"
      task_definition = module.ecs_task.task_definition_arns["issuefy-was"]
      desired_count   = 2
      iam_role_arn    = module.iam_roles["ecsTaskExecutionRole"].role_arn
      load_balancer = {
        target_group_arn = module.alb_target_group.target_group_arns["was"]
        container_name   = "issuefy-was"
        container_port   = 8080
      }
    }

    "issuefy-web" = {
      name            = "issuefy-web"
      task_definition = module.ecs_task.task_definition_arns["issuefy-web"]
      desired_count   = 1
      iam_role_arn    = module.iam_roles["ecsTaskExecutionRole"].role_arn
      load_balancer = {
        target_group_arn = module.alb_target_group.target_group_arns["web"]
        container_name   = "issuefy-web"
        container_port   = 80
      }
    }

    "issuefy-promtail" = {
      name            = "issuefy-promtail"
      task_definition = module.ecs_task.task_definition_arns["issuefy-promtail"]
      desired_count   = 1
      iam_role_arn    = module.iam_roles["ecsTaskExecutionRole"].role_arn
    }

    "issuefy-prometheus-lower" = {
      name            = "issuefy-prometheus-lower"
      task_definition = module.ecs_task.task_definition_arns["issuefy-prometheus-lower"]
      desired_count   = 1
      iam_role_arn    = module.iam_roles["ecsTaskExecutionRole"].role_arn
    }

    "issuefy-node-exporter" = {
      name            = "issuefy-node-exporter"
      task_definition = module.ecs_task.task_definition_arns["issuefy-node-exporter"]
      desired_count   = 1
      iam_role_arn    = module.iam_roles["ecsTaskExecutionRole"].role_arn
    }
  }
}

locals {
  ecs_task_definitions = {
    issuefy-was = {
      cpu                = 512
      memory             = 717
      network_mode       = "bridge"
      container_image    = "${local.ecr_repo_urls["issuefy-was"]}:latest"
      container_port = [8080, 9136]
      host_port = [0, 9136]
      log_group          = "/ecs/issuefy-was"
      task_role_arn      = module.iam_roles["ecsTaskExecutionRole"].role_arn
      execution_role_arn = module.iam_roles["ecsTaskExecutionRole"].role_arn
      environment = {}
      volumes = [
        {
          name      = "issuefy-log-volume"
          host_path = "/home/ec2-user/logs/"
        }
      ]
    }

    issuefy-web = {
      cpu                = 256
      memory             = 307
      network_mode       = "bridge"
      container_image    = "${local.ecr_repo_urls["issuefy-web"]}:latest"
      container_port = [80]
      host_port = [0]
      log_group          = "/ecs/issuefy-web"
      task_role_arn      = module.iam_roles["ecsTaskExecutionRole"].role_arn
      execution_role_arn = module.iam_roles["ecsTaskExecutionRole"].role_arn
      environment = {}
      volumes = []
    }

    issuefy-promtail = {
      cpu             = 256
      memory          = 262
      network_mode    = "host"
      container_image = "grafana/promtail:latest"
      container_port = []
      host_port = []
      log_group       = "/ecs/issuefy-promtail"
      task_role_arn   = module.iam_roles["ecsTaskExecutionRole"].role_arn
      execution_role_arn = module.iam_roles["ecsTaskExecutionRole"].role_arn
      ## have to change
      environment = {
        LOKI_URL = "http://10.0.15.90:3100/loki/api/v1/push"
      }
      volumes = [
        {
          name      = "issuefy-promtail-config"
          host_path = "/home/ec2-user/logs/config.yml"
        },
        {
          name      = "issuefy-promtail-logs"
          host_path = "/home/ec2-user/logs"
        }
      ]
    }

    issuefy-prometheus-lower = {
      cpu             = 256
      memory          = 262
      network_mode    = "host"
      container_image = "${local.ecr_repo_urls["issuefy-prometheus"]}:lower_1.0"

      container_port = [9090]
      host_port = [9090]
      log_group          = "/ecs/issuefy-prometheus-lower"
      task_role_arn      = module.iam_roles["ecsTaskExecutionRole"].role_arn
      execution_role_arn = module.iam_roles["ecsTaskExecutionRole"].role_arn
      environment = {}
      volumes = []
    }

    issuefy-node-exporter = {
      cpu                = 256
      memory             = 256
      network_mode       = "host"
      container_image    = "${local.ecr_repo_urls["issuefy-prometheus"]}:node_exporter_1.0"
      container_port = [9100]
      host_port = [9100]
      log_group          = "/ecs/issuefy-node-exporter"
      task_role_arn      = module.iam_roles["ecsTaskExecutionRole"].role_arn
      execution_role_arn = module.iam_roles["ecsTaskExecutionRole"].role_arn
      environment = {}
      volumes = []
    }
  }
}
