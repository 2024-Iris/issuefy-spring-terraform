variable "ecs_services" {
  type = map(object({
    name            = string
    task_definition = string
    desired_count   = number
  }))
}

variable "cluster_id" {
  type = string
}
