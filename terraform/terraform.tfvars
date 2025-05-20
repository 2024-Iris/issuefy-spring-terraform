ecr_repositories = {
  issuefy-was = {
    scan_on_push         = false
    image_tag_mutability = "MUTABLE"
    tags = {
      Service = "was"
    }
  }

  issuefy-prometheus = {
    scan_on_push         = false
    image_tag_mutability = "MUTABLE"
    tags = {
      Service = "prometheus"
    }
  }

  issuefy-web = {
    scan_on_push         = false
    image_tag_mutability = "MUTABLE"
    tags = {
      Service = "web"
    }
  }
}

instance_definitions = {
  prod = {
    ami                  = "ami-012ea6058806ff688"
    instance_type        = "t3a.small"
    iam_instance_profile = "ec2-to-ecs"
    key_name             = "issuefy-key"
  }

  monitoring = {
    ami                  = "ami-05377cf8cfef186c2"
    instance_type        = "t2.micro"
    iam_instance_profile = "ec2-monitoring"
    key_name             = "issuefy-key"
  }

  nat = {
    ami           = "ami-0fa9216d5e4fcd66d"
    instance_type = "t3.nano"
    key_name      = "issuefy-key"
  }
}
