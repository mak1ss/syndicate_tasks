package com.task12.handlers.reservations;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task12.dto.Reservation;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PostReservationHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String reservationsTableName = System.getenv("RESERVATIONS_NAME");
    private String tablesTableName = System.getenv("TABLES_NAME");
    private AmazonDynamoDB dynamoDBClient;
    private LambdaLogger logger;

    public PostReservationHandler(AmazonDynamoDB dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        logger = context.getLogger();
        try {
            Reservation reservation = Reservation.fromJson(event.getBody());

            if(!isTableExists(reservation.getTableNumber())) {
                throw new IllegalArgumentException("Table " + reservation.getTableNumber() + " does not exist");
            }
            if(isReservationOverlapping(reservation)) {
                throw new IllegalArgumentException("Your reservation on table "
                        + reservation.getTableNumber() + " is overlapping");
            }
            Map<String, AttributeValue> item = new HashMap<>();
            String id = UUID.randomUUID().toString();

            item.put("id", new AttributeValue().withS(id));
            item.put("tableNumber", new AttributeValue().withN(String.valueOf(reservation.getTableNumber())));
            item.put("clientName", new AttributeValue().withS(String.valueOf(reservation.getClientName())));
            item.put("phoneNumber", new AttributeValue().withS(String.valueOf(reservation.getPhoneNumber())));
            item.put("date", new AttributeValue().withS(reservation.getDate().toString()));
            item.put("slotTimeStart", new AttributeValue().withS(String.valueOf(reservation.getSlotTimeStart())));
            item.put("slotTimeEnd", new AttributeValue().withS(String.valueOf(reservation.getSlotTimeEnd())));

            PutItemRequest putItemRequest = new PutItemRequest(reservationsTableName, item);
            dynamoDBClient.putItem(putItemRequest);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject().put("reservationId", id).toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }

    private boolean isTableExists(Integer tableNumber) {
        logger.log("Looking for a table with number " + tableNumber);

        ScanRequest scanRequest = new ScanRequest()
                .withTableName(tablesTableName)
                .withFilterExpression("#num = :tableNum")
                .withExpressionAttributeNames(Map.of("#num", "number"))
                .withExpressionAttributeValues(Map.of(
                        ":tableNum", new AttributeValue().withN(tableNumber.toString())));

        ScanResult result = dynamoDBClient.scan(scanRequest);
        boolean isTableExists = result.getCount() > 0;
        if(isTableExists) {
            logger.log("Found table: \n" + result.getItems().get(0));
        }
        return isTableExists;
    }

    private boolean isReservationOverlapping(Reservation newReservation) {
        logger.log("Checking for reservation overlapping: \n" + newReservation);
        ScanRequest scanRequest = new ScanRequest()
                .withTableName(reservationsTableName)
                .withFilterExpression("tableNumber = :tableNum AND #dt = :dateVal")
                .withExpressionAttributeNames(Map.of(
                        "#dt", "date"))
                .withExpressionAttributeValues(Map.of(
                        ":tableNum", new AttributeValue().withN(newReservation.getTableNumber().toString()),
                        ":dateVal", new AttributeValue().withS(newReservation.getDate().toString())
                ));

        ScanResult result = dynamoDBClient.scan(scanRequest);
        List<Reservation> existingReservations = result.getItems().stream()
                .map(Reservation::new)
                .toList();

        if(!existingReservations.isEmpty()) {
            logger.log("Found reservations withing the same date: \n" + existingReservations);
        }
        return existingReservations.stream()
                .anyMatch(res -> isTimeOverlapping(res, newReservation));
    }

    private boolean isTimeOverlapping(Reservation existing, Reservation newReservation) {
        return !(newReservation.getSlotTimeEnd().isBefore(existing.getSlotTimeStart()) ||
                newReservation.getSlotTimeStart().isAfter(existing.getSlotTimeEnd()));
    }
}
