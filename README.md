<!--![](RDFMiner/docs/banner.png)-->
     _____  _____  ______ __  __ _                 
    |  __ \|  __ \|  ____|  \/  (_)                
    | |__) | |  | | |__  | \  / |_ _ __   ___ _ __ 
    |  _  /| |  | |  __| | |\/| | | '_ \ / _ \ '__|
    | | \ \| |__| | |    | |  | | | | | |  __/ |   
    |_|  \_\_____/|_|    |_|  |_|_|_| |_|\___|_|   
                                                
**VERSION 1.4**                               

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
3. Once the build phase is completed, we need to create and start all the services : ```sudo docker-compose up -d```

> You can check if it's correctly launched with the following command : ```sudo docker-compose ps```
```
        Name                      Command               State                Ports             
-----------------------------------------------------------------------------------------------
rdfmining_corese_1     /bin/sh -c $LAUNCH               Up      0.0.0.0:9100->9100/tcp,
                                                                :::9100->9100/tcp                      
rdfmining_rdfminer_1   ./rdfminer/scripts/run.sh  ...   Up                                     
rdfmining_virtuoso_1   /bin/bash /virtuoso/script ...   Up      1111/tcp, 8890/tcp, 
                                                                0.0.0.0:9000->9000/tcp,
                                                                :::9000->9000/tcp
```

## How to use RDFMiner

1. Put all the files you need (e.g. your .bnf grammar, axioms to assess in *.txt* file, ...) in the **/IO** folder.
2. Once this phase is complete, you can launch the *RDFMiner* container in order to launch the software : ```sudo docker-compose exec rdfminer ./rdfminer/scripts/run.sh [PARAMETERS]```

## Parameters

```
 -a (--axioms) AXIOM_FILE               : test axioms contained in this file
 -ckp (--Checkpoint) CHECK_POINT        : Checkpoint (default: 3)
 -cr (--type-crossover) TYPE_CROSSOVER  : use as this value as the type of
                                          parent selection operation (default:
                                          2)
 -cs (--classic-shacl) CLASSIC_SHACL    : use classic SHACL validation
                                          (default: false)
 -d (--dynamic-timeout) ANGULAR_COEFF   : use a dynamic time-out for axiom
                                          testing (default: 0.0)
 -dir (--directory) RESULTFOLDER        : path of output folder (default:
                                          results)
 -div (--diversity) DIVER_METHOD        : use as this value as the chose of
                                          diversity method (default: 1)
 -e (--endpoint) ENDPOINT               : specify the SPARQL endpoint to be
                                          used for sending requests
 -el (--elitism) ELITISM_SELECTION      : use as this value as the choose of
                                          elitism selection (default: 1)
 -g (--grammar) GRAMMAR                 : use this file as the axiom grammar
                                          (default: /rdfminer/code/resources/OWL
                                          2Axiom-test.bnf)
 -ge (--grammatical-evolution)          : activate the grammatical evolution
 GRAMMATICAL_EVOLUTION                    for the axiom's extraction (default:
                                          false)
 -gsd (--gold-standard) GOLD_STANDARD   : use as this value as the input
                                          Goldstandard file (default:
                                          GoldStandard.xlsx)
 -init (--init-len) INITLEN_CHROMOSOME  : use as this value as the initial
                                          length of chromosome (default: 20)
 -kb (--K_Base) K_BASE                  : KBase (default: 5000)
 -l (--loop) LOOP_CORESE                : Launch SubClassOf assessment with
                                          loop operator from Corese (default:
                                          false)
 -mxc (--max-codon) MAX_CODON           : use as this value as the max value of
                                          codon (default: 2147483647)
 -mxw (--max-wrapp) MAX_WRAPP           : use as this value as the max number
                                          of wrapping (default: 1)
 -p (--prefixes) PREFIXES               : use this file as the prefixes to be
                                          used in SPARQL queries
 -pc (--prob-cross) PROB_CROSSOVER      : use as this value as the probability
                                          of crossover operation (default: 0.8)
 -pm (--prob-mut) PROB_MUTATION         : use as this value as the probability
                                          of mutation operation (default: 0.01)
 -ps (--population-size)                : use as this value as the initial size
 POPULATION_SIZE                          of population (default: 200)
 -r (--random)                          : test randomly generated axioms
                                          (default: false)
 -s (--subclassof-list) FILE            : test subClassOf axioms generated from
                                          the list of subclasses in the given
                                          file
 -sa (--single-axiom) AXIOM             : test a single axiom given
 -se (--type-select) TYPE_SELECTION     : use as this value as the type of
                                          parent selection operation (default:
                                          2)
 -seez (--size-elitie) TYPE_SELECTION   : use as this value as the size of
                                          elitism selection (default: 0.02)
 -sez (--size-select) SIZE_SELECTION    : use as this value as the size of
                                          parent selection operation (default:
                                          0.7)
 -sf (--shapes-file) SHAPES_FILE        : test shapes contained in this file
 -shacl (--shacl-shapes) SHAPES         : enable SHACL Shapes mining (default:
                                          false)
 -shf (--shuffle) SHUFFLE_SELECTION     : use as this value as the chose of
                                          shuffle list (default: 1)
 -t (--timeout) SECONDS                 : use this time-out (in seconds) for
                                          axiom testing (default: 0)
 -tinit (--type-init) TYPE_INITIALIZATI : use as this value as the type of
 ON                                       initialization (default: 1)
 -twi (--twin) TWIN_SELECTION           : use as this value as the chose of
                                          twin acception (default: 1)
```
> The container takes the same parameters as RDFMiner jar file

> For instance : ```sudo docker-compose exec rdfminer ./rdfminer/scripts/run.sh -ge -r -g /rdfminer/io/OWL2Axiom-test9.bnf -dir example/ -ps 100 -kb 1000 -ckp 1 -pc 0.8 -pm 0.01 -sez 0.7 -el 1 -seez 0.02 -init 6 -div 1 -mxw 1 -se 2```

## Documentations

All resources about the project are avalaible on [docs](https://github.com/RemiFELIN/RDFMining/tree/main/RDFMiner/docs) folder. The [working-paper.pdf](https://github.com/RemiFELIN/RDFMining/blob/main/RDFMiner/docs/working-paper.pdf) gives an overview of RDFMiner project (context, aim, ...). Others documents are related to our publications. 
