### Service Execution Order

* Controller
* Participant
* DataSyncWorkflowManager

### Configure the participant with new tasks

* Extend the task class by BlockingTask or NonBlockingTask class
* Implement methods
* Annotate the class with @BlockingTaskDef or @NonBlockingTaskDef annotations. See ExampleBlockingTask and ExampleNonBlockingTask
* Register task in src/main/resources/task-list.yaml

