package fr.inria.corese.kgram.core;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Filter;
import fr.inria.corese.kgram.api.core.Loopable;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Evaluator;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Plugin;
import fr.inria.corese.kgram.api.query.ProcessVisitor;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.api.query.Provider;
import fr.inria.corese.kgram.api.query.Results;
import fr.inria.corese.kgram.api.query.SPARQLEngine;
import fr.inria.corese.kgram.event.Event;
import fr.inria.corese.kgram.event.EventImpl;
import fr.inria.corese.kgram.event.EventListener;
import fr.inria.corese.kgram.event.EventManager;
import fr.inria.corese.kgram.event.ResultListener;
import fr.inria.corese.kgram.path.PathFinder;
import fr.inria.corese.kgram.tool.Message;
import fr.inria.corese.kgram.tool.ResultsImpl;

/**
 * KGRAM Knowledge Graph Abstract Machine Compute graph homomorphism and
 * (extended) SPARQL Use: a Stack of expression Exp a Memory for Node/Edge
 * bindings an abstract Producer of candidate Node/Edge an abstract Evaluator of
 * Filter an abstract Matcher of Node/Edge
 *
 * - path statement is an EDGE with a boolean isPath this edge needs an Edge
 * Node (a property variable)
 *
 *
 * TODO: optimize: query ordering, search by dichotomy (in a cache)
 *
 * @author Olivier Corby, Edelweiss, INRIA 2010
 *
 */
public class Eval implements ExpType, Plugin {

    // true = new processing of named graph 
    public static boolean NAMED_GRAPH_DEFAULT = true;
    static Logger logger = LoggerFactory.getLogger(Eval.class);
    // draft test: when edge() has Mappings map parameter, push clause values(map)
    private static boolean pushEdgeMappings = true;
    // draft test: graph() has Mappings map parameter and eval body with map parameter
    private static boolean parameterGraphMappings = true;
    // draft test: union() has Mappings map parameter and eval branch with map parameter
    private static boolean parameterUnionMappings = true;
    
    static final int STOP = -2;
    public static int count = 0;
    ResultListener listener;
    EventManager manager;
    private ProcessVisitor visitor;
    private SPARQLEngine sparqlEngine;
    boolean hasEvent = false;
    boolean namedGraph = NAMED_GRAPH_DEFAULT;
    // Edge and Node producer
    Producer producer, saveProducer;
    Provider provider;
    // Filter evaluator
    Evaluator evaluator;
    Matcher match;
    List<PathFinder> lPathFinder;
    // Processing EXTERN expressions
    Plugin plugin;
    // Stacks for binding edges and nodes
    Memory memory;
    private Stack current;
    Query query;
    Exp maxExp;
    Node nn;
    Exp edgeToDiffer;
    Mapping mapping;
    Mappings results,
            // initial results to be completed
            initialResults;
    EvalSPARQL evalSparql;
    CompleteSPARQL completeSparql;
    List<Node> empty = new ArrayList<>(0);
    //HashMap<String, Boolean> local;
    
    EvalGraph evalGraphNew;
    EvalJoin join;
    EvalOptional optional;
    
    static {
        setNewMappingsVersion(true);
    }

    int // count number of eval() calls
            nbEdge = 0, nbCall = 0,
            rcount = 0,
            backjump = -1, indexToDiffer = -1,
            // max level in stack for debug
            level = -1,
            maxLevel = -1,
            limit = Integer.MAX_VALUE;
    private boolean debug = false;
    boolean isSubEval = false,
            // return only select variables in Mapping
            onlySelect = true,
            optim = true,
            draft = true;
    private boolean hasListener = false;
    private boolean isPathType = false;
    boolean storeResult = true;
    private int nbResult;
    boolean hasFilter = false;
    private boolean hasCandidate = false,
            hasStatement = false,
            hasProduce = false;
    private boolean stop = false;
    
    public Eval() {
    }

    /**
     *
     * @param p edge and node producer
     * @param e filter evaluator, given an environment (access to variable
     * binding)
     */
    public Eval(Producer p, Evaluator e, Matcher m) {
        producer = p;
        saveProducer = producer;
        evaluator = e;
        match = m;
        plugin = this;
        lPathFinder = new ArrayList<>();
        setVisitor(new ProcessVisitorDefault());
        e.setKGRAM(this);
        evalGraphNew = new EvalGraph(this);
        join = new EvalJoin(this);
        optional = new EvalOptional(this);

    }

    public static Eval create(Producer p, Evaluator e, Matcher m) {
        return new Eval(p, e, m);
    }

    public void set(Provider p) {
        provider = p;
    }

    public void set(Producer p) {
        producer = p;
    }

    public void set(Matcher m) {
        match = m;
    }

    public void set(Evaluator e) {
        evaluator = e;
    }

    public Results exec(Query q) throws SparqlException {
        Mappings maps = query(q, null);
        return ResultsImpl.create(maps);
    }

    /**
     * Eval KGRAM query and subquery For subquery, this eval is a copy which
     * shares the memory with outer eval
     */
    public Mappings query(Query q) throws SparqlException {
        return query(null, q, (Mapping) null);
    }
    
    public Mappings query(Query q, Mapping m) throws SparqlException {
        return query(null, q, m);
    }
    
    public Mappings query(Node gNode, Query q, Mapping m) throws SparqlException {
        return queryBasic(gNode, q, m);
    }
    
    Mappings queryBasic(Node gNode, Query q, Mapping m) throws SparqlException {
        if (hasEvent) {
            send(Event.BEGIN, q);
        }
        initMemory(q);
        share(m);
        producer.start(q);
        getVisitor().init(q);
        share(getVisitor());
        getVisitor().before(q);
        Mappings map = eval(gNode, q, m);
        getVisitor().orderby(map);
        getVisitor().after(map);

        producer.finish(q);
        if (hasEvent) {
            send(Event.END, q, map);
        }
        map.setBinding(memory.getBind());
        clean();
        return map;
    }

    // share global variables and ProcessVisitor
    void share(Mapping m) {
        if (m != null && m.getBind() != null) {
            if (memory.getBind() != null) {
                memory.getBind().share(m.getBind());
            }
            if (m.getBind().getVisitor() != null) {
                // use case: let (?g = construct where)
                // see Interpreter exist() getMapping()
                setVisitor(m.getBind().getVisitor());
            }
        }
    }

    // store ProcessVisitor into Bind for future sharing by
    // Transformer and Interpreter exist
    void share(ProcessVisitor vis) {
        if (vis.isShareable() && getMemory().getBind().getVisitor() == null) {
            getMemory().getBind().setVisitor(vis);
        }
    }

    public void finish(Query q, Mappings map) {
    }

    public Mappings eval(Node gNode, Query q, Mapping map) throws SparqlException {
        return eval(gNode, q, map, null);
    }
    
    /**
     * Mapping m is binding parameter, possibly null
     * a) from template call with parameter:   st:call-template(st:name, ?x, ?y)
     * b) from query exec call with parameter: exec.query(q, m)
     * Mappings map is results or previous statement, possibly null
     * use case: optional(A, B) map = relevant subset of results of A
     */
    Mappings eval(Node gNode, Query q, Mapping m, Mappings map) throws SparqlException {
        init(q);
        if (q.isValidate()) {
            // just compile and complete query
            return results;
        }
        if (q.isCheck()) {
            // Draft
            Checker check = Checker.create(this);
            check.check(q);
        }
       
        if (!q.isFail()) {           
            queryWE(gNode, q, m, map);

            if (q.getQueryProfile() == Query.COUNT_PROFILE) {
                countProfile();
            } else {
                if (q.isAlgebra()) {
                    memory.setResults(results);
                    completeSparql.complete(producer, results);
                }
                aggregate();
                // order by
                complete();
                template();
            }
        }

        if (isDebug() && !isSubEval && !q.isSubQuery()) {
            debug();
        }
        evaluator.finish(memory);
        return results;
    }
    
    int queryWE(Node gNode, Query q, Mapping m, Mappings map) throws SparqlException {
        try {
            return query(gNode, q, m, map);
        } catch (SparqlException ex) {
            if (ex.isStop()) {
                // LDScriptException stop means stop query processing
                return 0;
            }
            // exception means this is an error
            throw ex;
        }
    }

