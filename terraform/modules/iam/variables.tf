variable "cluster_name" {}
variable "task_family" {}
variable "task_cpu" {}
variable "task_memory" {}
variable "container_image" {}
variable "container_port" {}
variable "service_name" {}
variable "desired_count" {}
variable "subnets" { type = list(string) }
variable "security_group_id" {}
