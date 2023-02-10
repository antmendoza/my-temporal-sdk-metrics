

## ADD Load test: ??
By default, a namespace scales up to 200 actions/seconds. Even though this number scales beyond most applications’ required throughput, we understand that validating scalability can be an important step for launching your production application.


—


To get ready: 
- Collect data from client: how many namesapces and how many users?
- Run typescript example for data converters , encryption folder
- Start dasbhoard `cd /Users/antmendoza/dev/temporal/observability/docker`



We are going to cover today how to: 
-   Create namespaces
-   Onboard other users to the UI
-   Set up observability endpoint
-   View usage
-   Zendesk


Introductions
Namespace setup, usage and observability
Account, User and Role setup 
How to contact Temporal Support
Questions
 




# Introduction


Hello xxxx, how are you?

We are going to cover today the main things to get started with Temporal Cloud.

- Namespace setup, usage and observability
- Account, User and Role setup 
- How to contact Temporal Support
- Questions

First, you should have recieved two emails, one for Temporal Cloud and another one for Zendesk**

I was wondering how familiar are you with Temporal, are you using temporal in dev or production environment?
If appropiate: 
- For how long? 
- Which SDK are you using?

# Temporal cloud:

## Create namespaces

The first thing you want to do is to create a namespace. 

Welcome page!!

Couple of ways 

Welcome page or namepace icon
-   Namespace name
-   Region
-   Retention period: the time the system will keep your workflows after the workflow is closed.
-   CA certificate: All communications are secured over MTLS, so when we create a namespace we have to set the CA certificate (and then use end-entity certificates to connect from your workers/clients) we have documentation around this. And we have also a tool to help you with the certificate generation in case you don't have an infrastructure to generate them.
[https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud](https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud), 