    /**
     * Mapping m is binding parameter, possibly null
     * a) from template call with parameter:   st:call-template(st:name, ?x, ?y)
     * b) from query exec call with parameter: exec.query(q, m)
     * Mappings map is results or previous statement, possibly null
     * use case: optional(A, B) map = relevant subset of results of A
     */
    int query(Node gNode, Query q, Mapping m, Mappings map) throws SparqlException {
        if (m != null) {
            // bind mapping variables into memory
            bind(m);
        }
        if (q.getValues() == null) {
            // no external values
            return eval(gNode, q, map);
        } 
        // external values clause
        // select * where {} values var {}
//        else if (map == null && m == null) {
//            // there is no binding parameter
//            // external values evaluated as join(values, body)
//            return queryWithJoinValues(gNode, q, map);
//        } 
        else {
            // there is binding parameter (m and/or map)
            // Mapping m is bound in memory, keep it, mappings map is passed as eval parameter 
            // bind external values one by one in memory and eval one by one
            return queryWithValues(gNode, q, map);
        }
    }

    /**
     * External values clause evaluated as join(values, body)
     */
    int queryWithJoinValues(Node gNode, Query q, Mappings map)
            throws SparqlException {
        Exp values = Exp.create(AND, q.getValues());
        return evalExp(gNode, q, Exp.create(JOIN, values, q.getBody()), map);
    }
    
    /**
     * 
     * Bind external values one by one in memory and eval one by one
     */
    int queryWithValues(Node gNode, Query q, Mappings map)
            throws SparqlException {
        Exp values = q.getValues();

        if (!values.isPostpone() && !q.isAlgebra()) {
            for (Mapping m : values.getMappings()) {
                if (stop) {
                    return STOP;
                }
                if (binding(values.getNodeList(), m)) {
                    eval(gNode, q, map);
                    free(values.getNodeList(), m);
                }
            }
            return 0;
        }
        return eval(gNode, q, map);
    }

    int eval(Node gNode, Query q, Mappings map) throws SparqlException {
        if (q.isFunctional()) {
            // select xpath() as ?val
            // select unnest(fun()) as ?x
            function();
            return 0;
        } else {
            return evalExp(gNode, q, q.getBody(), map);
        }
    }
    
    int evalExp(Node gNode, Query q, Exp exp, Mappings map)
            throws SparqlException {
        Stack stack = Stack.create(exp);
        set(stack);
        return eval(gNode, stack, map, 0);
    }
    
    /**
     * We just counted number of results: nbResult Just build a Mapping
     */
    void countProfile() {
        Node n = evaluator.cast(nbResult, memory, producer);
        Mapping m = Mapping.create(query.getSelectFun().get(0).getNode(), n);
        results.add(m);
    }    
    
    public Mappings filter(Mappings map, Query q) throws SparqlException {
        Query qq = map.getQuery();
        init(qq);
        qq.compile(q.getHaving().getFilter());
        qq.index(qq, q.getHaving().getFilter());
        map.filter(evaluator, q.getHaving().getFilter(), memory);
        return map;
    }    

    /**
     * SPARQL algebra requires kgram to compute BGP exp and return Mappings
     * List<Node> from = query.getFrom(gNode); Mappings map =
     * p.getMappings(gNode, from, exp, memory);
     */
    Mappings exec(Node gNode, Producer p, Exp exp, Mapping m) throws SparqlException {
        if (true) {
            List<Node> from = query.getFrom(gNode);
            Mappings map = p.getMappings(gNode, from, exp, memory);
            return map;
        }
        Stack stack = Stack.create(exp);
        set(stack);
        if (m != null) {
            process(exp, m);
        }
        eval(p, gNode, stack, 0);
        Mappings map = Mappings.create(query);
        map.add(results);
        memory.start();
        results.clear();
        return map;
    }

    void process(Exp exp, Mapping m) {
        if (exp.getNodeList() != null) {
            for (Node qnode : exp.getNodeList()) {
                Node node = m.getNodeValue(qnode);
                if (node != null) {
                    memory.push(qnode, node, -1);
                }
            }
        } else {
            memory.push(m, 0);
        }
    }

    /**
     * Evaluate exp with SPARQL Algebra on Mappings, not with Memory stack
     *
     *
     */
    void process(Node gNode, Producer p, Exp exp) {
        results = evalSparql.eval(gNode, p, exp);
    }

    /**
     * Subquery processed by a function call that return Mappings Producer may
     * cast the result into Mappings use case: {select xpath(?x, '/book/title')
     * as ?val where {}} Mappings may be completed by filter (e.g. for casting)
     * Mappings will be processed later by aggregates and order by/limit etc.
     */
    private void function() throws SparqlException {
        Exp exp = query.getFunction();
        if (exp == null) {
            return;
        }
        Mappings lMap = evaluator.eval(exp.getFilter(), memory, exp.getNodeList());
        if (lMap != null) {
            for (Mapping map : lMap) {
                map = complete(map, producer);
                submit(map);
            }
        }
    }

    /**
     * additional filter of functional select xpath() as ?val xsd:integer(?val)
     * as ?int
     */
    private Mapping complete(Mapping map, Producer p) throws SparqlException {
        for (Exp ee : query.getSelectFun()) {
            Filter f = ee.getFilter();
            if (f != null && !f.isFunctional()) {
                memory.push(map, -1);
                Node node = eval(f, memory, producer);
                memory.pop(map);
                map.setNode(ee.getNode(), node);
            }
        }

        if (query.getOrderBy().size() > 0 || query.getGroupBy().size() > 0) {
            memory.push(map, -1);
            Mapping m = memory.store(query, p, true, true);
            memory.pop(map);
            map = m;
        }
        return map;
    }
    
    Node eval(Filter f, Environment env, Producer p) throws SparqlException {
        return evaluator.eval(f, memory, producer);
    }    

    /**
     * this eval is a fresh copy
     */   
    public Mappings subEval(Query q, Node gNode, Stack stack, int n) throws SparqlException {
        return subEval(q, gNode, stack, null, n);
    }
    
    Mappings subEval(Query q, Node gNode, Stack stack, Mappings map, int n) throws SparqlException {
        setSubEval(true);
        starter(q);
        if (q.isDebug()) {
            setDebug(true);
        }
        eval(gNode, stack, map, n);

        //memory.setResults(save);
        return results;
    }

    // draft for processing EXTERN expression
    public void add(Plugin p) {
        plugin = p;
    }

    public void setMatcher(Matcher m) {
        match = m;
    }

    public void setMappings(Mappings lMap) {
        initialResults = lMap;
    }

    void debug() {
        Message.log(Message.LOOP, nbCall + " " + nbEdge);
        if (results.size() == 0) {
            if (query.isFail()) {
                Message.log(Message.FAIL);
                System.out.println("eval: " + query);
                for (Filter filter : query.getFailures()) {
                    Message.log(filter + " ");
                }
                Message.log();
            } else {
                if (maxExp == null) {
                    Message.log(Message.FAIL_AT, "init phase, e.g. parameter binding");
                } else {
                    Message.log(Message.FAIL_AT);
                    Message.log(maxExp);
                    getTrace().append(String.format("SPARQL fail at: %s", maxExp)).append(Message.NL);
                }
            }
        }
    }
    
    StringBuilder getTrace() {
        return getMemory().getBind().getTrace();
    }

    /**
     * Eval exp in a fresh new Memory where exp is part of main expression
     * use case: main = optional, minus, union, join
     * Node gNode : actual graph node 
     * Node queryNode : exp query graph node
     */
    public Mappings subEval(Producer p, Node gNode, Node queryNode, Exp exp, Exp main) throws SparqlException {
        return subEval(p, gNode, queryNode, exp, main, null, null, false);
    }

    Mappings subEval(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map) throws SparqlException {
        return subEval(p, gNode, queryNode, exp, main, map, null, false);
    }

    Mappings subEval(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) throws SparqlException {
       return subEvalNew(p, gNode, queryNode, exp, main, map, m, bind, false) ;
    }
    
    /**
     * ext = false : gNode is named graph URI, queryNode is meaningless
     * ext = true :  gNode is external graph, queryNode is named graph variable if any or null
     * node: if ext=true & queryNode=null -> gNode can be null
     * external graph: external named graph in GraphStore or Node graph pointer
     * When external: Producer p is new Producer(externalGraph)
     * 
     */
    Mappings subEvalNew(Producer p, Node gNode, Node queryNode, Exp exp, Exp main, Mappings map, Mapping m, boolean bind, boolean external) throws SparqlException {    
        Memory mem = new Memory(match, getEvaluator());
        getEvaluator().init(mem);
        mem.share(memory);
        mem.init(query);
        mem.setAppxSearchEnv(this.memory.getAppxSearchEnv());
        Eval eval = copy(mem, p);
        if (external) {
            if (queryNode != null) {
                mem.push(queryNode, gNode, -1);
            }
            gNode = null;
        }
        bind(mem, exp, main, map, m, bind);
        Mappings lMap = eval.subEval(query, gNode, Stack.create(exp), map, 0);
        return lMap;
    }        

