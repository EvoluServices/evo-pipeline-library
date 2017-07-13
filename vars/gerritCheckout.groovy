#!/usr/bin/groovy


def call(repo, credentialsId, branch, refspec, additionalExtensions = []) {
  def defaultExtensions = [
    [
      $class      : 'BuildChooserSetting',
      buildChooser: [$class: 'GerritTriggerBuildChooser']
    ]
  ]

  checkout([
    $class                           : 'GitSCM',
    branches                         : [[name: branch]],
    doGenerateSubmoduleConfigurations: false,
    extensions                       : defaultExtensions + additionalExtensions,
    submoduleCfg                     : [],
    userRemoteConfigs                : [
      [
        credentialsId: credentialsId,
        refspec      : refspec,
        url          : "ssh://${repo}"
      ]
    ]
  ])
}