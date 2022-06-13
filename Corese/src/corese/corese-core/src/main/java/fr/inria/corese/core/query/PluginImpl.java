package fr.inria.corese.core.query;

import fr.inria.corese.core.approximate.ext.AppxSearchPlugin;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.storage.api.IStorage;
import fr.inria.corese.sparql.storage.util.StorageFactory;
import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.function.script.Function;
import fr.inria.corese.sparql.triple.parser.Metadata;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.compiler.eval.Interpreter;
import fr.inria.corese.compiler.eval.ProxyInterpreter;
import fr.inria.corese.compiler.parser.NodeImpl;
import fr.inria.corese.compiler.eval.QuerySolver;
import fr.inria.corese.compiler.eval.QuerySolverVisitorBasic;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Expr;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Matcher;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Mapping;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Memory;
import fr.inria.corese.core.Event;
import fr.inria.corese.core.EventManager;
import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.producer.DataProducer;
import fr.inria.corese.core.logic.Distance;
import fr.inria.corese.core.rule.RuleEngine;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.load.LoadFormat;
import fr.inria.corese.core.load.QueryLoad;
import fr.inria.corese.core.load.result.SPARQLResultParser;
import fr.inria.corese.core.load.Service;
import fr.inria.corese.core.print.ResultFormat;
import fr.inria.corese.core.transform.TemplateVisitor;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.core.util.GraphListen;
import fr.inria.corese.core.util.MappingsGraph;
import fr.inria.corese.core.util.SPINProcess;
import fr.inria.corese.core.workflow.ShapeWorkflow;
import fr.inria.corese.sparql.api.GraphProcessor;
import java.util.ArrayList;
import java.util.List;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.PointerType;
import static fr.inria.corese.kgram.api.core.PointerType.GRAPH;
import static fr.inria.corese.kgram.api.core.PointerType.MAPPINGS;
import fr.inria.corese.sparql.exceptions.SafetyException;
import fr.inria.corese.sparql.triple.function.term.Binding;
import fr.inria.corese.sparql.triple.parser.ASTExtension;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.URLServer;
import java.io.IOException;
import jakarta.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Plugin for filter evaluator 
 * Compute semantic similarity of classes and
 * solutions Implement graph specific function for LDScript
 *
 * @author Olivier Corby, Edelweiss, INRIA 2011
 *
 */
