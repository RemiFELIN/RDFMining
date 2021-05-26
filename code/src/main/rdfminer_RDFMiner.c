#include <sys/time.h>
#include <sys/resource.h>
#include "rdfminer_RDFMiner.h"
/*
Compile with:

g++ -I"/home/tettaman/mozilla/sun-java/stubs/include" -I"/home/tettaman/mozilla/dist/sdk/include" -shared -fPIC rdfminer_RDFMiner.c -o librdfminer_RDFMiner.so

On "souris", replace the -I option with -I"/usr/lib/jvm/java-7-openjdk-amd64/include/":

gcc -I/usr/lib/jvm/java-7-openjdk-amd64/include -o librdfminer_RDFMiner.so -shared rdfminer_RDFMiner.c -fPIC

The application must then be run with the JVM command-line option: -Djava.library.path=.

*/

/**
 * Return the total (= user + system) CPU time for this process, in ms.
 */
JNIEXPORT jlong JNICALL Java_rdfminer_RDFMiner_getProcessCPUTime(JNIEnv *env, jclass c)
{
	struct rusage usage;
	long t;

	if(getrusage(RUSAGE_SELF, &usage)==0)
		t = ((long) usage.ru_utime.tv_sec)*1000L + ((long) usage.ru_utime.tv_usec)/1000L +
		    ((long) usage.ru_stime.tv_sec)*1000L + ((long) usage.ru_stime.tv_usec)/1000L;
	else
		t = 31415926L;
	return (jlong) t;
}

