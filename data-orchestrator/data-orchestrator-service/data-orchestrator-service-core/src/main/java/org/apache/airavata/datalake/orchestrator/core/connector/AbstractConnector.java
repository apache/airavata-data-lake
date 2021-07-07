package org.apache.airavata.datalake.orchestrator.core.connector;

/**
 * Interface to implement external connectors
 */
public interface AbstractConnector<T> {

    void init(T configuration) throws Exception;

    void close() throws Exception;

    boolean isOpen();

}
