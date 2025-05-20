resource "aws_service_discovery_private_dns_namespace" "issuefy-ns" {
  name = "issuefy-prod"
  vpc  = var.vpc_id
}