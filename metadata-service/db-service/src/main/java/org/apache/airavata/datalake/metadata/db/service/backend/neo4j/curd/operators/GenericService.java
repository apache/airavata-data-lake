package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.db.service.backend.Connector;
import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.Neo4JConnector;
import org.apache.airavata.datalake.metadata.db.service.backend.neo4j.model.nodes.Entity;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public abstract class GenericService<T> implements Service<T> {

    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 1;

    private Connector connector;

    private Session session;

    public GenericService(@Autowired Connector connector) {
        this.connector = connector;
        this.session = ((Neo4JConnector) this.connector).openConnection();
    }


    @Override
    public Iterable<T> findAll() {
        return session.loadAll(getEntityType(), DEPTH_LIST);
    }

    @Override
    public T find(Long id) {
        return session.load(getEntityType(), id, DEPTH_ENTITY);
    }

    @Override
    public void delete(Long id) {
        session.delete(session.load(getEntityType(), id));
    }

    @Override
    public Collection<T> search(List<SearchOperator> searchOperatorList) {
        AtomicReference<Filter> filter = null;
        searchOperatorList.forEach(value -> {
            if (filter.get() == null) {
                filter.set(new Filter(value.getValue(), value.getComparisonOperator(), value.getValue()));
            } else {
                filter.get().and(new Filter(value.getValue(), value.getComparisonOperator(), value.getValue()));
            }

        });
        return session.loadAll(getEntityType(), filter.get());
    }

    @Override
    public Iterable<T> sort(SortOrder.Direction sortOrder, String property) {
       return  session.loadAll(getEntityType(),new SortOrder().add(sortOrder,property));
    }

    @Override
    public Iterable<T> sortAndPaging(SortOrder.Direction sortOrder, int pageNumber,
                                     int itemsPerPage, String property) {
        return  session.loadAll(getEntityType(),
                new SortOrder().add(sortOrder,property), new Pagination(pageNumber,itemsPerPage));
    }

    @Override
    public T createOrUpdate(T entity) {
        session.save(entity, DEPTH_ENTITY);
        return find(((Entity) entity).getId());
    }

    abstract Class<T> getEntityType();
}
