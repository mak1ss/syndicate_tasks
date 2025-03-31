package com.task11.services;

import com.task11.dto.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

public class CognitoService {

    private final String userPoolId = System.getenv("COGNITO_ID");
    private final String clientId = System.getenv("CLIENT_ID");
    private CognitoIdentityProviderClient cognitoClient;

    public CognitoService(CognitoIdentityProviderClient cognitoClient) {
        this.cognitoClient = cognitoClient;
    }

    public AdminCreateUserResponse signUp(SignUpRequest signUp) {
        var request = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .messageAction(MessageActionType.SUPPRESS)
                .username(signUp.email())
                .temporaryPassword(signUp.password())
                .userAttributes(AttributeType.builder()
                                .name("given_name")
                                .value(signUp.firstName())
                                .build(),
                        AttributeType.builder()
                                .name("family_name")
                                .value(signUp.lastName())
                                .build())
                .build();

        return cognitoClient.adminCreateUser(request);
    }

    public AdminRespondToAuthChallengeResponse confirmSignUp(SignUpRequest signUpRequest) {
        var adminCreateUserResponse = signIn(signUpRequest.email(), signUpRequest.password());
        Map<String, String> challengeResponses = Map.of(
                "USERNAME", signUpRequest.email(),
                "PASSWORD", signUpRequest.password(),
                "NEW_PASSWORD", signUpRequest.password()
        );

        return cognitoClient.adminRespondToAuthChallenge(AdminRespondToAuthChallengeRequest.builder()
                .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                .challengeResponses(challengeResponses)
                .userPoolId(userPoolId)
                .clientId(clientId)
                .session(adminCreateUserResponse.session())
                .build());
    }

    public AdminInitiateAuthResponse signIn(String email, String password) {
        Map<String, String> authParams = Map.of(
                "USERNAME", email,
                "PASSWORD", password
        );
        var request = AdminInitiateAuthRequest.builder()
                .clientId(clientId)
                .userPoolId(userPoolId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParams)
                .build();

        return cognitoClient.adminInitiateAuth(request);
    }
}
