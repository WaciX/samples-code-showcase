package com.remotecall.lambda.function.unattendedAccess.specialist;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.remotecall.lambda.error.ValidationError;
import com.remotecall.lambda.error.ValidationUtils;
import com.remotecall.lambda.model.auth.AuthContext;
import com.remotecall.lambda.model.factory.WsRequestFactory;
import com.remotecall.lambda.model.factory.WsResponseFactory;
import com.remotecall.lambda.model.unattendedAccess.specialist.SpecialistUnattendedAccessRemoveRequest;
import com.remotecall.lambda.model.unattendedAccess.specialist.SpecialistUnattendedAccessUpdateRequest;
import com.remotecall.lambda.service.LogService;
import com.remotecall.lambda.service.unattendedAccess.SpecialistUnattendedService;
import io.quarkus.funqy.Funq;
import org.jboss.logging.Logger;

import javax.inject.Inject;

public class SpecialistUnattendedAccessUpdateFunction {

    @Inject
    Logger log;

    @Inject
    SpecialistUnattendedService specialistUnattendedService;

    @Inject
    WsRequestFactory wsRequestFactory;

    @Inject
    WsResponseFactory wsResponseFactory;

    @Inject
    LogService logService;

    @Funq
    public APIGatewayV2WebSocketResponse specialistUnattendedAccessUpdate(APIGatewayV2WebSocketEvent event) {
        log.debug(event);

        var requestContext = event.getRequestContext();

        SpecialistUnattendedAccessUpdateRequest request = null;

        try {
            request = wsRequestFactory.wsBodyToRequest(event.getBody(), SpecialistUnattendedAccessUpdateRequest.class);

            var authContext = AuthContext.readFromMap(requestContext.getAuthorizer());

            var specialistId = authContext.getSpecialistId();
            ValidationUtils.requireNotNull(specialistId, ValidationError.INVALID_REQUEST);

            specialistUnattendedService.update(specialistId, request);

            logService.info(event.getRequestContext(), "Updated: " + request);

            return wsResponseFactory.wsOkResponse(requestContext);
        } catch (Throwable throwable) {
            return wsResponseFactory.wsErrorResponse(requestContext, throwable, request);
        }
    }
}
