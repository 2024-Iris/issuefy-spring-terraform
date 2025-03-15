resource "aws_lb_target_group" "web" {
  for_each = var.target_groups

  name     = each.key
  port     = each.value.port
  protocol = each.value.protocol
  vpc_id   = var.vpc_id
}

resource "aws_lb_target_group" "monitor" {
  for_each = var.target_groups

  name     = each.key
  port     = each.value.port
  protocol = each.value.protocol
  vpc_id   = var.vpc_id
}

resource "aws_lb_target_group" "prometheus" {
  for_each = var.target_groups

  name     = each.key
  port     = each.value.port
  protocol = each.value.protocol
  vpc_id   = var.vpc_id
}

resource "aws_lb_target_group" "web" {
  for_each = var.target_groups

  name     = each.key
  port     = each.value.port
  protocol = each.value.protocol
  vpc_id   = var.vpc_id
}