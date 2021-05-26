#!/bin/awk -f
# Replaces long IRIs with prefixes
#PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
#PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
#PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
#PREFIX foaf: <http://xmlns.com/foaf/0.1/>
#PREFIX dc: <http://purl.org/dc/elements/1.1/>
#PREFIX : <http://dbpedia.org/resource/>
#PREFIX dbpedia2: <http://dbpedia.org/property/>
#PREFIX dbpedia: <http://dbpedia.org/>
#PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
{
  x = gensub(/>/, "", "g")
  x = gensub(/<http:\/\/dbpedia.org\/ontology\//, "dbo:", "g", x)
  x = gensub(/<http:\/\/www.w3.org\/2002\/07\/owl\#/, "owl:", "g", x)
  x = gensub(/<http:\/\/schema.org\//, "schema:", "g", x)
  x = gensub(/<http:\/\/www.opengis.net\/gml\//, "gml:", "g", x)
  x = gensub(/<http:\/\/www.w3.org\/2004\/02\/skos\/core\#/, "skos:", "g", x)
  x = gensub(/<http:\/\/xmlns.com\/foaf\/0.1\//, "foaf:", "g", x)
#  x = gensub(//, ":", "g", x)
  print x
}

