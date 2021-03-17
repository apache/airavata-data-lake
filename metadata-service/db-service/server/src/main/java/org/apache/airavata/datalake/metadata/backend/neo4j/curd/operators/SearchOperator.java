package org.apache.airavata.datalake.metadata.backend.neo4j.curd.operators;

import org.neo4j.ogm.cypher.ComparisonOperator;

public class SearchOperator {
    private String key;
    private String value;
    private ComparisonOperator comparisonOperator;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ComparisonOperator getComparisonOperator() {
        return comparisonOperator;
    }

    public void setComparisonOperator(ComparisonOperator comparisonOperator) {
        this.comparisonOperator = comparisonOperator;
    }
}
