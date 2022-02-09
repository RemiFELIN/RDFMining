package fr.inria.corese.gui.query;

import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.event.EventListener;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.kgram.core.SparqlException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.context.ContextLog;

/**
 * Evaluator of SPARQL query by KGRAM Implement KGRAM on top of Corese with
 * Corese API, lightweight version: Mappings SPARQLQuery
 *
 * @author Olivier Corby, Edelweiss, INRIA 2009
 *
 */
public class QueryExec {

    protected QueryProcess exec;
    protected boolean isListGroup = false,
            isDebug = false;
    protected ArrayList<EventListener> list;

    public QueryExec() {
        list = new ArrayList<EventListener>();
    }

    public static QueryExec create() {
        return new QueryExec();
    }

    /**
     * Corese implementation
     */
    public static QueryExec create(GraphEngine engine) {
        QueryExec qe = new QueryExec();
        qe.add(engine);
        return qe;
    }
    
    public void finish() {
        if (exec != null) {
            exec.finish();
        }
    }
    
    public ContextLog getLog() {
        return getQueryProcess().getLog();            
    }

    /**
     * Draft with several engine
     *
     * TODO: add is done in first engine (see constructor in set() )
     */
    public void add(GraphEngine engine) {
        if (exec == null) {
            exec = engine.createQueryProcess();
            //exec.setListGroup(isListGroup);
            exec.setDebug(isDebug);
            for (EventListener el : list) {
                exec.addEventListener(el);
            }
        } else {
            exec.add(engine.getGraph());
        }
    }

    public void definePrefix(String p, String ns) {
        QueryProcess.definePrefix(p, ns);
    }

    public void setListGroup(boolean b) {
        isListGroup = b;
        if (exec != null) {
            exec.setListGroup(true);
        }
    }

    public void setDebug(boolean b) {
        isDebug = b;
        if (exec != null) {
            exec.setDebug(true);
        }
    }

    /**
     * User API query processor
     */
    public Mappings SPARQLQuery(String squery) throws EngineException {
        // use case: call @event   @public @prepare function () {}
        exec.prepare();
        Mappings map = exec.query(squery);
        return map;
    }
    
    // Mappings for transformer
    public Mappings SPARQLQuery(String squery, Mappings amap) throws EngineException {
        if (amap == null) {
            return SPARQLQuery(squery);
        }
        // use case: call @event   @public @prepare function () {}
        exec.prepare();
        Mappings map = exec.query(squery, Binding.create().setMappings(amap));
        return map;
    }
    
    public Mappings SPARQLQuery(Query query) throws EngineException {
        Mappings map = exec.query(query);
        return map;
    }
    

    public Query compile(String squery) throws EngineException {
        return exec.compile(squery);
    }
    
//    public void modifier(Query q, Mappings map) throws SparqlException {
//        exec.modifier(q, map);
//    }
    
    public Mappings modifier(String str, Mappings map) throws SparqlException {
       return exec.modifier(str, map);
    }
    
    /**
     * call after compile to prepare GUI order by
     * use case: user edit order by clause and click Sort button
     * We sort Mappings again according to new order by clause 
     * 
     */
    public void complete(Query q, Mappings map) throws EngineException {
        map.setQuery(q);
        map.setEval(exec.getEval());
    }

    public Mappings query(String squery) throws EngineException {
        Mappings map = exec.sparqlQuery(squery);
        return map;
    }

    public Mappings update(String squery) throws EngineException {
        Mappings map = exec.sparqlUpdate(squery);
        return map;
    }

    public Mappings SPARQLQuery(String squery, List<String> from, List<String> named) throws EngineException {
        Dataset ds = Dataset.newInstance(from, named);
        Mappings map = exec.query(squery, null, ds);
        return map;
    }

    public void addEventListener(EventListener el) {
        list.add(el);
        if (exec != null) {
            exec.addEventListener(el);
        }
    }

    public Mappings SPARQLQuery(ASTQuery ast) throws EngineException {
        Mappings map = exec.query(ast);
        return map;
    }
    
    public QueryProcess getQueryProcess() {
        return exec;
    }

}
