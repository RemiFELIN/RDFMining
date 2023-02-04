import csv
import json
import os
from SPARQLWrapper import SPARQLWrapper, JSON

# get the current path of the folder
WORKSPACE = os.getcwd() + "/"
# rules file path
RULES = WORKSPACE + "rules.csv"
# As the dataset used by Cadorel et al. to generate rules and ours are different,
# we will keep the rules where the antecedent and the consequent match with classes
# in our dataset.
# Consequently, we will query our dataset stored and queryable from the following endpoint:
service = "http://172.19.0.4:9100/sparql"
sparql = SPARQLWrapper(service)
# prefix base
prefix = """PREFIX :             <http://www.example.com/myDataGraph#> 
PREFIX rdfs:     <http://www.w3.org/2000/01/rdf-schema#>
PREFIX rdf:      <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX sh:       <http://www.w3.org/ns/shacl#> 

"""


def get_entity(label: str):
    """
    Check the existence of the given label (and its link with articles) into our dataset and return
    its Wikidata entity.
    :param label: label of Wikidata Class
    :return: the entity if the label is usable to build a shape, None otherwise
    """
    request = """
        PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
        SELECT distinct ?entity WHERE {{
            ?entity rdfs:label {x}@en .
            ?article a ?entity .
        }}""".format(x=label)
    # print(request)
    sparql.setQuery(request)
    sparql.setReturnFormat(JSON)
    res = json.load(sparql.query().response)
    if len(res["results"]["bindings"]) == 0:
        return None
    return res["results"]["bindings"][0]["entity"]["value"]


def get_ant_cons_from_file():
    """
    Get a list of tuples (antecedent,consequent)
    :return: a list of rules
    """
    # return a list of tuple such as : [(ant1,cons1), ..., (ant_n, cons_n)]
    res = []
    with open(RULES, "r", encoding="utf-8") as f:
        reader = csv.DictReader(f, delimiter=";")
        i = 0
        to_remove = []
        for row in reader:
            antecedent = str(row["antecedents-list"]).replace("[", "").replace("]", "")
            consequent = str(row["consequents-list"]).replace("[", "").replace("]", "")
            # compute sparql request and get entities
            ant_entity = get_entity(antecedent)
            cons_entity = get_entity(consequent)
            # check if it's defined
            if ant_entity is not None and cons_entity is not None:
                # append antecedant and consequent as a tuple in res
                res.append((ant_entity, cons_entity))
                i += 1
            # Else, we will report the label not relevant for our usage
            elif ant_entity is None:
                antecedent = antecedent.replace("\"", "")
                if antecedent not in to_remove:
                    to_remove.append(antecedent)
            elif cons_entity is None:
                consequent = consequent.replace("\"", "")
                if consequent not in to_remove:
                    to_remove.append(consequent)
        print("# Usable rules: " + str(i))
        print("Labels without any relations with our set of articles: " + str(to_remove))
        f.close()
    return res


if __name__ == '__main__':
    # construct a set of rules composed of 2 Wikidata entities
    tuples = get_ant_cons_from_file()
    # create a new file and write the future shapes
    with open("shapes.ttl", mode="w", encoding="utf-8") as file:
        # write prefix as 'header'
        file.write(prefix)
        # iterate on tuples
        id_shape = 1
        for rule in tuples:
            shape = """:{x} a sh:NodeShape ;
    sh:targetClass <{a}> ;
    sh:property [  
        sh:path rdf:type ;  
        sh:hasValue <{c}>;
] .

""".format(x=id_shape, a=rule[0], c=rule[1])
            id_shape += 1
            file.write(shape)
        file.close()
