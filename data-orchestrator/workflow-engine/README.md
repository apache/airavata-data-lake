### Service Execution Order

* org.apache.airavata.datalake.orchestrator.workflow.engine.services.controller.Controller
* org.apache.airavata.datalake.orchestrator.workflow.engine.services.participant.Participant
* org.apache.airavata.datalake.orchestrator.workflow.engine.services.wm.DataSyncWorkflowManager

### Configure the participant with new tasks

* Extend the task class by BlockingTask or NonBlockingTask class
* Implement methods
* Annotate the class with @BlockingTaskDef or @NonBlockingTaskDef annotations. See ExampleBlockingTask and ExampleNonBlockingTask
* Register task in src/main/resources/task-list.yaml

