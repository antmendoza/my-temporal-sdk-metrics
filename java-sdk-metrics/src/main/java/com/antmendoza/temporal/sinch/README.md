##

- Client send a request to the server to start a workflow
- Server persist the request, and deliver the task to the worker
- If there are no workers available, or the workers are overloaded the task will sit in the server 
until it can be delivered to the worker
- the worker get the task and execute the task, report back to the server and at the same time
store the workflow state in cache.
- sometimes, if your workers get restarted oryou don't have enough cache, the workflow state will be evicted from cache
- on the next workflow task delivered to the worker, the worker does not have the workflow state in cache, so
it has to poll the workflow history, and replay it to recover the workflow state and only then execute the task 


### Workflow per request
- advantages:
  - short running workflows:
    - easier to deal with, when you have to change your workflow code. 
  - do not handle event history limit and history size (50K)

- disadvantages:
  - have to keep the status of the entity in a separate database (send the request, update the db, and reply back)

### Signal + query
- signal: can change the workflow status, but can not return anything.
- query: can query workflow status, workflow variable, but can not mutate workflow state
  
- advantages:
    - Signaling a workflow is an is asynchronous operation
        - easier with you have to deploy changes to your workflow
    - Use other Temporal features like timers, cancellations etc.. 

- disadvantages:
    - more worklfow implementationn have to keep the status of the entity in a separate database (send the request, update the db, and reply back)
    -   - do not handle event history limit and history size (50K)




### Workflow update
This is a new feature of Temporal, before there was two main ways to interact with a workflow, 
- signal: can change the workflow status, but can not return anything.
- query: can query workflow status, workflow variable, but can not mutate workflow state

Now we have something called workflow update that allows the client to send a synchronous signal to the 
workflow and wait for the result. You can mutate workflow state and the method returns something. 




