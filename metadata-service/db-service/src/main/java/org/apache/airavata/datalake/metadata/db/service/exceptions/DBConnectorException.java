package org.apache.airavata.datalake.metadata.db.service.exceptions;

public class DBConnectorException extends RuntimeException{
    String msg;

    public DBConnectorException(String message, Throwable throwable) {
        super(message,throwable);
        this.msg = message;
    }
}
