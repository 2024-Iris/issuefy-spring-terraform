output "target_group_arns" {
  value = {
    for k, tg in aws_lb_target_group.issuefy_target_group :
    k => tg.arn
  }
}

output "target_group_names" {
  value = {
    for k, tg in aws_lb_target_group.issuefy_target_group :
    k => tg.name
  }
}
