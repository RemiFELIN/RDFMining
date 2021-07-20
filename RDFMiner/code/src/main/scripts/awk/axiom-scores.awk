#!/bin/awk -f
# Extract a list of axioms with their acceptance/rejection score from an RDFMiner XML results file
BEGIN {
  print "ARI\taxiom"
}
$0 ~ /<axiom>.*<\/axiom>/ {
  axiom = gensub(/<[^<>]*>/, "", "g")
  axiom = gensub(/\s{2,}/, "", "g", axiom)
  axiom = gensub(/&lt;/, "<", "g", axiom)
  axiom = gensub(/&gt;/, ">", "g", axiom)
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
  print (possibility + necessity - 1.0) "\t\"" axiom "\""
}

