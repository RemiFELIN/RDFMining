#!/bin/bash
# Set 'x' authorization for all files injected on container
# echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - parameters: "${@}
# arr=("$@")
# if [[ "$arr" != "launch" ]]
# then
# 	for index in ${!arr[@]}; do
# 		if [ "${arr[index]}" == "-af" ] || \
# 		[ "${arr[index]}" == "--axioms-file" ] || \
# 		[ "${arr[index]}" == "-g" ] || \
# 		[ "${arr[index]}" == "--grammar" ] || \
# 		[ "${arr[index]}" == "-sf" ] || \
# 		[ "${arr[index]}" == "--shapes-file" ]; then
# 			echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - set '+x' authorization : "${arr[index + 1]}\
# 			" for "${arr[index]}" option"
# 			# Set 'x' autorization for each file used
# 			chmod +x ${arr[index + 1]}
# 		fi
# 	done
echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - launch RDFminer-core server ..."
# Execute jar file with params
${JAVA_HOME}/bin/java -Xmx4g -Djava.library.path=${HOME}/code/resources/. -Dfile.encoding="UTF-8" -jar ${HOME}/jar/rdfminer-${RDFMINER_VERSION}.jar "${@}"
# else
# 	echo $(date +"%Y-%m-%d %H:%M:%S,%3N")" [run.sh] INFO - launch rdfminer service ..."
# 	# TMP : to keep a container running
# 	tail -F anything
# # echo $(date +"%d/%m/%Y %H:%M:%S")" [RDFMiner][run.sh] - files on shared folder"
# # cd rdfminer/in/ && ls -al && cd ..
# fi
