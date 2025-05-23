variable "target_groups" {
  type = map(object({
    name     = string
    port     = number
    protocol = string
  }))
}

variable "vpc_id" {}