    /**
     *
     * Copy current evaluator to eval subquery same memory (share bindings) new
     * exp stack
     */
    Eval copy(Memory m, Producer p) {
        return copy(m, p, getEvaluator(), query, false);
    }

    // extern = true if the statement to evaluate is LDScript query:
    // let (select where)
    public Eval copy(Memory m, Producer p, boolean extern) {
        return copy(m, p, getEvaluator(), query, extern);
    }

    // q may be the subQuery
    Eval copy(Memory m, Producer p, Evaluator e, Query q, boolean extern) {
        Eval ev = create(p, e, match);
        if (q != null) {
            ev.complete(q);
        }
        ev.setSPARQLEngine(getSPARQLEngine());
        ev.setMemory(m);
        ev.set(provider);
        if (!extern || getVisitor().isShareable()) {
            ev.setVisitor(getVisitor());
        }
        ev.startExtFun(q);
        ev.setPathType(isPathType);
        if (hasEvent) {
            ev.setEventManager(manager);
        }
        return ev;
    }
    
    public Memory createMemory(Environment env, Exp exp) {
        if (env instanceof Memory) {
            return getMemory((Memory) env, exp);
        } else if (env instanceof Mapping) {
            return getMemory((Mapping) env, exp);
        } else {
            return null;
        }
    }

    /**
     * copy of Memory may be stored in exp. Reuse data structure after cleaning
     * and init copy current memory content into target memory Use case: exists
     * {}
     */
    public Memory getMemory(Memory memory, Exp exp) {
        Memory mem;
        if (memory.isFake()) {
            // Temporary memory created by PathFinder
            mem = memory;
        } else if (!memory.hasBind() && exp.getObject() != null) {
            mem = (Memory) exp.getObject();
            mem.start();
            memory.copyInto(null, mem, exp);
        } else {
            mem = copyMemory(memory, memory.getQuery(), null, exp);
            exp.setObject(mem);
        }
        return mem;
    }

    /**
     * use case: exists {} in aggregate select (count(if (exists { BGP }, ?x,
     * ?y)) as ?c) Env is a Mapping Copy Mapping into fresh Memory in order to
     * evaluate exists {} in Memory TODO: optimize by storing mem
     *
     *
     */
    public Memory getMemory(Mapping map, Exp exp) {
        Memory mem = new Memory(match, evaluator);
        getEvaluator().init(mem);
        mem.init(query);
        mem.copy(map, exp);
        return mem;
    }

    /**
     * copy memory for sub query copy sub query select variables that are
     * already bound in current memory Use case: subquery and exists
     */
    private Memory copyMemory(Memory memory, Query query, Query sub, Exp exp) {
        Memory mem = new Memory(match, evaluator);
        getEvaluator().init(mem);
        if (sub == null) {
            mem.init(query);
        } else {
            mem.init(sub);
        }
        memory.copyInto(sub, mem, exp);
        if (hasEvent) {
            memory.setEventManager(manager);
        }
        return mem;
    }

    void setLevel(int n) {
        level = n;
    }

    public void setDebug(boolean b) {
        debug = b;
    }

    public void setSubEval(boolean b) {
        isSubEval = b;
    }

    public Memory getMemory() {
        return memory;
    }
    
    Query getQuery() {
        return query;
    }

    public Evaluator getEvaluator() {
        return evaluator;
    }

    public Matcher getMatcher() {
        return match;
    }

    public Producer getProducer() {
        return producer;
    }

    public Provider getProvider() {
        return provider;
    }

    public Environment getEnvironment() {
        return memory;
    }

    public Mappings getResults() {
        return results;
    }

    void setResult(Mappings r) {
        results = r;
    }

    public void setMemory(Memory mem) {
        memory = mem;
    }

    // total init (for global query)
    public void init(Query q) {
        initMemory(q);
        start(q);
        profile(q);
    }

    void initMemory(Query q) {
        if (memory == null) {
            // when subquery, memory is already assigned
            // assign stack index to EDGE and NODE
            q.complete(producer);//service while1 / Query
            memory = new Memory(match, evaluator);
            memory.setEval(this);
            getEvaluator().init(memory);
            // create memory bind stack
            memory.init(q);
            if (hasEvent) {
                memory.setEventManager(manager);
            }
            producer.init(q);
            evaluator.start(memory);
            setDebug(q.isDebug());
            if (q.isAlgebra()) {
                complete(q);
            }
            if (isDebug()) {
                System.out.println(q);
            }
        }
    }

    void complete(Query q) {
        evalSparql = new EvalSPARQL(q, this);
        completeSparql = new CompleteSPARQL(q, this);
    }

    void profile(Query q) {
        switch (q.getQueryProfile()) {

            // select (count(*) as ?c) where {}
            // do not built Mapping, just count them
            case Query.COUNT_PROFILE:
                storeResult = false;
        }
    }

    // partial init (for global query and subqueries)
    private void start(Query q) {
        limit = q.getLimitOffset();
        starter(q);
    }

    public void setLimit(int n) {
        limit = n;
    }

    // for sub exp
    private void starter(Query q) {
        query = q;
        // create result holder
        if (initialResults != null) {
            results = initialResults;
        } else {
            results = Mappings.create(query, isSubEval);
        }
        if (hasEvent) {
            results.setEventManager(manager);
        }
        startExtFun(q);
        // set new results in case of sub query (for aggregates)
        memory.setEval(this);
        memory.setResults(results);
    }

    void startExtFun(Query q) {
        hasStatement = getVisitor().statement();
        hasProduce = getVisitor().produce();
        hasCandidate = getVisitor().candidate();
        hasFilter = getVisitor().filter();
    }

    private void complete() {
        results.complete(this);
    }

    private void aggregate() throws SparqlException {
        results.aggregate(evaluator, memory, producer);
    }

    private void template() throws SparqlException {
        results.template(evaluator, memory, producer);
    }

    /**
     * We can bind nodes before processing query
     */
    boolean bind(Node qnode, Node node) {
        return memory.push(qnode, node, -1);
    }

    /**
     * Bind select nodes of Mapping to [select] nodes of query
     */
    void bind(Mapping map) {
        for (Node qNode : map.getSelectQueryNodes()) {
            Node qqNode = query.getOuterNode(qNode);
            if (qqNode != null) {
                Node node = map.getNode(qNode);
                if (node != null) {
                    bind(qqNode, node);
                    if (isDebug()) {
                        logger.debug("Bind: " + qqNode + " = " + node);
                    }
                }
            }
        }
    }

    private PathFinder getPathFinder(Exp exp, Producer p) {
        List<PathFinder> lp = lPathFinder;
        for (PathFinder pf : lp) {
            if (pf.getEdge() == exp.getEdge()) {
                return pf;
            }
        }
        PathFinder pathFinder = PathFinder.create(this, p, query);
        //pathFinder.setDefaultBreadth(false);
        if (hasEvent) {
            pathFinder.set(manager);
        }
        pathFinder.set(listener);
        pathFinder.setList(query.getGlobalQuery().isListPath());
        // rdf:type/rdfs:subClassOf* generated system path does not store the list of edges
        // to be optimized
        pathFinder.setStorePath(query.getGlobalQuery().isStorePath() && !exp.isSystem());
        pathFinder.setCache(query.getGlobalQuery().isCachePath());
        // TODO: subQuery 
        pathFinder.setCheckLoop(query.isCheckLoop());
        pathFinder.setCountPath(query.isCountPath());
        pathFinder.init(exp.getRegex(), exp.getObject(), exp.getMin(), exp.getMax());
        // TODO: check this with clean()
        if (p.getMode() == Producer.EXTENSION && p.getQuery() == memory.getQuery()) {
            // do nothing
        } else {
            lPathFinder.add(pathFinder);
        }
        return pathFinder;
    }

    /**
     * What should be done before throw LimitException - close path threads if
     * any
     */
    private void clean() {
        for (PathFinder pf : lPathFinder) {
            pf.stop();
        }
    }

