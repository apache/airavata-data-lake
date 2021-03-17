package org.apache.airavata.datalake.metadata.parsers;

import org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserParser {

    @Autowired
    private DozerBeanMapper dozerBeanMapper;


    public User parseUser(org.apache.airavata.datalake.metadata.service.User user) {

        return dozerBeanMapper.map(user,
                org.apache.airavata.datalake.metadata.backend.neo4j.model.nodes.User.class);
    }


}
