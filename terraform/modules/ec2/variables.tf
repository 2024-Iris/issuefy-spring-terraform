variable "instance_definitions" {
  description = "EC2 instance definitions for different roles including AMI and instance type"
  type = map(object({
    ami           = string
    instance_type = string
  }))
  default = {
    prod = {
      ami           = "ami-0163ca257044503d6"
      instance_type = "t3a.small"
    }
    monitoring = {
      ami           = "ami-0163ca257044503d6"
      instance_type = "t2.micro"

    }
    nat = {
      ami           = "ami-0fa9216d5e4fcd66d"
      instance_type = "t3.nano"
    }
  }
}

variable "instance_subnet_map" {
  type = map(string)
}

variable "tags" {
  type = map(string)
  default = {}
}

variable "name_prefix" {
  description = "Name prefix for instance naming"
  type        = string
}

variable "ec2_sg_id" {
  description = "Security Group ID for EC2 instances"
  type        = string
}