    private int solution(Producer p, Mapping m, int n) throws SparqlException {
        int backtrack = n - 1;
        int status = store(p, m);
        if (status == STOP) {
            return STOP;
        }
        if (results.size() >= limit) {
            clean();
            // backjump to send finish events to listener
            // and perform 'close instructions' if any
            return STOP;
        }
        if (!getVisitor().limit(results)) {
            clean();
            return STOP;
        }
        if (backjump != -1) {
            if (!isSubEval && optim) {
                // use case: select distinct ?x where
                // backjump where ?x is defined to get a new one
                backtrack = backjump;
                backjump = -1;
            }
        } else if (query.isDistinct()) {
            if (!isSubEval && optim) {
                int index = memory.getIndex(query.getSelect());
                if (index != -1) {
                    backtrack = index;
                }
            }
        }
        return backtrack;
    }

    /**
     * Eval a stack of KGRAM expressions
     *
     * Manage backjump, i.e. backtrack at lever less than n-1 needed for NOT in
     * order to backtrack at once before the not
     *
     *
     *
     */
    private int eval(Node gNode, Stack stack, int n) throws SparqlException {
        return eval(producer, gNode, stack, null, n);
    }
    
    private int eval(Node gNode, Stack stack, Mappings map, int n) throws SparqlException {
        return eval(producer, gNode, stack, map, n);
    }

    /**
     * gNode is the query graph name if any, may be null
     */
    int eval(Producer p, Node gNode, Stack stack, int n) throws SparqlException {
        return eval(p, gNode, stack, null, n);
    }
   
    /**
     * Mappings map, possibly null, is result of previous expression that may be used to evaluate current exp
     * optional(s p o, o q r)
     * map = eval(s p o); eval(o q r, map) -> can use o in map
     * map is relevant subset of result, projected on relevant subset of variables of right expression
     * special cases:
     * 1) when there is no relevant subset of results wrt variables, map=full result in case exp is a union
     * 2) relevant subset of result contains full result in case exp is a union
     * case union: each branch of union select its own relevant subset of results from full result
     * Mappings map is recursively passed as parameter until one exp can use it
     * It can be passed recursively through several statements: join(A, optional(union(B, C), D))
     * Eventually, and() edge() path() transform Mappings map into values clause
     */
    int eval(Producer p, Node gNode, Stack stack, Mappings map, int n) throws SparqlException {
        int backtrack = n - 1;
        boolean isEvent = hasEvent;
        Memory env = memory;

        nbCall++;

        if (n >= stack.size()) {
            backtrack = solution(p, null, n);
            return backtrack;
        }

        if (isDebug()) {

            if (n > level
                    || (maxExp.type() == UNION)) {
                Exp ee = stack.get(n);
                if (true){//(ee.type() != AND) {
                    level = n;
                    maxExp = stack.get(n);
                    String s = String.format("%02d", n);
                    Message.log(Message.EVAL, s + " " + maxExp);
                    if (map!=null) {
                        logger.warn(String.format("With mappings:\nvalues %s\n%s",
                                map.getNodeList(), map.toString(false, false, 5)));
                    }
                    getTrace().append(String.format("Eval: %02d %s", n, maxExp))
                            .append(Message.NL).append(Message.NL);
                }
            }
        }

        if (n > maxLevel) {
            maxLevel = n;
        }

        Exp exp = stack.get(n);
        if (hasListener) {
            exp = listener.listen(exp, n);
        }
        
        if (isEvent) {
            send(Event.START, exp, gNode, stack);
        }

        if (exp.isFail()) {
            // a false filter was detected at compile time
            // or exp was identified as always failing
            // no use to eval this exp
        } else {

            if (exp.isBGPAble()) {
                // evaluate and record result for next time
                // template optimization 
                exp.setBGPAble(false);
                backtrack = bgpAble(p, gNode, exp, stack, n);
                exp.setBGPAble(true);
            } else {
                // draft test
                if (query.getGlobalQuery().isAlgebra()) {
                    switch (exp.type()) {
                        case BGP:
                        case JOIN:
                        case MINUS:
                        case OPTIONAL:
                        case GRAPH:
                        case UNION:
                            process(gNode, p, exp);
                            return backtrack;
                    }
                };

                if (hasStatement) {
                    getVisitor().statement(this, getGraphNode(gNode), exp);
                }

                switch (exp.type()) {

                    case EMPTY:

                        eval(p, gNode, stack, n + 1);
                        break;

                    case AND:                        
                        backtrack = and(p, gNode, exp, stack, map, n);
                        break;

                    case BGP:
                        backtrack = bgp(p, gNode, exp, stack, n);
                        break;

                    case SERVICE:
                        // @note: map processing is not optimal for service with union
                        // we pass mappings only for variables that are in-scope in 
                        // both branches of the union
                        // it can be bypassed with values var {undef}
                        backtrack = service(p, gNode, exp, map, stack, n);
                        break;

                    case GRAPH:
                        backtrack = 
                                evalGraphNew.eval(p, gNode, exp, map, stack, n);
                        break;

                    case UNION:
                        backtrack = union(p, gNode, exp, map, stack, n);
                        break;
                    
                    case OPTIONAL:
                        backtrack = optional.eval(p, gNode, exp, map, stack, n);
                        break;
                    case MINUS:
                        backtrack = minus(p, gNode, exp, map, stack, n);
                        break;
                    case JOIN:
                        backtrack = join.eval(p, gNode, exp, map, stack, n);
                        break;
                    case QUERY:
                        backtrack = query(p, gNode, exp, map, stack, n); 
                        break;    
                    case FILTER:
                        backtrack = filter(p, gNode, exp, stack, n);
                        break;
                    case BIND:
                        backtrack = bind(p, gNode, exp, map, stack, n);
                        break;

                    case PATH:
                        backtrack = path(p, gNode, exp, map, stack, n);
                        break;

                    case EDGE:
                        if (query.getGlobalQuery().isPathType() && exp.hasPath()) {
                            backtrack = path(p, gNode, exp.getPath(), map, stack, n);
                        } else {
                            backtrack = edge(p, gNode, exp, map, stack, n);
                        }
                        break;

                    case VALUES:

                        backtrack = values(p, gNode, exp, stack, n);

                        break;

                    /**
                     * ********************************
                     *
                     * Draft extensions
                     *
                     */
                    case OPT_BIND:
                        /**
                         * use case: ?x p ?y FILTER ?t = ?y BIND(?t, ?y) ?z q ?t
                         *
                         */                       
                        backtrack = optBind(p, gNode, exp, stack, n);
                        break;

                    case ACCEPT:
                        // use case: select distinct ?x where
                        // check that ?x is distinct
                        if (optim) {
                            if (results.accept(env.getNode(exp.getNode()))) {
                                // backjump here when a mapping will be found with this node
                                // see store()
                                backjump = n - 1;
                                backtrack = eval(p, gNode, stack, n + 1);
                            }
                        } else {
                            backtrack = eval(p, gNode, stack, n + 1);
                        }
                        break;                                     
                }
            }
        }

        if (isEvent) {
            send(Event.FINISH, exp, gNode, stack);
        }

        return backtrack;

    }
    
//    Mappings getMappings() {
//        return memory.getResetJoinMappings();
//    }


    /**
     * ____________________________________________________ *
     */
    /**
     * use case:
     *
     * (n) EDGE{?x ?q ?z} (n+1) FILTER{?x = ?y} with BIND {?x := ?y} compiled
     * as:
     *
     * (n) BIND {?x := ?y} (n+1) EDGE{?x ?q ?z} (n+2) FILTER{?x = ?y}
     */
    private int optBind(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        Memory env = memory;
        int backtrack = n - 1;

        if (exp.isBindCst()) {
            backtrack = cbind(p, gNode, exp, stack, n);
            return backtrack;
        } else {

            // ?x = ?y
            int i = 0, j = 1;
            Node node = env.getNode(exp.get(i).getNode());
            if (node == null) {
                i = 1;
                j = 0;
                node = env.getNode(exp.get(i).getNode());
                if (node == null) {
                    // no binding: continue
                    backtrack = eval(p, gNode, stack, n + 1);
                    return backtrack;
                }
            }

            Node qNode = exp.get(j).getNode();
            if (!env.isBound(qNode) && producer.isBindable(node)) {
                // bind qNode with same index as other variable
                env.push(qNode, node, env.getIndex(exp.get(i).getNode()));
                if (hasEvent) {
                    send(Event.BIND, exp, qNode, node);
                }
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(qNode);
            } else {
                backtrack = eval(p, gNode, stack, n + 1);
            }

            return backtrack;
        }
    }

