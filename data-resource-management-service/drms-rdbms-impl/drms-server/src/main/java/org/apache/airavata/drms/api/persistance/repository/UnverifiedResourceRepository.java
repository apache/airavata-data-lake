package org.apache.airavata.drms.api.persistance.repository;

import org.apache.airavata.drms.api.persistance.model.Resource;
import org.apache.airavata.drms.api.persistance.model.UnverifiedResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UnverifiedResourceRepository extends JpaRepository<UnverifiedResource, String>  {


    public List<UnverifiedResource> getUnverifiedResourceByUnverifiedAssociatedOwnerAndErrorCode(String username,String errorCode);

}
