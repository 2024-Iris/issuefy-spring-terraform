resource "aws_ecs_service" "services" {
  for_each        = var.ecs_services

  name            = each.key
  cluster         = var.cluster_id
  task_definition = each.value.task_definition
  desired_count   = each.value.desired_count
  iam_role        = each.value.iam_role_arn
}
