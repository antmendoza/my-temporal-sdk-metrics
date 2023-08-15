package com.antmendoza.temporal.sinch.worker;

import com.antmendoza.temporal.sinch.ChatBotImpl;
import com.antmendoza.temporal.sinch.ProcessMessageImpl;
import com.antmendoza.temporal.sinch.ServiceFactory;
import com.antmendoza.temporal.sinch._3.ChatBotRequestImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;

import static com.antmendoza.temporal.sinch.Taskqueue.TASK_QUEUE;


public class WorkerChatBotSsl {


    private int workflowCacheSize = 20;
    private int maxConcurrentActivityExecutionSize = 20;
    private int maxConcurrentLocalActivityExecutionSize = 20;
    private int maxConcurrentWorkflowTaskExecutionSize = 20;

    private int maxWorkflowThreadCount = 10;

    public WorkerChatBotSsl(int workflowCacheSize,
                            int maxConcurrentActivityExecutionSize,
                            int maxConcurrentLocalActivityExecutionSize,
                            int maxConcurrentWorkflowTaskExecutionSize,
                            int maxWorkflowThreadCount) {
        this.workflowCacheSize = workflowCacheSize;
        this.maxConcurrentActivityExecutionSize = maxConcurrentActivityExecutionSize;
        this.maxConcurrentLocalActivityExecutionSize = maxConcurrentLocalActivityExecutionSize;
        this.maxConcurrentWorkflowTaskExecutionSize = maxConcurrentWorkflowTaskExecutionSize;
        this.maxWorkflowThreadCount = maxWorkflowThreadCount;
    }

    public void start(WorkflowServiceStubs service) {


        WorkflowClient client =
                WorkflowClient.newInstance(
                        service, WorkflowClientOptions.newBuilder()
                                .setNamespace(ServiceFactory.getNamespace())
                                .build());


        WorkerFactoryOptions build = WorkerFactoryOptions.newBuilder()
                .setWorkflowCacheSize(workflowCacheSize)
                .setMaxWorkflowThreadCount(maxWorkflowThreadCount)
                .build();

        WorkerFactory factory = WorkerFactory.newInstance(client, build);


        WorkerOptions workerOptions = WorkerOptions.newBuilder()
                //.setMaxConcurrentWorkflowTaskPollers(1)
                //.setMaxConcurrentLocalActivityExecutionSize(1)
                .setMaxConcurrentActivityExecutionSize(maxConcurrentActivityExecutionSize)
                .setMaxConcurrentLocalActivityExecutionSize(maxConcurrentLocalActivityExecutionSize)
                .setMaxConcurrentWorkflowTaskExecutionSize(maxConcurrentWorkflowTaskExecutionSize)

//                .setMaxConcurrentActivityTaskPollers(maxConcurrentActivityTaskPollers)
//                .setMaxConcurrentWorkflowTaskPollers(maxConcurrentWorkflowTaskPollers)
                .build();
        Worker worker = factory.newWorker(TASK_QUEUE, workerOptions);

        worker.registerWorkflowImplementationTypes(ChatBotImpl.class, ChatBotRequestImpl.class);
        worker.registerActivitiesImplementations(new ProcessMessageImpl());

        factory.start();
    }


}
