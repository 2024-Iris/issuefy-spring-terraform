variable "identifier" {
  description = "RDS instance identifier"
  type        = string
}

variable "engine" {
  description = "Database engine"
  type        = string
  default     = "mysql"
}

variable "engine_version" {
  description = "Database engine version"
  type        = string
  default     = "8.0.40"
}

variable "instance_class" {
  description = "Instance type"
  type        = string
}

variable "allocated_storage" {
  description = "Storage size (GB)"
  type        = number
}

variable "username" {
  description = "Master username"
  type        = string
}

variable "password" {
  description = "Master password"
  type        = string
  sensitive   = true
}

variable "vpc_security_group_ids" {
  description = "List of VPC security group IDs"
  type = list(string)
}

variable "multi_az" {
  description = "Multi-AZ deployment"
  type        = bool
  default     = false
}

variable "backup_retention_period" {
  description = "Backup retention (in days)"
  type        = number
  default     = 0
}

variable "storage_encrypted" {
  description = "Whether to encrypt storage"
  type        = bool
  default     = true
}

variable "tags" {
  description = "Tags for the RDS instance"
  type = map(string)
  default = {}
}

variable "private_subnet_ids" {
  description = "Private subnet IDs to associate with DB subnet group"
  type = list(string)
}

