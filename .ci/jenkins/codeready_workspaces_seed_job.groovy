String seedJobName = 'osio_ci_seed_job'
String[] worksapceStartupNames = ['workspace-startup-test-ephemeral', 'workspace-startup-test']
String workspaceStartupReporterJobName = 'workspace-startup-reporter'
String[] mountVolumeNames = ['mount-volume-preview-2a', 'mount-volume-preview-2a-large', 'mount-volume-production-2']

worksapceStartupNames.each { workspaceStartupJobName ->
  pipelineJob("${workspaceStartupJobName}") {
    concurrentBuild(false)
    definition {
      cpsScm {
        scm {
          git {
            remote { url('https://www.github.com/ScrewTSW/che-functional-tests.git') }
            branches('*/feature-migrate-jobs-to-crew-jenkins')
            scriptPath('che-start-workspace/' + "${workspaceStartupJobName}" + '.groovy')
            extensions { }
          }
        }
      }
    }
    parameters {
      stringParam('ZABBIX_SERVER', 'zabbix.devshift.net', 'An address of Zabbix server')
      stringParam('ZABBIX_PORT', '10051', 'A port of Zabbix server used by zabbix_sender utility')
      stringParam('CYCLES_COUNT', '1', 'Number of runs per user')
      stringParam('PIPELINE_TIMEOUT', '13', 'Job timeout in minutes')
      stringParam('START_SOFT_FAILURE_TIMEOUT', '60', 'Time in seconds after which the workspace startup is considered failed (didn\'t meet requirements)')
      stringParam('START_HARD_FAILURE_TIMEOUT', '300', 'Hard timeout for workspace startup (workspace failed to start)')
      stringParam('STOP_SOFT_FAILURE_TIMEOUT', '5', 'Time in seconds after which the workspace stop is considered failed (didn\'t meet requirements)')
      stringParam('STOP_HARD_FAILURE_TIMEOUT', '120', 'Hard timeout for workspace stop (workspace failed to stop)')
      credentialsParam('USERS_PROPERTIES_FILE_ID'){
        type('org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl')
        required()
        defaultValue("${workspaceStartupJobName}" + '.users.properties')
        description('StartWorkspaceTest user credentials')
      }
    }
    properties {
      buildDiscarder {
        strategy {
          logRotator {
            daysToKeepStr('3')
            numToKeepStr('')
            artifactDaysToKeepStr('')
            artifactNumToKeepStr('')
          }
        }
      }
      pipelineTriggers {
        triggers {
          cron {
            spec('H/15 * * * *')
          }
        }
      }
      durabilityHint {
        hint('MAX_SURVIVABILITY')
      }
    }
  }
}

pipelineJob("${workspaceStartupReporterJobName}") {
  definition {
    cpsScm {
      scm {
        git {
          remote { url('https://www.github.com/ScrewTSW/che-functional-tests.git') }
          branches('*/518-feature-implement-slack-reporter-for-zabbix-startup-data')
          scriptPath('che-start-workspace/' + "${workspaceStartupReporterJobName}" + '.groovy')
          extensions { }
        }
      }
      lightweight(true)
    }
  }
  parameters {
    stringParam('ZABBIX_URL', 'https://zabbix.devshift.net:9443/zabbix', 'URL for zabbix server endpoint')
    stringParam('SLACK_CHANNEL', '#devtools-che', 'Slack channel to send the reports to')
    credentialsParam('SLACK_URL_ID') {
      type('org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl')
      required()
      defaultValue('workspace-startup-reporter-slack-api-webhook-url')
      description('Slack API endpoint URL for bot account')
    }
    credentialsParam('ZABBIX_CREDENTIALS_ID') {
      type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
      required()
      defaultValue('workspace-startup-reporter-zabbix-auth')
      description('Credentials for Zabbix endpoint authentication to pull data via API')
    }
  }
  properties {
    buildDiscarder {
      strategy {
        logRotator {
          daysToKeepStr('7')
          numToKeepStr('')
          artifactDaysToKeepStr('')
          artifactNumToKeepStr('')
        }
      }
    }
    pipelineTriggers {
      triggers {
        cron {
          spec('H 10 * * *')
        }
      }
    }
  }
}

mountVolumeNames.each { mountVolumeJobName ->
  pipelineJob("${mountVolumeJobName}") {
    concurrentBuild(false)
    definition {
      cpsScm {
        scm {
          git {
            remote { url('https://www.github.com/ScrewTSW/rh-che.git') }
            branches('*/548-feature-use-jenkins-job-dsl-for-osioperf-jobs')
            scriptPath("${mountVolumeJobName}" + '.groovy')
            extensions { }
          }
        }
      }
    }
    parameters {
      stringParam('ZABBIX_SERVER', 'zabbix.devshift.net', 'An address of Zabbix server')
      stringParam('ZABBIX_PORT', '10051', 'A port of Zabbix server used by zabbix_sender utility')
      credentialsParam('MOUNT_VOLUME_ACCOUNT_CREDENTIALS_ID'){
        type('com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl')
        required()
        defaultValue("${mountVolumeJobName}" + '-credentials')
        description('Mount volume job account credentials')
      }
    }
    properties {
      buildDiscarder {
        strategy {
          logRotator {
            daysToKeepStr('28')
            numToKeepStr('')
            artifactDaysToKeepStr('')
            artifactNumToKeepStr('')
          }
        }
      }
      pipelineTriggers {
        triggers {
          cron {
            spec('H */2 * * *')
          }
        }
      }
    }
  }
}

listView('OSIO_CI') {
  description('Test view for learning DSL')
  filterBuildQueue()
  filterExecutors()
  jobs {
    for (jobName in worksapceStartupNames
    .plus(seedJobName)
    .plus(workspaceStartupReporterJobName)
    .plus(mountVolumeNames)) {
      name(jobName)
    }
  }
  columns {
    status()
    weather()
    name()
    lastSuccess()
    lastFailure()
    lastDuration()
    buildButton()
    lastBuildConsole()
  }
}