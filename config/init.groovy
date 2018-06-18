import jenkins.model.*
import hudson.security.*
import hudson.model.UpdateSite
import hudson.PluginWrapper
import hudson.model.RestartListener
import java.util.logging.Level
import java.util.logging.Logger


def instance = Jenkins.getInstance()

def hudsonRealm = new HudsonPrivateSecurityRealm(false)
hudsonRealm.createAccount("{{cfg.admin.username}}", "{{cfg.admin.password}}")
instance.setSecurityRealm(hudsonRealm)
instance.save()

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.setSlaveAgentPort({{cfg.config.slavePort}})
instance.save()

//what plugins should be installed (by plugin ID)
Set<String> plugins_to_install = [
    "git",
    "github",
    "github-oauth",
    "token-macro",
    "cloudbees-folder",
    "job-dsl",
    "view-job-filters",
    "embeddable-build-status",
    "groovy",
    "dashboard-view",
    "rich-text-publisher-plugin",
    "console-column-plugin",
    "docker-plugin"
]
//should we dynamically load plugins when installed?
Boolean dynamicLoad = false

/*
   Install Jenkins plugins
 */
def install(Collection c, Boolean dynamicLoad, UpdateSite updateSite) {
    c.each {
        println "Installing ${it} plugin."
        UpdateSite.Plugin plugin = updateSite.getPlugin(it)
        Throwable error = plugin.deploy(dynamicLoad).get().getError()
        if(error != null) {
            println "ERROR installing ${it}, ${error}"
        }
    }
    null
}

/*
   Make it so...
 */

//some useful vars to set
Boolean hasConfigBeenUpdated = false

//check to see if using Jenkins Enterprise and if so then use its update site
Set<String> update_sites = []
Jenkins.getInstance().getUpdateCenter().getSiteList().each {
    update_sites << it.id
}
UpdateSite updateSite
if('jenkins-enterprise' in update_sites) {
    updateSite = Jenkins.getInstance().getUpdateCenter().getById('jenkins-enterprise')
} else {
    updateSite = Jenkins.getInstance().getUpdateCenter().getById('default')
}
println "Using update site ${updateSite.id}."

List<PluginWrapper> plugins = Jenkins.instance.pluginManager.getPlugins()

//check the update site(s) for latest plugins
println 'Checking plugin updates via Plugin Manager.'
Jenkins.instance.pluginManager.doCheckUpdatesServer()

//disable submitting usage statistics for privacy
if(Jenkins.instance.isUsageStatisticsCollected()) {
    println "Disable submitting anonymous usage statistics to jenkins-ci.org for privacy."
    Jenkins.instance.setNoUsageStatistics(true)
    hasConfigBeenUpdated = true
}

//any plugins need updating?
Set<String> plugins_to_update = []
plugins.each {
    if(it.hasUpdate()) {
        plugins_to_update << it.getShortName()
    }
}
if(plugins_to_update.size() > 0) {
    println "Updating plugins..."
    install(plugins_to_update, dynamicLoad, updateSite)
    println "Done updating plugins."
    hasConfigBeenUpdated = true
}


//get a list of installed plugins
Set<String> installed_plugins = []
plugins.each {
    installed_plugins << it.getShortName()
}

//check to see if there are missing plugins to install
Set<String> missing_plugins = plugins_to_install - installed_plugins
if(missing_plugins.size() > 0) {
    println "Install missing plugins..."
    install(missing_plugins, dynamicLoad, updateSite)
    println "Done installing missing plugins."
    hasConfigBeenUpdated = true
}

if(hasConfigBeenUpdated) {
    println "Saving Jenkins configuration to disk."
    Jenkins.instance.save()
} else {
    println "Jenkins up-to-date.  Nothing to do."
}

instance.save()

//user configurable variable
if(!binding.hasVariable('timeout_seconds')) {
    timeout_seconds = 60
}

if(timeout_seconds in String) {
    timeout_seconds = Integer.decode(timeout_seconds)
}

//type check user defined parameters/bindings
if(!(timeout_seconds in Integer)) {
    throw new Exception('PARAMETER ERROR: timeout_seconds must be an integer.')
}

Logger logger = Logger.getLogger('jenkins.instance.restart')

//start a background thread
def thread = Thread.start {
    logger.log(Level.INFO, "Jenkins safe restart initiated.")
    while(true) {
        if(Jenkins.instance.isQuietingDown()) {
            if(RestartListener.isAllReady()) {
                Jenkins.instance.restart()
            }
            logger.log(Level.INFO, "Jenkins jobs are not idle.  Waiting ${timeout_seconds} seconds before next restart attempt.")
            sleep(timeout_seconds*1000)
        }
        else {
            logger.log(Level.INFO, "Shutdown mode not enabled.  Jenkins restart aborted.")
            break
        }
    }
}

println 'A safe restart has been scheduled.  See the Jenkins logs for restart status updates.  Logger is jenkins.instance.restart.'
