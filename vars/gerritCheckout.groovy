#!/usr/bin/groovy


def call(repo, credentialsId, branch, refspec) {
  checkout([
    $class                           : 'GitSCM',
    branches                         : [[name: branch]],
    doGenerateSubmoduleConfigurations: false,
    extensions                       : [
      [
        $class      : 'BuildChooserSetting',
        buildChooser: [$class: 'GerritTriggerBuildChooser']
      ]
    ],
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