#!/usr/bin/groovy


import com.cloudbees.groovy.cps.NonCPS

def call(currentBuild, channel) {
  def pb = currentBuild.previousBuild
  while(pb != null && pb.result == 'ABORTED') {
    pb = pb.previousBuild
  }

  def previousResult = (pb != null) ? pb.result : "SUCCESS"
  if (currentBuild.result == 'FAILURE' ||
     currentBuild.result == 'UNSTABLE' ||
     (currentBuild.result == 'SUCCESS' && previousResult != 'SUCCESS')) {

    // send build result
    slackSend channel: channel,
              color: getBuildColor(currentBuild.result),
              message: getBuildStatusMessage(currentBuild.result, previousResult)

    // send build changes
    slackSend channel: channel,
              color: getBuildColor(currentBuild.result),
              message: getChanges(currentBuild)
  }
}

@NonCPS
def getBuildColor(buildResult) {
  if (buildResult == 'SUCCESS') {
    return 'good'
  }

  if (buildResult == 'FAILURE') {
    return 'danger'
  }

  return 'warning'
}

@NonCPS
def getBuildStatusMessage(buildResult, previousResult) {
  def statusMessage = 'Unknown'

  if (buildResult == 'SUCCESS' && (previousResult == 'FAILURE' || previousResult == 'UNSTABLE')) {
    statusMessage = 'Back to normal'
  } else if (buildResult == 'FAILURE' && previousResult == 'FAILURE') {
    statusMessage = 'Still Failing'
  } else if (buildResult == 'SUCCESS') {
    statusMessage = 'Success'
  } else if (buildResult == 'FAILURE') {
    statusMessage = 'Failure'
  } else if (buildResult == 'ABORTED') {
    statusMessage = 'Aborted'
  } else if (buildResult == 'NOT_BUILT') {
    statusMessage = 'Not built'
  } else if (buildResult == 'UNSTABLE') {
    statusMessage = 'Unstable'
  }

  return "${env.JOB_NAME} - #${env.BUILD_NUMBER} ${statusMessage} (<${env.BUILD_URL}|Open>)"
}

@NonCPS
def getChanges(build) {
  def message = new StringBuffer().append("${env.JOB_NAME} - #${env.BUILD_NUMBER} ")

  def changeLogSets = build.rawBuild.changeSets
  if (changeLogSets.size() == 0) {
    message.append("No Changes.")
    return message.toString()
  }

  message.append("Changes:\n")
  for (int i = 0; i < changeLogSets.size(); i++) {
    def entries = changeLogSets[i].items
    for (int j = 0; j < entries.length; j++) {
      def entry = entries[j]
      message.append("- ${entry.msg} [${entry.author}]\n")
    }
  }

  return message.toString()
}
