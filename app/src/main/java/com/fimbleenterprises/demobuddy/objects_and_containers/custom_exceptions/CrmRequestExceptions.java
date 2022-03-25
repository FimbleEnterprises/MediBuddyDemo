package com.fimbleenterprises.demobuddy.objects_and_containers.custom_exceptions;

public class CrmRequestExceptions extends Exception {

    public CrmRequestExceptions(String title) {
        super(title);
    }

    /**
     * Exception raised when a Request object's arguments list (Requests.Request.arguments) is null.
     */
    public static class NullRequestException extends CrmRequestExceptions {
        public NullRequestException(String msg) {
            super(msg);
        }
    }

}
