package org.apache.airavata.datalake.orchestrator.core.adaptors;

import java.util.List;

public interface StorageAdaptor<T> {

    void save(T object) throws UnsupportedOperationException;

    void delete(String id) throws UnsupportedOperationException;

    T get(String id) throws UnsupportedOperationException;

    T update(T object) throws UnsupportedOperationException;

    List<T> poll(int numOfEvents) throws UnsupportedOperationException;

}
