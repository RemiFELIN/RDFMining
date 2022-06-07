#include <sys/time.h>
#include <sys/resource.h>
#include "rdfminer_RDFMiner.h"
/*

The application must then be run with the JVM command-line option: -Djava.library.path=.

*/

/**
 * Return the total (= user + system) CPU time for this process, in ms.
 */
JNIEXPORT jlong JNICALL Java_com_i3s_app_rdfminer_axiom_Axiom_getProcessCPUTime(JNIEnv *env, jclass c)
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

