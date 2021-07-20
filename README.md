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

> Output: 
```REPOSITORY          TAG             IMAGE ID       CREATED         SIZE
virtuoso            1.0             c219208b4923   11 hours ago    1.95GB
rdfminer            1.2             ba016286e4dc   17 hours ago    880MB
...
```

### Virtuoso

3. Once the build phase is completed, we need to create and start the *Virtuoso* service : ```sudo docker-compose up virtuoso```

### RDFMiner

4. Create a folder (anywhere on your machine) which contains your files (axioms, grammar, ...). 
> For the following step, let's assume that you have created the folder **shared** which contains a file ***axioms.txt***
5. Once this phase is complete, you can launch the container : ```sudo docker run -it -v $(pwd)/shared/:/rdfminer/io/ rdfminer:1.0 -a /rdfminer/io/axioms.txt```
> The container takes the same parameters as RDFMiner jar file

> **-v $(pwd)/[FOLDER_NAME]/:/rdfminer/io/** is very important to share input and output files between container and our machine, according to the official documentation of Docker. 

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
