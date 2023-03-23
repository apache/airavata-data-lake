package org.apache.airavata.datalake.service.monitor;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;

import java.io.IOException;


public class MessageSender {

    public enum SERVICE_STATUS {
        STARTED,
        STOPPED,
    }

    private final String slackWebhook;

    private final Slack slack;

    public MessageSender(String slackWebhook) {
        this.slackWebhook = slackWebhook;
        this.slack = Slack.getInstance();
    }


    public void sendMessage(String service, SERVICE_STATUS status) throws IOException {
        StringBuilder payload = new StringBuilder();
        payload.append(service);
        payload.append(" ");


        Payload message = null;
        if (status == SERVICE_STATUS.STOPPED) {
            payload.append(" has  ");
            payload.append(" stopped ");
            message = Payload.builder()
                    .text(payload.toString())
                    .build();
        } else if (status == SERVICE_STATUS.STARTED) {
            payload.append(" was ");
            payload.append("  restarted ");
            message = Payload.builder()
                    .text(payload.toString())
                    .build();
        }
        this.slack.send(slackWebhook, message);
    }
}

