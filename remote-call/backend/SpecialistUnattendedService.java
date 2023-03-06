package com.remotecall.lambda.service.unattendedAccess;

import com.remotecall.lambda.entity.*;
import com.remotecall.lambda.error.ValidationError;
import com.remotecall.lambda.error.ValidationException;
import com.remotecall.lambda.error.ValidationUtils;
import com.remotecall.lambda.model.email.EmailRequest;
import com.remotecall.lambda.model.email.EmailType;
import com.remotecall.lambda.model.factory.WsResponseFactory;
import com.remotecall.lambda.model.unattendedAccess.email.CustomerUnattendedAccessEmail;
import com.remotecall.lambda.model.unattendedAccess.specialist.*;
import com.remotecall.lambda.service.ApiGatewayWebsocketManagementService;
import com.remotecall.lambda.service.QueueService;
import com.remotecall.lambda.service.WebsocketConnectionService;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@ApplicationScoped
public class SpecialistUnattendedService {

    private static final Pattern VALID_COLOR = Pattern.compile("[\\dA-F]{8}");

    @Inject
    Logger log;

    @Inject
    OpenInvitationsService openInvitationsService;

    @Inject
    WebsocketConnectionService websocketConnectionService;

    @Inject
    QueueService queueService;

    @Inject
    ApiGatewayWebsocketManagementService apiGatewayWebsocketManagementService;

    @Inject
    WsResponseFactory wsResponseFactory;

    // Redacted

    public void update(String specialistId, SpecialistUnattendedAccessUpdateRequest request) {
        validateUnattendedAccess(specialistId);

        Customer customer = Customer.findById(request.getCustomerId());
        ValidationUtils.requireNotNull(customer, ValidationError.NOT_FOUND, "Customer");

        OpenInvitations openInvitations = OpenInvitations.find("rep_id = ?1 and customer_macAddr = ?2", specialistId, request.getCustomerId())
                .firstResult();
        if (openInvitations == null) {
            throw new ValidationException(ValidationError.INVALID_DATA, "Unattended access not allowed: no open invitation for Customer");
        }

        String color = StringUtils.defaultIfBlank(request.getColor(), null);

        if (color != null && !VALID_COLOR.matcher(color).matches()) {
            throw new ValidationException(ValidationError.INVALID_DATA, "Invalid color. Needs to be in ARGB hex format (8 characters).");
        }

        openInvitationsService.updateColor(openInvitations, request.getColor());
    }

    private void validateUnattendedAccess(String specialistId) {
        Rep rep = Rep.findById(specialistId);

        log.debug(rep);

        if (!rep.getAcceptsOpenInvitations()) {
            throw new ValidationException(ValidationError.INVALID_DATA, "Specialist does not accept open invitations");
        }

        Subscriber subscriber = Subscriber.findById(rep.getSubscriber_id());

        log.debug(subscriber);

        Charge charge = Charge.findById(subscriber.getCharge_id());
        ValidationUtils.validateUnattendedAccessCharge(charge);
    }
}
