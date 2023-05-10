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
2. Execute the *install* shell file such as: ```cd install && ./install.sh```
> It will take some time !
<!-- 3. Once the build phase is completed, we need to create and start **rdfminer** service : ```sudo docker-compose up -d rdfminer```
> You can check if it's correctly launched with the following command : ```sudo docker-compose ps``` -->

## How to launch experiments (on Linux/MAC)

### On CovidOnTheWeb dataset

4. Put *covidontheweb_data.ttl* (stored in **datasets/**) in the **Corese/data/** folder.
5. Launch the following script : ```cd experiments & sudo ./run.sh```

### On SURE-KG dataset

## CovidOnTheWeb dataset

The dataset used in the experiments is stored in **Corese/data/**: covidontheweb_data.ttl.
