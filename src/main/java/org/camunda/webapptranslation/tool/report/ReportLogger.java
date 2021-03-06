package org.camunda.webapptranslation.tool.report;

import java.util.logging.Logger;

public class ReportLogger implements ReportInt {
    private static final Logger logger = Logger.getLogger(ReportLogger.class.getName());

    public void severe(Class<?> header, String msg, Exception e) {
        logger.severe(header.getName() + ": " + msg + " " + e.toString());
    }

    public void severe(Class<?> header, String msg) {
        logger.severe(header.getName() + ": " + msg);
    }

    public void info(Class<?> header, String msg) {
        logger.info(header.getName() + ": " + msg);

    }

}
