package org.apache.airavata.datalake.orchestrator.registry.persistance;

public enum EventStatus {
    DATA_ORCH_RECEIVED,
    WORKFLOW_LAUNCHED,
    DATA_ORCH_PROCESSED_AND_SKIPPED,
    MFT_CALLBACK_RECEIVED,
    COMPLETED,
    ERRORED,

}
