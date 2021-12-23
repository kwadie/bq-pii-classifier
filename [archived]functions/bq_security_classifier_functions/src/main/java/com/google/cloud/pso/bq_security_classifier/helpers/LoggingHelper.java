package com.google.cloud.pso.bq_security_classifier.helpers;

import com.google.cloud.pso.bq_security_classifier.functions.tagger.Tagger;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggingHelper {

    private final Logger logger;
    private final String logName;
    private final String applicationName;
    private final String defaultLog;
    private final String trackerLog;
    private final Integer functionNumber;

    public LoggingHelper(String logName, String applicationName, String defaultLog, String trackerLog, Integer functionNumber) {
        this.logName = logName;
        this.applicationName = applicationName;
        this.defaultLog = defaultLog;
        this.trackerLog = trackerLog;
        this.functionNumber = functionNumber;

        logger = Logger.getLogger(logName);
    }

    public void logInfoWithTracker(String log, String tracker, String msg) {
        logWithTracker(log, tracker, msg, Level.INFO);
    }

    public void logInfoWithTracker(String tracker, String msg) {
        logWithTracker(defaultLog, tracker, msg, Level.INFO);
    }

    public void logWarnWithTracker(String log, String tracker, String msg) {
        logWithTracker(log, tracker, msg, Level.WARNING);
    }

    public void logWarnWithTracker(String tracker, String msg) {
        logWithTracker(defaultLog, tracker, msg, Level.WARNING);
    }

    public void logSevereWithTracker(String log, String tracker, String msg) {
        logWithTracker(log, tracker, msg, Level.SEVERE);
    }

    public void logSevereWithTracker(String tracker, String msg) {
        logWithTracker(defaultLog, tracker, msg, Level.SEVERE);
    }

    public void logWithTracker(String log, String tracker, String msg, Level level) {
        logger.log(level, String.format("%s | %s | %s | %s",
                applicationName,
                log,
                tracker,
                msg
                )
        );
    }

    public void logFunctionStart(String trackingId) {
        logInfoWithTracker(
                trackerLog,
                trackingId,
                String.format("%s | %s | %s",
                        logName,
                        functionNumber,
                        "Start")
        );
    }

    public void logFunctionEnd(String trackingId) {
        logInfoWithTracker(
                trackerLog,
                trackingId,
                String.format("%s | %s | %s",
                        logName,
                        functionNumber,
                        "End")
        );
    }
}
