package org.apache.airavata.datalake.metadata.mergers;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators.*;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.*;
import org.apache.airavata.datalake.metadata.parsers.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class GenericMerger implements Merger {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericMerger.class);

    private static Connector connector;

    public GenericMerger(Connector connector) {
        this.connector = connector;
    }

    @Override
    public Entity merge(Entity entity) {
        ExecutionContext executionContext = entity.getExecutionContext();
        executionContext.getNeo4JConvertedModels().values().forEach(en -> {
            LOGGER.info("Entity name " + ((Entity) en).getSearchableId());
            List<Entity> entityList = genericService((Entity) en).find(en);
            if (!entityList.isEmpty()) {
                Entity exEnt = entityList.get(0);
                LOGGER.info("selected entity name " + exEnt.getSearchableId());
                ((Entity) en).setId(exEnt.getId());
            }
        });
        return entity;
    }

    public static GenericService genericService(Entity entity) {
        if (entity instanceof Tenant) {
            return new TenantServiceImpl(connector);
        } else if (entity instanceof Resource) {
            return new ResourceServiceImpl(connector);
        } else if (entity instanceof Group) {
            return new GroupServiceImpl(connector);
        } else if (entity instanceof User) {
            return new UserServiceImpl(connector);
        }
        return null;
    }


}
