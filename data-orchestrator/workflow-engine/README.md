### Service Execution Order

* org.apache.airavata.datalake.workflow.engine.controller.Controller
* org.apache.airavata.datalake.workflow.engine.worker.Participant
* org.apache.airavata.datalake.workflow.engine.wm.datasync.DataSyncWorkflowManager

### Configure the participant with new tasks

* Extend the task class by BlockingTask or NonBlockingTask class
* Implement methods
* Annotate the class with @BlockingTaskDef or @NonBlockingTaskDef annotations. See ExampleBlockingTask and ExampleNonBlockingTask
* Register task in src/main/resources/task-list.yaml

