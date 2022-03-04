package fr.inria.corese.core.print;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.sparql.triple.parser.NSManager;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Turtle & Trig Format
 * 
 * Olivier Corby, Wimmics INRIA 2013
 */
public class TripleFormat extends RDFFormat {
    public static boolean DISPLAY_GRAPH_KEYWORD = false;

    static final String PREFIX = "@prefix";
    static final String PV = " ;";
    static final String DOT = " .";
    static final String OPEN = "<";
    static final String CLOSE = ">";
    static final String GRAPH = "graph";
    static final String OGRAPH = "{";
    static final String CGRAPH = "}";

    boolean isGraph = false;
    // when true:  display default graph kg:default with embedding graph kg:default {}
    // when false: display default graph in turtle (without graph kg:default {})
    private boolean displayDefaultGraphURI = false;
    // true when this pretty print is for a future translation into sparql select where
    private boolean graphQuery = false;
    private Mappings mappings;
    int tripleCounter = 0;

    TripleFormat(Graph g, NSManager n) {
        super(g, n);
    }

    public static TripleFormat create(Graph g, NSManager n) {
        return new TripleFormat(g, n);
    }

    public static TripleFormat create(Mappings map) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {            
            return create(g, getNSM(map)).setMappings(map);
        }
        return create(Graph.create()).setMappings(map);
    }

    public static TripleFormat create(Graph g) {
        return new TripleFormat(g, nsm());
    }

    public static TripleFormat create(Mappings map, boolean isGraph) {
        Graph g = (Graph) map.getGraph();
        if (g != null) {            
            TripleFormat t = new TripleFormat(g, getNSM(map));
            t.setGraph(isGraph);
            return t.setMappings(map);
        }
        return create(Graph.create()).setMappings(map);
    }
    
    static NSManager getNSM(Mappings map) {
        Query q = map.getQuery();
        if (q == null) {
            return nsm();
        }
        return  q.getAST().getNSM();
    }

    // isGraph = true -> Trig
    public static TripleFormat create(Graph g, boolean isGraph) {
        TripleFormat t = new TripleFormat(g, nsm());
        t.setGraph(isGraph);
        return t;
    }

    public void setGraph(boolean b) {
        isGraph = b;
    }
    
    @Override
    public String toString() {
        StringBuilder bb = getStringBuilder();
        return bb.toString();
    }
    
    public String toString(Node node) {
        StringBuilder bb = getStringBuilder(node);
        return bb.toString();
    }

    @Override
    public StringBuilder getStringBuilder() {
         return getStringBuilder(null);
    }
    
    public StringBuilder getStringBuilder(Node node) {
        sb = new StringBuilder();
        if (graph == null && map == null) {
            return sb;
        }
        
        if (node != null) {
            print(null, node);
        }
        else if (isGraph) {
            graphNodes();
        } else {
            nodes();
        }

        StringBuilder bb = new StringBuilder();
        header(bb);
        bb.append(NL);
        bb.append(NL);
        bb.append(sb);
        return bb;
    }

    // iterate on subject nodes and pprint their edges
    void nodes() {
        for (Node node : getSubjectNodes()) {
            if (tripleCounter > getNbTriple()) {
                break;
            }
            print(null, node);
        }
    }

    // iterate named graph nodes and pprint their content
    void graphNodes() {
        // start by default graph
        graphNodes(graph.getDefaultGraphNode());
        
        for (Node gNode : graph.getGraphNodes()) {
            if (tripleCounter > getNbTriple()) {
                break;
            }
            if (! graph.isDefaultGraphNode(gNode)) {
                graphNodes(gNode);
            }
        }
    }
    
    void graphNodes(Node gNode) {
        if (accept(gNode)) {
            if (graph.isDefaultGraphNode(gNode) && !isDisplayDefaultGraphURI()) {
                basicGraphNode(gNode);

            } else {
                graphNode(gNode);
            }
        }
    }
    
    void graphNodes2() {
        for (Node gNode : graph.getGraphNodes()) {
            if (tripleCounter > getNbTriple()) {
                break;
            }
            if (accept(gNode)) {
                if (graph.isDefaultGraphNode(gNode) && ! isDisplayDefaultGraphURI()) {
                    basicGraphNode(gNode);

                } else {
                    graphNode(gNode);
                }
            }
        }
    }
    
    // pprint content of named graph with trig syntax: uri { }
    void graphNode(Node gNode) {
        if (DISPLAY_GRAPH_KEYWORD || isGraphQuery()) {
            // isGraphQuery() : trig format for AST query graph pattern
            sdisplay(GRAPH);
            sdisplay(SPACE);
        }
        node(gNode);
        sdisplay(SPACE);
        sdisplay(OGRAPH);
        display();

        basicGraphNode(gNode);

        display(CGRAPH);
        display();
    }
    
    // pprint content of named graph
    void basicGraphNode(Node gNode) {         
        for (Node node : graph.getNodeGraphIterator(gNode)) {
            print(gNode, node.getNode());
        }
    }
   
    @Override
    void header(StringBuilder bb) {
        link(bb);
        bb.append(nsm.toString(PREFIX, false, false));
    }
    
    void link(StringBuilder bb) {
        if (getMappings() != null && !getMappings().getLinkList().isEmpty()) {
            bb.append("#").append(NL);

            for (String link : getMappings().getLinkList()) {
                bb.append("# link href = ").append(link).append(NL);
            }

            bb.append("#").append(NL);
        }
    }

    // pprint edges where node is subject
    // when isGraph == true consider edges in named graph gNode
    // otherwise consider all edges
    void print(Node gNode, Node node) {
        boolean first = true;
        boolean annotation = false;
        
        for (Edge edge : getEdges(gNode, node)) {
            if (edge != null && accept(edge) && edge.isAsserted()) {
                // isAsserted() == true is the general case 
                // false means rdf star nested triple
                // pprinted as subject ot object of an asserted triple
                if (tripleCounter++ > getNbTriple()) {
                    break;
                }
                if (first) {
                    first = false;
                    subject(edge);
                    sdisplay(SPACE);
//                    if (annotation(edge)) {
//                        annotation = true;
//                        sdisplay("{| ");
//                    }
                } else {
                    sdisplay(PV);
                    sdisplay(NL);
                }
                edge(edge);
            }
        }
//        if (annotation){
//            sdisplay(" |}");
//        }

        if (!first) {
            sdisplay(DOT);
            sdisplay(NL);
            sdisplay(NL);
        }
    }

    // iterate edges where node is subject
    // when isGraph == true consider edges in gNode named graph
    Iterable<Edge> getEdges(Node gNode, Node node) {
        if (isGraph) {
            return graph.getNodeEdges(gNode, node);
        } else {
            return graph.getNodeEdges(node);
        }
    }

    void subject(Edge ent) {
        node(ent.getSubjectValue());
    }

       
    void predicate(Node node) {
        String pred = nsm.toPrefix(node.getLabel());
        sdisplay(pred);
    }
    
    void node(Node node) {
        node(node, false);
    }
    
    void node(Node node, boolean rec) {
        IDatatype dt = node.getValue();
        if (dt.isTripleWithEdge()) {
            // rdf star nested triple
            nestedTriple(node, dt.getEdge(), rec);
        }
        else if (dt.isLiteral()) {
            sdisplay(dt.toSparql(true, false, nsm));
        } else if (dt.isBlank()) {
            sdisplay(dt.getLabel());
        } else {
            uri(dt.getLabel());
        }
    }
       
    // node is triple reference of edge
    // node is subject/object
    void triple(Node node, Edge edge) {
        triple(node, edge, false);
    }

    void triple(Node node, Edge edge, boolean rec) {
        nestedTriple(node, edge, rec);
    }

    // node is triple reference of edge
    // node is subject/object
    void nestedTriple(Node node, Edge edge, boolean rec) {
        sdisplay("<<");
        basicTriple(node, edge, rec);
        sdisplay(">>");
    }    

    void basicTriple(Node node, Edge edge, boolean rec) {
        node(edge.getSubjectNode(), true);
        sdisplay(SPACE);
        predicate(edge.getEdgeNode());
        sdisplay(SPACE);
        node(edge.getObjectNode(), true);
    }
    
            
