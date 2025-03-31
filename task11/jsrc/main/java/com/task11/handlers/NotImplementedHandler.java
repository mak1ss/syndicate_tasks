package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import org.json.JSONObject;

public class NotImplementedHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(501)
                .withBody(
                        new JSONObject().put(
                                "message",
                                "Handler for the %s method on the %s path is not implemented."
                                        .formatted(event.getHttpMethod(), event.getPath())
                        ).toString()
                );
    }
}
