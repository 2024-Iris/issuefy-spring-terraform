resource "aws_alb_listener" "web" {
  load_balancer_arn = var.alb_arn
  port              = "3000"
  protocol          = "HTTP"
  default_action {
    type = "forward"
  }
}

resource "aws_alb_listener" "prometheus" {
  load_balancer_arn = var.alb_arn
  port              = "9090"
  protocol          = "HTTP"
  default_action {
    type = "forward"
  }
}

resource "aws_alb_listener" "loki" {
  load_balancer_arn = var.alb_arn
  port              = "3100"
  protocol          = "HTTP"
  default_action {
    type = "forward"
  }
}

resource "aws_alb_listener" "http" {
  load_balancer_arn = var.alb_arn
  port              = "80"
  protocol          = "HTTP"
  default_action {
    type = "forward"
  }
}

resource "aws_alb_listener" "https" {
  load_balancer_arn = var.alb_arn
  port              = "443"
  protocol          = "HTTPS"
  default_action {
    type = "forward"
  }
}