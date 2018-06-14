import jenkins.model.Jenkins;

final List<String> REQUIRED_PLUGINS = [
  "aws-credentials",
  "copyartifact",
  "git",
  "ssh-agent",
  "tap",
  "workflow-aggregator",
]
if (Jenkins.instance.pluginManager.plugins.collect {
    it.shortName
  }.intersect(REQUIRED_PLUGINS).size() != REQUIRED_PLUGINS.size()) {
  REQUIRED_PLUGINS.collect {
    Jenkins.instance.updateCenter.getPlugin(it).deploy(true)
  }.each {
    it.get()
  }
  Jenkins.instance.restart()
}

Jenkins.instance.securityRealm.createAccount("admin","password123")

Jenkins.instance.setSlaveAgentPort(9999)
Jenkins.instnace.save()
