#!/bin/bash
mkdir log

# V1 ||P||/E choice
for pop in 100 200 500
do 
    for effort in 5000 10000 20000
    do 
        for i in {1..10}
        do
            mkdir ../IO/users/admin/V1_${pop}_${effort}_${i}/
            cp ../IO/shacl-shapes-ar.bnf ../IO/users/admin/V1_${pop}_${effort}_${i}/grammar.bnf
            chmod +x ../IO/users/admin/V1_${pop}_${effort}_${i}/grammar.bnf
            docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
            -user admin -dir V1_${pop}_${effort}_${i} \
            -target "http://corese:9100/sparql" \
            -ge -rs \
            -psh -shacl-p 0.5 -shacl-a 0.05  \
            -g V1_${pop}_${effort}_${i}/grammar.bnf \
            -ps $pop -kb $effort -cr 4 -pc 0.75 -mu 1 -pm 0.05 -se 3 -er 0.2 -sr 0.4 -tr 0.25 -init 2 -div 0 -mxw 1 \
            > ./log/V1_${pop}_${effort}_${i}.log;
        done
    done
done

# V2 Selection (R) pressure with Scaled Roulette wheel selection
for rate in 0.2 0.4 0.6
do 
    for i in {1..10}
    do 
        mkdir ../IO/users/admin/V2_Roulette_${rate}_${i}/
        cp ../IO/shacl-shapes-ar.bnf ../IO/users/admin/V2_Roulette_${rate}_${i}/grammar.bnf
        chmod +x ../IO/users/admin/V2_Roulette_${rate}_${i}/grammar.bnf
        docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
        -user admin -dir V2_Roulette_${rate}_${i} \
        -target "http://corese:9100/sparql" \
        -ge -rs \
        -psh -shacl-p 0.5 -shacl-a 0.05  \
        -g V2_Roulette_${rate}_${i}/grammar.bnf \
        -ps 100 -kb 20000 -cr 4 -pc 0.75 -mu 1 -pm 0.05 -se 2 -er 0.2 -sr $rate -init 2 -div 0 -mxw 1 \
        > ./log/V2_Roulette_${rate}_${i}.log;
    done
done

# V2 Selection (R) pressure with Tournament selection
for rate in 0.2 0.4 0.6
do 
    for tour in 0.1 0.25 0.5
    do 
        for i in {1..10}
        do
            mkdir ../IO/users/admin/V2_Tournament_${rate}_${tour}_${i}/
            cp ../IO/shacl-shapes-ar.bnf ../IO/users/admin/V2_Tournament_${rate}_${tour}_${i}/grammar.bnf
            chmod +x ../IO/users/admin/V2_Tournament_${rate}_${tour}_${i}/grammar.bnf
            docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
            -user admin -dir V2_Tournament_${rate}_${tour}_${i} \
            -target "http://corese:9100/sparql" \
            -ge -rs \
            -psh -shacl-p 0.5 -shacl-a 0.05  \
            -g V2_Tournament_${rate}_${tour}_${i}/grammar.bnf \
            -ps 100 -kb 20000 -cr 4 -pc 0.75 -mu 1 -pm 0.05 -se 3 -er 0.2 -sr $rate -tr $tour -init 2 -div 0 -mxw 1 \
            > ./log/V2_Tournament_${rate}_${tour}_${i}.log;
        done
    done
done
