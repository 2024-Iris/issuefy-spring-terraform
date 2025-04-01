output "ec2_sg_id" {
  value = aws_security_group.ec2.id
}

output "rds_sg_id" {
  description = "Security group ID for RDS"
  value       = aws_security_group.rds.id
}