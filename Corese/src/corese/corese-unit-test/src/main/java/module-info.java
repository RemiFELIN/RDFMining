module fr.inria.corese.corese_test {
    requires fr.inria.corese.sparql;
    requires fr.inria.corese.corese_core;
    //requires fr.inria.corese.kgram;
    requires fr.inria.corese.compiler;
    requires java.logging;
    requires java.xml;

    opens fr.inria.corese.engine;
}