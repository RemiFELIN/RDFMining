docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
                    -ge -ra -ns -l \
                    -train "http://172.19.0.2:9000/sparql" \
                    -g /rdfminer/io/OWL2Axiom-subclassof.bnf \
                    -dir debug_axioms/ \
                    -ps 10 \
                    -kb 100 \
                    -cr 4 \
                    -pc 0.75 \
                    -pm 0.05 \
                    -se 1 \
                    -div 0 \
                    -init 2 \
                    -st 3000 -tc 1 > debug.log

# docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh \
#     -ge -rs -psh \
#     -shacl-p 0.5 \
#     -g /rdfminer/io/shacl-shapes-test.bnf \
#     -dir /DEBUG/ \
#     -ps 10 \
#     -kb 100 \
#     -cr 4 \
#     -pc 0.75 \
#     -pm 0.05 \
#     -div 0 \
#     -mxw 1 \
#     -se 1 \
#     -init 2 \
#     > debug.log
