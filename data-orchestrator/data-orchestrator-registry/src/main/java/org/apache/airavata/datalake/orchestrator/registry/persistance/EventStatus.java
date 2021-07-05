package org.apache.airavata.datalake.orchestrator.registry.persistance;

public enum EventStatus {
    DATA_ORCH_RECEIVED,
    DISPATCHED_TO_WORFLOW_ENGING,
    DATA_ORCH_PROCESSED_AND_SKIPPED,
    MFT_CALLBACK_RECEIVED,
    COMPLETED,
    ERRORED,

}
