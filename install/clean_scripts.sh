#!/bin/bash

# Format our shell scripts 
# > Allows us to catch errors related to the use of the software with Windows
# > It does not impact Linux users, as well as the execution of the following script...
echo "----------------------------------------------"
for script in `find ./../RDFMiner/ ./../Virtuoso/scripts/ ./../Corese/scripts/ . -name "*.sh"`
do 
	echo -n "   "$script" | Init: "$(cat $script | wc -l)
	sed -i -e 's/\r$//' $script
	echo " | Final: "$(cat $script | wc -l)" |" 
done
echo "----------------------------------------------"