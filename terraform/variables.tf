variable "name_prefix" {
  description = "project name prefix"
  type        = string
  default     = "issuefy"
}

variable "ecr_repositories" {
  type = map(object({
    scan_on_push         = bool
    image_tag_mutability = string
    tags = map(string)
  }))
}

variable "instance_definitions" {
  type = map(object({
    ami           = string
    instance_type = string
    iam_instance_profile = optional(string)
  }))
}

variable "tags" {
  type = map(string)
  default = {}
}


