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
