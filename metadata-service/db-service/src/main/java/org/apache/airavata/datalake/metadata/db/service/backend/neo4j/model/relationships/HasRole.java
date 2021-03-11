package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.relationships;

import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "HAS_ROLE")
public class HasRole extends Relationship{

}
