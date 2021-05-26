#!/bin/awk -f
# Convert a file which is a concatenation of small XML files into a single valid
# XML file whose root element is <report>
BEGIN {
  print "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
  print "<?xml-stylesheet type=\"text/xsl\" href=\"report.xsl\" ?>"
  print "<report>"
}
END { print "</report>" }
$0 !~ /^<\?xml/ { print "    ", $0 }

