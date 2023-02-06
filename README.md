# ESWC Conference : A Framework to Include and Exploit Probabilistic Information in SHACL Validation Reports

## Revised article

The revised article is avalaible in the **article/** folder.

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
3. Once the build phase is completed, we need to create and start **rdfminer** service : ```sudo docker-compose up -d rdfminer```
> You can check if it's correctly launched with the following command : ```sudo docker-compose ps```

## How to launch experiments (on Linux/MAC)

4. Put all the files you need (containing SHACL Shapes) in the **/IO** folder.
5. Launch the following script : ```cd experiments & sudo ./run.sh```

## Association rules to SHACL shapes

All the informations about it are contained in the **AR-SHACL** folder. 

## CovidOnTheWeb dataset

The dataset used in the experiments is stored in **Corese/data/**: covidontheweb_data.ttl.

## SHACL Shapes evaluation

This section provides a way to launch your own experiments:

### How to load your RDF data 

1. Interrupt the *Corese server*: ```sudo docker-compose stop corese```
2. Put your data file(s) in the **/Corese/data/** folder (and remove the unused data file, i.e. *covidontheweb_data.ttl*)
3. Launch *Corese server*: ```sudo docker-compose up -d corese```
> **INFO** You can query your data on the following SPARQL endpoint: ```http://172.19.0.4:9100/sparql```

### Standard SHACL Validation

> **COMMAND LINE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -cs -sf rdfminer/io/*[YourShapesFile.ttl]* -dir *[YourResultsFolder/]* 

### Probabilistic SHACL Validation

> **COMMAND LINE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -psh -shacl-p *[p]* -sf /rdfminer/io/*[YourShapesFile.ttl]* -dir *[YourResultsFolder/]* 

> **INFO** For both modes used, the content of *YourShapesFile.ttl* MUST contains well-formed SHACL Shapes (e.g. using Turtle format) like:
```
# You must define prefixes used in this file
PREFIX :         <http://www.example.com/myDataGraph#> .
PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sh:       <http://www.w3.org/ns/shacl#> 

:2 a sh:NodeShape ;
    sh:targetClass <http://www.wikidata.org/entity/Q194290> ;
    sh:property [  
        sh:path rdf:type ;  
        sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;
    ] .  
```

## RDFS Schema

The RDF Schema is provided in the **rdfs/** folder.