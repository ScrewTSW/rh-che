# Mount volume

This job is running at [Codeready Workspaces Jenkins](https://codeready-workspaces-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/OSIO_CI/)  
This job is taking care of gathering metrics for performance of `glusterfs` based volume mounting for `PVC volumes` into pods on `OpenShift`
Currently this job is running against one [production](https://codeready-workspaces-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/OSIO_CI/job/mount-volume-production-2/) cluster and one `preview` cluster with two variants: [empty volume](https://codeready-workspaces-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/OSIO_CI/job/mount-volume-preview-2a/) and [large volume](https://codeready-workspaces-jenkins.rhev-ci-vms.eng.rdu2.redhat.com/view/OSIO_CI/job/mount-volume-preview-2a-large/) filled with reasonable amount of data  
In case the volumes have been deleted, the job has [logic to re-create](https://github.com/redhat-developer/rh-che/blob/master/.ci/mount-volume/setup_pvc.sh#L20) the volumes for both empty and large volumes.
In the past we had issues with the volume mounting taking too long time and was causing problems with workspaces at `OSIO`  

## Job workflow

This job consists of several elements:
* Mount volume [pipeline](https://github.com/redhat-developer/rh-che/blob/master/.ci/mount-volume/mount_volume_job.groovy), which takes care of getting the credentials for the cluster and passing it over to the `run_tests.sh` script.
* [setup_pvc.sh](https://github.com/redhat-developer/rh-che/blob/master/.ci/mount-volume/setup_pvc.sh) which takes care of the volume creation and filling them with data
* [run_tests.sh](https://github.com/redhat-developer/rh-che/blob/master/.ci/mount-volume/run_test.sh) script which runs the main logic behind the job, logs into the `OpenShift` cluster and manages the `openshift project`, calls the main test script `simple-pod.sh` and after the job has been completed calls `zabbix.sh` to process data and report it into [Zabbix](https://zabbix.devshift.net:9443/zabbix/latest.php?fullscreen=0&hostids%5B%5D=11838&hostids%5B%5D=11839&application=&select=mount_volume&show_without_data=1&filter_set=Filter)
* [simple-pod.sh](https://github.com/redhat-developer/rh-che/blob/master/.ci/mount-volume/simple-pod.sh) is the main script that takes care of creating, starting, stopping and deleting all the pods and mounting the correct volumes into them
* [zabbix.sh](https://github.com/redhat-developer/rh-che/blob/master/.ci/mount-volume/zabbix.sh) this script takes care of processing the locust output into zabbix compatible metrics csv and then uses `zabbix_sender` to send the data into our `Zabbix` instance

## Dependencies

* Python 3.x
* locust
* locustio
* [zabbix_sender](https://www.zabbix.com/documentation/current/manpages/zabbix_sender)