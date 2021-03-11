package org.apache.airavata.datalake.metadata.db.service.backend.neo4j.curd.operators;

import org.neo4j.ogm.cypher.query.SortOrder;

import java.util.Collection;
import java.util.List;

public interface Service<T> {

    Iterable<T> findAll();

    T find(Long id);

    void delete(Long id);

    Collection<T> search(List<SearchOperator> searchOperatorList);

    Iterable<T> sort(SortOrder.Direction sortOrder, String property);

    Iterable<T> sortAndPaging(SortOrder.Direction direction, int pageNumber, int itemsPerPage, String property);

    T createOrUpdate(T Object);

}
