terraform {
  backend "s3" {
    bucket         = "issuefy-prod-terraform-state-ap-northeast-2"
    key            = "prod/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "issuefy-terraform-lock"
  }
}
