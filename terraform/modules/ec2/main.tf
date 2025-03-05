resource "aws_instance" "multi_role" {
  for_each = var.instance_definitions

  ami           = each.value.ami
  instance_type = each.value.instance_type
  subnet_id     = var.instance_subnet_map[each.key]
  vpc_security_group_ids = [var.ec2_sg_id]

  tags = {
    Name = "${var.name_prefix}-${each.key}"
  }
}