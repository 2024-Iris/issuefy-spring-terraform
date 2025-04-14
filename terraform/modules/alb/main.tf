resource "aws_alb" "issuefy_alb" {
  name               = "${var.name_prefix}-alb"
  internal           = false
  load_balancer_type = var.loadbalancer_type
  security_groups    = [var.alb_security_group]
  subnets            = var.subnets

  enable_deletion_protection = true

  tags = {
    Environment = "issuefy-alb"
  }
}