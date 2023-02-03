<!--![](RDFMiner/docs/banner.png)-->
     _____  _____  ______ __  __ _                 
    |  __ \|  __ \|  ____|  \/  (_)                
    | |__) | |  | | |__  | \  / |_ _ __   ___ _ __ 
    |  _  /| |  | |  __| | |\/| | | '_ \ / _ \ '__|
    | | \ \| |__| | |    | |  | | | | | |  __/ |   
    |_|  \_\_____/|_|    |_|  |_|_|_| |_|\___|_|   
                                                
**VERSION 1.4**                               

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

## How to install RDFMiner

1. Clone this repository
2. Execute the *install* shell file such as: ```cd install && ./install.sh```
> It will take some time !
3. Once the build phase is completed, we need to create and start RDFMiner service : ```sudo docker-compose up -d rdfminer```
> You can check if it's correctly launched with the following command : ```sudo docker-compose ps```

## How to launch experiments (on Linux/MAC)

1. Install RDFMiner
2. Put all the files you need (containing SHACL Shapes) in the **/IO** folder.
3. Launch the following script : ```cd experiments & sudo ./run.sh```

## CovidOnTheWeb dataset



## Use cases 

Here are some practical examples depending of the choosen context:

### OWL Axioms mining 

#### SubClassOf axioms 

> **INFO** BNF Grammar file of atomic SubClassOf axioms: *OWL2Axiom-subclassof.bnf*

> **INFO** BNF Grammar file of complex SubClassOf axioms: *OWL2Axiom-complex-subclassof.bnf*

> **EXAMPLE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -ge -ra -l -train "http://172.19.0.2:9000/sparql" -g /rdfminer/io/OWL2Axiom-complex-subclassof.bnf -dir test_complex_subclassof/ -ps 20 -kb 100 -cr 1 -pc 0.7 -pm 0.01 -div 1 -mxw 1 -se 2 -t 500

> **INFO** The **-l** provides a efficient way to assess SubClassOf axioms, based on SPARQL Queries optimisation.

#### DisjointClasses axioms 

> **INFO** BNF Grammar file of atomic DisjointClasses axioms: *OWL2Axiom-disjoint.bnf*

> **INFO** BNF Grammar file of complex DisjointClasses axioms: *OWL2Axiom-complex-disjoint.bnf*

> **EXAMPLE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -ge -ra -l -train "http://172.19.0.2:9000/sparql" -g /rdfminer/io/OWL2Axiom-complex-subclassof.bnf -dir test_complex_subclassof/ -ps 50 -kb 100 -cr 1 -pc 0.7 -pm 0.01 -div 1 -mxw 1 -se 2

### OWL Axioms evaluation

> **EXAMPLE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -a your_axioms.txt -dir test_eval_axioms/

> **INFO** The content of *your_axioms.txt* MUST contains well-formed OWL axioms like:
```
SubClassOf(<c1> <c2>) 
SubClassOf(<c3> <c1>)
...
SubClassOf(<cn> <ck>)
# <c1>; <c2>; <c3>; <cn> and <ck> are OWL Classes
```

### SHACL Shapes evaluation

#### Standard SHACL Validation

> **EXAMPLE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -cs -sf your_shapes.ttl -dir test_sh_eval/ 

> **INFO** The content of *your_shapes.ttl* MUST contains well-formed SHACL Shapes (e.g. using Turtle format) like:
```
# You must define prefixes used in this file
BASE             <http://rdfminer.com/shapes/>
PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sh:       <http://www.w3.org/ns/shacl#> 

<2> a sh:NodeShape ;
    sh:targetClass <c1> ;
    sh:property [  
        sh:path rdf:type ;  
        sh:hasValue <c2>;
    ] .  
```

#### Probabilistic SHACL Validation

> **EXAMPLE** docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -psh -shacl-p 0.5 -sf your_shapes.ttl -dir test_psh_eval/

## Documentations

All resources about the project are avalaible on [docs](https://github.com/RemiFELIN/RDFMining/tree/main/RDFMiner/docs) folder. The [working-paper.pdf](https://github.com/RemiFELIN/RDFMining/blob/main/RDFMiner/docs/working-paper.pdf) gives an overview of RDFMiner project (context, aim, ...). Others documents are related to our publications. 
