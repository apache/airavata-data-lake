package org.apache.airavata.drms.core.deserializer;

import org.apache.airavata.datalake.drms.storage.AnyStorage;
import org.apache.airavata.datalake.drms.storage.AnyStoragePreference;
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

            if (values.size() == 5) {
                Value srcStr = values.get(0);
                Value srcSpr = values.get(1);
                Value dstStr = values.get(2);
                Value dstSp = values.get(3);
                Value tm = values.get(4);

                if (!srcStr.isNull() && !srcSpr.isNull() && !tm.isNull()
                        && !dstStr.isNull() && !dstSp.isNull()) {
                    AnyStorage storage = AnyStorageDeserializer.deriveStorageFromMap(srcStr.asMap());
                    AnyStoragePreference srcPreference = AnyStoragePreferenceDeserializer
                            .deriveStoragePrefFromMap(srcSpr.asMap(), storage);

                    AnyStorage dstStorage = AnyStorageDeserializer.deriveStorageFromMap(dstStr.asMap());
                    AnyStoragePreference dstPreference = AnyStoragePreferenceDeserializer
                            .deriveStoragePrefFromMap(dstSp.asMap(), dstStorage);

                    Map<String, Object> map = tm.asMap();
                    TransferMapping transferMapping = TransferMapping.newBuilder()
                            .setTransferScope(TransferScope.valueOf(map.get("scope").toString()))
                            .setId(map.get("entityId").toString())
                            .setSourceStoragePreference(srcPreference)
                            .setDestinationStoragePreference(dstPreference)
                            .setUserId(map.get("owner").toString())
                            .build();

                    transferMappings.add(transferMapping);

                }
            }
        }
        return transferMappings;
    }

}
