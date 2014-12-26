package com.kenai.redminenb.util;

import com.taskadapter.redmineapi.NotFoundException;
import com.taskadapter.redmineapi.RedmineAuthenticationException;
import com.taskadapter.redmineapi.RedmineProcessingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

public class ExceptionHandler {

    public static void handleException(Logger logger, String message, Exception ex) {
        if (ex instanceof RedmineProcessingException
                || ex instanceof NotFoundException
                || ex instanceof RedmineAuthenticationException) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    message + "\n\n" + ex.getMessage(),
                    NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            logger.log(Level.INFO, message, ex);
        } else {
            logger.log(Level.WARNING, message, ex);
        }
    }
}