-  certificate filters, in case you use the same CA for different namespaces, you can uses certificate filter to limit access to your workers to certain namespaces.
[https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#manage-certificate-filters](https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#manage-certificate-filters) 

-   Search attributes. You can set here custom search attributes . Temporal already  provide a set of fields to search for, like workflowId or WorkflowType, 
-   Default search attributes: (WorkflowId) [https://docs.temporal.io/visibility#default-search-attributes](https://docs.temporal.io/visibility#default-search-attributes)
    
-   Yo can define yout own search attributes [https://docs.temporal.io/visibility#custom-search-attributes](https://docs.temporal.io/visibility#custom-search-attributes)

Search attributes are not encripted. One thing to keep in mind if you have sensible information. 


Once we have put the information, we can create a new namespace.

**gRPC endpoint**

**Show the customer how to search in the UI**


To connect with temporal cloud you need: 
- end-entity certificates
- gRPC end-point
- namespace

Deploy/run workflows:

- Java: https://github.com/temporalio/samples-java/tree/main/src/main/java/io/temporal/samples/ssl    
- Go: https://github.com/temporalio/samples-go/tree/main/helloworldmtls
- Python: https://github.com/temporalio/samples-python/blob/main/hello/hello_mtls.py
- Typescript: https://github.com/temporalio/samples-typescript/tree/main/hello-world-mtls
- PHP: https://github.com/temporalio/samples-php/tree/master/app/src/MtlsHelloWorld

Segregate environments/ namespaces: 
-   Teamx-prod (dash)
-   Teamx-dev (dash)


**Questions?** 



## Edit namespace


https://cloud.temporal.io/namespaces/antonio-perez.temporal-dev



-----

## Usage

[https://cloud.temporal.io/usage](https://cloud.temporal.io/usage) 

Here you can see the usage per account and per namespace, 

Similar I am doing here you will be able to see the usage for the entire account or by namespace. 


~~This is almost realtime, it has a couple of hours of delay.~~


**Do you have an idea of the number of actions per seconds?** 
Can i ask for the workload you are planning to run in temporal cloud. How many workflows per day/week? 


## Invite users to temporal cloud

Invite users [https://docs.temporal.io/cloud/how-to-get-started-with-temporal-cloud#invite-users](https://docs.temporal.io/cloud/how-to-get-started-with-temporal-cloud#invite-users)

-   Global Admin: is an account admin

-   Developer: they are allowed to manage namespaces.

-   Read-only access: read-only



—


## Monitor and alerting: 

[https://www.notion.so/temporalio/Monitor-and-Alert-Documentation-e20846fa68a24dc69f40834bac257c4e](https://www.notion.so/temporalio/Monitor-and-Alert-Documentation-e20846fa68a24dc69f40834bac257c4e)

We always recommend to set up your metrics, both SDK and Server metrics. Mainly sdk metrics. 

SDK metrics are in your side, this is for you to monitor your workers and clients. And this metrics are gonna tell you mainly two things:

-   If your worker are well provisioned / if you have hight schedule to start latencies, it means that your workers are unprovisioned. 

-   If your workflows are experiencing errors, either workflowTask error or activity errors. 

Links:
-   SDK Metrics: https://docs.temporal.io/references/sdk-metrics

	- Number of failed workflow-tasks
	- Number of failed activity-tasks
	- latencies, pollers 

-   [Developer's guide - Observability | Temporal Documentation](https://docs.temporal.io/application-development/observability)

-   [Setting up Prometheus and Grafana to view metrics | Temporal Documentation](https://docs.temporal.io/kb/prometheus-grafana-setup)

Server metrics, in this case, Temporal Cloud metrics 

-  This is a PromQL [Prometheus HTTP API](https://prometheus.io/docs/prometheus/latest/querying/api/) endpoint (not a scrape endpoint).

- [Configure a metrics endpoint in Temporal Cloud](https://docs.temporal.io/cloud/how-to-monitor-temporal-cloud-metrics): 
- Grafana dashboard  https://github.com/temporalio/dashboards/blob/b427de25b2d262c0be511205642e14ba62266211/cloud/temporal_cloud.json , if you import if
	- We have a grafana dashboard that should allow you to start seeing data once you import it.


Datadog data dog https://github.com/temporalio/samples-server/tree/main/cloud/observability 



#####  **sync match rate** **This should stay around 95+
This is gonna tell you whether the task are waiting in the queue (in the server) to be pulled by a worker, meaning you have workers availables 

sum by(temporal_namespace) (  
    rate(  
      temporal_cloud_v0_poll_success_sync_count{temporal_namespace=~"$namespace"}[5m]  
    )  
  )  
/  
  sum by(temporal_namespace) (  
    rate(  
      temporal_cloud_v0_poll_success_count{temporal_namespace=~"$namespace"}[5m]  
    )  
  )**



## Data converter dataconverter: 

[https://docs.temporal.io/concepts/what-is-a-data-converter](https://docs.temporal.io/concepts/what-is-a-data-converter), 

Data converter is a  plugin to your workers, it is an interface you can implement to encrypt and decrypt the payload that are gonna be sent across the wired. 

So you have encryption across the wired, not only at rest with temporal cloud, so obviously we can not see your data. 

So the payload is encrypted before being sent to temporal cloud and it is decrypted whenever your worker has to perform a task. 

Search attributes are not encrypted.

### Codec server

https://docs.temporal.io/security#codec-server
JWT - gey
G - Gi

https://temporalio.atlassian.net/browse/TW-31 Revisar esto para familiarizarme




## Learning

We have a 101 course, it is Goland but even if you are not familiar with Goland is a very easy to follow, the team is working to create more content. For this course and other resources you can go to [https://learn.temporal.io/](https://learn.temporal.io/)

# How to contact support:


Temporal Support: 
- Zendesk: ([https://support.temporal.io/](https://support.temporal.io/) , 
- Slack: ([temporalio.slack.com](http://temporalio.slack.com/))
- Forum: ([https://community.temporal.io/](https://community.temporal.io/))

In slack, community forum you can ask general questions, but if you have anything specific about your account or namespace please use Zendesk 


---

# Concepts: 

[https://status.temporal.io/](https://status.temporal.io/)

[https://us-11514.app.gong.io/call?id=9175536959764185412&highlights=%5B%7B%22type%22%3A%22SHARE%22%2C%22from%22%3A2304%2C%22to%22%3A2338%7D%5D](https://us-11514.app.gong.io/call?id=9175536959764185412&highlights=%5B%7B%22type%22%3A%22SHARE%22%2C%22from%22%3A2304%2C%22to%22%3A2338%7D%5D)

---

## Privatelink:

 we only have private link setup in AWS, I have seen companies setting a private link between google cloud (GCP) and AWS and from there we can set up a private link to temporal cloud. But right now we don’t have any setup to support private links from GCP. 

  
  

## GCP and Azure

There are plans to support GCP and Azure in the future. Probably during 2023 but I can not give you any specific time of arrival for these.


## Security: 

All communications are secured over MTLS, so when you create a namespace you have to set your CA certificate and then use end-entity certificates to connect from your workers/clients. The data is encrypted at rest.


We don’t want to see your data, so we always recommend our customers to use [https://docs.temporal.io/concepts/what-is-a-data-converter](https://docs.temporal.io/concepts/what-is-a-data-converter), 






## Retention period.

The amount of time we hold your closed workflows, so during this period of time you will be able to query your workflows. Now it can be between 1 and 90 days. If you need more: 

-   Our short-term plan is to help customers do this by themselves.  Meaning we will provide a workflow that would require small/minimal changes to upload workflow history to code storage, and the customer will be responsible for operating such workflows.
    
-   Internally we will be looking at understanding the limiting factors to support longer retention, and after that, we will make a decision. 
    
-   In the future, we will support the export feature on Cloud. Something similar to Archival for OSS [https://docs.temporal.io/concepts/what-is-archival](https://docs.temporal.io/concepts/what-is-archival)


## Certificate filter

You can use it to, let's say you have 5 different namespaces set up with the same root CA certificate, so you can limit the access to end-entity certificates based on this filters.  [https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#manage-certificate-filters](https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#manage-certificate-filters) 

This is handy if you don’t want to manage intermediate certificates, so you have just one root certificate and secure access to the namespaces based on the filters. 

  
  

## Search attributes

There is a set of fields you can search for, for example workflowId, runId, workflowType.. And some more. And you can also create your own custom search attributes, this will allows you to search across workflows, let say.. CustomerId

-   [https://docs.temporal.io/visibility#default-search-attributes](https://docs.temporal.io/visibility#default-search-attributes)
    
-   [https://docs.temporal.io/visibility#custom-search-attributes](https://docs.temporal.io/visibility#custom-search-attributes)

Inside the workflow code you can set a value for the search attribute. 


Also, let you know that we can schedule a design session/code review session if you need it or you have any questions. 

  
  

## Automation / API Keys

Creating namespaces through scripts are not possible like now.

Today we don’t have API support for creating namespaces or role management. It is on the roadmap, we have a lot of people asking.

  
  

## Workflow migration from self-hosted service to Temporal Cloud

This is a common request from customers but unfortunately, we don’t have a tool or a solution right now to do it. 

  
  

## SAML

This is just for authentication right now. You will have for now to manage the roles for each user in  Temporal Cloud, now in the UI and in the future, we are going to provide and API to manage users and namespaces.    

If you navigate to [https://cloud.temporal.io](https://cloud.temporal.io), you will be able to log in with SSO in the case of Google (Single Sing On). 

  

We also provide integration with SAML, for now, we have tested this with Okta and Microsoft Active Directory, if you need it, you can request it. 



— 

  

You bring your own database into your own services, but we take care of the workflow state. It is persisted in our database. 

  

Unfortunately, that is where we are at. 

  

Is that a blocker to you? Security blocker? If there are other security questions please reach us out. 

  
  

You are evaluating temporal right now, can I ask what kind of evaluation are you doing in terms of 

"Is, or there's some kind of performance metrics?"

"You're looking at? Is there kind of?"

"Developer velocity  that you're looking at? What are some of the things…"

"You're evaluating?"[Link to snippet](https://us-11514.app.gong.io/call?id=9175536959764185412&highlights=%5B%7B%22type%22%3A%22SHARE%22%2C%22from%22%3A1523%2C%22to%22%3A1534%7D%5D)

  
  
  

Load test: we don’t want to allocate capacity on something that will be idle. Combined capacity, we can arrange when you need it

  

We can provision the namespace for you. 

  

Would it be a practical approach for you? 

  

Ahead of time. 

  

What do the activities do? Do they talk to the database, do they call a web service? Roundtrip latency
