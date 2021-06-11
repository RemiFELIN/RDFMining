# RDFMining

## Requirements

### For users 

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/) or [Linux](https://docs.docker.com/engine/install/)

### For contributors

- Docker for [Windows](https://docs.docker.com/docker-for-windows/install/) or [Linux](https://docs.docker.com/engine/install/)
- [Maven](https://maven.apache.org/download.cgi)
- [Java](https://www.java.com/fr/download/)

## How to use RDFMiner

1. Clone this repository
2. On terminal, tap : ```sudo docker build -t rdfminer:1.0 . --no-cache``` (takes a long time)
> It is important to execute this command at the same level of ***Dockerfile***
3. Create a folder (anywhere on your machine) which contains your files (axioms, grammar, ...). 
> For the following step, let's assume that you have created the folder **shared** which contains a file ***axioms.txt***
4. Once this phase is complete, you can launch the container : ```sudo docker run -it -v $(pwd)/shared/:/rdfminer/io/ rdfminer:1.0 -a /rdfminer/io/axioms.txt```
> The container takes the same parameters as RDFMiner jar file

> **-v $(pwd)/[FOLDER_NAME]/:/rdfminer/io/** is very important to share input and output files between container and our machine, according to the official documentation of Docker. 

## How to build a locale RDFMiner instance on Linux

1. Clone this repository.
2. On terminal, execute the script : ```./scripts/compile_c_code.sh```
> IMPORTANT : you need to execute this line to set the correct HOME on your machine: ```export HOME=$(pwd)``` on the root of project.
3. On terminal, move on **/code** folder and tap: ```mvn clean install```
> Maven is required for this step.

> If the Maven build is success, you must move on **/jar** folder to find the executable JAR.
4. On the **/jar** folder, you can now launch the JAR with this synthax: ```java -Xmx4g -Djava.library.path=./../code/resources/. -jar rdfminer-1.0.jar [params]```

## Parameters

```
 -a (--axioms) AXIOMFILE              : test axioms contained in this file
 -d (--dynamic-timeout) ANGULAR_COEFF : use a dynamic time-out for axiom
                                        testing (default: 0.0)
 -g (--grammar) GRAMMAR               : use this file as the axiom grammar
                                        (default: /rdfminer/code/resources/OWL2A
                                        xiom-test.bnf)
 -o (--output) RESULTFILE             : name of output file (without
                                        extension): the name 'results' is
                                        chosen if -o is not used (default:
                                        results)
 -r (--random)                        : test randomly generated axioms
                                        (default: false)
 -s (--subclasslist) FILE             : test subClassOf axioms generated from
                                        the list of subclasses in the given file
 -sa (--single-axiom) AXIOM           : test a single axiom given
 -t (--timeout) MINUTES               : use this time-out (in minutes) for
                                        axiom testing (default: 0)
```

## Documentations

All resources about the project are avalaible on [docs](https://github.com/RemiFELIN/RDFMining/tree/main/docs) folder. See [working-paper.pdf](https://github.com/RemiFELIN/RDFMining/tree/main/docs/working-paper.pdf)
