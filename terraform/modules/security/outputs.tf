output "ec2_sg_id" {
  description = "Security group ID for EC2"
  value       = aws_security_group.ec2.id
}

output "rds_sg_id" {
  description = "Security group ID for RDS"
  value       = aws_security_group.rds.id
}

output "alb_sg_id" {
  description = "Security group ID for ALB"
  value       = aws_security_group.rds.id
}