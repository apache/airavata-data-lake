package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, String> {


    @Query(value = "select * from RESOURCE t where t.PARENT_RESOURCE_ID =?1 and  t.TENANT_ID = ?2 " +
            "order by t.id limit ?3 offset ?4", nativeQuery = true)
    List<Resource> findAllByParentResourceIdAndTenantIdAndResourceTypeWithPagination(String parentResourdeId, String tenantId,
                                                                             int limit, int offset);


    List<Resource> findAllByParentResourceIdAndTenantId(String parentResourdeId, String tenantId);
}
