resource "aws_instance" "multi_role" {
  for_each = var.instance_definitions

  ami           = each.value.ami
  instance_type = each.value.instance_type
  subnet_id     = var.instance_subnet_map[each.key]
  vpc_security_group_ids = [var.ec2_sg_id]
  iam_instance_profile = try(each.value.iam_instance_profile, null)
  key_name = try(each.value.key_name, null)
  user_data = try(each.value.user_data, null)
  user_data_replace_on_change = true

  tags = {
    Name = "${var.name_prefix}-${each.key}"
  }
}