//    void triple2(Node node, Edge edge, boolean rec) {
//        if (edge.isNested() || hasNestedTriple(edge) || rec) {
//            nestedTriple(node, edge, rec);
//        } else {
//            basicTriple(node, edge, rec);
//        }
//    }
//    
    
//    void basicTriple(Node node, Edge edge) {
//        basicTriple(node, edge, false);
//    }
    
    boolean hasNestedTriple(Edge edge) {
        return edge.getSubjectValue().isTripleWithEdge() || edge.getObjectValue().isTripleWithEdge();
    }

    void uri(String label) {
        sdisplay(nsm.toPrefixURI(label));
    }

    @Override
    void edge(Edge edge) {        
        predicate(edge.getEdgeNode());
        sdisplay(SPACE);
        // object triple node displayed with << >>
        node(edge.getObjectNode(), true);
    }
    
    boolean annotation(Edge edge) {
        return annotation(edge.getSubjectNode());
    }
    
    boolean annotation(Node node) {
        return node.isTripleWithEdge() && 
                node.getEdge().isAsserted() && 
                ! hasNestedTriple(node.getEdge());
    }

    public Mappings getMappings() {
        return mappings;
    }

    public TripleFormat setMappings(Mappings mappings) {
        this.mappings = mappings;
        return this;
    }
    
    @Override
    public TripleFormat setNbTriple(int nbTriple) {
        super.setNbTriple(nbTriple);
        return this;
    }

    public boolean isDisplayDefaultGraphURI() {
        return displayDefaultGraphURI;
    }

    public TripleFormat setDisplayDefaultGraphURI(boolean displayDefaultGraphURI) {
        this.displayDefaultGraphURI = displayDefaultGraphURI;
        return this;
    }

    public boolean isGraphQuery() {
        return graphQuery;
    }

    public TripleFormat setGraphQuery(boolean graphQuery) {
        this.graphQuery = graphQuery;
        return this;
    }

}
