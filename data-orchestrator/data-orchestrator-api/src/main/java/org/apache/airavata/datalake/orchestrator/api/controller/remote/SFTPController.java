/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.datalake.orchestrator.api.controller.remote;

import org.apache.airavata.datalake.orchestrator.api.db.entity.SFTPRemoteEntity;
import org.apache.airavata.datalake.orchestrator.api.db.repo.SFTPRemoteRepository;
import org.apache.airavata.datalake.orchestrator.api.model.remote.SFTPRemote;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping(path = "/remotes/sftp")
public class SFTPController {

    @Autowired
    private SFTPRemoteRepository sftpRemoteRepository;

    @GetMapping(value = "/{remoteId}", produces = "application/json")
    public SFTPRemote fetchSFTPRemote(@PathVariable(name = "remoteId") String remoteId) {

        Optional<SFTPRemoteEntity> entityOp = sftpRemoteRepository.findById(remoteId);
        SFTPRemoteEntity sftpRemoteEntity = entityOp.orElseThrow(
                                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, remoteId + " not found"));
        DozerBeanMapper mapper = new DozerBeanMapper();
        return mapper.map(sftpRemoteEntity, SFTPRemote.class);
    }

    @PostMapping(value = "")
    public String createSFTPRemote(@RequestBody SFTPRemote sftpRemote) {

        DozerBeanMapper mapper = new DozerBeanMapper();
        SFTPRemoteEntity sftpRemoteEntity = mapper.map(sftpRemote, SFTPRemoteEntity.class);
        SFTPRemoteEntity saved = sftpRemoteRepository.save(sftpRemoteEntity);
        return saved.getId();
    }

    @PutMapping(value = "/{remoteId}")
    public String updateSFTPRemote(@RequestBody SFTPRemote sftpRemote,
                                   @PathVariable(name = "remoteId") String remoteId) {

        DozerBeanMapper mapper = new DozerBeanMapper();
        SFTPRemoteEntity sftpRemoteEntity = mapper.map(sftpRemote, SFTPRemoteEntity.class);
        sftpRemoteEntity.setId(remoteId);
        SFTPRemoteEntity saved = sftpRemoteRepository.save(sftpRemoteEntity);
        return saved.getId();
    }

    @DeleteMapping(value = "/{remoteId}")
    public String removeSFTPRemote(@PathVariable(name = "remoteId") String remoteId) {

        if (! sftpRemoteRepository.existsById(remoteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, remoteId + " not found");
        }
        sftpRemoteRepository.deleteById(remoteId);
        return "Success";
    }
}
