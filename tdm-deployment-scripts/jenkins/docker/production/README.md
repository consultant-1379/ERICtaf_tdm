# Initial Docker Swarm cluster setup

 Before running the deploy script from CI one need to setup Docker Swarm cluster on target machines:
  - 1 manager node for nginx
  - 2 worker nodes for back-end

## 1. Install Docker on all the hosts.

### stop docker and replace the current outdated docker with a latest one
```bash
$ sudo service docker stop
$ sudo yum update docker-engine
```

### install/update latest Docker compose to latest version
```bash
#!/bin/bash

# get latest docker compose released tag
COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep 'tag_name' | cut -d\" -f4)

# Install docker-compose
sh -c "curl -L https://github.com/docker/compose/releases/download/${COMPOSE_VERSION}/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose"
chmod +x /usr/local/bin/docker-compose
sh -c "curl -L https://raw.githubusercontent.com/docker/compose/${COMPOSE_VERSION}/contrib/completion/bash/docker-compose > /etc/bash_completion.d/docker-compose"

# Output compose version
docker-compose -v

exit 0
```

### start the docker service
```bash
$ sudo service docker start
```

## 2. Ensure that firewall is configured to allow Swarm mode traffic. See [more for details](https://www.digitalocean.com/community/tutorials/how-to-configure-the-linux-firewall-for-docker-swarm-on-centos-7)

## 3. Initialize Swarm manager node by the following command:
```
docker swarm init --advertise-addr manager_node_ip_address
```
In the command output note the swarm token.

## 4. Add worker nodes to the manager of cluster.
```
docker swarm join --token your_swarm_token manager_node_ip_address:2377
```

## 5. View all members of the cluster.
```
docker node ls
```
For more details on Swarm mode setup refer to [Docker documentation](https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/)

# FAQ

## Image not found while deploy to production on swarm cluster
Usually this is due to authentication issue with private registry at `armdocker.rnd.ericsson.se`.
Try to manually login by `tafuser` this registry on target node and pull corresponding image:
```
docker login armdocker.rnd.ericsson.se
User(tafuser):
Password:
docker pull <image_name>:<tag>
```

## Why is used ELK installation?
The main purpose is log management in a docker swarm, i.e. collect logs from all the containers in all the nodes of the swarm.

`Elasticsearch` is the database to store the log data and query for it. `Logstash` is a log collection pipeline 
that ingests logs from multiple sources and feeds it to `Elasticsearch`. `Kibana` is the web UI to display `Elasticsearch` data. 

The `logspout` at each nodes in the swarm connect all logs and feed it to `logstash`. 

All these tools are open-source and can be deployed as a container.

## Elasticsearch container does not start properly due to memory limit. How to resolve it?
If you receive the following error message:
```
max virtual memory areas vm.max_map_count [65530] likely too low, increase to at least [262144]
```
This is [know issue](https://github.com/docker-library/elasticsearch/issues/111). 
Perform the following (Linux) command on master host node of swarm cluster:
```
sudo sysctl -w vm.max_map_count=262144
```
