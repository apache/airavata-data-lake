package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.Connector;
import org.apache.airavata.datalake.metadata.backend.neo4j.Neo4JConnector;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.cypher.query.Pagination;
import org.neo4j.ogm.cypher.query.SortOrder;
import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;


public abstract class GenericService<T> implements Service<T> {

    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    private static final int DEPTH_LIST = 0;
    private static final int DEPTH_ENTITY = 1;

    private final Connector connector;

    private final Session session;

    public GenericService(Connector connector) {
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
        AtomicReference<Filter> filter = new AtomicReference<>();
        searchOperatorList.forEach(operator -> {
            if (filter.get() == null) {
                LOGGER.info("Setting filter###");
                filter.set(new Filter(operator.getKey(), operator.getComparisonOperator(), operator.getValue()));
            } else {
                filter.get().and(new Filter(operator.getKey(), operator.getComparisonOperator(), operator.getValue()));
            }

        });
        LOGGER.info("Loading ###" + getEntityType());
        return session.loadAll(getEntityType(), filter.get(), DEPTH_ENTITY);
    }

    @Override
    public Iterable<T> sort(SortOrder.Direction sortOrder, String property) {
        return session.loadAll(getEntityType(), new SortOrder().add(sortOrder, property));
    }

    @Override
    public Iterable<T> sortAndPaging(SortOrder.Direction sortOrder, int pageNumber,
                                     int itemsPerPage, String property) {
        return session.loadAll(getEntityType(),
                new SortOrder().add(sortOrder, property), new Pagination(pageNumber, itemsPerPage));
    }

    @Override
    public Iterable<Map<String, Object>> execute(String query) {
        return session.query(query, Collections.EMPTY_MAP);
    }

    @Override
    public void createOrUpdate(T entity) {
        session.save(entity);
    }

    abstract Class<T> getEntityType();
}
