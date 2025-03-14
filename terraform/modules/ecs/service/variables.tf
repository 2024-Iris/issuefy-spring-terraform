variable "ecs_services" {
  type = map(object({
    name            = string
    task_definition = string
    desired_count   = number
    iam_role_arn    = string
  }))
}

variable "cluster_id" {
  type = string
}
