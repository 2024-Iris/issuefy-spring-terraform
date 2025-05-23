resource "aws_alb_listener" "this" {
  for_each = var.listeners

  load_balancer_arn = var.alb_arn
  port              = each.value.port
  protocol          = each.value.protocol

  default_action {
    type             = "forward"
    target_group_arn = each.value.target_group_arn
  }
}
