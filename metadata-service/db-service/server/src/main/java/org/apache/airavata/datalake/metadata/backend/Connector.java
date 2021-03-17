package org.apache.airavata.datalake.metadata.backend;

import java.io.Closeable;

public interface Connector extends Closeable {

    boolean init();


}
