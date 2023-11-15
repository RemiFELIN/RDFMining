package fr.inria.corese.server.webservice;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.GraphStore;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.storage.api.dataManager.DataManager;
import fr.inria.corese.core.workflow.Data;
import fr.inria.corese.core.workflow.ResultProcess;
import fr.inria.corese.core.workflow.SemanticWorkflow;
import fr.inria.corese.core.workflow.WorkflowParser;
import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.sparql.exceptions.EngineException;
import fr.inria.corese.sparql.triple.parser.Access.Level;
import fr.inria.corese.sparql.triple.parser.Context;
import fr.inria.corese.sparql.triple.parser.Dataset;
import fr.inria.corese.sparql.triple.parser.URLParam;

/**
 *
 * Workflow Engine, "independent" of Web server
 * 
 * @author Olivier Corby, Wimmics INRIA I3S, 2017
 *
 */
public class TransformerEngine {

    private static Logger logger = LogManager.getLogger(Transformer.class);
    private static final String PARAM = "$param";
    private static final String MODE = "$mode";

    // TripleStore RDF Graph
    private GraphStore graph;
    private DataManager dataManager;
    // Profile RDF graph: profile, server and workflow definitions
    GraphStore profile;
    // Context shared by Workflow and transformations
    private Context context;
    private EventManager eventManager;
    // Web service parameters
    Param param;

    private boolean debug = false;

    /**
     * 
     * @param graph   RDF graph to be processed
     * @param profile RDF graph specifies workflow to be executed on graph
     * @param param   service Param and Context
     * 
     *                Workflow URI/bnode IDatatype dt =
     *                param.getContext().get(Context.STL_WORKFLOW) // st:workflow
     *                Workflow node = profile.getNode(dt)
     */
    public TransformerEngine(GraphStore graph, GraphStore profile, Param param) {
        this.graph = graph;
        this.profile = profile;
        this.param = param;
        init();
    }

    void init() {
        setContext(create(param));
        complete(getGraph(), profile, context);
    }

    /**
     * Create and run a Workflow
     * Workflow result is data.stringValue();
     */
    public Data process() throws EngineException, LoadException {
        Dataset ds = createDataset(param.getFrom(), param.getNamed());
        SemanticWorkflow sw = workflow(getContext(), ds, profile);
        // sw.setDebug(isDebug());
        if (isDebug()) {
            logger.info("Run workflow");
            // graph.setVerbose(true);
        }
        if (getEventManager() != null) {
            getEventManager().call(sw.getContext());
        }
        Data data = sw.process(new Data(getGraph(), getDataManager()));
        return data;
    }

    /**
     * Create a Workflow to process service
     * If there is no explicit workflow
     * specification, i.e no st:workflow [ ] create a Workflow with
     * query/transform.
     */
    SemanticWorkflow workflow(Context context, Dataset dataset, Graph profile) throws LoadException {
        SemanticWorkflow wp = new SemanticWorkflow();
        wp.setContext(context);
        wp.setDataset(dataset);
        // logger.info("Workflow Context:\n" + context);
        wp.setLog(true);
        IDatatype swdt = context.get(Context.STL_WORKFLOW);
        IDatatype querydt = context.get(Context.STL_QUERY);
        String query = (querydt == null) ? null : querydt.stringValue();
        String transform = context.getTransform();
        Level level = param.getLevel();
        boolean isUserQuery = param.isUserQuery();
        // System.out.println("TE: key " + param.getKey() + " " + level);
        // boolean isRestricted = false; //param.isRestricted() &&
        // context.hasValue(Context.STL_RESTRICTED, DatatypeMap.TRUE);
        // Access.Level level = Access.getQueryAccessLevel(isUserQuery, isRestricted);
        logger.info("Parse workflow: " + swdt);
        if (swdt != null) {
            // there is a workflow
            logger.info("Parse workflow: " + swdt.getLabel());
            WorkflowParser parser = new WorkflowParser(wp, profile);
            parser.setServerMode(true);
            // parser.setDebug(true);
            parser.setProcessor(new Translator());
            parser.parseWE(profile.getNode(swdt));
            query = getQuery(wp, query);
            if (query != null) {
                logger.info("Workflow query: " + query);
                wp.addQuery(query, 0, isUserQuery, level);
            }
        }
        // there is no workflow
        else if (query != null) {
            if (transform == null) {
                logger.info("SPARQL endpoint");
                wp.addQueryMapping(query, isUserQuery, level);
                wp.add(new ResultProcess(context.stringValue(Context.STL_FORMAT)));
                return wp;
            } else {
                // select where return Graph Mappings
                logger.info("Transformation: " + transform);
                wp.addQueryGraph(query, isUserQuery, level);
            }
        }
        defaultTransform(wp, transform);
        return wp;
    }

