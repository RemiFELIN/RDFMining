# EuroGP 2024 - An Algorithm Based on Grammatical Evolution for SHACL Constraints Discovering

## RDF dataset: Covid-on-the-web

The RDF dataset used to performe experiments are stored into **Corese/data/**: *covidontheweb_data.ttl*

## Results

All results are stored into **results/**

- **covid-on-the-web**:
  - $\|\mathcal{P}\|/E$ choice: **V1_[POPULATION_SIZE]\_[EFFORT]_[i]**
  - Selection ($\mathcal{R}$) pressure: 
    - **V2_Roulette_[SELECTION_RATE]_[i]**
    - **V2_Tournament_[SELECTION_RATE]\_[TOURNAMENT_RATE]_[i]** 
- **solution-space-analysis**: used to estimate the **recall** $R(x)$ measure.
- **acceptable-shapes**: all the $1,766$ distinct and acceptable shapes discovered from all the experiments + details (CSV format).


## How to reproduce the experiments 

### Requirements

#### For users 

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/), [Linux](https://docs.docker.com/engine/install/) or [Mac](https://docs.docker.com/docker-for-mac/install)
- [Docker Compose](https://docs.docker.com/compose/install) 

#### For contributors

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/), [Linux](https://docs.docker.com/engine/install/) or [Mac](https://docs.docker.com/docker-for-mac/install)
- [Docker Compose](https://docs.docker.com/compose/install) 
- [Maven](https://maven.apache.org/download.cgi)
- [Java](https://www.java.com/fr/download/)

### How to install

1. Clone this repository
2. Execute the installation script such as: ```cd install/ && ./install.sh```
> **INFO:** it will take some time !

### How to launch experiments (on Linux/MAC)

> **WARNING:** take care to do not mix datasets between them into **Corese/data/** folder (1 RDF dataset per experiment) to ensure consistent results

> **INFO:** Each results of these experiments will be stored in the **IO/users/admin/[PROJECT_NAME]** folder

#### Shape mining on CovidOnTheWeb dataset

<!-- 1. Put *covidontheweb_data.ttl* (stored in **datasets/**) in the **Corese/data/** folder. -->
2. Start the **Corese** service: ```sudo docker-compose up -d corese```
<!-- > **WARNING:** if the service has been started beforehand, it must first be stopped with the following command: ```sudo docker-compose stop corese``` -->
3. Start the **RDFMiner** service: ```sudo docker-compose up -d rdfminer```
4. Launch the following script: ```cd experiences & sudo ./CovidOnTheWeb.sh```
