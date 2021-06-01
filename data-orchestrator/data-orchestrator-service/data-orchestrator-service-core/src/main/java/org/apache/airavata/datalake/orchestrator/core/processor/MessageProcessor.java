package org.apache.airavata.datalake.orchestrator.core.processor;

public interface MessageProcessor extends Runnable{

     void init() throws Exception;

      void close() throws Exception;
}