public class PluginImpl
        extends ProxyInterpreter
        implements GraphProcessor {

    static public Logger logger = LoggerFactory.getLogger(PluginImpl.class);
    static String DEF_PPRINTER = Transformer.PPRINTER;
    private static final String NL = System.getProperty("line.separator");

    static int nbBufferedValue = 0;
    static final String EXT = ExpType.EXT;
    public static final String METADATA = EXT + "metadata";
    public static final String VISITOR = EXT + "visitor";
    public static final String LISTEN = EXT + "listen";
    public static final String SILENT = EXT + "silent";
    public static final String DEBUG = EXT + "debug";
    public static final String EVENT = EXT + "event";
    public static final String VERBOSE = EXT + "verbose";
    public static final String METHOD = EXT + "method";
    public static final String EVENT_HIGH = EXT + "events";
    public static final String EVENT_LOW = EXT + "eventLow";
    public static final String SHOW = EXT + "show";
    public static final String HIDE = EXT + "hide";
    public static final String NODE_MGR = EXT + "nodeManager";
    public static final String RDF_STAR = EXT + "rdfstar";
    public static final String TYPECHECK = EXT + "typecheck";
    public static final String RDF_TYPECHECK = EXT + "rdftypecheck";
    public static final String VARIABLE = EXT + "variable";
    public static final String URI = EXT + "uri";
    public static final String TRANSFORMER = EXT + "transformer";
    public static final String BINDING = EXT + "binding";
    public static final String DYNAMIC_CAPTURE = EXT + "dynamic";

    private static final String QM = "?";

    String PPRINTER = DEF_PPRINTER;
    MatcherImpl match;
    // Plugin for Transformer functions
    private PluginTransform pt;
    // draft storage for large literal values (not used)
    private static IStorage storageMgr;
    // Plugin for approximate search
    private AppxSearchPlugin approximateSearch;

    int index = 0;

    public PluginImpl() {
        init();
    }

    PluginImpl(Matcher m) {
        this();
        if (m instanceof MatcherImpl) {
            match = (MatcherImpl) m;
        }
    }

    void init() {
        pt = new PluginTransform(this);
        setApproximateSearch(new AppxSearchPlugin(this));
    }

    public static PluginImpl create(Matcher m) {
        return new PluginImpl(m);
    }

    @Override
    public void setMode(int mode) {
    }

    @Override
    public void start(Producer p, Environment env) {
        setMethodHandler(p, env);
    }

    @Override
    public PluginTransform getComputerTransform() {
        return pt;
    }

    /**
     * Draft test Assign class hierarchy to query extension Goal: emulate method
     * inheritance for xt:method(name, term) Search method name in type
     * hierarchy
     *
     * @test select where
     */
    void setMethodHandler(Producer p, Environment env) {
        ASTExtension ext = env.getQuery().getActualExtension();
        ASTQuery ast =  env.getQuery().getAST();
        if (ext != null && ext.isMethod() && ast.hasMetadata(Metadata.METHOD)) {
            ClassHierarchy ch = new ClassHierarchy(getGraph(p));
            if (env.getQuery().getGlobalQuery().isDebug()) {
                ch.setDebug(true);
            }
            ext.setHierarchy(ch);
            // WARNING: draft test below
            // store current graph in the Interpreter 
            // hence it does not scale with several graph
            // e.g. in server mode
            Interpreter.getExtension().setHierarchy(ch);
        }
    }

    /**
     * sparql states end of query processing
     */
    @Override
    public void finish(Producer p, Environment env) {
        Graph g = getGraph(p);
        if (g != null) {
            g.getContext().setQuery(env.getQuery());
        }
    }

    // function xt:spin
    @Override
    public IDatatype spin(IDatatype dt) {
        SPINProcess sp = SPINProcess.create();
        try {
            Graph g = sp.toSpinGraph(dt.stringValue());
            return DatatypeMap.createObject(g);
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

    // function xt:graph
    @Override
    public IDatatype graph(IDatatype dt) {
        if (dt.getPointerObject() == null) {
            return null;
        }
        Graph g;

        switch (dt.pointerType()) {
            case MAPPINGS:
                g = graph(dt.getPointerObject().getMappings());
                if (g == null) {
                    return null;
                }
                return DatatypeMap.createObject(g);

            case GRAPH:
                return dt;
        }

        return null;
    }

    Graph graph(Mappings map) {
        return MappingsGraph.create(map).getGraph();
    }
    
    // function xt:list xt:graph xt:map xt:json 
    @Override
    public IDatatype create(IDatatype dt) {
        switch (dt.getLabel()) {
            case IDatatype.GRAPH_DATATYPE:
                return DatatypeMap.createObject(GraphStore.create());
            case IDatatype.LIST_DATATYPE:
                return DatatypeMap.list();
            case IDatatype.MAP_DATATYPE:
                return DatatypeMap.map();
            case IDatatype.JSON_DATATYPE:
                return DatatypeMap.json();    
        }
        return null;
    }

    // function xt:xml xt:json xt:rdf
    @Override
    public IDatatype format(Mappings map, int format) {
        ResultFormat ft = ResultFormat.create(map, format);
        return DatatypeMap.newInstance(ft.toString());
    }

    // function st:format
    @Override
    public IDatatype format(IDatatype[] ldt) {
        return getPluginTransform().format(ldt);
    }

    // function xt:approximate
    // xt:approximate(var, val, algo, threshold)
    // algo: jw --jaro winckler ng --ngram wn eq
    @Override
    public IDatatype approximate(Expr exp, Environment env, Producer p, IDatatype[] param) {
        return getApproximateSearch().eval(exp, env, p, param);
    }

    // function xt:sim
    // compute similarity of solution ?
    @Override
    public IDatatype approximate(Expr exp, Environment env, Producer p) {
        return getApproximateSearch().eval(exp, env, p);
    }

    // function xt:similarity
    // compute similarity between classes wrt class hierarchy
    @Override
    public IDatatype similarity(Environment env, Producer p, IDatatype dt1, IDatatype dt2) {
        Graph g = getGraph(p);
        Node n1 = g.getNode(dt1);
        Node n2 = g.getNode(dt2);
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    // utility
    IDatatype ancestor(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1);
        Node n2 = g.getNode(dt2);
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setClassDistance();
        Node n = distance.ancestor(n1, n2);
        return  n.getValue();
    }

    // utility
    IDatatype pSimilarity(Graph g, IDatatype dt1, IDatatype dt2) {
        Node n1 = g.getNode(dt1.getLabel());
        Node n2 = g.getNode(dt2.getLabel());
        if (n1 == null || n2 == null) {
            return null;
        }

        Distance distance = g.setPropertyDistance();
        double dd = distance.similarity(n1, n2);
        return getValue(dd);
    }

    /**
     * Similarity of a query solution  when @relax mode 
     * Sum distance of approximate
     * types Divide by number of nodes and edge
     *
     */
    @Override
    public IDatatype similarity(Environment env, Producer p) {
        Graph g = getGraph(p);
        if (g == null) {
            return null;
        }
        if (!(env instanceof Memory)) {
            return getValue(0);
        }
        Memory memory = (Memory) env;
        if (memory.getQueryEdges() == null) {
            return getValue(0);
        }
        Hashtable<Node, Boolean> visit = new Hashtable<>();
        Distance distance = g.setClassDistance();

        // number of node + edge in the answer
        int count = 0;
        float dd = 0;

        for (Edge qEdge : memory.getQueryEdges()) {

            if (qEdge != null) {
                Edge edge = memory.getEdge(qEdge);

                if (edge != null) {
                    count += 1;

                    for (int i = 0; i < edge.nbNode(); i++) {
                        // count nodes only once
                        Node n = edge.getNode(i);
                        if (!visit.containsKey(n)) {
                            count += 1;
                            visit.put(n, true);
                        }
                    }

                    if ((g.isType(qEdge) || env.getQuery().isRelax(qEdge))
                            && qEdge.getNode(1).isConstant()) {

                        Node qtype = g.getNode(qEdge.getNode(1).getLabel());
                        Node ttype = g.getNode(edge.getNode(1).getLabel());

                        if (qtype == null) {
                            // query type is undefined in ontology
                            qtype = qEdge.getNode(1);
                        }
                        if (ttype == null) {
                            // target type is undefined in ontology
                            ttype = edge.getNode(1);
                        }

                        if (!subClassOf(g, ttype, qtype, env)) {
                            dd += distance.distance(ttype, qtype);
                        }
                    }
                }
            }
        }

        if (dd == 0) {
            return getValue(1);
        }

        double sim = distance.similarity(dd, count);

        return getValue(sim);

    }

    boolean subClassOf(Graph g, Node n1, Node n2, Environment env) {
        if (match != null) {
            return match.isSubClassOf(n1, n2, env);
        }
        return g.isSubClassOf(n1, n2);
    }

    IDatatype load(IDatatype dt) throws SafetyException {
        return load(dt, null);
    }

    public IDatatype load(IDatatype dt, IDatatype format) throws SafetyException {
        return load(dt, null, format, null, Level.USER_DEFAULT);
    }

    // function xt:load
    // expectedFormat: 
    // st:text -> argument is rdf text  
    // otherwise st:turtle st:rdfxml st:json
    @Override
    public IDatatype load(IDatatype dt, IDatatype graph, IDatatype expectedFormat, IDatatype requiredFormat,
            Level level) throws SafetyException {
        Graph g;
        if (graph == null || graph.pointerType() != GRAPH) {
            g = Graph.create();
        } else {
            g = (Graph) graph.getPointerObject();
        }
        Load ld = Load.create(g);
        ld.setLevel(level);
        try {
            if (expectedFormat != null && expectedFormat.getLabel().equals(Transformer.TEXT)) {
                ld.loadString(dt.stringValue(), getFormat(requiredFormat));
            }
            else if (requiredFormat == null) {
                ld.parse(dt.getLabel(), getFormat(expectedFormat));
            }            
            else {
                ld.parseWithFormat(dt.getLabel(), getFormat(requiredFormat));
            }
        } catch (LoadException ex) {
            if (ex.isSafetyException()) {
                throw ex.getSafetyException();
            }
            logger.error(String.format("Load error: %s \n%s %s", dt.stringValue(), 
                    ((expectedFormat == null) ? "" :expectedFormat), ((requiredFormat == null) ? "" :requiredFormat)));
            logger.error(ex.getMessage());
        }
        IDatatype res = DatatypeMap.createObject(g);
        return res;
    }

    // st:turtle st:rdfxml st:json
    int getFormat(IDatatype dt) {
        return (dt == null) ? Load.UNDEF_FORMAT : LoadFormat.getDTFormat(dt.getLabel());
    }

    // function xt:write
    // write in /tmp/
    @Override
    public IDatatype write(IDatatype dtfile, IDatatype dt) {
        QueryLoad ql = QueryLoad.create();
        String str = ql.writeTemp(dtfile.getLabel(), dt);
        if (str == null) {
            return null;
        }
        return DatatypeMap.newInstance(str);
    }
    
    // function xt:write
    // write anywhere (need specific access right)
    // see sparql GraphSpecifFunction
    @Override
    public IDatatype superWrite(IDatatype dtfile, IDatatype dt) {
        QueryLoad ql = QueryLoad.create();
        ql.write(dtfile.getLabel(), dt);
        return dtfile;
    }

    // function xt:syntax
    // syntax: st:rdfxml st:turtle
    @Override
    public IDatatype syntax(IDatatype syntax, IDatatype graph, IDatatype node) {
        Graph g = (Graph) graph.getPointerObject();
        ResultFormat ft = ResultFormat.create(g, syntax.getLabel());
        String str = (node == null) ? ft.toString() : ft.toString(node);
        return DatatypeMap.newInstance(str);
    }

    private IDatatype even(Expr exp, IDatatype dt) {
        boolean b = dt.intValue() % 2 == 0;
        return getValue(b);
    }

    private IDatatype odd(Expr exp, IDatatype dt) {
        boolean b = dt.intValue() % 2 != 0;
        return getValue(b);
    }

    public PluginTransform getPluginTransform() {
        return pt;
    }

    // function st:index
    @Override
    public IDatatype index(Environment env, Producer p) {
        return getValue(index++);
    }

    // function xt:entailment
    @Override
    public IDatatype entailment(Environment env, Producer p, IDatatype dt) throws EngineException {
        Binding bind =  env.getBind();
        Graph g = getGraph(p);
        String uri = null;
        if (dt != null) {
            if (dt.pointerType() == GRAPH) {
                g = (Graph) dt.getPointerObject().getTripleStore();
            } else {
                uri = dt.getLabel();
            }
        }
        boolean b = env.getEval().getSPARQLEngine().isSynchronized();
        if (g.isReadLocked() && !b) {
            // use case where isSynchronised():
            // @afterUpdate, QueryProcess isSynchronised(), we can perform entailment
            logger.info("Graph locked, perform entailment on copy");
            g = g.copy();
        }

        RuleEngine re = create(g, uri, b, bind.getAccessLevel());
        re.process(bind);
        return DatatypeMap.createObject(g);
    }

    RuleEngine create(Graph g, String uri, boolean b, Level level) throws EngineException {
        if (uri == null) {
            return create(g, b);
        } else {
            return create(g, uri, level);
        }
    }

    RuleEngine create(Graph g, boolean b) throws EngineException {
        try {
            RuleEngine re = RuleEngine.create(g);
            re.setSynchronized(b);
            re.setProfile(RuleEngine.Profile.OWLRL);
            return re;
        } catch (LoadException ex) {
            throw ex.getCreateEngineException();
        }
    }

    RuleEngine create(Graph g, String uri, Level level) throws EngineException {
        try {
            Load ld = Load.create(g);
            ld.setLevel(level);
            ld.parse(uri, Load.RULE_FORMAT);
            return ld.getRuleEngine();
        } catch (LoadException ex) {
            throw ex.getCreateEngineException();
        }
    }

    /**
     * function xt:shaclGraph xt:shaclNode
     */
    @Override
    public IDatatype shape(Expr exp, Environment env, Producer p, IDatatype[] param) {
        switch (exp.oper()) {
            case XT_SHAPE_GRAPH:
                return shapeGraph(getGraph(p), param);
            case XT_SHAPE_NODE:
                return shapeNode(getGraph(p), param);
        }
        return null;
    }

    // param[0] = shapeGrah ; param [1] = shape
    IDatatype shapeGraph(Graph g, IDatatype[] param) {
        if (param.length == 2) {
            return new ShapeWorkflow().processGraph(g, param[0], param[1]);
        }
        return new ShapeWorkflow().processGraph(g, param[0]);
    }

    // param[0] = shapeGrah = s ; param.length >= 2
    IDatatype shapeNode(Graph g, IDatatype[] param) {
        switch (param.length) {
            case 2:
                return new ShapeWorkflow().processNode(g, param[0], param[1]);
            case 3:
                return new ShapeWorkflow().processNode(g, param[0], param[1], param[2]);
        }
        return null;
    }

    // function xt:insert
    // xt:insert(namedGraphURI, s, p o)
    // xt:insert(graph, s, p, o)
    @Override
    public IDatatype insert(Environment env, Producer p, IDatatype... param) {
        Graph g = getGraph(p);
        IDatatype first = param[0];
        Edge e;
        if (param.length == 3) {
            e = g.add(first, param[1], param[2]);
        } else if (first.pointerType() == PointerType.GRAPH) {
            Graph gg = (Graph) first.getPointerObject();
            e = gg.add(param[1], param[2], param[3]);
        } else {
            e = g.add(first, param[1], param[2], param[3]);
        }
        return (e == null) ? FALSE : TRUE;
    }

    // function xt:delete
    // xt:delete(s, p, o)
    // xt:delete(namedGraphURI, s, p o)
    // xt:delete(graph, s, p, o)    
    @Override
    public IDatatype delete(Environment env, Producer p, IDatatype... param) {
        Graph g = getGraph(p);
        IDatatype first = param[0];
        List<Edge> le;
        if (param.length == 3) {
            le = g.delete(first, param[1], param[2]);
        } 
        else if (first.pointerType() == PointerType.GRAPH) {
            Graph gg = (Graph) first.getPointerObject();
            le = gg.delete(param[1], param[2], param[3]);
        }
        else {
            le = g.delete(first, param[1], param[2], param[3]);
        }
        return (le == null) ? FALSE : TRUE;
    }

    // function xt:value    
    // xt:value(subject, predicate)
    // xt:value(subject, predicate, index)
    // xt:value(graph, subject, predicate)
    // xt:value(graph, subject, predicate, index)  
    // index: index of result node
    @Override
    public IDatatype value(Environment env, Producer p, IDatatype graph, IDatatype node, IDatatype predicate, int n) {
        Graph g = (Graph) ((graph == null) ? p.getGraph() : graph.getPointerObject());
        Node val = g.value(node, predicate, n);
        if (val == null) {
            return null;
        }
        return  val.getDatatypeValue();
    }

    // function xt:exists
    // xt:exist()
    // xt:exist(p)
    // xt:exist(s, p)
    // xt:exist(s, p, o)
    @Override
    public IDatatype exists(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj) {
        DataProducer dp = new DataProducer(getGraph(p));
        if (env.getGraphNode() != null) {
            dp.from(env.getGraphNode());
        }
        for (Edge ent : dp.iterate(subj, pred, obj)) {
            return (ent == null) ? FALSE : TRUE;
        }
        return FALSE;
    }

    // function xt:mindegree
    // check whether node has degree >= dtmin
    // xt:mindegree(node, min)
    // xt:mindegree(node, index, min)
    // xt:mindegree(node, predicate, min)
    // xt:mindegree(node, predicate, index, min) 
    // index  = 0|1
    @Override
    public IDatatype mindegree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index, IDatatype dtmin) {
        int min = dtmin.intValue();
        if (index == null) {
            // input + output edges
            int d = degree(env, p, node, pred, 0, min) + degree(env, p, node, pred, 1, min);
            return DatatypeMap.newInstance(d >= min);
        }
        int d = degree(env, p, node, pred, index.intValue(), min);
        return DatatypeMap.newInstance(d >= min);
    }

    // function xt:degree
    // xt:degree(node)
    // xt:degree(node, index)
    // xt:degree(node, predicate)
    // xt:degree(node, predicate, index) 
    // index  = 0|1
    @Override
    public IDatatype degree(Environment env, Producer p, IDatatype node, IDatatype pred, IDatatype index) {
        int min = Integer.MAX_VALUE;
        if (index == null) {
            // input + output edges
            int d = degree(env, p, node, pred, 0, min) + degree(env, p, node, pred, 1, min);
            return DatatypeMap.newInstance(d);
        }
        int d = degree(env, p, node, pred, index.intValue(), min);
        return DatatypeMap.newInstance(d);
    }

    int degree(Environment env, Producer p, IDatatype node, IDatatype pred, int n, int min) {
        IDatatype sub = (n == 0) ? node : null;
        IDatatype obj = (n == 1) ? node : null;
        DataProducer dp = new DataProducer(getGraph(p));
        Node graph = env.getGraphNode();
        if (graph != null) {
            dp = dp.from(graph);
        }
        int count = 0;

        for (Edge edge : dp.iterate(sub, pred, obj)) {
            if (edge == null) {
                break;
            }
            if (node.equals(edge.getNode(n).getDatatypeValue())) {
                count++;
                if (count >= min) {
                    break;
                }
            } else {
                break;
            }
        }
        return count;
    }

    public Graph getGraph() {
        return (Graph) getProducer().getGraph();
    }

    // name of  current named graph 
//    IDatatype name(Environment env) {
//        if (env.getGraphNode() == null) {
//            return null;
//        }
//        Node n = env.getNode(env.getGraphNode());
//        if (n == null) {
//            return null;
//        }
//        return  n.getDatatypeValue();
//    }
    
    /**
     * rdf star
     * triple(s, p, o)  
     * filter bind <<s p o>>
     */
    @Override
    public IDatatype triple(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj) {
        IDatatype ref = getGraph(p).createTripleReference();
        Edge e = getGraph(p).create(
                getGraph(p).getDefaultGraphDatatypeValue(), 
                subj, pred, obj, ref);
        e.setCreated(true);
        e.setNested(true);
        return ref;
    }

    // function xt:edge
    // return iterator Edge
    // xt:edge()
    // xt:edge(p)
    // xt:edge(s, p)
    // xt:edge(s, p, o)
    @Override
    public IDatatype edge(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj) {
        return edgeList(env, p, subj, pred, obj, null);
    }

    // function xt:edge
    // return iterator Edge
    // xt:edge(s, p, o, g)
    @Override
    public IDatatype edge(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        return edgeList(env, p, subj, pred, obj, graph);
    }

    public IDatatype edge(IDatatype subj, IDatatype pred) {
        return DatatypeMap.createObject(getDataProducer(null, getProducer(), subj, pred, null));
    }

    public IDatatype edge(IDatatype subj, IDatatype pred, IDatatype obj) {
        return DatatypeMap.createObject(getDataProducer(null, getProducer(), subj, pred, obj));
    }

    IDatatype edgeList(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getEdgeProducer(env, p, subj, pred, obj, graph);
        return dp.getEdges();
    }

    // function xt:subjects
    // return list of subject of xt:edge()
    @Override
    public IDatatype subjects(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getEdgeProducer(env, p, subj, pred, obj, graph).setDuplicate(false);
        return dp.getSubjects();
    }

    // function xt:subjects
    // return list of object of xt:edge()
    @Override
    public IDatatype objects(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getEdgeProducer(env, p, subj, pred, obj, graph).setDuplicate(false);
        return dp.getObjects();
    }

    DataProducer getEdgeProducer(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj, IDatatype graph) {
        DataProducer dp = getDataProducer(env, p, subj, pred, obj);
        if (graph != null && (!graph.isList() || graph.size() > 0)) {
            dp.from(graph);
        } else if (env != null && env.getGraphNode() != null) {
            dp.from(env.getGraphNode());
        }
        return dp;
    }

    DataProducer getDataProducer(Environment env, Producer p, IDatatype subj, IDatatype pred, IDatatype obj) {
        return new DataProducer(getGraph(p)).setDuplicate(true).iterate(subj, pred, obj);
    }

    // function xt:union
    @Override
    public IDatatype union(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2) {
        if ((!(dt1.isPointer() && dt2.isPointer()))
                || (dt1.pointerType() != dt2.pointerType())) {
            return null;
        }

        if (dt1.pointerType() == MAPPINGS) {
            return algebra(exp, env, p, dt1, dt2);
        }

        if (dt1.pointerType() == GRAPH) {
            Graph g1 = (Graph) dt1.getPointerObject();
            Graph g2 = (Graph) dt2.getPointerObject();
            Graph g = g1.union(g2);
            return DatatypeMap.createObject(g);
        }

        return null;
    }
    
    // function xt:merge
    @Override
    public IDatatype merge(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2) {    
        if (dt1.pointerType() == GRAPH && dt2.pointerType() == GRAPH) {
            Graph g1 = (Graph) dt1.getPointerObject();
            Graph g2 = (Graph) dt2.getPointerObject();
            g1.merge(g2);
            return dt1;
        }

        return null;
    }

    // function xt:minus xt:optional xt:union xt:join
    @Override
    public IDatatype algebra(Expr exp, Environment env, Producer p, IDatatype dt1, IDatatype dt2) {
        if ((!(dt1.isPointer() && dt2.isPointer()))
                || (dt1.pointerType() != dt2.pointerType())) {
            return null;
        }

        if (dt1.pointerType() == MAPPINGS) {
            Mappings m1 = dt1.getPointerObject().getMappings();
            Mappings m2 = dt2.getPointerObject().getMappings();

            Mappings m = null;
            switch (exp.oper()) {
                case XT_MINUS:
                    m = m1.minus(m2);
                    break;
                case XT_JOIN:
                    m = m1.join(m2);
                    break;
                case XT_OPTIONAL:
                    m = m1.optional(m2);
                    break;
                case XT_UNION:
                    m = m1.union(m2);
                    break;
            }

            return DatatypeMap.createObject(m);
        }

        return null;
    }

    // function xt:tune
    @Override
    public IDatatype tune(Expr exp, Environment env, Producer p, IDatatype... dt) {
        if (dt.length < 2) {
            return null;
        }
        IDatatype dt1 = dt[0];
        IDatatype dt2 = dt[1];
        IDatatype dt3 = (dt.length > 2) ? dt[2] : null;
        Graph g = getGraph(p);
        String label = dt1.getLabel();
        switch (label) {
            case LISTEN:
                if (dt2.booleanValue()) {
                    if (env.getEval() != null) {
                        g.addListener(new GraphListen(env.getEval()));
                    }
                } else {
                    g.removeListener();
                }
                break;
            case VERBOSE:
                getEventManager(p).setVerbose(dt2.booleanValue());
                break;
            case DEBUG:
                switch (dt2.getLabel()) {
                    case TRANSFORMER:
                        // xt:tune(st:debug, st:transformer, st:ds)
                        Transformer.debug(dt3.getLabel(), dt.length > 3 ? dt[3].booleanValue() : true);
                        break;

                    case BINDING:
                        env.getBind().setDebug(dt3.booleanValue());
                        break;

                    default:
                        getEvaluator().setDebug(dt2.booleanValue());
                }
                break;

            case EVENT_HIGH:
                getEventManager(p).setVerbose(dt2.booleanValue());
                getGraph(p).setDebugMode(dt2.booleanValue());
                break;
            case EVENT_LOW:
                getEventManager(p).setVerbose(dt2.booleanValue());
                getEventManager(p).hide(Event.Insert);
                getEventManager(p).hide(Event.Construct);
                getGraph(p).setDebugMode(dt2.booleanValue());
                break;
            case METHOD:
                getEventManager(p).setMethod(dt2.booleanValue());
                break;
            case SHOW:
                getEventManager(p).setVerbose(true);
                Event e = Event.valueOf(dt2.stringValue().substring(NSManager.EXT.length()));
                if (e != null) {
                    getEventManager(p).show(e);
                }
                break;
            case HIDE:
                getEventManager(p).setVerbose(true);
                e = Event.valueOf(dt2.stringValue().substring(NSManager.EXT.length()));
                if (e != null) {
                    getEventManager(p).hide(e);
                }
                break;
            case NODE_MGR:
                getGraph(p).tuneNodeManager(dt2.booleanValue());
                break;
            case VISITOR:
                QuerySolver.setVisitorable(dt2.booleanValue());
                break;
            case EVENT:
                QuerySolverVisitorBasic.setEvent(dt2.booleanValue());
                break;

            case RDF_STAR:
                if (dt2.getLabel().equals(VARIABLE)) {
                    ASTQuery.REFERENCE_QUERY_BNODE = !ASTQuery.REFERENCE_QUERY_BNODE;
                    System.out.println("rdf* query variable: " + ASTQuery.REFERENCE_QUERY_BNODE);
                } else if (dt2.getLabel().equals(URI)) {
                    ASTQuery.REFERENCE_DEFINITION_BNODE = !ASTQuery.REFERENCE_DEFINITION_BNODE;
                    System.out.println("rdf* id uri: " + ASTQuery.REFERENCE_DEFINITION_BNODE);
                }
                break;

            case TYPECHECK:
                Function.typecheck = dt2.booleanValue();
                System.out.println("typecheck: " + Function.typecheck);
                break;
            case RDF_TYPECHECK:
                Function.rdftypecheck = dt2.booleanValue();
                System.out.println("rdftypecheck: " + Function.rdftypecheck);
                break;

        }

        return TRUE;
    }

    EventManager getEventManager(Producer p) {
        return getGraph(p).getEventManager();
    }
   
    Node node(Graph g, IDatatype dt) {
        Node n = g.getNode(dt, false, false);
        return n;
    }

    // function xt:depth
    @Override
    public IDatatype depth(Environment env, Producer p, IDatatype dt) {
        Graph g = getGraph(p);
        Node n = node(g, dt);
        if (n == null) {
            return null;
        }
        Integer d = g.setClassDistance().getDepth(n);
        if (d == null) {
            return null;
        }
        return getValue(d);
    }

    IDatatype db(Environment env, Graph g) {
        ASTQuery ast =  env.getQuery().getAST();
        String name = ast.getMetadataValue(Metadata.DB);
        return db(name, g);
    }

    IDatatype db(String name, Graph g) {
        Producer p = QueryProcess.getCreateProducer(g, QueryProcess.DB_FACTORY, name);
        return DatatypeMap.createObject(p);
    }

    // function xt:sparql
    // xt:sparql(query)
    // xt:sparql(query, var, val)
    @Override
    public IDatatype sparql(Environment env, Producer p, IDatatype[] param) throws EngineException {
        Mapping m = createMapping(p, param, 1);
        // share global variables, context, log and access level
        m.setBind(env.getBind());
        return kgram(env, getGraph(p), param[0].getLabel(), m);
    }

    /**
     * First param is query other param are variable bindings (variable, value)
     */
    Mapping createMapping(Producer p, IDatatype[] param, int start) {
        ArrayList<Node> var = new ArrayList<>();
        ArrayList<Node> val = new ArrayList<>();
        for (int i = start; i < param.length; i += 2) {
            var.add(NodeImpl.createVariable(clean(param[i].getLabel())));
            val.add(p.getNode(param[i + 1]));
        }
        return Mapping.create(var, val);
    }

    String clean(String name) {
        if (name.startsWith("$")) {
            return QM.concat(name.substring(1));
        }
        return name;
    }
   
    // share @report with subquery
    Dataset getDataset(Environment env) {
        Metadata meta = env.getQuery().getAST().getMetadata();
        if (meta!=null) {
            Metadata m = meta.selectSparql();
            if (m != null) {
                return new Dataset().setMetadata(m);
            }
        }
        return null;
    }
    
    IDatatype kgram(Environment env, Graph g, String query, Mapping m) throws EngineException{
        QueryProcess exec = QueryProcess.create(g, true);
        exec.setRule(env.getQuery().isRule());
        try {
            Mappings map;
            if (g.getLock().getReadLockCount() == 0 && !g.getLock().isWriteLocked()) {
                // use case: LDScript direct call  
                // accept update
                map = exec.query(query, m, getDataset(env)); 
            } else {
                // reject update
                map = exec.sparqlQuery(query, m, getDataset(env));
            }
            // use case: subquery create Log or Context
            // outer query processing inherits it
            env.getBind().subShare(exec.getEnvironmentBinding());
            
            if (map.getQuery().isDebug()) {
                System.out.println("result:");
                System.out.println(map);
            }
            if (map.getGraph() == null) {
                // draft: service evaluation detail report
                env.setReport(map.getReport());
                return DatatypeMap.createObject(map);
            } else {
                return DatatypeMap.createObject(map.getGraph());
            }
        } 
        catch (SafetyException e) {
            throw e;
        }
        catch (EngineException e) {
            logger.error(e.getMessage());
            env.getBind().subShare(exec.getEnvironmentBinding());
            return DatatypeMap.createObject(new Mappings());
        }
    }

    public Mappings parseSPARQLResult(String path, String... format) throws EngineException {
        SPARQLResultParser parser = new SPARQLResultParser();
        try {
            return  parser.parse(path, format);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new EngineException(ex);
        }
    }
    
    public Mappings parseSPARQLResultString(String str, String... format) throws EngineException {
        SPARQLResultParser parser = new SPARQLResultParser();
        try {          
            return  parser.parseString(str, format);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new EngineException(ex);
        }
    }
    
    // function xt:loadMappings
    // parse Query Results Format from file
    // format: xt:json xt:xml
    @Override
    public IDatatype readSPARQLResult(IDatatype path, IDatatype... dtformat) {
        Mappings map = null;
        try {
            if (dtformat.length == 0) {
                map = parseSPARQLResult(path.getLabel());
            } else {
                map = parseSPARQLResult(path.getLabel(), dtformat[0].getLabel());
            }
        } catch (EngineException ex) {
            logger.error(ex.getMessage() + " " + path);
        }
        if (map == null) {
            return null;
        }
        return DatatypeMap.createObject(map);
    }
    
    // function xt:parseMappings
    // parse Query Results Format from string
    // format: xt:json xt:xml
    @Override
    public IDatatype readSPARQLResultString(IDatatype str, IDatatype... dtformat) {
        Mappings map = null;
        try {
            if (dtformat.length == 0) {
                map = parseSPARQLResultString(str.getLabel());
            }
            else {
               map = parseSPARQLResultString(str.getLabel(), dtformat[0].getLabel());
            }
        } catch (EngineException ex) {
            logger.error(ex.getMessage());
        }
        if (map == null) {
            return null;
        }
        return DatatypeMap.createObject(map);
    }

    // function xt:read
    @Override
    public IDatatype read(IDatatype dt) {
        QueryLoad ql = QueryLoad.create();
        String str = null;
        try {
            str = ql.readProtect(dt.getLabel());
        } catch (LoadException ex) {
            logger.error("Read error");
            logger.error(ex.getMessage());
        }
        if (str == null) {
            return null; //str = "";
        }
        return DatatypeMap.newInstance(str);
    }

    // function xt:httpget
    @Override
    public IDatatype httpget(IDatatype uri) {
        try {
            Service s = new Service();
            Response res = s.get(uri.getLabel());
            String str = res.readEntity(String.class);
            return DatatypeMap.newInstance(str);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\n" + uri.getLabel(), "");
        }
        return null;
    }
    
    // function xt:httpget
    // accept: header accept format
    @Override
    public IDatatype httpget(IDatatype uri, IDatatype accept) {
        try {
            Service s = new Service();
            String format = ResultFormat.TEXT;
            if (accept != null) {
                format = ResultFormat.decodeOrText(accept.getLabel());
            }
            String url = new URLServer(uri.getLabel()).encoder();
            String str = s.getBasic(url, format);
            return DatatypeMap.newInstance(str);
        } catch (Exception e) {
            logger.error(e.getMessage() + "\n" + uri.getLabel(), "");
        }
        return null;
    }

    String getLabel(IDatatype dt) {
        if (dt == null) {
            return null;
        }
        return dt.getLabel();
    }

    Graph getGraph(Producer p) {
        if (p.getGraph() instanceof Graph) {
            return (Graph) p.getGraph();
        }
        return null;
    }

    public TemplateVisitor getVisitor(Binding b, Environment env, Producer p) {
        return pt.getVisitor(b, env, p);
    }
    
    /**
     * STTL create intermediate string result (cf Proxy STL_CONCAT) Save string
     * value to disk using Fuqi StrManager Each STTL Transformation would have
     * its own StrManager Managed in the Context to be shared between
     * subtransformation (cf OWL2)
     */
    //@Override
    public IDatatype getBufferedValue(StringBuilder sb, Environment env) {
        if (storageMgr == null) {
            createManager();
        }
        if (storageMgr.check(sb.length())) {
            IDatatype dt = getValue(sb.toString());
            dt.setValue(dt.getLabel(), nbBufferedValue++, storageMgr);
            return dt;
        } else {
            return DatatypeMap.newStringBuilder(sb);
        }
    }

    void createManager() {
        storageMgr = StorageFactory.create(IStorage.STORAGE_FILE, null);
        storageMgr.enable(true);
    }

    @Override
    public GraphProcessor getGraphProcessor() {
        return this;
    }

    public AppxSearchPlugin getApproximateSearch() {
        return approximateSearch;
    }

    public void setApproximateSearch(AppxSearchPlugin approximateSearch) {
        this.approximateSearch = approximateSearch;
    }

}
