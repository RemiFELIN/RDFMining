#!/bin/bash
#################################################################################
# ISWC 2023                                                                     #
# experiments: CovidOnTheWeb dataset                                            #
#################################################################################
# rebase
cd ../RDFMining/
mkdir log
### n# exec:            10    -  10    -  10
### Population size:    200   -  500   -  1000
### KB:                 20000 -  50000 -  100000 (100 generations for each pop size considered)
### Crossover type:     SWAP Crossover
### Mutation type:      INT FLIP Mutation
### P(Crossover):       0,75
### P(Mutation):        0,05
### SHACL P-value:      0,5
#
# Pop 200
#
for i in {1..10}
do 
    docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -ge -rs \
    -shacl-p 0.5 -psh -g /rdfminer/io/shacl-shapes-ar.bnf \
    -dir /results_200/0dot5_swap_20000_$i/ \
    -ps 200 -kb 20000 -cr 4 -pc 0.75 -pm 0.05 -div 0 -mxw 1 -se 1 -init 2 \
    > ./log/covidontheweb_0dot5_200_$i.log
done
#
# Pop 500
#
for i in {1..10}
do 
    docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -ge -rs \
    -shacl-p 0.5 -psh -g /rdfminer/io/shacl-shapes-ar.bnf \
    -dir /results_500/0dot5_swap_50000_$i/ \
    -ps 500 -kb 50000 -cr 4 -pc 0.75 -pm 0.05 -div 0 -mxw 1 -se 1 -init 2 \
    > ./log/covidontheweb_0dot5_500_$i.log
done
#
# Pop 1000
#
for i in {1..10}
do 
    docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -ge -rs \
    -shacl-p 0.5 -psh -g /rdfminer/io/shacl-shapes-ar.bnf \
    -dir /results_1000/0dot5_swap_100000_$i/ \
    -ps 1000 -kb 100000 -cr 4 -pc 0.75 -pm 0.05 -div 0 -mxw 1 -se 1 -init 2 \
    > ./log/covidontheweb_0dot5_1000_$i.log
done
