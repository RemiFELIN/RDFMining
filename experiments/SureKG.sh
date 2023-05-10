#!/bin/bash
#################################################################################
# ISWC 2023                                                                     #
# experiments: Sure-KG dataset                                                  #
#################################################################################
# rebase
cd ../RDFMining/
mkdir log
### shacl p-value:		0.1	- 0.2 - 0.3 - 0.4 - 0.5
### n# exec:            10  - 10  - 10  - 10  - 10
### Population size:    200
### KB:                 20000 (100 generations)
### Crossover type:     SWAP Crossover
### Mutation type:      INT FLIP Mutation
### P(Crossover):       0,75
### P(Mutation):        0,05
#
# Pop 200
#
for j in `seq 0.1 .1 .5`
do
	# p value for probabilistic SHACL validation
	p=$(echo $j | sed 's/,/./g');
	echo $p
	for i in {1..10}
	do 
    		docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -ge -rs \
    			-shacl-p $p -psh -g /rdfminer/io/shacl-shapes-grammar.bnf \
    			-dir /surekg/results_p_$p/$i/ \
    			-ps 200 -kb 20000 -cr 4 -pc 0.75 -pm 0.05 -div 0 -mxw 1 -se 1 -init 6 \
    		> ./log/surekg_$p_$i.log
	done
done

