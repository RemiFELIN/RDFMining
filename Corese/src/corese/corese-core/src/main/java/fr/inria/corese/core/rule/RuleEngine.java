package fr.inria.corese.core.rule;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.sparql.triple.printer.SPIN;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Graphable;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.core.Sorter;
import fr.inria.corese.core.api.Engine;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.EventManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.api.DataManager;
import fr.inria.corese.core.logic.Closure;
import fr.inria.corese.core.logic.Entailment;
import fr.inria.corese.core.query.Construct;
import fr.inria.corese.core.query.QueryEngine;
import fr.inria.corese.core.query.QueryProcess;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.query.update.GraphManager;
import static fr.inria.corese.core.rule.RuleEngine.Profile.OWLRL;
import static fr.inria.corese.core.rule.RuleEngine.Profile.OWLRL_EXT;
import static fr.inria.corese.core.rule.RuleEngine.Profile.OWLRL_LITE;
import static fr.inria.corese.core.rule.RuleEngine.Profile.STDRL;
import static fr.inria.corese.core.rule.RuleEngine.Profile.RDFS;
import fr.inria.corese.core.util.Property;
import fr.inria.corese.core.visitor.solver.QuerySolverVisitorRule;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.sparql.triple.function.core.UUIDFunction;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.Access;
import fr.inria.corese.sparql.triple.parser.Access.Feature;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.AccessRight;

/**
 * Forward Rule Engine 
 * Use construct {} where {} SPARQL Query as Rule
 * Optimizations:
 * Do not create Mappings, create triples directly
 * Consider rules for which new relevant triples are available
 * Consider solutions with new triple
 * Focus on new triples using specific Graph Index sorted by timestamp
 * Eval transitive rule at saturation using specific Java code
 * Eval pseudo transitive rule just after it's transitive rule 
 * (cf rdf:type & rdfs:subClassOf)
 * 
 * OWL_RL profile load specific rule base
 * 
 * @author Olivier Corby, Edelweiss INRIA 2011
 * Wimmics INRIA I3S, 2014
 */
public class RuleEngine implements Engine, Graphable {
   
    static final String NL = System.getProperty("line.separator");
    static final String OWL_RL_PROFILE = NSManager.OWL_RL_PROFILE;
    public static final int OWL_RL_FULL = -1;
    public static final int STD = 0;
    public static final int OWL_RL = 1;
    public static final int OWL_RL_LITE = 2;
    public static final int OWL_RL_EXT = 3;
    public static final int RDFS_RL = 4;
    public static boolean OWL_CLEAN = true;
   
    private static final String UNKNOWN = "unknown";
    public static Logger logger = LoggerFactory.getLogger(RuleEngine.class);
    Graph graph;
    private GraphManager graphManager;
    QueryProcess exec;
    private QueryEngine qengine;
    private List<Rule> rules;
    List<Record> records;
    private Object spinGraph;
    private Dataset ds;
    STable stable;
    // check that kgram solutions contain a newly entailed edge
    ResultWatcher rw;
    // kgram ResultListener create edges instead of create Mappings
    // LIMITATION: do not use if rule creates Node because graph would be 
    // modified during query execution
    private boolean isConstructResult = false;
    // run rules for wich new edges were created at loop n-1
    // check that rule solutions contains one edge from loop n-1
    // LIMITATION: do not use if Corese RDFS entailment is set to true
    // because we test predicate equality (we do not check rdfs:subPropertyOf)
    private boolean isOptimize = false;
    private boolean optimizable = true;
    private boolean debug = false;
    boolean trace = false;
    private boolean test = false;
    int loop = 0;
    Profile profile = STDRL;
    private boolean isActivate = true;
    private boolean isOptTransitive = false;
    private boolean isFunTransitive = false;
    private boolean isConnect = false;
    private boolean isDuplicate = false;
    private boolean isSkipPath = false;
    private boolean synchronize = false;
    private boolean event = true;
    private boolean record = false;
    private Context context;
    private ProcessVisitor visitor;
    private String base;
    private Level level = Level.USER_DEFAULT;
    private AccessRight accessRight;
    private List<RuleError> errorList;
    
    public enum Profile {
        
        STDRL,
        OWLRL      ("/rule/owlrl.rul"),
        OWLRL_LITE ("/rule/owlrllite.rul"),
        OWLRL_EXT  ("/rule/owlrlext.rul") ,
        RDFS       ("/rule/rdfs.rul")       ;
        
        String path;
        
        Profile() {}
        
        Profile(String path) {
            this.path = path;
        }
        
        String getPath() {
            return path;
        }
    
    };
    
    

    public RuleEngine() {
        rules = new ArrayList<>();
        errorList = new ArrayList<>();
    }
    
    
    
