package com.task05;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.model.RetentionSetting;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@LambdaHandler(
    lambdaName = "api_handler",
	roleName = "api_handler-role",
	isPublishVersion = true,
	aliasName = "${lambdas_alias_name}",
	logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@EnvironmentVariables(value = {
		@EnvironmentVariable(key = "table_name", value = "${target_table}"),
		@EnvironmentVariable(key = "region", value = "${region}")}
)
public class ApiHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private AmazonDynamoDB amazonDynamoDB;

	String tableName = System.getenv("table_name");
	String region = System.getenv("region");

	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		var logger = context.getLogger();
		logger.log("Received request: " + request.getBody());
		ObjectMapper mapper = new ObjectMapper();

        Request personRequest = null;
        try {
            personRequest = mapper.readValue(request.getBody(), Request.class);
        } catch (JsonProcessingException e) {
			logger.log("Error occurred during request body deserialization: " + e.getMessage());
        }
        this.initDynamoDbClient();

		var result = persistData(personRequest);
		logger.log("Persisted data: " + result);

		var apiResponse = new APIGatewayProxyResponseEvent();
		apiResponse.setStatusCode(201);

        try {
            apiResponse.setBody(mapper.writeValueAsString(new Response.Event(result)));
        } catch (JsonProcessingException e) {
			logger.log("Error occurred during request body serialization: " + e.getMessage());
        }

        return apiResponse;
	}

	private Map<String, AttributeValue> persistData(Request personRequest) throws ConditionalCheckFailedException {

		Map<String, AttributeValue> attributesMap = new HashMap<>();

		String id = UUID.randomUUID().toString();
		attributesMap.put("id", new AttributeValue().withS(id));
		attributesMap.put("principalId", new AttributeValue().withN(String.valueOf(personRequest.getPrincipalId())));
		attributesMap.put("createdAt", new AttributeValue().withS(LocalDateTime.now().toString()));
		attributesMap.put("body", new AttributeValue().withM(Map.of(
				"name", new AttributeValue(personRequest.getContent().getName()),
				"surname", new AttributeValue(personRequest.getContent().getSurname())
		)));

		PutItemRequest req = new PutItemRequest(tableName, attributesMap);
		amazonDynamoDB.putItem(req);

		GetItemRequest getRequest = new GetItemRequest()
				.withTableName(tableName)
				.withKey(Map.of("id", new AttributeValue().withS(id)));

		return amazonDynamoDB.getItem(getRequest).getItem();
	}

	private void initDynamoDbClient() {
		this.amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
				.withRegion(region)
				.build();
	}
}
