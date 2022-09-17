<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->
# Apache Airavata Data Lake

[![License](http://img.shields.io/badge/license-Apache--2-blue.svg?style=flat)](https://apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/apache/airavata-data-lake.svg?branch=master)](https://travis-ci.org/apache/airavata-data-lake)

<!---
1. Include Java Version
2. Instructions to set it up
-->

Apache Airavata use cases enable capture of data from observational and experimental instruments and computations resulting from Airavata's orchestration capabilities. As the data deluges into vast amounts, harvesting the data, capturing metadata, presenting it for easy and controlled access becomes unmanageable.

Airavata data lake will bundle stand alone services to catalog data in various data stores, extract and capture semantics and metadata descriptions of the data and preserve associated data provenance. The data lake will provide API's, query and search capabilities to programmatically search and retrieve data and power building user interactivity and data analysis applications on top of it.

![Airavata Data Lake Overview](https://cwiki.apache.org/confluence/download/attachments/165224787/Airavata%20Data%20Lake.png?version=1&modificationDate=1605020620000&api=v2)

Airavata Data Lake will provide file watcher and other trigger capabilities to ingest data from scientific instruments as they become available. The framwork will enable pluggable data parsers to read structured and unstructured data files and extract meaningful descriptions.

A bundled Data replica catalogs will associate pointers to data at multiple storgae locations. The replica catalog maps logical file names to the physical locations. Data Lake client SDK's will provide API functions to query replica location and resolve into multiple physical file locations. The client will be bundled with access protocols to retrive the data or to embedd into computational pipelines.

Interfacing with Airavata [Managed File Transfer Service](https://github.com/apache/airavata-mft) Data can moved and archiving into longer term persistant storages like tapped archives. The Data archives will be indexed and have search capabilities  

Data Lake's provenance will provide information to capture parameters influenced the production or modification of the data. An abstraction API will enable plugging fine granted provenance based on Airavata tentant context. Interfacing with Airavata Orchestration Services, pointers to experiment catalog will enable restructuring of the underting computations.
