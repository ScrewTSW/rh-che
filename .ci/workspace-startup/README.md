# Workspace startup

This job is running at [Codeready Workspaces Jenkins](https://codeready-workspaces-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/OSIO_CI/)  
The main logic behind these jobs is to collect data about workspace startup times for both [Ephemeral](https://codeready-workspaces-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/OSIO_CI/job/workspace-startup-test-ephemeral/) and [PVC](https://codeready-workspaces-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/OSIO_CI/job/workspace-startup-test/) devfiles.  
Data from these jobs is being reported into `#devtools-che` channel at [CoreOS slack](https://coreos.slack.com)  

## Job workflow

The jobs consist of several elements:  
* Jenkins pipelines [PVC](https://github.com/redhat-developer/rh-che/blob/master/.ci/workspace-startup/workspace-startup-test.groovy), [Ephemeral](https://github.com/redhat-developer/rh-che/blob/master/.ci/workspace-startup/workspace-startup-test-ephemeral.groovy), which take care of the job initialization, gather access tokens and prepare the job user accounts
* Python based [locust script](https://github.com/redhat-developer/rh-che/blob/master/.ci/workspace-startup/osioperf.py) which takes care of the workspace management and time collection
* [Zabbix reporter](https://github.com/redhat-developer/rh-che/blob/master/.ci/workspace-startup/workspace-startup-test.groovy#L57) which then processes the locust output logs and submits the data for each [zabbix trapper metric](https://zabbix.devshift.net:9443/zabbix/screenconf.php?filter_name=che-perf&filter_set=Filter)

## Dependencies

* Python 3.x
* locust
* locustio
* [zabbix_sender](https://www.zabbix.com/documentation/current/manpages/zabbix_sender)