    public static RuleEngine create(Graph g) {
        return create(new GraphManager((g)));
    }
    
    
    public static RuleEngine create(GraphManager gm) {
        RuleEngine eng = new RuleEngine();
        eng.set(gm.getGraph());
        eng.set(QueryProcess.create(gm.getGraph()));
        eng.setGraphManager(gm);
        return eng;
    }
    
    public static RuleEngine create(DataManager dm) {
        RuleEngine eng = new RuleEngine();
        eng.set(QueryProcess.create(dm));
        eng.set(eng.getQueryProcess().getGraph());
        eng.setGraphManager(eng.getQueryProcess().getUpdateGraphManager());
        // optimizer needs to record index in entailed edges
        // here we have no waranty that external graph record index
        // hence skip optimization
        eng.setOptimizable(false);
        return eng;
    }
    
    void set(Graph g) {
        graph = g;
    }
    
    public Graph getGraphStore() {
        return graph;
    }

    public void set(QueryProcess p) {
        exec = p;
        p.setListPath(true);
    }
    
    public void setProfile(Profile p) throws LoadException {
        profile = p;
        loadProfile(p);
    }
    
    public void setProfile(String p) throws LoadException {
        switch (p) {
            case OWL_RL_PROFILE:
                setProfile(OWL_RL);
                break;
                
            default:
                throw new LoadException(new EngineException("Undefined Rule Base: " + p));
        }
    }
    
    void loadProfile(Profile p) throws LoadException {
        switch (p) {
            case OWLRL:
            case OWLRL_LITE:
            case OWLRL_EXT:
                if (Property.stringValue(Property.Value.OWL_RL) != null) {
                    // user defined OWL RL
                    logger.info("Load user OWL RL: " + Property.stringValue(Property.Value.OWL_RL));
                    loadPath(Property.stringValue(Property.Value.OWL_RL));
                } else {
                    load(p.getPath());
                }
                break;
            case RDFS: load(p.getPath());
                break;
        }
    }
    
    /**
     * setProfile(OWL_RL) load OWL RL rule base and clean the OWL/RDF graph
     * 
     */
    public void setProfile(int n)  {
        try {
        switch (n) {
            case OWL_RL:        setProfile(OWLRL)  ; break;              
            case OWL_RL_LITE:   setProfile(OWLRL_LITE)  ; break;
            case OWL_RL_EXT:    setProfile(OWLRL_EXT)  ; break;   
            case RDFS_RL:       setProfile(RDFS)  ; break;    
        }
        }
        catch (LoadException e) {
            logger.error(e.getMessage());
        }
    }
    
    void processProfile(){
        switch (profile) {
            case OWLRL:                   
            case OWLRL_LITE:    
            case OWLRL_EXT:
                optimizeOWLRL();               
                break;        
        }
    }
    
    void load(String name) throws LoadException {
        Load ld = Load.create(graph);
        ld.setEngine(this);
        ld.setQueryProcess(getQueryProcess());
        InputStream stream = RuleEngine.class.getResourceAsStream(name);
        ld.parse(stream, NSManager.RESOURCE+name, Load.RULE_FORMAT);
    }
    
    void loadPath(String name) throws LoadException {
        Load ld = Load.create(graph);
        ld.setEngine(this);
        ld.setQueryProcess(getQueryProcess());
        ld.parse(name, Load.RULE_FORMAT);
    }
      
    /**
     * 
     */
    public void optimizeOWLRL() {
        if (isOptimizable()) {
            setSpeedUp(true);
            try {
                if (OWL_CLEAN) {
                    cleanOWL();
                }
            } catch (IOException | EngineException | LoadException ex) {
                logger.error("", ex);
            }
            // enable graph Index by timestamp
            getGraphStore().setHasList(true);
        }
    }
    
    /**
     * Clean OWL RDF graph
     */
    public void cleanOWL() throws IOException, EngineException, LoadException{
        Cleaner cl = new Cleaner(graph);
        cl.setDebug(isDebug());
        if (isEvent()) {
            cl.setVisitor(getVisitor());
        }
        getEventManager().start(Event.CleanOntology);
        cl.clean(Cleaner.OWL);
        getGraphStore().getIndex(1).clean();
        getEventManager().finish(Event.CleanOntology);
    }
    
    EventManager getEventManager() {
        return getGraphStore().getEventManager();
    }
       
    public Profile getProfile(){
        return profile;
    }

    public QueryProcess getQueryProcess() {
        return exec;
    }

    public void set(Sorter s) {
        if (exec != null) {
            exec.set(s);
        }
    }

    public void setOptimize(boolean b) {
        isOptimize = b;
    }
    
