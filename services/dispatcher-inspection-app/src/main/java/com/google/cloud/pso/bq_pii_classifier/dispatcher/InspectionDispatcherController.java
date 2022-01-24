package com.google.cloud.pso.bq_pii_classifier.dispatcher;


import com.google.cloud.pso.bq_pii_classifier.functions.dispatcher.BigQueryScope;
import com.google.cloud.pso.bq_pii_classifier.functions.dispatcher.Dispatcher;
import com.google.cloud.pso.bq_pii_classifier.entities.NonRetryableApplicationException;
import com.google.cloud.pso.bq_pii_classifier.helpers.ControllerExceptionHelper;
import com.google.cloud.pso.bq_pii_classifier.helpers.LoggingHelper;
import com.google.cloud.pso.bq_pii_classifier.helpers.TrackingHelper;
import com.google.cloud.pso.bq_pii_classifier.services.BigQueryScannerImpl;
import com.google.cloud.pso.bq_pii_classifier.services.BigQueryServiceImpl;
import com.google.cloud.pso.bq_pii_classifier.services.PubSubServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.google.gson.Gson;
import com.google.cloud.pso.bq_pii_classifier.entities.PubSubEvent;

import java.util.Base64;


@SpringBootApplication(scanBasePackages = "com.google.cloud.pso.bq_pii_classifier")
@RestController
public class InspectionDispatcherController {

    private final LoggingHelper logger;

    private static final Integer functionNumber = 1;

    private Gson gson;
    private Environment environment;

    public InspectionDispatcherController() {

        gson = new Gson();
        environment = new Environment();
        logger = new LoggingHelper(
                InspectionDispatcherController.class.getSimpleName(),
                functionNumber,
                environment.getProjectId());
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public ResponseEntity receiveMessage(@RequestBody PubSubEvent requestBody) {

        String runId = TrackingHelper.generateInspectionRunId();

        try {

            if (requestBody == null || requestBody.getMessage() == null) {
                String msg = "Bad Request: invalid message format";
                logger.logSevereWithTracker(runId, msg);
                throw new NonRetryableApplicationException("Request body or message is Null.");
            }

            String requestJsonString = new String(Base64.getDecoder().decode(
                    requestBody.getMessage().getData()
            ));

            // remove any escape characters (e.g. from Terraform
            requestJsonString = requestJsonString.replace("\\", "");

            logger.logInfoWithTracker(runId, String.format("Received payload: %s", requestJsonString));

            BigQueryScope bqScope = gson.fromJson(requestJsonString, BigQueryScope.class);

            logger.logInfoWithTracker(runId, String.format("Parsed JSON input %s ", bqScope.toString()));

            Dispatcher dispatcher = new Dispatcher(
                    environment.toConfig(),
                    new BigQueryServiceImpl(),
                    new PubSubServiceImpl(),
                    new BigQueryScannerImpl(),
                    runId
            );

            dispatcher.execute(bqScope);

            return new ResponseEntity("Process completed successfully.", HttpStatus.OK);
        }
        catch (Exception e ){
            return ControllerExceptionHelper.handleException(e, logger, runId);
        }
    }

    public static void main(String[] args) {
        SpringApplication.run(InspectionDispatcherController.class, args);
    }
}

