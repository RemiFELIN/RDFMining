# ESWC 2023 Experiments
#
cd ../
# save log of experiments from Corese server
# i.e. from probabilistic validation of shapes
mkdir -p IO/log
docker-compose up corese > IO/log/corese_xp.log &
sleep 15
# Considering p = {0.05; 0.10; 0.15; 0.20; 0.25; 0.30; 0.35; 0.40; 0.45; 0.50; 0.55; 0.60; 0.65; 0.70; 0.75}
# and aplha = 0.05 
for i in `seq .05 .05 1`;
do
    # launch xp
    echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [psh] INFO - run probabilistic SHACL eval with p=$i ..."
    docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -psh -shacl-p $i -shacl-a 0.05 -sf /rdfminer/io/shapes/shapes.ttl -dir /results/res_p_$i/
    # Stop the server
    echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [psh] INFO - Done ..."
    # docker-compose stop corese &
    # sleep 20
done
