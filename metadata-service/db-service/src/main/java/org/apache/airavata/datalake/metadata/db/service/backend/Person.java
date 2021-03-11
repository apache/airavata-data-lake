package org.apache.airavata.datalake.metadata.db.service.backend;


import org.neo4j.ogm.annotation.Properties;
import org.neo4j.ogm.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@NodeEntity
public class Person {


    @Id
    @GeneratedValue
//    //Add this annotation
//    @org.springframework.data.annotation.Id
//    @Property
    private Long id;

    @Property
    private String name;

    @Properties(allowCast = true)
    private Map<String, Object> valueProperties = new HashMap<>();

    private Person() {
        // Empty constructor required as of Neo4j API 2.0.5
    }

    ;

    public Person(String name) {
        this.name = name;
    }


    /**
     * Neo4j doesn't REALLY have bi-directional relationships. It just means when querying
     * to ignore the direction of the relationship.
     * https://dzone.com/articles/modelling-data-neo4j
     */
    @Relationship(type = "TEAMMATE")
    public Set<Person> teammates;

    public void worksWith(Person person) {
        if (teammates == null) {
            teammates = new HashSet<>();
        }
        teammates.add(person);
    }


    public void addProperty(String key, String value) {
        this.valueProperties.put(key, value);
    }

    public String toString() {

        return this.name + "'s teammates => "
                + Optional.ofNullable(this.teammates).orElse(
                Collections.emptySet()).stream()
                .map(Person::getName)
                .collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}