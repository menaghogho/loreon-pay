# Secrets Manager: Terraform creates only the empty secrets. Their values are set
# separately (see "Secrets" in the README), so they never reach Terraform state.

locals {
  # Secrets the cluster can read through External Secrets Operator
  cluster_secrets = toset([
    "loreon/catalog-db",
    "loreon/orders-db",
    "loreon/orders-rabbitmq",
  ])

  external_secrets_namespace       = "external-secrets"
  external_secrets_service_account = "external-secrets"
  oidc_issuer                      = replace(aws_iam_openid_connect_provider.eks.url, "https://", "")
}

resource "aws_secretsmanager_secret" "cluster" {
  for_each = local.cluster_secrets

  name = each.value
}

# Used only by Docker Compose on a developer machine; the cluster cannot read it
resource "aws_secretsmanager_secret" "local_compose" {
  name = "loreon/local/db-password"
}

# OIDC provider: lets Kubernetes service accounts assume IAM roles (IRSA)

resource "aws_iam_openid_connect_provider" "eks" {
  url            = aws_eks_cluster.main.identity[0].oidc[0].issuer
  client_id_list = ["sts.amazonaws.com"]
}

# IAM role for External Secrets Operator, limited to the cluster secrets above

resource "aws_iam_role" "external_secrets" {
  name = "${var.cluster_name}-external-secrets"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Action    = "sts:AssumeRoleWithWebIdentity"
      Principal = { Federated = aws_iam_openid_connect_provider.eks.arn }
      Condition = {
        StringEquals = {
          "${local.oidc_issuer}:aud" = "sts.amazonaws.com"
          "${local.oidc_issuer}:sub" = "system:serviceaccount:${local.external_secrets_namespace}:${local.external_secrets_service_account}"
        }
      }
    }]
  })
}

resource "aws_iam_role_policy" "external_secrets" {
  name = "read-loreon-secrets"
  role = aws_iam_role.external_secrets.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect = "Allow"
      Action = [
        "secretsmanager:GetSecretValue",
        "secretsmanager:DescribeSecret",
      ]
      Resource = [for secret in aws_secretsmanager_secret.cluster : secret.arn]
    }]
  })
}

output "external_secrets_role_arn" {
  description = "IAM role to set on the External Secrets Operator service account"
  value       = aws_iam_role.external_secrets.arn
}
