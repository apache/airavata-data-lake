package org.apache.airavata.datalake.orchestrator.core.processor;

public interface MessageProcessor<T> extends Runnable{

     void init(T configuration) throws Exception;

      void close() throws Exception;
}
