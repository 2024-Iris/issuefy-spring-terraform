resource "aws_ecs_cluster" "issuefy_cluster" {
  name = var.cluster_name

  setting {
    name  = "containerInsights"
    value = "disabled"
  }

  tags = {
    Name = var.cluster_name
  }
}