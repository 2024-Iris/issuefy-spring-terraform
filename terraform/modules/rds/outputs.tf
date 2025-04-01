output "endpoint" {
  description = "RDS endpoint address"
  value       = aws_db_instance.this.endpoint
}

output "arn" {
  description = "RDS instance ARN"
  value       = aws_db_instance.this.arn
}