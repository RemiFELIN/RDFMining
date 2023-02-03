# ESWC Conference : A Framework to Include and Exploit Probabilistic Information in SHACL Validation Reports

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

All the informations about it are contained in **AR-SHACL** folder. 

## CovidOnTheWeb dataset

The dataset used in the experiments is stored in **Corese/data/**: covidontheweb_data.ttl.

## SHACL Shapes evaluation

### Standard SHACL Validation

> **COMMAND LINE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -cs -sf rdfminer/io/*[YourShapesFile.ttl]* -dir *[YourResultsFolder/]* 

### Probabilistic SHACL Validation

> **COMMAND LINE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -psh -shacl-p 0.5 -sf /rdfminer/io/*[YourShapesFile.ttl]* -dir *[YourResultsFolder/]* 

> **INFO** For both modes used, the content of *YourShapesFile.ttl* MUST contains well-formed SHACL Shapes (e.g. using Turtle format) like:
```
# You must define prefixes used in this file
BASE             <http://example.com/shapes/>
PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sh:       <http://www.w3.org/ns/shacl#> 

<2> a sh:NodeShape ;
    sh:targetClass <http://www.wikidata.org/entity/Q194290> ;
    sh:property [  
        sh:path rdf:type ;  
        sh:hasValue <http://www.wikidata.org/entity/Q16023751> ;
    ] .  
```
