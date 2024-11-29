resource "aws_vpc" "issuefy" {
  cidr_block                           = "10.0.0.0/16"
  enable_dns_hostnames                 = true
  enable_dns_support                   = true
  enable_network_address_usage_metrics = true

  tags = {
    Name        = "issuefy-vpc"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_subnet" "issuefy-pub-sub-2a" {
  vpc_id                  = aws_vpc.issuefy.id
  cidr_block              = "10.0.0.0/20"
  availability_zone       = "ap-northeast-2a"
  map_public_ip_on_launch = true
  tags = {
    Name        = "issuefy-pub-sub-2a"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_subnet" "issuefy-pri-sub-2a" {
  vpc_id            = aws_vpc.issuefy.id
  cidr_block        = "10.0.128.0/20"
  availability_zone = "ap-northeast-2a"
  tags = {
    Name        = "issuefy-pri-sub-2a"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_subnet" "issuefy-pub-sub-2c" {
  vpc_id                  = aws_vpc.issuefy.id
  cidr_block              = "10.0.16.0/20"
  availability_zone       = "ap-northeast-2c"
  map_public_ip_on_launch = true
  tags = {
    Name        = "issuefy-pub-sub-2c"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_subnet" "issuefy-pri-sub-2c" {
  vpc_id            = aws_vpc.issuefy.id
  cidr_block        = "10.0.144.0/20"
  availability_zone = "ap-northeast-2c"
  tags = {
    Name        = "issuefy-pri-sub-2c"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_internet_gateway" "issuefy-igw" {
  vpc_id = aws_vpc.issuefy.id
  tags = {
    Name        = "issuefy-igw"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_route_table" "issuefy-rt-pub" {
  vpc_id = aws_vpc.issuefy.id

  tags = {
    Name        = "issuefy-rt-pub"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_route_table" "issuefy-rt-pri-2a" {
  vpc_id = aws_vpc.issuefy.id

  tags = {
    Name        = "issuefy-rt-pri-2a"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_route_table" "issuefy-rt-pri-2c" {
  vpc_id = aws_vpc.issuefy.id

  tags = {
    Name        = "issuefy-rt-pri-2c"
    Environment = "prod"
    Owner       = "2024-iris"
    Project     = "issuefy"
    Service     = "infrastructure"
  }
}

resource "aws_route_table_association" "issuefy-rt-pub-2a" {
  subnet_id      = aws_subnet.issuefy-pub-sub-2a.id
  route_table_id = aws_route_table.issuefy-rt-pub.id
}

resource "aws_route_table_association" "issuefy-rt-pub-2c" {
  subnet_id      = aws_subnet.issuefy-pub-sub-2c.id
  route_table_id = aws_route_table.issuefy-rt-pub.id
}

resource "aws_route_table_association" "issuefy-rt-pri-2a" {
  subnet_id      = aws_subnet.issuefy-pri-sub-2a.id
  route_table_id = aws_route_table.issuefy-rt-pri-2a.id
}

resource "aws_route_table_association" "issuefy-rt-pri-2c" {
  subnet_id      = aws_subnet.issuefy-pri-sub-2c.id
  route_table_id = aws_route_table.issuefy-rt-pri-2c.id
}

resource "aws_route" "public-internet-gateway" {
  route_table_id         = aws_route_table.issuefy-rt-pub.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.issuefy-igw.id
}