    /**
     * Consider rules if there are new triples
     * Consider solutions that contain new triples
     * Do not create Mappings, create edge directly
     * Loop on transitive rules
     * Specific Closure code on transitive rules
     */
    public void setSpeedUp(boolean b) {
        setOptimize(b);
        setConstructResult(b);
        setOptTransitive(b);
        setFunTransitive(b);
        getQueryProcess().setListPath(b);
    }
    
    public IDatatype setFast(boolean b) {
        setSpeedUp(b);
        return DatatypeMap.TRUE;
    }

    public void setTrace(boolean b) {
        trace = b;
    }

   

//    public static RuleEngine create(QueryProcess q) {
//        RuleEngine eng = new RuleEngine();
//        eng.set(q);
//        return eng;
//    }
//
//    public static RuleEngine create(Graph g, QueryProcess q) {
//        RuleEngine eng = new RuleEngine();
//        eng.set(g);
//        eng.set(q);
//        return eng;
//    }
    
    /**
     * 
     * @return true if there is no Constraint Violation
     */
    public boolean success(){
        try {
            String q = QueryLoad.create().getResource("/query/rulesuccess.rq");
            QueryProcess ex = QueryProcess.create(graph);
            Mappings map = ex.query(q);
            return map.size() == 0;
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return true;
    }
    
    /**
     * @return a Graph of Constraint Violation, may be empty
     * @todo: DataManager
     */
    public Graph constraint(){
         try {
            String q = QueryLoad.create().getResource("/query/ruleconstraint.rq");
            QueryProcess ex = QueryProcess.create(getGraphStore());
            Mappings map = ex.query(q);
            return ex.getGraph(map);
        } catch (IOException | EngineException ex) {
            logger.error(ex.getMessage());
        }
        return Graph.create();
    }

    @Override
    public boolean process() throws EngineException {
        return process(Binding.create());
    }
    
    /**
     * LDScript Binding stack is shared by rule processing
     * Hence global and static variables are available
     * Binding may manage AccessRight which is shared by rule processing
     */
    public boolean process(Binding b) throws EngineException {
        logger.info("process: " + getPath());
        before(b);
        int size = getGraphManager().size(); 
        Mapping m = Mapping.create(b);
        entail(m, b);
        after();
        return getGraphManager().size() > size;
    }
    
    /**
     * Process Rule engine without interfering with Graph Workflow if any, 
     * in particular with RDFS entailment when cleaning the Ontology
     * @return 
     */
    public boolean processWithoutWorkflow() throws EngineException {
        boolean status = getGraphStore().getWorkflow().isActivate();
        getGraphStore().getWorkflow().setActivate(false);
        try {
            boolean b = process();
            return b;
        }
        finally {
            getGraphStore().getWorkflow().setActivate(status);
        }
    }    
    
    public IDatatype getPath() {
        String str = getProfile().getPath();
        if (str == null) {
            str = "rule base";
        }
        return DatatypeMap.newResource(str);
    }
    
    void before(Binding b) {
        getErrorList().clear();
        setEvent(Access.accept(Feature.EVENT, b.getAccessLevel()));
        try {
            setVisitor(QuerySolverVisitorRule.create(this, getQueryProcess().getEval()));            
            getVisitor().getProcessor().getEnvironment().setBind(b);
            if (isEvent()) {
                getVisitor().init();
                getVisitor().beforeEntailment(getPath());
            }
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        if (getGraphStore() == null) {
            set(Graph.create());
        }
        getQueryProcess().setSynchronized(isSynchronized());
        getGraphStore().getEventManager().start(Event.InferenceEngine, getClass().getName());
    }
    
//    void beforeConstraint() {
//        getGraphManager().clearConstraintGraph();
//    }
    
    void after() throws EngineException {
        getGraphStore().getEventManager().finish(Event.InferenceEngine, getClass().getName());
        if (isEvent()) getVisitor().afterEntailment(getPath());
        if (! getErrorList().isEmpty()) {
            throw new EngineException("RuleEngine Constraint Error", getErrorList()) ;
        }
    }
        
    public Graph getRDFGraph() {
        return graph;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public void clear() {
        getRules().clear();
    }
    
    public boolean isEmpty(){
        return getRules().isEmpty();
    }
    
    public String getConstraintViolation(){
        try {
            String q = QueryLoad.create().getResource("/query/constraint.rq");
            QueryProcess ex = QueryProcess.create(graph);
            Mappings map = ex.query(q);
            return map.getTemplateStringResult();
        } catch (IOException ex) {
            LoggerFactory.getLogger(RuleEngine.class.getName()).error(  "", ex);
        } catch (EngineException ex) {
            LoggerFactory.getLogger(RuleEngine.class.getName()).error(  "", ex);
        }
        return null;
    }

    /**
     * Define a construct {} where {} rule
     */
    public Query defRule(String rule) throws EngineException {
        return defRule(getRuleID(), rule);
    }

    public void defRule(Query rule) {
        defRule(Rule.create(getRuleID(), rule));
    }
    
    public static String getRuleID() {
        return UUIDFunction.getUUID();
    }
    
    public void defRule(Rule rule) {
        declare(rule);
        getRules().add(rule);
    }

    public void addRule(String rule) {
        try {
            defRule(rule);
        } catch (EngineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public IDatatype remove(IDatatype uriList) {
        List<String> list = new ArrayList<>();
        for (IDatatype dt : uriList) {
            list.add(dt.getLabel());
        }
        remove(list);
        return uriList;
    }

    public void remove(List<String> uriList) {
        for (int i = 0; i < getRules().size(); ) {
            Rule r = getRules().get(i);
            if (r.getQuery().getURI()!=null && match(r.getQuery().getURI(), uriList)) {
                getRules().remove(i);
            }  
            else {
                i++;
            }
        }
    }
    
    boolean match(String name, List<String> uriList) {
        for (String uri : uriList) {
            if (name.startsWith(uri)) {
                return true;
            }
        }
        return false;
    }

    public ResultWatcher getResultListener() {
        return rw;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public Query defRule(String name, String rule) throws EngineException {
        return defRule(name, rule, Rule.RULE_TYPE);
    }
    
    public Query defRule(String name, String rule, String type) throws EngineException {
        if (type == null) {
            type = Rule.RULE_TYPE;
        }
        if (isTransformation()) {
            if (getQueryEngine() == null) {
                setQueryEngine(QueryEngine.create(getGraphStore()));
            }
            getQueryEngine().setLevel(getLevel());
            return getQueryEngine().defQuery(rule);
        } else {
            // compile time access level
            getCreateDataset().setLevel(getLevel());
            Query qq = exec.compileRule(rule, getDataset());
            if (qq != null) {
                cleanContext(qq);
                if (name == null) {
                    name = getRuleID();
                }
                Rule r = Rule.create(name, qq, type);
                defRule(r);
                return qq;
            }
            return null;
        }
    }
    
    /**
     * Remove compile time context
     * Use case: server may have runtime Context
     */
    void cleanContext(Query q) {
        q.setContext(null);
        q.getAST().setContext(null);
    }
    
    Dataset getCreateDataset() {
        if (getDataset() == null) {
            setDataset(Dataset.create());
        }
        return getDataset();
    }
    
    void declare(Rule r) {
        Query q = r.getQuery();
        q.setID(getRules().size());
        r.setIndex(getRules().size());
        // Provenance Node set to entailed triples
        Node prov = DatatypeMap.createObject(q.getAST().toString(), q, IDatatype.RULE);
        q.setProvenance(prov);
        r.setProvenance(prov);
    }


    /**
     * Process rule base at saturation PRAGMA: not synchronized on write lock
     */
    public int entail(Mapping m, Binding b) throws EngineException {
        begin();
        int start = getGraphManager().size();
        try {
            infer(m, b);
            if (trace){
                //traceSize();
            }
            return getGraphManager().size() - start;
        }
        catch (OutOfMemoryError e){
            throw new EngineException(e);
        }
        finally {
            end();
            clean();
        }
    }
    
    // take a picture of graph Index, store it in graph kg:re1
    void begin(){
        processProfile();
        if (isRecord()) {
            getGraphStore().getContext().storeIndex(NSManager.KGRAM+"re1");
        }
        context();
    }
    
    void context(){
        if (getContext() != null){
            for  (Rule r : getRules()){
                r.getQuery().setContext(getContext());
            }
        }
    }
    
    /**
     * take a picture of graph Index, store it in graph kg:re2
     * store this engine in graph context, 
     * get rule base SPIN graph using: graph kg:engine {} 
     */
    void end(){
        getGraphStore().getContext().setRuleEngine(this);
        if (isRecord()) {
            getGraphStore().getContext().storeIndex(NSManager.KGRAM+"re2");
        }
    }
    
    
    void traceSize() {
        // Get current size of heap in bytes
        long heapSize = Runtime.getRuntime().totalMemory();
        System.out.println("size: " + heapSize / 1000000);

        // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
        // Any attempt will result in an OutOfMemoryException.
        long heapMaxSize = Runtime.getRuntime().maxMemory();
        System.out.println("max size: " + heapMaxSize / 1000000);

        // Get amount of free memory within the heap in bytes. This size will increase
        // after garbage collection and decrease as new objects are created.
        long heapFreeSize = Runtime.getRuntime().freeMemory();
        System.out.println("free size: " + heapFreeSize / 1000000);
    }
    
    
     
    void infer(Mapping m, Binding b) throws EngineException{
        int size = getGraphManager().size(),
                start = size;
        loop = 0;
        int skip = 0, nbrule = 0, loopIndex = 0, tskip = 0, trun = 0, tnbres = 0;
        boolean go = true;

        // Entailment 
        getGraphStore().getEventManager().start(Event.RuleEngine);

        Record nt = null;
        stable = new STable();

        if (isOptimize) {
            // consider only rules that match newly entailed edge predicates
            // consider solutions that contain at least one newly entailed edge           
            start();
            initOptimize();
        }

        while (go) {
            getEventManager().start(Event.InferenceCycle);
            if (isEvent()) getVisitor().loopEntailment(getPath());
            skip = 0;
            nbrule = 0;
            tnbres = 0;
            if (trace) {
                System.out.println("Loop: " + loop);
            }

          
            if (isOptimize){
                rw.start(loop);
                rw.setTrace(trace);
            }
            
            for (Rule rule : getRules()) {
               if (isDebug()) {
                    rule.getQuery().setDebug(true);
                }

                int nbres = 0;

                if (isOptimize) {
                    // start exec ResultWatcher, it checks that each solution 
                    // of rule contains at least one new edge 
                    rw.start(rule);

                    if (loop == 0) {
                        // run all rules, add all solutions
                        nt = record(rule, loopIndex, loop);
                        nbres = process(rule, m, b, nt, loop, loopIndex,  nbrule);
                        if (isClosure(rule)){
                            // rule run at saturation: record nb edge after saturation
                            nt = record(rule, loopIndex+1, loop);
                        }
                        setRecord(rule, nt);
                        tnbres += nbres;
                        nbrule++;
                        loopIndex++;
                    } else {
                        // run rules for which new edges have been created
                        // since previous run
                        int save = getGraphManager().size();
                        nt = record(rule, loopIndex, loop);
                        Record ot = rule.getRecord();
                        
                        if (nt.accept(ot)) {
                            
                            if (trace){
                                ot.trace(nt);
                            }
                            
                            rw.start(ot, nt);
                            nbres = process(rule, m, b, nt, loop, loopIndex,  nbrule);
                            if (isClosure(rule)){
                                // rule run at saturation: record nb edge after execution
                                nt = record(rule, loopIndex+1, loop);
                            }
                            setRecord(rule, nt);
                            tnbres += nbres;
                            nbrule++;
                            loopIndex++;
                        } else {
                            skip++;
                        }

                    }

                    rw.finish(rule);
                } else {
                    nbres = process(rule, m, b, null, loop, -1,  nbrule);
                    nbrule++;
                }

                if (trace) {
                    stable.record(rule, nbres);
                }               
            }



            if (trace) {
                System.out.println("NBrule: " + nbrule);
                System.out.println("Graph: " + getGraphManager().size());
            }

            if (isDebug()) {
                System.out.println("Skip: " + skip);
                System.out.println("Run: " + nbrule);
                System.out.println("Graph: " + getGraphManager().size());
                tskip += skip;
                trun += nbrule;
            }

            if (graph.size() > size) {
                // There are new edges: entailment again
                size = getGraphManager().size();
                loop++;
            } 
            else {
                go = false;
            } 
            
            getEventManager().finish(Event.InferenceCycle);
        }        
        
        if (isDebug()) {
            System.out.println("Total Skip: " + tskip);
            System.out.println("Total Run: " + trun);     
            logger.debug("** Rule: " + (graph.size() - start));
        }               
    }
    
    void initOptimize() {
        // kgram return solutions that contain newly entailed edge
        rw = new ResultWatcher(graph);
        rw.setSkipPath(isSkipPath);
        if (isConstructResult) {
            // Construct will take care of duplicates
            rw.setDistinct(false);
        }
        // kgram interact with result watcher
        exec.addResultListener(rw);
    }
    
    
    
    /**
     * r is transitive closure
     * OR
     * r  = ?x rdf:type ?c2 :- ?x rdf:type ?c1 & ?c1 rdfs:subClassOf ?c2
     * pr = ?c1 rdfs:subClassOf ?c3 := ?c1 rdfs:subClassOf ?c2 & ?c2 rdfs:subClassOf ?c3
     * r is considered as closure (because previous pr is closure)
     */
    boolean isClosure(Rule r){
        if (r.isClosure()){
            // transitive rule at saturation
            return true;
        }
        if (r.isPseudoTransitive()){
            // r = rdf:type ? after pr = rdfs:subClassOf ?
            if (r.getIndex() > 0){
                Rule pr = getRules().get(r.getIndex() - 1);
                if (pr.isClosure()){
                    // rdfs:subClassOf ?
                    return r.isPseudoTransitive(pr);
                }
            }
        }
        return false;
    }
    
    void cleanRules(){
        for (Rule r : getRules()){
            r.clean();
        }
    }

    public void trace() {
        for (Rule r : stable.sort()) {
            System.out.println(stable.get(r) + " " + r.getQuery().getAST());
        }
    }

    /**
     * Clean index of edges that are stored when isOptim=true
     */
    public void clean() {
       getGraphStore().clean();
       getGraphStore().compact();
       cleanRules();
    }

    /**
     * Process one rule 
     */
    int process(Rule rule, Mapping m, Binding b, Record nt,  int loop, int loopIndex,  int nbr) throws EngineException {
        
        if (trace){
           System.out.println(loop + " : " + nbr + " : " + rule.getIndex() + " " + ((rw!=null)?rw.isNew():""));
           System.out.println(rule.getAST());
        }
        getEventManager().start(Event.Rule);
        
        Date d1 = new Date();
        boolean isConstruct = isOptimize && isConstructResult;

        Query qq = rule.getQuery();  
        GraphManager mgr = getGraphManager().getGraphManager(rule.isConstraint());
        Construct cons = Construct.createRule(qq, mgr);
        // named graph to store inference rule entailment OR constraint rule error
        cons.setDefaultGraph(mgr.getRuleGraphName(rule.isConstraint()));
        cons.setAccessRight(b.getAccessRight());        
        cons.setRule(rule, rule.getIndex(), rule.getProvenance());
        cons.setLoopIndex(loopIndex);
        cons.setDebug(isDebug());
        if (isEvent()) cons.setVisitor(getVisitor());

        if (isConstruct) {
            // TODO AR
            // kgram Result Listener create edges in list
            // after query completes, edges are inserted in graph
            // no Mappings are created by kgram
            cons.setBuffer(true);
            cons.setInsertList(new ArrayList<>());
            Mappings map = Mappings.create(qq);
            // ResultWatcher call cons to create edges when a solution occur
            rw.setConstruct(cons);
            rw.setMappings(map);
        }

        int start = getGraphManager().size();
        
        if (isOptTransitive() && isFunTransitive() && rule.isTransitive()){
            // Java code emulate a transitive rule
            Closure clos = getClosure(rule);
            int index = (rule.getRecord() == null) ? -1 : rule.getRecord().getIndex();
            clos.closure(loop, loopIndex, index);
        }
        else {
            process(rule, m, b, cons);
            
            if (graph.size() > start && isConstruct 
                    && isOptTransitive() && rule.isAnyTransitive()){ 
                 // optimization for transitive rules: eval at saturation
                 transitive(rule, m, b, cons);               
            }
        }

        
        Date d2 = new Date();
        if (trace){
            double tt = (d2.getTime() - d1.getTime()) / ( 1000.0) ;
            if (tt > 1){
                System.out.println("Time : " + tt);
                //System.out.println(rule.getAST());
                rule.setTime(tt + rule.getTime());
            }
            System.out.println("New: " + (getGraphManager().size() - start));
            System.out.println("Size: " + getGraphManager().size());

        }
        
        getEventManager().finish(Event.Rule);

        return getGraphManager().size() - start;
    }
    
    /**
     * Transitive Rule is executed at saturation in a loop
       for loops after first one, kgram take new edge list into account
       for evaluating the where part, the first query edge matches new edges only
       Producer will consider list of edges created at preceeding loop.
    */
    void transitive(Rule rule, Mapping m, Binding b, Construct cons) throws EngineException {
        rule.setClosure(true);
        Query qq = rule.getQuery();
        boolean go = true;
        
        while (go) {
            // Producer will take this edge list into account
            qq.setEdgeList(cons.getInsertList());
            qq.setEdgeIndex(rule.getEdgeIndex());
            cons.setInsertList(new ArrayList<>());
            int size = getGraphManager().size();

            process(rule, m, b, cons);

            if (getGraphManager().size() == size) {
                qq.setEdgeList(null);
                go = false;
            }
        }
    }
    
    Closure getClosure(Rule r){
       if (r.getClosure() == null){
          Closure c = new Closure(graph, rw.getDistinct());
          c.setTrace(trace);
          r.setClosure(c);
          c.setQuery(r.getQuery());
          c.setConnect(isConnect());
          c.init(r.getPredicate(0), r.getPredicate(1));
          r.setClosure(true);
       }
       return r.getClosure();
    }
   
    
    // process rule
    void process(Rule r, Mapping m, Binding b, Construct cons) throws EngineException {
        Query qq = r.getQuery();
        if (isEvent()) getVisitor().beforeRule(qq);
        Mappings map = exec.query(qq, m);
        
        if (cons.isBuffer()) {
            // cons insert list contains only new edge that do not exist
            cons.getGraphManager().insert(r.getUniquePredicate(), cons.getInsertList());
        } else {
            // create edges from Mappings as usual
            cons.entailment(map);
        }

        if (r.isConstraint()) {
            // constraint succeed when there is no solution (cf owlrl.rul)
            boolean success = (cons.isBuffer()) ? cons.getInsertList().isEmpty() : map.isEmpty();
            if (! success) {
                logger.error("Constraint error: " + r.getName());
                logger.error((cons.isBuffer()?cons.getInsertList():map).toString());               
                getErrorList().add(error(r, cons, map));
            }
            if (isEvent()) {
                getVisitor().constraintRule(qq, cons.isBuffer() ? cons.getInsertList() : map, DatatypeMap.newInstance(success));
            }
        }
        if (isEvent()) {            
            getVisitor().afterRule(qq, cons.isBuffer() ? cons.getInsertList() : map);
        }
    }
    
    RuleError error(Rule r, Construct cons, Mappings map) {
        if (cons.isBuffer()) {
            return new RuleError(r, cons.getInsertList());
        } else {
            return new RuleError(r, map);
        }
    }
    
    /**
     * **************************************************
     *
     * Compute rule predicates Accept rule if some predicate has new triple in
     * graph
     *
     * *************************************************
     */
    /**
     * Compute table of rule predicates, for all rules
     */
    void start() {
        records = new ArrayList<>();
        sort();
        int i = 0;
        for (Rule r : getRules()) {
            init(r);
            r.setIndex(i);
            r.getQuery().setID(i);
            i++;
        }
        
        getGraphStore().cleanEdge();
    }

  

    /**
     * Store list of predicates of this rule
     */
    void init(Rule rule) {
        rule.set(rule.getQuery().getNodeList());
    }

    /**
     * @return the isConstructResult
     */
    public boolean isConstructResult() {
        return isConstructResult;
    }

    /**
     * @param isConstructResult the isConstructResult to set
     */
    public void setConstructResult(boolean isConstructResult) {
        this.isConstructResult = isConstructResult;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return ds;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.ds = dataset;
    }

    /**
     * @return the isFunTransitive
     */
    public boolean isFunTransitive() {
        return isFunTransitive;
    }

    /**
     * @param isFunTransitive the isFunTransitive to set
     */
    public void setFunTransitive(boolean isFunTransitive) {
        this.isFunTransitive = isFunTransitive;
    }

    /**
     * @return the isConnect
     */
    public boolean isConnect() {
        return isConnect;
    }

    /**
     * @param isConnect the isConnect to set
     */
    public void setConnect(boolean isConnect) {
        this.isConnect = isConnect;
    }

    /**
     * @return the isDuplicate
     */
    public boolean isDuplicate() {
        return isDuplicate;
    }

    /**
     * @param isDuplicate the isDuplicate to set
     */
    public void setDuplicate(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    /**
     * @return the isSkipPath
     */
    public boolean isSkipPath() {
        return isSkipPath;
    }

    /**
     * @param isSkipPath the isSkipPath to set
     */
    public void setSkipPath(boolean isSkipPath) {
        this.isSkipPath = isSkipPath;
    }

    /**
     * @return the isOptTransitive
     */
    public boolean isOptTransitive() {
        return isOptTransitive;
    }

    /**
     * @param isOptTransitive the isOptTransitive to set
     */
    public void setOptTransitive(boolean isOptTransitive) {
        this.isOptTransitive = isOptTransitive;
    }

    /**
     * @return the test
     */
    public boolean isTest() {
        return test;
    }

    /**
     * @param test the test to set
     */
    public void setTest(boolean test) {
        this.test = test;
    }

    @Override
    public String toGraph() {
        return toRDF();
    }
      
    /**
     * Return the rule base as a SPIN graph
     * graph eng:engine {}
     */
    public String toRDF() {    
        SPIN sp = SPIN.create();
        for (Rule r : getRules()){   
            sp.init();
            ASTQuery ast = (ASTQuery) r.getAST();
            sp.visit(ast, "kg:r" + r.getIndex()); 
            sp.nl();
        }
        return sp.toString();
    }
    
     /**
     *  graph eng:record {}
     */
    public Graphable getRecord(){
        final RuleEngine re = this;
        return new Graphable(){

            @Override
            public String toGraph() {
                return re.toRDFRecord();
            }

            @Override
            public void setGraph(Object obj) {
            }

            @Override
            public Object getGraph() {
                return null;            
            }
            
        };
    }
    
    public String toRDFRecord() { 
        String str = "";
        for (Record r : records){
            str += r.toRDF();
        }
        return str;
    }

    @Override
    public void setGraph(Object obj) {
        spinGraph = obj;
    }

    @Override
    public Object getGraph() {
        return spinGraph;
    }

    /**
     * @return the context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * @return the qengine
     */
    public QueryEngine getQueryEngine() {
        return qengine;
    }

    /**
     * @param qengine the qengine to set
     */
    public void setQueryEngine(QueryEngine qengine) {
        this.qengine = qengine;
    }

    /**
     * @return the isTransformation
     */
    public boolean isTransformation() {
        return qengine != null && qengine.isTransformation();
    }

    /**
     * @return the base
     */
    public String getBase() {
        return base;
    }

    /**
     * @param base the base to set
     */
    public void setBase(String base) {
        this.base = base;
    }

    class STable extends Hashtable<Rule, Integer> {

        void record(Rule r, int n) {
            Integer i = get(r);
            if (i == null) {
                i = 0;
            }
            put(r, i + n);
        }

        List<Rule> sort() {
            ArrayList<Rule> list = new ArrayList<Rule>();

            for (Rule r : keySet()) {
                list.add(r);
            }

            Collections.sort(list,
                    new Comparator<Rule>() {
                @Override
                public int compare(Rule o1, Rule o2) {
                    return get(o2).compareTo(get(o1));
                }
            });

            return list;
        }
    }
    
    /**
     * Put pseudo transitive rule after it's transitive rule
     * tr = c1 subclassof c3 :- c1 subclassof c2 & c2 subclassof c3   
     * pr = x type c2        :- x type c1        & c1 subclassof c2
     * when tr runs at saturation (in a loop), one execution of pr just after tr
     * generates all rdf:type triples (at once)
     * then, if no new rdf:type/rdfs:subClassOf occur after pr, 
     * we can skip tr and pr at next loop
     * hence we gain one execution at last loop
     */
    void sort(){
        for (int i = 0; i < getRules().size(); i++){
            Rule tr = getRules().get(i);
            if (tr.isTransitive()){
                for (int j = 0; j < getRules().size(); j++){
                    Rule pr = getRules().get(j);
                    if (pr.isPseudoTransitive(tr)){
                        getRules().remove(tr);
                        getRules().remove(pr);
                        getRules().add(tr);
                        getRules().add(pr);
                        return;
                    }
                }
            }
        }
    }
    
   
    /**
     * Record predicates cardinality in graph
     */
    Record record(Rule r, int n, int l) {
        Record itable = new Record(r, n, l, getGraphManager().size());

        for (Node pred : r.getPredicates()) {
            int size = getGraphManager().size(pred);
            itable.put(pred, size);
        }

        return itable;
    }    

    
    public List<Record> getRecords(){
        return records;
    }

    void setRecord(Rule r, Record t) {
        r.setRecord(t);
        records.add(t);
    }
    
    @Override
    public void init() {
    }

    @Override
    public void onDelete() {
    }

    @Override
    public void onInsert(Node gNode, Edge edge) {
    }

    @Override
    public void onClear() {
    }

    @Override
    public void setActivate(boolean b) {
        isActivate = b;
    }

    @Override
    public boolean isActivate() {
        return isActivate;
    }

    @Override
    public void remove() {
        getGraphStore().clear(Entailment.RULE, true);
        getGraphStore().clean();
    }

    @Override
    public int type() {
        return RULE_ENGINE;
    }

    /**
     * @return the synchronize
     */
    public boolean isSynchronized() {
        return synchronize;
    }

    /**
     * @param synchronize the synchronize to set
     */
    public void setSynchronized(boolean synchronize) {
        this.synchronize = synchronize;
        if (getQueryProcess() != null) {
            getQueryProcess().setSynchronized(synchronize);
        }
    }

    /**
     * @return the visitor
     */
    public ProcessVisitor getVisitor() {
        return visitor;
    }

    /**
     * @param visitor the visitor to set
     */
    public void setVisitor(ProcessVisitor visitor) {
        this.visitor = visitor;
    }

    /**
     * @param rules the rules to set
     */
    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
    
    public Level getLevel() {
        return level;
    }
    
    public void setLevel(Level level) {
        this.level = level;
    }

    /**
     * @return the event
     */
    public boolean isEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(boolean event) {
        this.event = event;
    }
    
    /**
     * @return the accessRight
     */
    public AccessRight getAccessRight() {
        return accessRight;
    }

    /**
     * @param accessRight the accessRight to set
     */
    public void setAccessRight(AccessRight accessRight) {
        this.accessRight = accessRight;
    }

    public List<RuleError> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<RuleError> errorList) {
        this.errorList = errorList;
    }

    public boolean isDebug() {
        return debug;
    }

    public GraphManager getGraphManager() {
        return graphManager;
    }

    public void setGraphManager(GraphManager graphManager) {
        this.graphManager = graphManager;
    }

    public boolean isOptimizable() {
        return optimizable;
    }

    public void setOptimizable(boolean optimizable) {
        this.optimizable = optimizable;
    }

    public boolean isRecord() {
        return record;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }   

}
