# RDFMining

## Requirements

### For users 

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/), [Linux](https://docs.docker.com/engine/install/) or [Mac](https://docs.docker.com/docker-for-mac/install)
- [Docker Compose](https://docs.docker.com/compose/install) 

### For contributors

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/), [Linux](https://docs.docker.com/engine/install/) or [Mac](https://docs.docker.com/docker-for-mac/install)
- [Docker Compose](https://docs.docker.com/compose/install) 
- [Maven](https://maven.apache.org/download.cgi)
- [Java](https://www.java.com/fr/download/)

## How to use RDFMiner on Linux

1. Clone this repository
2. On terminal, tap : ```sudo docker-compose build``` (takes a long time)
> This command build all the environment of RDFMiner and Virtuoso instance

> Remember to check if the build phase worked well with ```sudo docker images```

```REPOSITORY          TAG             IMAGE ID       CREATED         SIZE
virtuoso            1.0             c219208b4923   11 hours ago    1.95GB
rdfminer            1.2             ba016286e4dc   17 hours ago    880MB
...
```

3. Once the build phase is completed, we need to create and start the *Virtuoso* service : ```sudo docker-compose up``` to launch *Virtuoso* and *RDFMiner* containers

> You can check with : ```sudo docker-compose ps```
```
        Name                      Command               State                Ports              
------------------------------------------------------------------------------------------------
rdfmining_rdfminer_1   ./rdfminer/scripts/run.sh  ...   Up                                      
rdfmining_virtuoso_1   /bin/bash /virtuoso/script ...   Up      1111/tcp, 0.0.0.0:8890->8890/tcp
```

4. Put all the files you need (e.g. your .bnf grammar) in the **/IO** folder.
5. Once this phase is complete, you can launch the container : ```sudo docker-compose exec rdfminer ./rdfminer/scripts/run.sh [PARAMETERS]```

> For instance :

```sudo docker-compose exec rdfminer ./rdfminer/scripts/run.sh -ge -r -g /rdfminer/io/OWL2Axiom-test9.bnf -bf /rdfminer/io/test/buffer -sre /rdfminer/io/test/stat-results.xlsx -fax /rdfminer/io/test/axioms-results.xlsx -ps 2000 -kb 20000 -ckp 3 -pc 0.8 -pm 0.01 -sez 0.7 -el 1 -seez 0.02 -init 6 -div 1 -mxw 1 -se 2```
> The container takes the same parameters as RDFMiner jar file

## Parameters

```
 -a (--axioms) AXIOMFILE                : test axioms contained in this file
 -bf (--BufferFile) BUFFER FILE         : use as this value as the name of
                                          buffer file of chromosome for next
                                          generation (default: buffer)
 -ckp (--Checkpoint) CHECK_POINT        : Checkpoint (default: 3)
 -cr (--typeCrossover) TYPE_CROSSOVER   : use as this value as the type of
                                          parent selection operation (default:
                                          2)
 -d (--dynamic-timeout) ANGULAR_COEFF   : use a dynamic time-out for axiom
                                          testing (default: 0.0)
 -div (--diversity) DIVER_METHOD        : use as this value as the chose of
                                          diversity method (default: 1)
 -el (--elitism) ELITISM_SELECTION      : use as this value as the choose of
                                          elitism selection (default: 1)
 -fax (--FileAxioms) STATISTICS_AXIOMS  : use as this value as the name of
                                          output statistics axioms (default:
                                          AxiomsStatistics)
 -g (--grammar) GRAMMAR                 : use this file as the axiom grammar
                                          (default: /home/remi/Bureau/dev/RDFMin
                                          ing/code/resources/OWL2Axiom-test.bnf)
 -ge (--grammatical-evolution)          : activate the grammatical evolution
 GRAMMATICAL_EVOLUTION                    for the axiom's extraction (default:
                                          false)
 -gsd (--GoldStandard) GOLD_STANDARD    : use as this value as the input
                                          Goldstandard file (default:
                                          GoldStandard.xlsx)
 -init (--initlen) INITLEN_CHROMOSOME   : use as this value as the initial
                                          length of chromosome (default: 20)
 -kb (--K_Base) K_BASE                  : KBase (default: 5000)
 -mxc (--maxcodon) MAX_CODON            : use as this value as the max value of
                                          codon (default: 2147483647)
 -mxw (--maxwrapp) MAX_WRAPP            : use as this value as the max number
                                          of wrapping (default: 1)
 -ngen (--n-generation) GENERATION_NUMB : use as this value as the number of
 ER                                       generation (default: 5)
 -o (--output) RESULTFILE               : name of output file (without
                                          extension): the name 'results' is
                                          chosen if -o is not used (default:
                                          results)
 -pc (--probcross) PROB_CROSSOVER       : use as this value as the probability
                                          of crossover operation (default: 0.8)
 -pm (--probmut) PROB_MUTATION          : use as this value as the probability
                                          of mutation operation (default: 0.01)
 -ps (--population-size)                : use as this value as the initial size
 POPULATION_SIZE                          of population (default: 200)
 -r (--random)                          : test randomly generated axioms
                                          (default: false)
 -s (--subclasslist) FILE               : test subClassOf axioms generated from
                                          the list of subclasses in the given
                                          file
 -sa (--single-axiom) AXIOM             : test a single axiom given
 -se (--typeselect) TYPE_SELECTION      : use as this value as the type of
                                          parent selection operation (default:
                                          2)
 -seez (--sizeelitie) TYPE_SELECTION    : use as this value as the size of
                                          elitism selection (default: 0.02)
 -sez (--sizeselect) SIZE_SELECTION     : use as this value as the size of
                                          parent selection operation (default:
                                          0.7)
 -shf (--shuffle) SHUFFLE_SELECTION     : use as this value as the chose of
                                          shuffle list (default: 1)
 -sre (--StatisticsResult)              : use as this value as the name of
 STATISTICS_RESULT                        output statistics result file
                                          (default: StatisticsResult.xlsx)
 -t (--timeout) MINUTES                 : use this time-out (in minutes) for
                                          axiom testing (default: 0)
 -tinit (--typeinit) TYPE_INITIALIZATIO : use as this value as the type of
 N                                        initialization (default: 1)
 -twi (--twin) TWIN_SELECTION           : use as this value as the chose of
                                          twin acception (default: 1)
```

## Documentations

All resources about the project are avalaible on [docs](https://github.com/RemiFELIN/RDFMining/tree/main/docs) folder. See [working-paper.pdf](https://github.com/RemiFELIN/RDFMining/tree/main/docs/working-paper.pdf)
