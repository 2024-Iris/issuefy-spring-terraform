resource "aws_ecs_cluster" "main" {
  name = var.cluster_name
}

resource "aws_ecs_task_definition" "app" {
  family                   = var.task_family
  network_mode             = "bridge"
  requires_compatibilities = ["FARGATE"]
  cpu                      = var.task_cpu
  memory                   = var.task_memory

  container_definitions = jsonencode([
        {
            "name": "issuefy-was",
            "image": "058264560944.dkr.ecr.ap-northeast-2.amazonaws.com/issuefy-ecr:1.18.2",
            "cpu": 0,
            "portMappings": [
                {
                    "name": "issuefy-was-80-tcp",
                    "containerPort": 8080,
                    "hostPort": 0,
                    "protocol": "tcp",
                    "appProtocol": "http"
                },
                {
                    "name": "issuefy-was-9136-tcp",
                    "containerPort": 9136,
                    "hostPort": 9136,
                    "protocol": "tcp"
                }
            ],
            "essential": true,
            "environment": [],
            "mountPoints": [
                {
                    "sourceVolume": "issuefy-log-volume",
                    "containerPath": "/logs",
                    "readOnly": false
                }
            ],
            "volumesFrom": [],
            "stopTimeout": 30,
            "systemControls": []
        }
    ])
}

resource "aws_ecs_service" "app" {
  name            = var.service_name
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets         = var.subnets
    security_groups = [var.security_group_id]
  }
}
