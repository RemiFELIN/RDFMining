# EGC 2024 - Extraction probabiliste de formes SHACL à l'aide d'algorithmes évolutionnaires

Ce dépôt a pour but de partager l'ensemble du code nécessaire à la réalisation des expériences, le jeu de données RDF ainsi que les résultats obtenus.

## Jeu de données RDF: Covid-on-the-web

Le jeu de données RDF utilisé dans le cadre de nos expériences se trouve dans le dossier **Corese/data/**: *covidontheweb_data.ttl*

## Résultats

Les résultats expérimentaux sont présentés dans le dossier **resultats/**


## Comment utiliser le code 
*documentation en anglais

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
<!-- 3. Once the build phase is completed, we need to create and start **rdfminer** service : ```sudo docker-compose up -d rdfminer```
> You can check if it's correctly launched with the following command : ```sudo docker-compose ps``` -->

### How to launch experiments (on Linux/MAC)

> **WARNING:** take care to do not mix datasets between them into **Corese/data/** folder (1 RDF dataset per experiment) to ensure consistent results

> **INFO:** Each results of these experiments will be stored in the **IO/users/admin/[PROJECT_NAME]** folder

#### Shape mining on CovidOnTheWeb dataset

<!-- 1. Put *covidontheweb_data.ttl* (stored in **datasets/**) in the **Corese/data/** folder. -->
2. Start the **Corese** service: ```sudo docker-compose up -d corese```
<!-- > **WARNING:** if the service has been started beforehand, it must first be stopped with the following command: ```sudo docker-compose stop corese``` -->
3. Start the **RDFMiner** service: ```sudo docker-compose up -d rdfminer```
4. Launch the following script: ```cd experiences & sudo ./CovidOnTheWeb.sh```
