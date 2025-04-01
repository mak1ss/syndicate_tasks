package com.task12.handlers.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.task12.dto.SignUpRequest;
import com.task12.services.CognitoService;
import org.json.JSONObject;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

public class SignUpHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private CognitoService cognitoService;

    public SignUpHandler(CognitoService cognitoService) {
        this.cognitoService = cognitoService;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            SignUpRequest signUp = SignUpRequest.fromJson(event.getBody());

            String userId = cognitoService.signUp(signUp)
                    .user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .map(AttributeType::value)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Sub not found."));

            String idToken = cognitoService.confirmSignUp(signUp)
                    .authenticationResult()
                    .idToken();

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(new JSONObject()
                            .put("message", "User has been successfully signed up.")
                            .put("userId", userId)
                            .put("accessToken", idToken)
                            .toString());
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(new JSONObject().put("error", e.getMessage()).toString());
        }
    }
}
