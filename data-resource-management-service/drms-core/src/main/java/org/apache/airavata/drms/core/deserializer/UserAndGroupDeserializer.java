package org.apache.airavata.drms.core.deserializer;

import org.apache.airavata.datalake.drms.groups.User;
import org.apache.airavata.drms.core.constants.UserAndGroupConstants;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalRecord;
import org.neo4j.driver.types.Node;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAndGroupDeserializer {


    public static List<User> deserializeUserList(List<Record> neo4jRecords) throws Exception {
        List<User> userList = new ArrayList<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();
            for (Value value : values) {
                Node node = value.asNode();
                if (node.hasLabel(UserAndGroupConstants.USER_LABEL)) {
                    userList.add(deriveUserFromMap(node.asMap()));
                }
            }
        }
        return userList;
    }

    public static User deriveUserFromMap(Map<String, Object> fixedMap) throws Exception {

        Map<String, Object> asMap = new HashMap<>(fixedMap);
        User.Builder builder = User.newBuilder();
        asMap.remove(UserAndGroupConstants.USER_LABEL);
        setObjectFieldsUsingMap(builder, asMap);
        return builder.build();
    }


    private static void setObjectFieldsUsingMap(Object target, Map<String, Object> values) {
        for (String field : values.keySet()) {
            BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(target);
            beanWrapper.setPropertyValue(field, values.get(field));
        }
    }
}
