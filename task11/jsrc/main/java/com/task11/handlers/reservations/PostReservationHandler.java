package com.task11.handlers.reservations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.dto.Reservation;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PostReservationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String reservationsTableName = System.getenv("RESERVATIONS_NAME");
    private String tablesTableName = System.getenv("TABLES_NAME");
    private AmazonDynamoDB dynamoDBClient;

    public PostReservationHandler(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            Reservation reservation = Reservation.fromJson(event.getBody());

            if(!isTableExists(reservation.getTableNumber())) {
                throw new IllegalArgumentException("Table " + reservation.getTableNumber() + " does not exist");
            }

            Map<String, AttributeValue> item = new HashMap<>();
            String id = UUID.randomUUID().toString();

            item.put("id", new AttributeValue().withS(id));
            item.put("tableNumber", new AttributeValue().withN(String.valueOf(reservation.getTableNumber())));
            item.put("clientName", new AttributeValue().withS(String.valueOf(reservation.getClientName())));
            item.put("date", new AttributeValue().withS(reservation.getDate().toString()));
            item.put("slotTimeStart", new AttributeValue().withS(String.valueOf(reservation.getSlotTimeStart())));
            item.put("slotTimeEnd", new AttributeValue().withS(String.valueOf(reservation.getSlotTimeEnd())));

            PutItemRequest putItemRequest = new PutItemRequest(reservationsTableName, item);
            dynamoDBClient.putItem(putItemRequest);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject().put("id", id).toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

    private boolean isTableExists(Integer tableNumber) {
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tablesTableName)
                .withFilterExpression("tableNumber = :tableNum")
                .withExpressionAttributeValues(Map.of(
                        ":tableNum", new AttributeValue().withN(tableNumber.toString())));

        ScanResult result = dynamoDBClient.scan(scanRequest);

        return result.getItems() != null;
    }
}
