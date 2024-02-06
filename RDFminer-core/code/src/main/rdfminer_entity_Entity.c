#define _GNU_SOURCE
#include <sys/time.h>
#include <sys/resource.h>
#include "rdfminer_entity_Entity.h"
/*

The application must then be run with the JVM command-line option: -Djava.library.path=.

*/

/**
 * Return the total (= user + system) CPU time for this process, in ms.
 */

JNIEXPORT jlong JNICALL Java_com_i3s_app_rdfminer_entity_Entity_getProcessCPUTime (JNIEnv *env, jclass c)
{
	struct rusage usage;
	long t;
	// Since RDFMiner works with a multi-threading system, we have evolved the method to take only the time consumed by the current thread.
	// Here is a link that described the fix : https://www.delftstack.com/fr/howto/c/getrusage-example-in-c/
	if(getrusage(RUSAGE_THREAD, &usage)==0)
		t = ((long) usage.ru_utime.tv_sec)*1000L + ((long) usage.ru_utime.tv_usec)/1000L +
		    ((long) usage.ru_stime.tv_sec)*1000L + ((long) usage.ru_stime.tv_usec)/1000L;
	else
		t = 31415926L;
	return (jlong) t;
}

