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
