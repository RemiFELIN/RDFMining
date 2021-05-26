#!/bin/bash
# Set 'x' authorization for all files injected on container
echo $(date +"%d/%m/%Y %H:%M:%S")" [RDFMiner][run.sh] - params: "${@}
arr=("$@")
for index in ${!arr[@]}; do
	if [ ${arr[index]} == "-a" ] || \
	   [ ${arr[index]} == "--axioms" ] || \
	   [ ${arr[index]} == "-g" ] || \
	   [ ${arr[index]} == "--grammar" ] || \
	   [ ${arr[index]} == "-s" ] || \
	   [ ${arr[index]} == "--subclasslist" ]; then
		echo $(date +"%d/%m/%Y %H:%M:%S")" [RDFMiner][run.sh] - set 'x' authorization : "${arr[index + 1]}\
		" for "${arr[index]}" option"
		# Set 'x' autorization for each file used
		chmod +x ${arr[index + 1]}
	fi
done
# echo $(date +"%d/%m/%Y %H:%M:%S")" [RDFMiner][run.sh] - files on shared folder"
# cd rdfminer/in/ && ls -al && cd ..
echo $(date +"%d/%m/%Y %H:%M:%S")" [RDFMiner][run.sh] - launch rdfminer.jar ..."
# Execute jar file with params
${JAVA_HOME}bin/java -Xmx4g -Djava.library.path=${HOME}/code/resources/. -jar ${HOME}/jar/rdfminer-${RDFMINER_VERSION}.jar ${@}
