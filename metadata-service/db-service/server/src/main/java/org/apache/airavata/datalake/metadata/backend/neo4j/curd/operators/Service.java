package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Entity;
import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.Tenant;
import org.neo4j.ogm.cypher.query.SortOrder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Service<T> {

    Iterable<T> findAll();

    T find(UUID id);

    void delete(UUID id);

    Collection<T> search(List<SearchOperator> searchOperatorList);

    Iterable<T> sort(SortOrder.Direction sortOrder, String property);

    Iterable<T> sortAndPaging(SortOrder.Direction direction, int pageNumber, int itemsPerPage, String property);

    Iterable<Map<String,Object>> execute(String query, Map<String, ?> parameterMap);

    void createOrUpdate(T Object);

    public List<T> find(T entity);

}
