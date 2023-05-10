# ISWC Conference - Probabilistic SHACL Shape Mining Using Evolutionary Algorithms

## Requirements

### For users 

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/), [Linux](https://docs.docker.com/engine/install/) or [Mac](https://docs.docker.com/docker-for-mac/install)
- [Docker Compose](https://docs.docker.com/compose/install) 

### For contributors

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/), [Linux](https://docs.docker.com/engine/install/) or [Mac](https://docs.docker.com/docker-for-mac/install)
- [Docker Compose](https://docs.docker.com/compose/install) 
- [Maven](https://maven.apache.org/download.cgi)
- [Java](https://www.java.com/fr/download/)

## How to install

1. Clone this repository
2. Execute the *install* shell file such as: ```cd install/ && ./install.sh```
> **INFO:** it will take some time !
<!-- 3. Once the build phase is completed, we need to create and start **rdfminer** service : ```sudo docker-compose up -d rdfminer```
> You can check if it's correctly launched with the following command : ```sudo docker-compose ps``` -->

## How to launch experiments (on Linux/MAC)

> **WARNING:** take care to do not mix datasets between them into **Corese/data/** folder (1 RDF dataset per experiment)

> **INFO** Each results of these experiments will be stored in the **IO/** folder

### Solution space analysis on CovidOnTheWeb dataset

1. Put *covidontheweb_data.ttl* (stored in **datasets/**) in the **Corese/data/** folder.
2. Start the **Corese** service: ```sudo docker-compose up -d corese```
> **WARNING:** if the service has been started beforehand, it must first be stopped with the following command: ```sudo docker-compose stop corese```
3. Start the **RDFMiner** service: ```sudo docker-compose up -d rdfminer```
4. Generate the shapes file using *main.py* such as: ```cd experiments/solution_space_analysis/ && python main.py```
> **WARNING:** install requirements before: ```pip3 install -r requirements.txt```
5. Launch the probabilistic SHACL evaluation with the **RDFMiner** service using the following script: ```cd experiments/solution_space_analysis & sudo ./launch_psh_eval.sh```

### Shape mining on CovidOnTheWeb dataset

1. Put *covidontheweb_data.ttl* (stored in **datasets/**) in the **Corese/data/** folder.
2. Start the **Corese** service: ```sudo docker-compose up -d corese```
> **WARNING:** if the service has been started beforehand, it must first be stopped with the following command: ```sudo docker-compose stop corese```
3. Start the **RDFMiner** service: ```sudo docker-compose up -d rdfminer```
4. Launch the following script: ```cd experiments & sudo ./CovidOnTheWeb.sh```

### Shape mining on SURE-KG dataset

1. Put *sure_kg.ttl* (stored in **datasets/**) in the **Corese/data/** folder.
2. Start the **Corese** service: ```sudo docker-compose up -d rdfminer```
> **WARNING:** if the service has been started beforehand, it must first be stopped with the following command: ```sudo docker-compose stop corese```
3. Start the **RDFMiner** service: ```sudo docker-compose up -d rdfminer```
4. Launch the following script: ```cd experiments & sudo ./SureKG.sh```

## RDF Datasets

The RDF datasets used in the experiments is stored in **datasets/**: *covidontheweb_data.ttl* and *sure_kg.ttl*

## Results

The results of each experiments are presented in **results/**