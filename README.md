# Habitat package: jenkins

## Description

Build an run Jenkins preconfigured for CI/CD with habitat.

## Usage

`hab svc load jmery\jenkins`

See `default.toml` for default config values.

Includes a Terraform plan for AWS.  This will spin up the jenkins instance in aWS along with external DNS.   You must already have a TLD setup in Route53. Provide the zone name and zone id in your `terraform.tfvars` file.  See `example.tfvars`.
