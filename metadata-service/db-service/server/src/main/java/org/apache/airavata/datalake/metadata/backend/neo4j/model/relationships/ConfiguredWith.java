package org.apache.airavata.datalake.metadata.backend.neo4j.model.relationships;

import org.neo4j.ogm.annotation.RelationshipEntity;

@RelationshipEntity(type = "CONFIGURED_WITH")
public class ConfiguredWith extends Relationship {
}
