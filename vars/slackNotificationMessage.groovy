#!/usr/bin/groovy

import groovy.json.JsonOutput

def call(String channel, String message, String color = 'warning') {
  withCredentials([string(credentialsId: 'slack-webhook-url', variable: 'SLACK_WEBHOOK_URL')]) {
    def payload = [
      channel : channel,
      username: "Jenkins Jurassic",
      attachments: [[
        color: color,
        text : message
      ]]
    ]

    def jsonPayload = JsonOutput.toJson(payload)
    def escapedPayload = jsonPayload.replace("'", "'\"'\"'")

    sh """
      HTTP_CODE=\$(curl -sS -o /tmp/slack_response.txt -w "%{http_code}" -X POST \
        --data-urlencode 'payload=${escapedPayload}' "\$SLACK_WEBHOOK_URL")

      cat /tmp/slack_response.txt
      test "\$HTTP_CODE" = "200"
    """
  }
}