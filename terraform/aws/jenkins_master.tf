resource "aws_instance" "jenkins_master" {
  connection {
    user        = "${var.aws_centos_image_user}"
    private_key = "${file("${var.aws_key_pair_file}")}"
  }

  ami                         = "${data.aws_ami.centos.id}"
  instance_type               = "${var.test_server_instance_type}"
  key_name                    = "${var.aws_key_pair_name}"
  subnet_id                   = "${aws_subnet.jenkins_demo_subnet.id}"
  vpc_security_group_ids      = ["${aws_security_group.jenkins_demo.id}", "${aws_security_group.habitat_supervisor.id}"]
  associate_public_ip_address = true

  tags {
    Name          = "jenkins_demo_${random_id.instance_id.hex}"
    X-Dept        = "${var.tag_dept}"
    X-Customer    = "${var.tag_customer}"
    X-Project     = "${var.tag_project}"
    X-Application = "${var.tag_application}"
    X-Contact     = "${var.tag_contact}"
    X-TTL         = "${var.tag_ttl}"
  }

  provisioner "remote-exec" {
    inline = [
      "sudo rm -rf /etc/machine-id",
      "sudo systemd-machine-id-setup",
      "sudo hostname jenkins-master-${var.prod_channel}",
    ]

    connection {
      host        = "${self.public_ip}"
      type        = "ssh"
      user        = "${var.aws_centos_image_user}"
      private_key = "${file("${var.aws_key_pair_file}")}"
    }
  }

  provisioner "habitat" {
    use_sudo     = true
    service_type = "systemd"

    service {
      name     = "${var.origin}/jenkins"
      channel  = "stable"
      user_toml = "${file("files/jenkins.toml")}"
    }

    connection {
      host        = "${self.public_ip}"
      type        = "ssh"
      user        = "${var.aws_centos_image_user}"
      private_key = "${file("${var.aws_key_pair_file}")}"
    }
  }
}

resource "aws_route53_record" "jenkins_master" {
  zone_id = "${var.route53_zone_id}"
  name    = "jenkins-master-${var.prod_channel}.${var.route53_zone_name}"
  type    = "A"
  ttl     = "300"
  records = ["${aws_instance.jenkins_master.public_ip}"]
}