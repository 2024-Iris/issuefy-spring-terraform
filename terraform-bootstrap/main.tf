terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.54.1"
    }
  }
}

provider "aws" {
  region = "ap-northeast-2"
}

resource "aws_s3_bucket" "issuefy_bucket" {
  bucket        = "issuefy-prod-terraform-state-ap-northeast-2"
  force_destroy = false

  tags = {
    Name        = "issuefy-prod-terraform-state-ap-northeast-2"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_s3_bucket_versioning" "issuefy_bucket_versioning" {
  bucket = aws_s3_bucket.issuefy_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_lifecycle_configuration" "issuefy_bucket_lifecycle" {
  bucket = aws_s3_bucket.issuefy_bucket.id

  rule {
    id     = "state-file-lifecycle"
    status = "Enabled"

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }

    noncurrent_version_expiration {
      noncurrent_days = 90
    }
  }
}

resource "aws_dynamodb_table" "terraform_lock" {
  name         = "issuefy-terraform-lock"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "LockID"

  attribute {
    name = "LockID"
    type = "S"
  }

  tags = {
    Name        = "issuefy-terraform-lock"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

module "ecr" {
  source = "../terraform/modules/ecr"

  for_each = var.ecr_repositories

  repository_name      = each.key
  scan_on_push         = each.value.scan_on_push
  image_tag_mutability = each.value.image_tag_mutability
  tags                 = each.value.tags
}