variable "group_name" {
  description = "IAM group name"
  type        = string
}

variable "user_name" {
  description = "IAM user name"
  type        = string
}

variable "policy_arns" {
  description = "List of IAM Policy ARNs to attach to the group"
  type = list(string)
}

variable "enable_console_access" {
  description = "Whether to enable AWS Management Console access"
  type        = bool
  default     = false
}

variable "console_password" {
  description = "Initial password for console login"
  type        = string
  default     = null
}

variable "enable_mfa_enforcement" {
  description = "Whether to enforce MFA via IAM policy"
  type        = bool
  default     = true
}


variable "tags" {
  description = "Tags for the IAM user"
  type = map(string)
  default = {}
}
