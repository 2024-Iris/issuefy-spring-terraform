resource "aws_iam_group" "this" {
  name = var.group_name
}

resource "aws_iam_user" "this" {
  name = var.user_name
  tags = var.tags
}

resource "aws_iam_group_membership" "membership" {
  name  = "${var.group_name}-membership"
  users = [aws_iam_user.this.name]
  group = aws_iam_group.this.name
}

resource "aws_iam_group_policy_attachment" "policies" {
  for_each = toset(var.policy_arns)

  group      = aws_iam_group.this.name
  policy_arn = each.value
}

resource "aws_iam_policy" "deny_if_no_mfa" {
  count  = var.enable_mfa_enforcement ? 1 : 0
  name   = "${var.group_name}-deny-no-mfa"
  policy = data.aws_iam_policy_document.deny_without_mfa.json
}

resource "aws_iam_group_policy_attachment" "deny_no_mfa_attach" {
  count      = var.enable_mfa_enforcement ? 1 : 0
  group      = aws_iam_group.this.name
  policy_arn = aws_iam_policy.deny_if_no_mfa[0].arn
}

data "aws_iam_policy_document" "deny_without_mfa" {
  statement {
    effect = "Deny"
    actions = ["*"]
    resources = ["*"]

    condition {
      test     = "BoolIfExists"
      variable = "aws:MultiFactorAuthPresent"
      values = ["false"]
    }

    principals {
      type = "AWS"
      identifiers = ["*"]
    }
  }
}
