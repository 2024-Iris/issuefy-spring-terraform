ecr_repositories = {
  was = {
    scan_on_push         = false
    image_tag_mutability = "MUTABLE"
    tags = {
      Service = "was"
    }
  }

  prometheus = {
    scan_on_push         = false
    image_tag_mutability = "MUTABLE"
    tags = {
      Service = "prometheus"
    }
  }

  web = {
    scan_on_push         = false
    image_tag_mutability = "MUTABLE"
    tags = {
      Service = "web"
    }
  }
}

instance_definitions = {
  prod = {
    ami                  = "ami-0163ca257044503d6"
    instance_type        = "t3a.small"
    iam_instance_profile = "ec2-to-ecs"
  }

  monitoring = {
    ami                  = "ami-0163ca257044503d6"
    instance_type        = "t2.micro"
    iam_instance_profile = "ec2-monitoring"
  }

  nat = {
    ami           = "ami-0fa9216d5e4fcd66d"
    instance_type = "t3.nano"
  }
}
