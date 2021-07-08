package org.apache.airavata.drms.core.serializer;

import com.google.protobuf.Descriptors;
import org.apache.airavata.datalake.drms.resource.GenericResource;

import java.util.HashMap;
import java.util.Map;

public class GenericResourceSerializer {


    public static Map<String, Object> serializeToMap(GenericResource anyResource) {

        Map<String, Object> fields = new HashMap<>();
        Map<Descriptors.FieldDescriptor, Object> allFields = anyResource.getAllFields();

        if (allFields != null) {
            allFields.forEach((descriptor, value) -> {
                String fieldName = descriptor.getJsonName();
                fields.put(fieldName, value);
            });
        }

        return fields;
    }


}
