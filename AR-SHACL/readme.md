# AR-SHACL

## Overview

The script *rulesToSHACL.py* read the CSV file *rules.csv* which contains the association rules (as Horn clauses)
(from the experimental results of Cadorel et al [1]) and convert it in SHACL Shapes considering 
the following structure:

```
@base       <http://example.com/shapes/> .
@prefix sh: <http://www.w3.org/ns/shacl#> .

<int> a sh:NodeShape ;
    sh:targetClass ~ANTECEDENT~ ;
    sh:property [  
        sh:path rdf:type ;  
        sh:hasValue ~CONSEQUENT~ ;
    ] .
```

The script produces a new file *shapes.ttl* containing these SHACL shapes.

### Remark

As the dataset used by Cadorel et al. to generate these rules and ours are different,
the script implements methods to keep the rules where the antecedent and the consequent match with classes
in our dataset.

## Installation

```pip3 install -r requirements.txt```

## How to use it

```python rulesToSHACL.py```

## References 

[1] Cadorel, L., Tettamanzi, A.: Mining rdf data of covid-19 scientific literature for
interesting association rules. 2020 IEEE/WIC/ACM International Joint Conference on 
Web Intelligence and Intelligent Agent Technology (WI-IAT) pp. 145–152 (2020)