    /**
     * exp : BIND{?x = cst1 || ?x = cst2} Bind ?x with all its values
     */
    private int cbind(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = memory;
        Producer prod = producer;

        Node qNode = exp.get(0).getNode();
        if (!exp.status() || env.isBound(qNode)) {
            return eval(p, gNode, stack, n + 1);
        }

        if (exp.getNodeList() == null) {
            // Constant are not yet transformed into Node
            for (Object value : exp.getObjectValues()) {
                // get constant Node
                Expr cst = (Expr) value;
                Node node = prod.getNode(cst.getValue());
                if (node != null && prod.isBindable(node)) {
                    // store constant Node into Bind expression
                    // TODO: 
                    // if there are several producers, it is considered
                    // bindable for all producers. This may be a problem.
                    exp.addNode(node);
                } else {
                    // Constant fails being a Node: stop binding
                    exp.setNodeList(null);
                    exp.status(false);
                    break;
                }
            }
        }

        if (exp.getNodeList() != null) {
            // get variable Node
            for (Node node : exp.getNodeList()) {
                // Enumerate constant Node
                env.push(qNode, node, n);
                if (hasEvent) {
                    send(Event.BIND, exp, qNode, node);
                }
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(qNode);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        } else {
            backtrack = eval(p, gNode, stack, n + 1);
        }
        return backtrack;
    }

    /**
     * fresh memory mem inherits data from current memory to evaluate exp (in
     * main)
     * Use case: template parameters are bound in memory, bind them in mem
     *
     */
    void bind(Memory mem, Exp exp, Exp main, Mappings map, Mapping m, boolean bind) {
        if (m != null) {
            mem.push(m, -1);
        }

        if (main.isGraph() && main.getNodeList() != null) {
            bindExpNodeList(mem, main, main.getGraphName());
        } else if ((bind || main.isBinary()) && exp.getNodeList() != null) {           
            // A optional B
            // bind variables of A from environment
            bindExpNodeList(mem, exp, null);
        }

        joinMappings(mem, exp, main, map);
    }

    /**
     * Use case: federated query, service clause Eval exp in the context of
     * partial solution Mappings join(A, B) optional(A, B) minus(A, B) union(A,
     * B) A and/or B evaluated in the context of partial solution map map taken
     * into account by service clause if any
     */
    void joinMappings(Memory mem, Exp exp, Exp main, Mappings map) {
        switch (main.type()) {
            case Exp.JOIN:
                service(exp, mem);
        }
        //mem.setJoinMappings(map);
    }

    // except may be a graphNode: do not bind it here 
    // because it is bound by graphNode()
    void bindExpNodeList(Memory mem, Exp exp, Node except) {
        for (Node qnode : exp.getNodeList()) {
            // getOuterNodeSelf use case: join(subquery, exp)  -- federated query use case
            // qnode in subquery is not the same as qnode in memory
            if (except == null || qnode != except) {
                Node myqnode = memory.getQuery().getOuterNodeSelf(qnode);               
                Node node = memory.getNode(myqnode);
                if (node != null) {
                    mem.push(qnode, node, -1);
                }
            }
        }
    }

    /**
     * JOIN(service ?s {}, exp) if ?s is bound, bind it for subeval ...
     */
    void service(Exp exp, Memory mem) {
        if (exp.type() == SERVICE) {
            bindService(exp, mem);
        } else {
            for (Exp ee : exp.getExpList()) {

                switch (ee.type()) {

                    case SERVICE:
                        bindService(ee, mem);
                        break;

                    case AND:
                    case BGP:
                    case JOIN:
                        service(ee, mem);
                        break;
                }
            }
        }
    }

    void bindService(Exp exp, Memory mem) {
        Node serv = exp.first().getNode();
        if (serv.isVariable() && memory.isBound(serv)) {
            //System.out.println("KG: " + serv + " " + memory.getNode(serv));
            mem.push(serv, memory.getNode(serv));
        }
    }

    /**
     * Bind graph node in new memory if it is bound in current memory use case:
     * graph ?g {pat1 minus pat2}
     */
    
    private void graphNode(Producer p, Node graphNode, Node queryNode, Memory mem) {
        if (graphNode != null) {
            Node qNode = (queryNode == null) ? graphNode : queryNode;
            if (graphNode.isConstant()) {
                mem.push(qNode, p.getNode(graphNode));
            }
            else if (memory.isBound(graphNode)) {
                mem.push(qNode, memory.getNode(graphNode));
            } 
        }
    }


    private int minus(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        boolean hasGraph = gNode != null;
        Memory env = memory;
        Node queryNode = query.getGraphNode();

        Node node1 = null, node2 = null;
        if (hasGraph) {
            node1 = queryNode;
            node2 = exp.getGraphNode();
        }
        Mappings map1 = subEval(p, gNode, node1, exp.first(), exp, data);
        if (stop) {
            return STOP;
        }
        if (map1.isEmpty()) {
            return backtrack;
        }

        MappingSet set1 = new MappingSet(memory.getQuery(), map1);
        Exp rest = exp.rest(); 
        Mappings minusMappings = set1.prepareMappingsRest(rest);
        
        Mappings map2 = subEval(p, gNode, node2, rest, exp, minusMappings); 

        getVisitor().minus(this, getGraphNode(gNode), exp, map1, map2);

        MappingSet set = new MappingSet(memory.getQuery(), exp, set1, new MappingSet(memory.getQuery(), map2));
        set.setDebug(query.isDebug());
        set.start();
        
        for (Mapping map : map1) {
            if (stop) {
                return STOP;
            }
            boolean ok = !set.minusCompatible(map);
            if (ok) {
                if (env.push(map, n)) {
                    // query fake graph node must not be bound
                    // for further minus ...
//                    if (newGraph) { } //do nothing
//                    else if (hasGraph) {
//                        env.pop(queryNode);
//                    }
                    backtrack = eval(p, gNode, stack, n + 1);
                    env.pop(map);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }
        return backtrack;
    }

   
    boolean isFederate(Exp exp) {
        if (memory.getQuery().getGlobalQuery().isFederate()) {
            return true;
        }
        return exp.isRecFederate();
    }
        


    // new 
    private int union(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        // join(A, union(B, C)) ; map = eval(A).distinct(inscopenodes())

        Mappings map1 = unionBranch(p, gNode, exp.first(), exp, data);
        if (stop) {
            return STOP;
        }
        Mappings map2 = unionBranch(p, gNode, exp.rest(), exp, data);

        getVisitor().union(this, getGraphNode(gNode), exp, map1, map2);
             
        int b1 = unionPush(p, gNode, exp, stack, n, map1);
        int b2 = unionPush(p, gNode, exp, stack, n, map2);

        return backtrack;
    }

    /**
     * Eval one exp of union main map is partial solution Mappings resulting
     * from previous statement evaluation, typically eval(A) in join(A, union(B,
     * C)) In federated case (or if exp is itself a union), map is passed to
     * subEval(exp, map), it may be taken into account by service in exp In non
     * federated case, map is included in copy of exp as a values(var, map)
     * clause
     */
    Mappings unionBranch(Producer p, Node gNode, Exp exp, Exp main, Mappings data) throws SparqlException {
        Node queryNode = (gNode == null) ? null : query.getGraphNode();
        
        if (exp.isFirstWith(UNION)) {
            // union in union: eval inner union with parameter data as is
            return subEval(p, gNode, queryNode, exp, main, data);
        }
        else if (isFederate(exp) || exp.isUnion() || isParameterUnionMappings()) {
            Mappings unionData = unionData(exp, data);          
            return subEval(p, gNode, queryNode, exp, main, unionData);
        }
        else {
            // exp += values(var, map)
            Exp ee = exp.complete(data);
            return subEval(p, gNode, queryNode, ee, main);
        }
    }
    
    /**
     * exp is a branch of union
     * extract from data relevant mappings for exp in-scope variables
     */
    Mappings unionData(Exp exp, Mappings data) {
        if (data != null) {
            if (data.getNodeList() == null) {
                MappingSet ms = new MappingSet(getQuery(), data);
                Mappings map = ms.prepareMappingsRest(exp); 
//                if (map!=null) System.out.println(
//                        String.format("union branch: %s\n%s", map.getNodeList(), map.toString(true)));
                return map;
            } 
            else if (data.getJoinMappings()!=null) {
                // select relevant variables from original join Mappings 
                return unionData(exp, data.getJoinMappings());
            }
        }        
        return data;
    }

    /**
     * Push Mappings of branch of union in the stack
     */
    int unionPush(Producer p, Node gNode, Exp exp, Stack stack, int n, Mappings map) throws SparqlException {
        int backtrack = n - 1;
        Memory env = memory;
        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            if (env.push(m, n)) {
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(m);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }
        return backtrack;
    }
      
    private int and(Producer p, Node gNode, Exp exp, Stack stack, Mappings data, int n) throws SparqlException {
        getVisitor().bgp(this, getGraphNode(gNode), exp, null);

        if (data != null && exp.size() > 0 && exp.get(0).isEdgePath()) {
            // pass Mappings data as values clause
            exp = exp.complete(data);
            //System.out.println(exp);
            stack = stack.and(exp, n);
            // Mappings data is in stack, not in parameter
            return eval(p, gNode, stack, n);
        }
        else {
            stack = stack.and(exp, n);
            // pass Mappings data as parameter
            return eval(p, gNode, stack, data, n);
        }
    }
    
    private int bgp(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        List<Node> from = query.getFrom(gNode);
        Mappings map = p.getMappings(gNode, from, exp, memory);

        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            m.fixQueryNodes(query);
            boolean b = memory.push(m, n, false);
            if (b) {
                int back = eval(p, gNode, stack, n + 1);
                memory.pop(m);
                if (back < n) {
                    return back;
                }
            }
        }
        return backtrack;
    }

    /**
     *
     * Exp evaluated as a BGP, get result Mappings, push Mappings and continue
     * Use case: cache the Mappings
     */
    private int bgpAble(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Mappings map = getMappings(p, gNode, exp);
        for (Mapping m : map) {
            if (stop) {
                return STOP;
            }
            m.fixQueryNodes(query);
            boolean b = memory.push(m, n, false);
            if (b) {
                int back = eval(p, gNode, stack, n + 1);
                memory.pop(m);
                if (back < n) {
                    return back;
                }
            }
        }
        return backtrack;
    }

    /**
     * Mappings of exp may be cached for specific query node Use case: datashape
     * template exp = graph ?shape {?sh sh:path ?p} exp is evaluated once for
     * each value of ?sh and Mappings are cached successive evaluations of exp
     * on ?sh get Mappings from cache
     */
    Mappings getMappings(Producer p, Node gNode, Exp exp) throws SparqlException {
        if (exp.hasCache()) {
            Node n = memory.getNode(exp.getCacheNode());
            if (n != null) {
                Mappings m = exp.getMappings(n);
                if (m == null) {
                    m = subEval(p, gNode, gNode, exp, exp, null, null, true);
                    exp.cache(n, m);
                }
                return m;
            }
        }
        return subEval(p, gNode, gNode, exp, exp, null, null, true);
    }

    private int service(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = memory;
        Node serv = exp.first().getNode();
        Node node = serv;

        if (serv.isVariable()) {
            node = env.getNode(serv);
        }

        if (provider != null) {
            // service delegated to provider
            Mappings lMap = provider.service(node, exp, selectQueryMappings(data), this);

//            if (stack.isCompleted()) {
//                return result(p, lMap, n);
//            }
                        
            for (Mapping map : lMap) {
                if (stop) {
                    return STOP;
                }
                // push each Mapping in memory and continue
                complete(query, map, false);
                if (env.push(map, n, false)) {
                    backtrack = eval(gNode, stack, n + 1);
                    env.pop(map, false);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        } else {
            Query q = exp.rest().getQuery();
            return query(p, gNode, q, data, stack, n);
        }

        return backtrack;
    }
    
    // stack = just one service: store and return result directly
    int result(Producer p, Mappings lMap, int n) throws SparqlException {
        for (Mapping map : lMap) {
            complete(query, map, true);
            solution(p, map, n);
        }
        return STOP;
    }

    void complete(Query q, Mapping map, boolean addNode) {
        int i = 0;
        for (Node node : map.getQueryNodes()) {
            Node out = q.getOuterNode(node);
            // draft: use case ?_server_0
            if (out == null) {
                out = node;
                if (addNode && ! q.getSelect().contains(node)) {
                    q.getSelect().add(node);
                }
            }
            map.getQueryNodes()[i] = out;
            i++;
        }
    }
    
    Node getNode(Producer p, Node gNode) {
        if (gNode.isConstant()) {
            return p.getNode(gNode);
        } 
        return memory.getNode(gNode);        
    }
    
    Node getGraphNode(Node node) {
        return (node == null) ? null : node.isConstant() ? node : memory.getNode(node);
    }

    /**
     * bind(exp as var)
     */
    private int bind(Producer p, Node gNode, Exp exp, Mappings map, Stack stack, int n) throws SparqlException {
        if (exp.isFunctional()) {
            return extBind(p, gNode, exp, stack, n);
        }

        int backtrack = n - 1;
        Memory env = memory;

        env.setGraphNode(gNode);
        Node node = eval(exp.getFilter(), env, p);
        env.setGraphNode(null);

        getVisitor().bind(this, getGraphNode(gNode), exp, node == null ? null : node.getDatatypeValue());

        if (node == null) {
            backtrack = eval(p, gNode, stack, map, n + 1);
        } else if (memory.push(exp.getNode(), node, n)) {
            backtrack = eval(p, gNode, stack, map, n + 1);
            memory.pop(exp.getNode());
        }

        return backtrack;
    }

    /**
     * values (?x ?y) { unnest(exp) }
     * compiled as extended bind 
     */
    private int extBind(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = memory;
        env.setGraphNode(gNode);
        Mappings map = evaluator.eval(exp.getFilter(), env, exp.getNodeList());
        env.setGraphNode(null);
        getVisitor().values(this, getGraphNode(gNode), exp, map);
        if (map != null) {
            HashMap<String, Node> tab = toMap(exp.getNodeList());
            for (Mapping m : map) {
                if (stop) {
                    return STOP;
                }
                if (env.push(tab, m, n)) {
                    backtrack = eval(p, gNode, stack, n + 1);
                    env.pop(tab, m);
                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }

        return backtrack;
    }

    HashMap<String, Node> toMap(List<Node> list) {
        HashMap<String, Node> m = new HashMap<>();
        for (Node node : list) {
            m.put(node.getLabel(), node);
        }
        return m;
    }

    /**
     * Special case: optional{} !bound(?x) When filter fail, backjump before
     * optional
     *
     */
    private int filter(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        Memory env = memory;
        boolean success = true;

        if (exp.isPostpone()) {
            // use case: optional { filter (exp) }, eval later
        } else {
            env.setGraphNode(gNode);
            success = test(exp.getFilter(), env, p);
            env.setGraphNode(null);

            if (hasFilter) {
                success = getVisitor().filter(this, getGraphNode(gNode), exp.getFilter().getExp(), success);
            }
        }

        if (hasEvent) {
            send(Event.FILTER, exp, success);
        }

        if (success) {
            backtrack = eval(p, gNode, stack, n + 1);
        }

        return backtrack;
    }
    
    boolean test(Filter f, Environment env, Producer p) throws SparqlException {
        return evaluator.test(f, env, p);
    }
          
    boolean test(Filter f, Environment env) throws SparqlException {
       return evaluator.test(f, env);
    }

    private int path(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1, evENUM = Event.ENUM;
        PathFinder path = getPathFinder(exp, p);
        Filter f = null;
        Memory env = memory;
        Query qq = query;
        boolean isEvent = hasEvent;
        
        if (data!=null && data.getNodeList()!=null && isPushEdgeMappings()) {   
            // push values(data) before edge in stack
            logger.warn(String.format("Push path mappings:\nvalue %s\n%s", 
                 data.getNodeList(), data.toString(false, false, 5)));
            return eval(p, gNode, stack.addCopy(n, exp.getValues(data)), n);
        }

        if (stack.size() > n + 1) {
            if (stack.get(n + 1).isFilter()) {
                f = stack.get(n + 1).getFilter();
            }
        }

        path.start(exp.getEdge(), query.getPathNode(), env, f);
        boolean isSuccess = false;

        List<Node> list = qq.getFrom(gNode);
        Node bNode = gNode;

        if (p.getMode() == Producer.EXTENSION) {
            if (p.getQuery() == memory.getQuery()) {
                list = empty;
                bNode = p.getGraphNode();
            } else {
                bNode = null;
            }
        }

        for (Mapping map : path.candidate(gNode, list, env)) {
            if (stop) {
                path.stop();
                return STOP;
            }
            boolean b = match(map);
            boolean success = match(map) && env.push(map, n);

            if (isEvent) {
                send(evENUM, exp, map, success);
            }

            if (success) {
                isSuccess = true;
                backtrack = eval(p, gNode, stack, n + 1);
                env.pop(map);
                //map.setRead(true);

                if (backtrack < n) {
                    path.stop();
                    // remove it to get fresh automaton next time
                    lPathFinder.remove(path);
                    return backtrack;
                }
            }
        }

        if (!isSuccess && optim) {
            // backjump to max index where nodes are bound for first time:
            int bj = env.getIndex(bNode, exp.getEdge());
            backtrack = bj;
        }
        path.stop();
        return backtrack;
    }

    private int values(Producer p, Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1;
        getVisitor().values(this, getGraphNode(gNode), exp, exp.getMappings());
        
        for (Mapping map : exp.getMappings()) {
            if (stop) {
                return STOP;
            }
            if (binding(exp.getNodeList(), map, n)) {
                backtrack = eval(p, gNode, stack, n + 1);
                free(exp.getNodeList(), map);

                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;

    }

    /**
     * values var { val }
     */
    boolean binding(List<Node> varList, Mapping map) {
        return binding(varList, map, -1);
    }

    boolean binding(List<Node> varList, Mapping map, int n) {
        int i = 0;
        for (Node qNode : varList) { //map.getQueryNodes()) {

            Node node = map.getNode(qNode);
            if (node != null) {
                Node value = producer.getNode(node.getValue());
                boolean suc = memory.push(qNode, value, n);
                if (!suc) {
                    popBinding(varList, map, i);
                    return false;
                }
            }
            i++;
        }
        return true;
    }

    boolean popBinding(List<Node> varList, Mapping map, int i) {
        int j = 0;
        for (Node qq : varList) { 
            Node nn = map.getNode(qq);
            if (nn != null) {
                if (j >= i) {
                    return false;
                } else {
                    j++;
                }
                memory.pop(qq);
            }
        }
        return false;
    }

    void free(List<Node> varList, Mapping map) {
        for (Node qNode : varList) {
            memory.pop(qNode);
        }
    }

    /**
     * Enumerate candidate edges
     *
     */
    private int edge(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isSuccess = false,
                hasGraphNode = gNode != null,
                isEvent = hasEvent;
        Edge qEdge = exp.getEdge();
        Edge previous = null;
        Node graph = null;
        Memory env = memory;
        env.setExp(exp);
        //Producer prod = producer;
        Query qq = query;
        List<Node> list = qq.getFrom(gNode);
        // the backtrack gNode
        Node bNode = gNode;
        boolean matchNBNode = qEdge.isMatchArity();
       
        if (data != null && data.getNodeList() != null) {
            if (isPushEdgeMappings()) {
                // push values(data) before edge in stack
                if (isDebug()) {
                    logger.warn(String.format("Push edge mappings:\nvalues %s\n%s",
                            data.getNodeList(), data.toString(false, false, 5)));
                }
                return eval(p, gNode, stack.addCopy(n, exp.getValues(data)), n);
            } else if (isDebug()) {
                logger.warn(String.format("Eval edge skip mappings:\nvalues %s\n%s",
                        data.getNodeList(), data.toString(false, false, 5)));
            }
        }

        if (p.getMode() == Producer.EXTENSION) {
            // Producer is Extension only for the query that created it
            // use case: templates may share same Producer
            if (p.getQuery() == memory.getQuery()) {
                list = empty;
                bNode = p.getGraphNode();
            } else {
                bNode = null;
            }
        }

//        StopWatch sw = new StopWatch();
//        sw.start();
        Iterable<Edge> entities;
        if (hasProduce) {
            // draft
            entities = produce(p, gNode, list, qEdge);
            if (entities == null) {
                entities = p.getEdges(gNode, list, qEdge, env);
            }
        } else {
            entities = p.getEdges(gNode, list, qEdge, env);
        }

        Iterator<Edge> it = entities.iterator();

        while (it.hasNext()) {

            if (stop) {
                return STOP;
            }

            Edge edge = it.next();
            //if (query.isDebug())System.out.println("E: " + edge);
            if (edge != null) {
                nbEdge++;
                if (hasListener && !listener.listen(qEdge, edge)) {
                    continue;
                }

                //Edge edge = ent;
                graph = edge.getGraph();

//				if (draft && edgeToDiffer != null && previous != null){
//					// draft backjump with position
//					// backjump require different node
//					// between previous and current edge
////					if (indexToDiffer != n){
////						System.out.println(query);
////						edgeToDiffer = null;
////					}
////					else 
//						if (! differ(exp, edgeToDiffer, previous, edge)){
//						continue;
//					}
//					else {
//						edgeToDiffer = null;
//					}
//				}
                previous = edge;
                boolean bmatch = match(qEdge, edge, gNode, graph, env);

                if (matchNBNode) {
                    bmatch &= (qEdge.nbNode() == edge.nbNode());
                }

                if (bmatch) {
                    if (hasCandidate) {
                        DatatypeValue dt = getVisitor().candidate(this, getGraphNode(gNode), qEdge, edge);
                        if (dt != null) {
                            bmatch = dt.booleanValue();
                        }
                    }

                    bmatch &= push(p, qEdge, edge, gNode, graph, n);
                }

                if (isEvent) {
                    send(evENUM, exp, edge, bmatch);
                }

                if (bmatch) {
                    isSuccess = true;
                    backtrack = eval(p, gNode, stack, n + 1);

                    env.pop(qEdge, edge);
                    if (hasGraphNode) {
                        env.pop(gNode);
                    }

                    if (backtrack < n) {
                        return backtrack;
                    }
                }
            }
        }
//        sw.stop();
//        logger.info("\n\tGet EDGE in " + sw.getTime() + "ms.  \n\tFOR "+exp+"\n");

        //edgeToDiffer = null;
        if (!isSuccess && optim) {
            // backjump to max index where nodes are bound for first time:
            // (2) x r t
            // (1) y q z
            // (0) x p y
            // (2) may backjump to (0) because they share x
            // in addition we may require to change the value of x by
            // setting edgeToDiffer to x r t
            //int bj = env.getIndex(gNode, qEdge);
            int bj = env.getIndex(bNode, qEdge);
            backtrack = bj;
//			if (draft && !option && ! isSubQuery && bj >=0 && stack.get(bj).isEdge()){
//				// advanced backjump between edge only
//				// require that edge at index bj change at least one node
//				// not with option 
//				edgeToDiffer = exp;
//				// fake index of x
//				//indexToDiffer = bj;
//			}

        }

        return backtrack;
    }

    /**
     * Draf extension where a Visitor provides Edge iterator
     */
    Iterable<Edge> produce(Producer p, Node gNode, List<Node> from, Edge edge) {
        DatatypeValue res = getVisitor().produce(this, gNode, edge);
        if (res == null) {
            return null;
        }
        if (res.getObject() != null && (res.getObject() instanceof Iterable)) {
            return new IterableEntity((Iterable) res.getObject());
        } else if (res instanceof Loopable) {
            Iterable loop = ((Loopable) res).getLoop();
            if (loop != null) {
                return new IterableEntity(loop);
            }
        }
        return null;
    }
   
    /**
     * select * where {{select distinct ?y where {?x p ?y}} . ?y q ?z} new eval,
     * new memory, share only sub query select variables
     *
     */
    private int query(Producer p, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        return query(p, p, gNode, exp, data, stack, n);
    }       

    private int query(Producer p1, Producer p2, Node gNode, Exp exp, Mappings data, Stack stack, int n) throws SparqlException {
        int backtrack = n - 1, evENUM = Event.ENUM;
        boolean isEvent = hasEvent;
        Query subQuery = exp.getQuery();
        Memory env = memory;
        getVisitor().start(subQuery);

        // copy current Eval,  new stack
        // bind sub query select nodes in new memory
        Eval ev = copy(copyMemory(memory, query, subQuery, null), p1, evaluator, subQuery, false);      
        ev.setDebug(isDebug());
        Mappings lMap = ev.eval(gNode, subQuery, null, selectQueryMappings(data));
        
        if (isDebug()) {
            logger.info("subquery results size:\n"+ lMap.size());
            //logger.info(lMap.toString());
        }

        getVisitor().query(this, getGraphNode(gNode), exp, lMap);
        getVisitor().finish(lMap);

        // enumerate the result of the sub query
        // bind the select nodes into the stack
        for (Mapping map : lMap) {
            if (stop) {
                return STOP;
            }
            boolean bmatch = push(subQuery, map, n);

            if (isEvent) {
                send(evENUM, exp, map, bmatch);
            }

            if (bmatch) {
                backtrack = eval(p2, gNode, stack, n + 1);
                pop(subQuery, map);
                if (backtrack < n) {
                    return backtrack;
                }
            }
        }

        return backtrack;
    }
    
    /**
     * Skip Mappings with null nodeList for subquery ans service
     */
    Mappings selectQueryMappings(Mappings data) {
        if (data != null) {
            if (data.getNodeList() == null) {
                // no variable in-scope wrt select clause
                return null;
            } else {
                // forget original join Mappings (in case of union in body)
                // because original Mappings may not fit the select clause
                data.setJoinMappings(null);
            }
        }
        return data;
    }
    
    /**
     * exp.first() is a subquery that implements a BIND() pop the binding at the
     * end of group pattern
     */
    private int pop(Node gNode, Exp exp, Stack stack, int n) throws SparqlException {
        for (Exp ee : exp.first().getQuery().getSelectFun()) {
            Node node = ee.getNode();
            memory.pop(node);
            break;
        }
        return eval(gNode, stack, n + 1);
    }
     
    /**
     * res is a result of sub query bind the select nodes of sub query into
     * current memory retrieve outer node that correspond to sub node
     *
     */
    private boolean push(Query subQuery, Mapping res, int n) {
        int k = 0;
        Memory env = memory;
        Matcher mm = match;
        Query qq = query;

        for (Exp exp : subQuery.getSelectFun()) {
            Node subNode = exp.getNode();
            Node node = res.getNode(subNode);
            Node outNode; //= query.getNode(subNode);
            if (exp.size() == 0) {
                // store outer node for next time
                outNode = qq.getOuterNodeSelf(subNode);   //ici              
                exp.add(outNode);
            } else {
                outNode = exp.get(0).getNode();
            }

            if (node != null) {
                // a value may be null because of an option {}
                if (!(mm.match(outNode, node, env) && env.push(outNode, node, n))) {
                    for (int i = 0; i < k; i++) {
                        subNode = subQuery.getSelect().get(i);
                        outNode = qq.getOuterNodeSelf(subNode);
                        Node value = res.getNode(subNode);
                        if (value != null) {
                            env.pop(outNode);
                            env.popPath(outNode);
                        }
                    }
                    return false;
                } else {
                    if (res.isPath(subNode)) {
                        env.pushPath(outNode, res.getPath(subNode));
                    }
                }
            }
            k++;
        }

        return true;
    }

    /**
     * pop select nodes of sub query
     */
    private void pop(Query subQuery, Mapping ans) {
        Memory env = memory;
        Query qq = query;

        for (Node subNode : subQuery.getSelect()) {
            if (ans.isBound(subNode)) {
                Node outNode = qq.getOuterNodeSelf(subNode);
                env.pop(outNode);
                env.popPath(outNode);
            }
        }
    }


    @Override
    @Deprecated
    public void exec(Exp exp, Environment env, int n) {
        if (exp.getObject() instanceof String) {
            String label = (String) exp.getObject();
            if (env.getNode(label) != null) {
                logger.debug(n + ": " + label + " " + env.getNode(label).getLabel());
            }
        }
    }

    /**
     * Store a new result
     */
    private int store(Producer p, Mapping m) throws SparqlException {
        boolean store = true;
        if (listener != null) {
            store = listener.process(memory);
        }
        if (store) {
            nbResult++;
        }
        if (storeResult && store) {
            Mapping ans = m;
            if (m == null) {
               ans = memory.store(query, p, isSubEval);
            }
            store(ans);
        }
        return -1;
    }
    
    void store(Mapping ans) {
        if (ans != null && acceptable(ans)) {
            //submit(ans);
            if (hasEvent) {
                send(Event.RESULT, ans);
            }
            boolean b = true;
            if (!isSubEval) {
                b = getVisitor().distinct(this, query, ans);
                if (b) {
                    b = getVisitor().result(this, results, ans);
                }
            }
            if (b) {
                results.add(ans);
            }
        }
    }

    boolean acceptable(Mapping m) {
        return query.getGlobalQuery().isAlgebra() || results.acceptable(m);
    }

    void submit(Mapping map) {
        if (query.getGlobalQuery().isAlgebra()) {
            // eval distinct later
            results.add(map);
        } else {
            results.submit(map);
        }
    }

    public int nbResult() {
        return results.size();
    }

    public int getCount() {
        return nbEdge;
    }

    private boolean match(Edge qEdge, Edge edge, Node gNode, Node graphNode, Memory memory) {
        if (!match.match(qEdge, edge, memory)) {
            return false;
        }
        if (gNode == null || graphNode == null) {
            return true;
        }
        return match.match(gNode, graphNode, memory);
    }

    private boolean push(Producer p, Edge qEdge, Edge ent, Node gNode, Node node, int n) {
        Memory env = memory;
        if (!env.push(p, qEdge, ent, n)) {
            return false;
        }
        if (gNode != null && !env.push(gNode, node, n)) {
            env.pop(qEdge, ent);
            return false;
        }
        return true;
    }

    private boolean match(Node qNode, Node node, Node gNode, Node graphNode) {
        Memory env = memory;
        if (!match.match(qNode, node, env)) {
            return false;
        }
        if (gNode == null) {
            return true;
        }
        return match.match(gNode, graphNode, env);
    }

    private boolean push(Node qNode, Node node, Node gNode, Node graphNode, int n) {
        Memory env = memory;
        if (!env.push(qNode, node, n)) {
            return false;
        }
        if (gNode != null && !env.push(gNode, graphNode, n)) {
            env.pop(qNode);
            return false;
        }
        return true;
    }

    // for path 
    private boolean match(Mapping map) {
        int i = 0;
        Memory env = memory;
        Matcher mm = match;
        for (Node qNode : map.getQueryNodes()) {
            Node node = map.getNode(i++);
            if (!mm.match(qNode, node, env)) {
                return false;
            }
        }
        return true;
    }

    /**
     * ********************************************************
     *
     * @param el
     */
    public void addResultListener(ResultListener el) {
        listener = el;
        hasListener = listener != null;
        if (hasListener) {
            evaluator.addResultListener(el);
        }
    }

    public void addEventListener(EventListener el) {
        createManager();
        el.setObject(this);
        manager.addEventListener(el);
    }

    void createManager() {
        if (manager == null) {
            setEventManager(new EventManager());
            if (memory != null) {
                memory.setEventManager(manager);
            }
            if (results != null) {
                results.setEventManager(manager);
            }
        }
    }

    public void setEventManager(EventManager man) {
        manager = man;
        hasEvent = true;
    }

    public EventManager getEventManager() {
        return manager;
    }

    boolean send(int type, Object obj) {
        Event e = EventImpl.create(type, obj);
        return manager.send(e);
    }

    boolean send(int type, Object obj, Object arg) {
        Event e = EventImpl.create(type, obj, arg);
        return manager.send(e);
    }

    boolean send(int type, Object obj, Object arg, Object arg2) {
        Event e = EventImpl.create(type, obj, arg, arg2);
        return manager.send(e);
    }

    void set(Stack current) {
        this.current = current;
    }

    public Stack getStack() {
        return current;
    }

    /**
     * @return the isPathType
     */
    public boolean isPathType() {
        return isPathType;
    }

    /**
     * @param isPathType the isPathType to set
     */
    public void setPathType(boolean isPathType) {
        this.isPathType = isPathType;
    }

    /**
     * @return the sparqlEngine
     */
    public SPARQLEngine getSPARQLEngine() {
        return sparqlEngine;
    }

    /**
     * @param sparqlEngine the sparqlEngine to set
     */
    public void setSPARQLEngine(SPARQLEngine sparqlEngine) {
        this.sparqlEngine = sparqlEngine;
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
     * @return the stop
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * @param stop the stop to set
     */
    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public void finish() {
        setStop(true);
        join.setStop(true);
        evalGraphNew.setStop(true);
        optional.setStop(true);
    }

    public static boolean isPushEdgeMappings() {
        return pushEdgeMappings;
    }

    public static void setPushEdgeMappings(boolean aPushEdgeMappings) {
        pushEdgeMappings = aPushEdgeMappings;
    }

    public static boolean isParameterGraphMappings() {
        return parameterGraphMappings;
    }

    public static void setParameterGraphMappings(boolean aParameterGraphMappings) {
        parameterGraphMappings = aParameterGraphMappings;
    }

    public static boolean isParameterUnionMappings() {
        return parameterUnionMappings;
    }

    public static void setParameterUnionMappings(boolean aParameterUnionMappings) {
        parameterUnionMappings = aParameterUnionMappings;
    }
    
    public static void setNewMappingsVersion(boolean b) {
        setPushEdgeMappings(b);
        setParameterGraphMappings(b);
        setParameterUnionMappings(b);
    }

    public boolean isDebug() {
        return debug;
    }

}
