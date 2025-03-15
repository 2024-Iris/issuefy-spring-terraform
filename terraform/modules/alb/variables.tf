variable "alb_name" {}

variable "alb_target_group_name" {}

variable "loadbalancer_type" {
  default = "application"
}

variable "alb_security_group" {}

variable "subnets" {}

variable "name_prefix" {}