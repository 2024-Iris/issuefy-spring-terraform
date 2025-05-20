variable "ecs_task_definitions" {
  type = map(object({
    container_image    = string
    container_port = optional(list(number), [])
    host_port = optional(list(number), [])
    cpu                = number
    memory             = number
    task_role_arn      = string
    execution_role_arn = string
    network_mode       = string
    log_group          = string
    volumes = optional(list(object({
      name      = string
      host_path = string
    })), [])
    environment = optional(map(string), {})
  }))
}
