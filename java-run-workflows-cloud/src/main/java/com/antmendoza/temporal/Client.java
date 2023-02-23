package com.antmendoza.temporal;

import com.google.protobuf.ByteString;
import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.SearchAttributes;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.workflow.v1.WorkflowExecutionInfo;
import io.temporal.api.workflowservice.v1.*;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.OperatorServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.util.Map;

public class Client {
    static SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();
    static WorkflowServiceStubs service =
            WorkflowServiceStubs.newServiceStubs(
                    WorkflowServiceStubsOptions.newBuilder()
                            .setSslContext(sslContextBuilderProvider.getSslContext())
                            .setTarget(sslContextBuilderProvider.getTargetEndpoint())
                            .build());






    // Now setup and start workflow worker, which uses SSL enabled gRPC service to communicate with
    // backend.
    // client that can be used to start and signal workflows.
    static WorkflowClient client =
            WorkflowClient.newInstance(
                    service, WorkflowClientOptions.newBuilder()
                            .setNamespace(sslContextBuilderProvider.getNamespace())
                            .build());



    public static void main(String[] args) throws Exception {
        //listWF(null);

        listOpenWorkflows(null);


    }



    private static void listOpenWorkflows(ByteString token) {
        ListOpenWorkflowExecutionsRequest req;
        if (token == null) {
            System.out.println("******** Open workflow executions: ");
            req = ListOpenWorkflowExecutionsRequest.newBuilder()
                    .setNamespace(client.getOptions().getNamespace())
                    .build();
        } else {
            req = ListOpenWorkflowExecutionsRequest.newBuilder()
                    .setNamespace(client.getOptions().getNamespace())
                    .setNextPageToken(token)
                    .build();
        }



        try{

            ScanWorkflowExecutionsRequest scanWorkflowExecutionsRequest = ScanWorkflowExecutionsRequest.newBuilder()
                    //.setNamespace(sslContextBuilderProvider.getNamespace())
                    .setQuery("ExecutionStatus=\"TimedOut\"").build();
            ScanWorkflowExecutionsResponse scan = service.blockingStub().scanWorkflowExecutions(scanWorkflowExecutionsRequest);

            for (WorkflowExecutionInfo exec : scan.getExecutionsList()) {
                SearchAttributes searchAttributes = exec.getSearchAttributes();

                Map<String, Payload> indexedFieldsMap = searchAttributes.getIndexedFieldsMap();
                indexedFieldsMap.get("WorkflowId");
            }

        }catch(Exception e){
            System.out.println("Exceotion");
        }


        try{

        DescribeWorkflowExecutionResponse desc =
                service.blockingStub().describeWorkflowExecution(
                        DescribeWorkflowExecutionRequest.newBuilder().setNamespace("antonio-perez.temporal-dev")
                        .setExecution(WorkflowExecution.newBuilder().setWorkflowId("2f16b29f-9385-4031-9efd-9ab0899516e7").build()

                        ).build()
                );



        System.out.println(desc.getWorkflowExecutionInfo().getStatus());




        }catch(Exception e){



            e.printStackTrace();
        }

        ListOpenWorkflowExecutionsResponse res =
                service.blockingStub().listOpenWorkflowExecutions(ListOpenWorkflowExecutionsRequest.newBuilder()
                        .setNamespace(client.getOptions().getNamespace())
                        .build());
        for (WorkflowExecutionInfo info : res.getExecutionsList()) {
            System.out.println("* id: " + info.getExecution().getWorkflowId() + " rid: " +
                    info.getExecution().getRunId() + " status: " + info.getStatus().name());
        }

        if (res.getNextPageToken() != null && res.getNextPageToken().size() > 0) {
            listOpenWorkflows(res.getNextPageToken());
        }
    }

    private static void listWF(ByteString token) {


        final ListWorkflowExecutionsRequest request;
        final String query = "CloseTime between '2021-10-22T15:04:05+00:00' and '2022-09-26T10:24:05+00:00'";
        int pageSize = 1000;
        final String namespace = "default";//sslContextBuilderProvider.getNamespace();
        if (token == null) {
            request = ListWorkflowExecutionsRequest.newBuilder()
                    .setNamespace(namespace)
                    .setQuery(query)
                    .setPageSize(pageSize)
                    .build();
        } else {
            request = ListWorkflowExecutionsRequest.newBuilder()
                    .setNamespace(namespace)
                    .setQuery(query)
                    .setNextPageToken(token)
                    .setPageSize(pageSize)
                    .build();
        }
        final ListWorkflowExecutionsResponse response =
                service.blockingStub().listWorkflowExecutions(request);

        System.out.println("Size " + response.getExecutionsList().size());

        if (response.getNextPageToken() != null && response.getNextPageToken().size() > 0) {
            listWF(response.getNextPageToken());
        }
    }
}
