# Create environment


We use doker compose to run the Temporal server, Prometheus and Grafana. 
If you start Temporal server using a different method, you must run `create-temporal-network.sh` to create the network `temporal-network` 
used by the other docker-compose files.


### Start Temporal server
```bash
./1-start-local-server.sh

```
[Temporal UI](http://localhost:8088/)


### Start Grafana and Prometheus
```bash
./2-start-grafana_prometheus.sh
```
[Grafana UI](http://localhost:3000/)

