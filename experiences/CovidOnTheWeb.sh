#!/bin/bash
mkdir log

for i in {1..10}
do 
    mkdir ../IO/users/admin/pop200_$i/
    cp ../IO/shacl-shapes-ar.bnf ../IO/users/admin/pop200_$i/grammar.bnf
    chmod +x ../IO/users/admin/pop200_$i/grammar.bnf
    docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
    -user admin -dir pop200_$i \
    -target "http://corese:9100/sparql" \
    -ge -rs \
    -psh -shacl-p 0.5 -shacl-a 0.05  \
    -g pop200_$i/grammar.bnf \
    -ps 200 -kb 40000 -cr 1 -pc 0.75 -mu 1 -pm 0.05 -se 3 -er 0.2 -sr 0.4 -tr 0.2 -init 2 -div 0 -mxw 1 \
    > ./log/pop200_$i.log;
done

for i in {1..10}
do 
    mkdir ../IO/users/admin/pop500_$i/
    cp ../IO/shacl-shapes-ar.bnf ../IO/users/admin/pop500_$i/grammar.bnf
    chmod +x ../IO/users/admin/pop500_$i/grammar.bnf
    docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
    -user admin -dir pop500_$i \
    -target "http://corese:9100/sparql" \
    -ge -rs \
    -psh -shacl-p 0.5 -shacl-a 0.05  \
    -g pop500_$i/grammar.bnf \
    -ps 500 -kb 100000 -cr 1 -pc 0.75 -mu 1 -pm 0.05 -se 3 -er 0.2 -sr 0.4 -tr 0.2 -init 2 -div 0 -mxw 1 \
    > ./log/pop500_$i.log;
done

for i in {1..10}
do 
    mkdir ../IO/users/admin/pop1000_$i/
    cp ../IO/shacl-shapes-ar.bnf ../IO/users/admin/pop1000_$i/grammar.bnf
    chmod +x ../IO/users/admin/pop1000_$i/grammar.bnf
    docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
    -user admin -dir pop1000_$i \
    -target "http://corese:9100/sparql" \
    -ge -rs \
    -psh -shacl-p 0.5 -shacl-a 0.05  \
    -g pop1000_$i/grammar.bnf \
    -ps 1000 -kb 200000 -cr 1 -pc 0.75 -mu 1 -pm 0.05 -se 3 -er 0.2 -sr 0.4 -tr 0.2 -init 2 -div 0 -mxw 1 \
    > ./log/pop1000_$i.log;
done
