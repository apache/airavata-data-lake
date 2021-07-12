package org.apache.airavata.drms.core.deserializer;

import org.apache.airavata.datalake.drms.storage.AnyStorage;
import org.apache.airavata.datalake.drms.storage.TransferMapping;
import org.apache.airavata.datalake.drms.storage.TransferScope;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.InternalRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TransferMappingDeserializer {

    public static List<TransferMapping> deserializeList(List<Record> neo4jRecords) throws Exception {
        List<TransferMapping> transferMappings = new ArrayList<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();

            if (values.size() == 3) {
                Value srcStr = values.get(0);
                Value dstStr = values.get(1);
                Value tm = values.get(2);

                if (!srcStr.isNull() && !tm.isNull()
                        && !dstStr.isNull()) {
                    AnyStorage storage = AnyStorageDeserializer.deriveStorageFromMap(srcStr.asMap());
                    AnyStorage dstStorage = AnyStorageDeserializer.deriveStorageFromMap(dstStr.asMap());

                    Map<String, Object> map = tm.asMap();
                    TransferMapping transferMapping = TransferMapping.newBuilder()
                            .setTransferScope(TransferScope.valueOf(map.get("scope").toString()))
                            .setId(map.get("entityId").toString())
                            .setSourceStorage(storage)
                            .setDestinationStorage(dstStorage)
                            .setUserId(map.get("owner").toString())
                            .build();

                    transferMappings.add(transferMapping);

                }
            }
        }
        return transferMappings;
    }

    public static List<TransferMapping> deserializeListExceptDestinationStorage(List<Record> neo4jRecords) throws Exception {
        List<TransferMapping> transferMappings = new ArrayList<>();
        for (Record record : neo4jRecords) {
            InternalRecord internalRecord = (InternalRecord) record;
            List<Value> values = internalRecord.values();

            if (values.size() == 2) {
                Value srcStr = values.get(0);
                Value tm = values.get(1);

                if (!srcStr.isNull() && !tm.isNull()) {
                    AnyStorage storage = AnyStorageDeserializer.deriveStorageFromMap(srcStr.asMap());

                    Map<String, Object> map = tm.asMap();
                    TransferMapping transferMapping = TransferMapping.newBuilder()
                            .setTransferScope(TransferScope.valueOf(map.get("scope").toString()))
                            .setId(map.get("entityId").toString())
                            .setSourceStorage(storage)
                            .setUserId(map.get("owner").toString())
                            .build();

                    transferMappings.add(transferMapping);

                }
            }
        }
        return transferMappings;
    }

}
