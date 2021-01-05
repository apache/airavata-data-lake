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
package org.apache.airavata.datalake.orchestrator.api.controller;

import org.apache.airavata.datalake.orchestrator.api.model.TransferEntry;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/transfer")
public class TransferController {

    @PostMapping(path = "")
    public String createTransfer(@RequestBody TransferEntry transferEntry) {
        return "transfer-id";
    }

    @GetMapping(path = "/start/{transferId}")
    public String startTransfer(@PathVariable(name = "transferId") String transferId) {
        return "trackingId";
    }

    @GetMapping(path = "/track/{trackingId}")
    public String trackTransfer(@PathVariable(name = "trackingId") String trackingId) {
        return "RUNNING";
    }

    @GetMapping(path = "/cancel/{trackingId}")
    public String cancelTransfer(@PathVariable(name = "trackingId") String trackingId) {
        return "CANCELLED";
    }
}
