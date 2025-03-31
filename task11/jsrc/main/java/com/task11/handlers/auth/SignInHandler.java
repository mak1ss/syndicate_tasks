package com.task11.handlers.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task11.dto.SignInRequest;
import com.task11.services.CognitoService;
import org.json.JSONObject;

public class SignInHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private CognitoService cognitoService;

    public SignInHandler(CognitoService cognitoService) {
        this.cognitoService = cognitoService;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        try {
            SignInRequest request = SignInRequest.fromJson(input.getBody());

            String idToken = cognitoService.signIn(request.email(), request.password())
                    .authenticationResult()
                    .idToken();

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject().put("idToken", idToken).toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());

        }
    }
}
