package org.apache.airavata.datalake.metadata.mergers;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;

public interface Merger {

    public Entity merge(Entity entity);

}
