resource "aws_lb_target_group" "issuefy_target_group" {
  for_each = var.target_groups

  name     = each.key
  port     = each.value.port
  protocol = each.value.protocol
  vpc_id   = var.vpc_id
}
