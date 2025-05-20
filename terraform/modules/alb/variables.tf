variable "name_prefix" {
  type = string
  description = "Prefix for ALB and related resources"
}

variable "subnets" {
  type = list(string)
  description = "List of public subnet IDs for the ALB"
}

variable "alb_security_group" {
  type = string
  description = "Security group ID for the ALB"
}

variable "loadbalancer_type" {
  type    = string
  default = "application"
  description = "Type of ALB (application | network)"
}

variable "internal" {
  type    = bool
  default = false
  description = "Whether the ALB is internal or internet-facing"
}
