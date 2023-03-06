package com.remotecall.lambda.function.unattendedAccess.specialist;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.remotecall.lambda.entity.OpenInvitations;
import com.remotecall.lambda.error.ValidationError;
import com.remotecall.lambda.model.email.EmailRequest;
import com.remotecall.lambda.model.email.EmailType;
import com.remotecall.lambda.model.factory.WsRequestFactory;
import com.remotecall.lambda.model.factory.WsResponseFactory;
import com.remotecall.lambda.model.unattendedAccess.email.CustomerUnattendedAccessEmail;
import com.remotecall.lambda.model.unattendedAccess.specialist.SpecialistUnattendedAccessRemoveRequest;
import com.remotecall.lambda.model.unattendedAccess.specialist.SpecialistUnattendedAccessUpdateRequest;
import com.remotecall.lambda.service.QueueService;
import com.remotecall.lambda.utils.*;
import io.quarkus.amazon.lambda.test.LambdaClient;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestProfile(TestProfiles.SpecialistUnattendedAccessUpdateFunctionProfile.class)
class SpecialistUnattendedAccessUpdateFunctionTest {

    @Inject
    DataExecuteHelper dataExecuteHelper;

    @Inject
    WsResponseFactory wsResponseFactory;

    @BeforeEach
    public void setUp() {
        dataExecuteHelper.cleanup();
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_incorrectRequestFormat_validationFailed() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriberInactive();
        var charge = DataFactory.buildCharge();
        dataExecuteHelper.persist(charge, subscriber, rep, customer);

        var event = WebSocketEventFactory.buildWebSocketEventForSpecialistUnattendedAccessTypeWithBody("{\"test\":1}");

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(DataFactory.buildValidationExceptionBody(ValidationError.INVALID_JSON.getFullMessage("Missing 'body' in the json request")));
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_incorrectRequestFormat2_validationFailed() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriberInactive();
        var charge = DataFactory.buildCharge();
        dataExecuteHelper.persist(charge, subscriber, rep, customer);

        var event = WebSocketEventFactory.buildWebSocketEventForSpecialistUnattendedAccessTypeWithBody("{\"body\":{\"test\":1}}");

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(DataFactory.buildValidationExceptionBody(ValidationError.INVALID_JSON.getFullMessage("[Field customerId, error: must not be blank, Field customerId, error: must not be null]")));
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_doesNotAcceptOpenInvitations_validationFailed() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialistWithoutUnattendedAccess();
        var subscriber = DataFactory.buildSubscriber();
        var charge = DataFactory.buildCharge();
        dataExecuteHelper.persist(charge, subscriber, rep, customer);

        var event = buildEvent(null);

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(DataFactory.buildValidationExceptionBody(ValidationError.INVALID_DATA.getFullMessage("Specialist does not accept open invitations")));
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_chargeDoesNotAllowOpenInvitations_validationFailed() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriber();
        var charge = DataFactory.buildChargeWithoutUnattendedAccess();
        dataExecuteHelper.persist(charge, subscriber, rep, customer);

        var event = buildEvent(null);

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(DataFactory.buildValidationExceptionBody(ValidationError.INVALID_DATA.getFullMessage("Charge does not allow for unattended access")));
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_chargeIncludeZeroOpenInvitations_validationFailed() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriber();
        var charge = DataFactory.buildChargeWithZeroIncludedUnattendedAccessGrants();
        dataExecuteHelper.persist(charge, subscriber, rep, customer);

        var event = buildEvent(null);

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(DataFactory.buildValidationExceptionBody(ValidationError.INVALID_DATA.getFullMessage("Charge does not allow for unattended access")));
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_colorNull_okResponseColorUpdated() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriber();
        var charge = DataFactory.buildCharge();
        var openInvitation1 = DataFactory.buildOpenInvitationC1R1();
        dataExecuteHelper.persist(charge, subscriber, rep, customer, openInvitation1);

        var event = buildEvent(null);

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\"action\":\"test-route\"}");
        List<OpenInvitations> openInvitations = OpenInvitations.listAll();
        assertThat(openInvitations).hasSize(1);
        var openInvitation = openInvitations.get(0);
        assertThat(openInvitation.getCustomer_macAddr()).isEqualTo(Constants.CUSTOMER_ID);
        assertThat(openInvitation.getRep_id()).isEqualTo(Constants.SPECIALIST_ID);
        assertThat(openInvitation.getStage()).isEqualTo(OpenInvitations.Stage.Z);
        assertThat(openInvitation.getColor()).isNull();
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_colorProvided_okResponseColorUpdated() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriber();
        var charge = DataFactory.buildCharge();
        var openInvitation1 = DataFactory.buildOpenInvitationC1R1();
        dataExecuteHelper.persist(charge, subscriber, rep, customer, openInvitation1);

        var event = buildEvent("FF00F0AB");

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\"action\":\"test-route\"}");
        List<OpenInvitations> openInvitations = OpenInvitations.listAll();
        assertThat(openInvitations).hasSize(1);
        var openInvitation = openInvitations.get(0);
        assertThat(openInvitation.getCustomer_macAddr()).isEqualTo(Constants.CUSTOMER_ID);
        assertThat(openInvitation.getRep_id()).isEqualTo(Constants.SPECIALIST_ID);
        assertThat(openInvitation.getStage()).isEqualTo(OpenInvitations.Stage.Z);
        assertThat(openInvitation.getColor()).isEqualTo("FF00F0AB");
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_colorInvalidSize_okResponseColorUpdated() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriber();
        var charge = DataFactory.buildCharge();
        var openInvitation1 = DataFactory.buildOpenInvitationC1R1();
        dataExecuteHelper.persist(charge, subscriber, rep, customer, openInvitation1);

        var event = buildEvent("F0F0F0");

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(DataFactory.buildValidationExceptionBody(ValidationError.INVALID_DATA.getFullMessage("Invalid color. Needs to be in ARGB hex format (8 characters).")));
    }

    @Test
    public void updateUnattendedAccessEventForSpecialist_colorInvalidFormat_okResponseColorUpdated() {
        var customer = DataFactory.buildCustomer();
        var rep = DataFactory.buildRepSpecialist();
        var subscriber = DataFactory.buildSubscriber();
        var charge = DataFactory.buildCharge();
        var openInvitation1 = DataFactory.buildOpenInvitationC1R1();
        dataExecuteHelper.persist(charge, subscriber, rep, customer, openInvitation1);

        var event = buildEvent("F0F0F0XX");

        var response = LambdaClient.invoke(APIGatewayV2WebSocketResponse.class, event);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(DataFactory.buildValidationExceptionBody(ValidationError.INVALID_DATA.getFullMessage("Invalid color. Needs to be in ARGB hex format (8 characters).")));
    }

    private APIGatewayV2WebSocketEvent buildEvent(String color) {
        var request = SpecialistUnattendedAccessUpdateRequest.builder()
                .customerId(Constants.CUSTOMER_ID)
                .color(color)
                .build();
        var response = wsResponseFactory.wsOkWithBodyResponse("test", request);
        return WebSocketEventFactory.buildWebSocketEventForSpecialistUnattendedAccessTypeWithBody(response.getBody());
    }
}
