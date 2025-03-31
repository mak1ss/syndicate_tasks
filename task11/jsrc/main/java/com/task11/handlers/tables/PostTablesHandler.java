package com.task11.handlers.tables;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.dto.Table;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PostTablesHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String tableName = System.getenv("TABLES_NAME");
    private AmazonDynamoDB dynamoDBClient;

    public PostTablesHandler(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            Table table = Table.fromJson(event.getBody());
            Map<String, AttributeValue> item = new HashMap<>();
            Integer id = table.getId();

            item.put("id", new AttributeValue().withS(String.valueOf(id)));
            item.put("number", new AttributeValue().withN(String.valueOf(table.getNumber())));
            item.put("places", new AttributeValue().withN(String.valueOf(table.getPlaces())));
            item.put("isVip", new AttributeValue().withBOOL(table.getVip()));
            if (table.getMinOrder() != null) {
                item.put("minOrder", new AttributeValue().withN(String.valueOf(table.getMinOrder())));
            }

            PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
            dynamoDBClient.putItem(putItemRequest);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(201)
                    .withBody(new JSONObject().put("id", id).toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }
}
