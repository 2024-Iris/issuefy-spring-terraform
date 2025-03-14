resource "aws_ecs_task_definition" "tasks" {
  for_each = var.ecs_task_definitions

  family                   = each.key
  network_mode             = each.value.network_mode
  requires_compatibilities = ["EC2"]
  cpu                      = each.value.cpu
  memory                   = each.value.memory
  task_role_arn            = each.value.task_role_arn
  execution_role_arn       = each.value.execution_role_arn

  dynamic "volume" {
    for_each = each.value.volumes
    content {
      name      = volume.value.name
      host_path = volume.value.host_path
    }
  }

  container_definitions = jsonencode([
    {
      name      = each.key
      image     = each.value.container_image
      cpu       = each.value.cpu
      memory    = each.value.memory
      essential = true
      stopTimeout = 30
      portMappings = [for idx, port in each.value.container_port : {
        name          = "${each.key}-${port}-tcp"
        containerPort = port
        hostPort      = each.value.host_port[idx]
        protocol      = "tcp"
      }]
      environment = [for k, v in each.value.environment : { name = k, value = v }]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          awslogs-group         = each.value.log_group
          awslogs-create-group  = "true"
          awslogs-region        = "ap-northeast-2"
          awslogs-stream-prefix = "ecs"
        }
      }
      mountPoints = [for vol in each.value.volumes : {
        sourceVolume  = vol.name
        containerPath = (vol.name == "issuefy-promtail-config" ? "/etc/promtail/config.yml" : "/logs")
        readOnly      = false
      }]
    }
  ])
}
