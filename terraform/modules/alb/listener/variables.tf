variable "alb_arn" {}

variable "listeners" {
  type = map(object({
    port             = number
    protocol         = string
    target_group_arn = string
  }))
}
