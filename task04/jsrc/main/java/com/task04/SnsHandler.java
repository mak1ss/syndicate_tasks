package com.task04;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.syndicate.deployment.annotations.EventSource;
import com.syndicate.deployment.annotations.events.SnsEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.EventSourceType;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;


@LambdaHandler(
    lambdaName = "sns_handler",
	roleName = "sns_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@SnsEventSource(
        targetTopic = "lambda_topic"
)
@EventSource(
		eventType = EventSourceType.SNS_TOPIC_TRIGGER
)
@DependsOn(
		name = "lambda_topic",
		resourceType = ResourceType.SNS_TOPIC
)
public class SnsHandler implements RequestHandler<SNSEvent, Void> {

	public Void handleRequest(SNSEvent event, Context context) {
		event.getRecords().forEach(r -> processMessage(r.getSNS(), context));
		return null;
	}

	private void processMessage(SNSEvent.SNS msg, Context context) {
		try {
			context.getLogger().log("Processed message " + msg.getMessage());
		} catch (Exception e) {
			context.getLogger().log("An error occurred");
			throw e;
		}
	}
}
