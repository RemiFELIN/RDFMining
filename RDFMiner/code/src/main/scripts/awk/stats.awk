#!/bin/awk -f
# Extract statistics from an RDFMiner XML results file
# so that they can be easily processed with R
BEGIN {
  print "axiom\trefc\tconf\texpt\tposs\tnec\ttime"
}
$0 ~ /<axiom>.*<\/axiom>/ {
  x = gensub(/<[^<>]*>/, "", "g")
  x = gensub(/\s{2,}/, "", "g", x)
  x = gensub(/&lt;/, "<", "g", x)
  x = gensub(/&gt;/, "", "g", x)
  x = gensub(/<http:\/\/dbpedia.org\/ontology\//, "dbo:", "g", x)
  x = gensub(/<http:\/\/www.w3.org\/2002\/07\/owl\#/, "owl:", "g", x)
  x = gensub(/<http:\/\/schema.org\//, "schema:", "g", x)
  x = gensub(/<http:\/\/www.opengis.net\/gml\//, "gml:", "g", x)
  x = gensub(/<http:\/\/www.w3.org\/2004\/02\/skos\/core\#/, "skos:", "g", x)
  axiom = gensub(/<http:\/\/xmlns.com\/foaf\/0.1\//, "foaf:", "g", x)
}
$0 ~ /<referenceCardinality>.*<\/referenceCardinality>/ {
  referenceCardinality = gensub(/<[^<>]*>/, "", "g")
  referenceCardinality = gensub(/\s/, "", "g", referenceCardinality)
}
$0 ~ /<numConfirmations>.*<\/numConfirmations>/ {
  numConfirmations = gensub(/<[^<>]*>/, "", "g")
  numConfirmations = gensub(/\s/, "", "g", numConfirmations)
}
$0 ~ /<numExceptions>.*<\/numExceptions>/ {
  numExceptions = gensub(/<[^<>]*>/, "", "g")
  numExceptions = gensub(/\s/, "", "g", numExceptions)
}
$0 ~ /<possibility>.*<\/possibility>/ {
  possibility = gensub(/<[^<>]*>/, "", "g")
  possibility = gensub(/\s/, "", "g", possibility)
}
$0 ~ /<necessity>.*<\/necessity>/ {
  necessity = gensub(/<[^<>]*>/, "", "g")
  necessity = gensub(/\s/, "", "g", necessity)
}
$0 ~ /<elapsedTime>.*<\/elapsedTime>/ {
  elapsedTime = gensub(/<[^<>]*>/, "", "g")
  elapsedTime = gensub(/\s/, "", "g", elapsedTime)
  print "\"" axiom "\"\t" referenceCardinality "\t" numConfirmations "\t" numExceptions "\t" possibility "\t" necessity "\t" elapsedTime
}

