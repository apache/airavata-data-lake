package org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships;

import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "HAS_CHILD_GROUP")
public class HasChildGroup extends Relationship {


}
