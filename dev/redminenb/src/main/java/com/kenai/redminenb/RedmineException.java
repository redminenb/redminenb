package com.kenai.redminenb;

/**
 *
 * @author Mykolas
 */
public class RedmineException extends Exception {

    public RedmineException(Throwable cause) {
        super(cause);
    }

    public RedmineException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedmineException(String message) {
        super(message);
    }
}
