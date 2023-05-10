import json
import math
import os
from SPARQLWrapper import SPARQLWrapper, JSON
import random
from scipy.stats import norm

# IMPORTANT: we'll define a seed to generate a random population of shapes
# and reproduce the same results
random.seed(30)
# get the current path of the folder
WORKSPACE = os.getcwd() + "/"
# results file
OUTPUT = WORKSPACE + "shapes_to_assess.ttl"
# we will query our dataset stored and queryable from the following endpoint:
service = "http://172.19.0.4:9100/sparql"
sparql = SPARQLWrapper(service)
# prefix base
PREFIX = """PREFIX :         <http://www.example.com/myDataGraph#>
PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sh:       <http://www.w3.org/ns/shacl#> 

"""
# list of entities
ENTITIES = []


def fill_entities():
    """
    return the list of entities provided in Corese datastore for the CovidOnTheWeb dataset
    """
    request = """
        SELECT distinct ?entity WHERE {
            ?article a ?entity .
        }"""
    sparql.setQuery(request)
    sparql.setReturnFormat(JSON)
    res = json.load(sparql.query().response)
    if len(res["results"]["bindings"]) != 0:
        for result in res["results"]["bindings"]:
            ENTITIES.append(result["entity"]["value"])


def generate_random_shape(size):
    # select random antecedant
    shapes = ""
    candidates = []
    for i in range(size):
        # print('i: ', i)
        ant_idx = random.randint(0, len(ENTITIES) - 1)
        cons_idx = random.randint(0, len(ENTITIES) - 1)
        shape = """:{x} a sh:NodeShape ; sh:targetClass <{ant}> ; sh:property [ sh:path rdf:type ; sh:hasValue <{cons}>; ] .\n""" \
            .format(x=i + 1, ant=ENTITIES[ant_idx], cons=ENTITIES[cons_idx])
        # filter potential duplicates
        if shape not in candidates:
            candidates.append(shape)
            shapes += shape
        else:
            print("duplicates !")
    return shapes


def get_population_size(confidence, p, error_rate):
    z = round(norm.ppf(1 - (1 - confidence) / 2), 2)
    print(z*z)
    return math.ceil((z ** 2 * p * (1 - p)) / error_rate ** 2)


def generate_shapes(confidence, p, error_rate):
    # get entites
    fill_entities()
    # find the size according to the desired representativeness
    size = get_population_size(confidence, p, error_rate)
    # edit output file
    with open(OUTPUT, mode="w", encoding="utf-8") as file:
        file.write(PREFIX)
        # generate random shapes
        file.write(generate_random_shape(size))
    file.close()


if __name__ == '__main__':
    generate_shapes(0.99, 0.5, 0.02)
    # print(get_population_size(0.99, 0.5, 0.02))
