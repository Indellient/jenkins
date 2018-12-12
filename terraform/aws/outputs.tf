output "jenkins_master_url" {
  value = "http://${aws_route53_record.jenkins_master.fqdn}:8080"
}

output "jenkins_master_ip" {
  value = "${aws_instance.jenkins_master.public_ip}"
}
