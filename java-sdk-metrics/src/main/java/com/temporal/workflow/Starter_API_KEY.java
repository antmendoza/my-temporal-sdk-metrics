package com.temporal.workflow;

import com.temporal.grpc.HeaderLoggingInterceptor;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.temporal.api.workflowservice.v1.ListWorkflowExecutionsRequest;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;

import java.time.Duration;
import java.util.Collection;
import java.util.List;

public class Starter_API_KEY {


    public static void main(String[] args) throws InterruptedException {

        try {


            final String namespace = "antonio.a2dd6";
            String key = "-";

            key = "-";

            final ClientInterceptor interceptr = new ClientInterceptor() {
                @Override
                public <ReqT, RespT> io.grpc.ClientCall<ReqT, RespT> interceptCall(
                        io.grpc.MethodDescriptor<ReqT, RespT> method,
                        io.grpc.CallOptions callOptions,
                        io.grpc.Channel next) {


                    //Print api key value
                    return next.newCall(method, callOptions);
                }
            };
            final Collection<ClientInterceptor> interceptor = List.of(interceptr, new HeaderLoggingInterceptor());


            String finalKey = key;
            WorkflowServiceStubsOptions.Builder stubOptions =
                    WorkflowServiceStubsOptions.newBuilder()
                            .addApiKey(() -> finalKey)
                            .setEnableHttps(true)
                            .addGrpcMetadataProvider(() -> {
                                Metadata metadata = new Metadata();
                                metadata.put(Metadata.Key.of("temporal-namespace", Metadata.ASCII_STRING_MARSHALLER), namespace);
                                return metadata;
                            })
                            .setGrpcClientInterceptors(interceptor)
                            .setTarget("us-west-2.aws.api.temporal.io:7233")
                    //        .setTarget("antonio.a2dd6.tmprl.cloud:7233")
                    ;

            WorkflowServiceStubs service = WorkflowServiceStubs.newConnectedServiceStubs(
                    stubOptions.build()
                    , Duration.ofSeconds(10)
            );


            WorkflowClientOptions clientOptions =
                    WorkflowClientOptions.newBuilder()
                            .setNamespace(namespace)
//                        .setDataConverter(dataConverter)
                            .build();
            WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


            try {
                client.getWorkflowServiceStubs().blockingStub().listWorkflowExecutions(ListWorkflowExecutionsRequest.newBuilder().setNamespace(namespace).build());
            } catch (Exception e) {
                e.printStackTrace();
            }



        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