    String getQuery(SemanticWorkflow wp, String query) {
        if (query == null) {
            return getQuery(wp);
        }
        return query;
    }

    /**
     * Try to retrieve query from context graph using uri
     */
    String getQuery(SemanticWorkflow wp) {
        if (context.hasValue(Context.STL_URI)
                && (context.hasValue(Context.STL_MODE) || 
                context.hasValue(Context.STL_PARAM))) {
            // URI of query in context graph (use case: tutorial)
            // with query parameter mode/param (this means we want to execute the query)
            IDatatype qdt = wp.getContextParamValue(context.get(Context.STL_URI), Context.STL_QUERY);
            logger.info("st:uri " + qdt);
            if (qdt == null) {
                return null;
            }
            // try st:value in workflow graph
            IDatatype qdtval = wp.getContextParamValue(qdt, Context.STL_VALUE);
            //logger.info("st:value " + qdtval);
            if (qdtval == null) {
                context.set(Context.STL_QUERY, qdt);
                return qdt.stringValue();
            }
            else {
                context.set(Context.STL_QUERY, qdtval);
                return qdtval.stringValue();
            }
        }
        return null;
    }

    String getQuery2(SemanticWorkflow wp) {
        if (context.hasValue(Context.STL_URI)
                && (context.hasValue(Context.STL_MODE) || context.hasValue(Context.STL_PARAM))) {
            // URI of query in context graph (use case: tutorial)
            // with query parameter mode/param (this means we want to execute the query)
            IDatatype qdt = wp.getContextParamValue(context.get(Context.STL_URI), Context.STL_QUERY);
            if (qdt != null) {
                context.set(Context.STL_QUERY, qdt);
                return qdt.stringValue();
            }
        }
        return null;
    }

    /**
     * If transform = null and workflow does not end with transform: use
     * st:sparql as default transform
     */
    void defaultTransform(SemanticWorkflow wp, String transform) {
        boolean isDefault = false;
        if (transform == null && !wp.hasTransformation() && !wp.hasResult()) {
            isDefault = true;
            transform = fr.inria.corese.core.transform.Transformer.SPARQL;
        }
        if (transform != null) {
            logger.info("Default transformation: " + transform);
            wp.addTemplate(transform, isDefault);
            wp.getContext().setTransform(transform);
            wp.getContext().set(Context.STL_DEFAULT, true);
        }
    }

    Context create(Param par) {
        Context ctx = par.createContext();
        complete(ctx, par);
        return ctx;
    }

    Context complete(Context c, Param par) {
        if (par.isAjax()) {
            c.setProtocol(Context.STL_AJAX);
            c.export(Context.STL_PROTOCOL, c.get(Context.STL_PROTOCOL));
        }
        completeParam(c, par);
        return c;
    }
    
    // process param=sv:key~val;sv:key~val
    void completeParam(Context c, Param par) {
        String param = par.getParam();
        if (param!=null && param.contains(URLParam.SEPARATOR) && param.contains(URLParam.SERVER)) {
            c.mode(c, URLParam.PARAM, param);
        }
    }

    void complete(GraphStore graph, GraphStore profile, Context context) {
        Graph cg = graph.getNamedGraph(Context.STL_CONTEXT);
        if (cg != null) {
            context.set(Context.STL_CONTEXT, DatatypeMap.createObject(Context.STL_CONTEXT, cg));
        }
        // st:param [ st:contextlist (st:geo) ]
        // extended named graph st:geo from initial dataset to be stored in context by
        // st:set(st:geo, gg)
        IDatatype list = context.get(Context.STL_CONTEXT_LIST);
        if (list != null) {
            for (IDatatype name : list.getValues()) {
                Graph gg = graph.getNamedGraph(name.stringValue());
                if (gg != null) {
                    context.set(name, DatatypeMap.createObject(name.stringValue(), gg));
                }
            }
        }
        context.set(Context.STL_DATASET, DatatypeMap.createObject(Context.STL_DATASET, graph));
        context.set(Context.STL_SERVER_PROFILE, DatatypeMap.createObject(Context.STL_SERVER_PROFILE, profile));
    }

    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris) {
        return createDataset(defaultGraphUris, namedGraphUris, null);
    }

    private Dataset createDataset(List<String> defaultGraphUris, List<String> namedGraphUris, Context c) {
        if (c != null
                || ((defaultGraphUris != null) && (!defaultGraphUris.isEmpty()))
                || ((namedGraphUris != null) && (!namedGraphUris.isEmpty()))) {
            Dataset ds = Dataset.instance(defaultGraphUris, namedGraphUris);
            ds.setContext(c);
            return ds;
        } else {
            return null;
        }
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
     * @return the debug
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public GraphStore getGraph() {
        return graph;
    }

    public void setGraph(GraphStore graph) {
        this.graph = graph;
    }

}
