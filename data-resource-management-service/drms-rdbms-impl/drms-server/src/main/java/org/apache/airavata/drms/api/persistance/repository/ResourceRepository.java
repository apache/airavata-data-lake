package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.ResourceProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, String> {


    @Query(value = "select * from RESOURCE t where t.parent_resource_id =?1 and  t.tenant_id = ?2 and  t.type = ?3 " +
            "order by t.id limit ?3 offset ?4", nativeQuery = true)
    List<Resource> findAllByParentResourceIdAndTenantIdAndResourceTypeWithPagination(String parentResourdeId, String tenantId, String type,
                                                                             int limit, int offset);


    List<Resource> findAllByParentResourceIdAndTenantIdAndResourceType(String parentResourdeId, String tenantId, String type);
}
