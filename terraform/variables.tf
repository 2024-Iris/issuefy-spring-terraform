variable "name_prefix" {
  description = "project name prefix"
  type        = string
  default     = "issuefy"
}

variable "instance_definitions" {
  type = map(object({
    ami           = string
    instance_type = string
    iam_instance_profile = optional(string)
    key_name      = string
    user_data = optional(string)
  }))
}

variable "tags" {
  type = map(string)
  default = {}
}


