package org.apache.airavata.datalake.metadata.db.service.backend;

import java.io.Closeable;

public interface Connector extends Closeable {

    public boolean init();


}
