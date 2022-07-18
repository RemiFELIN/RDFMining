package fr.inria.corese.storage.jenatdb1;

import java.util.ArrayList;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Quad;

import fr.inria.corese.core.NodeImpl;
import fr.inria.corese.core.edge.EdgeImpl;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.sparql.datatype.DatatypeMap;
import fr.inria.corese.storage.jenatdb1.convertDatatype.CoreseDatatypeToJenaRdfNode;
import fr.inria.corese.storage.jenatdb1.convertDatatype.JenaRdfNodeToCoreseDatatype;

public class ConvertJenaCorese {

    // Factories
    private static Model jena_factory = ModelFactory.createDefaultModel();

    // Defaults contexts
    private static Node corese_default_context = DatatypeMap.createResource(ExpType.DEFAULT_GRAPH);
    private static org.apache.jena.graph.Node jena_default_context = Quad.defaultGraphIRI;

    /****************************
     * Node : Jena to Corese *
     ****************************/

    /**
     * Convert a Jena node to a Corese node.
     * 
     * @param jena_node Jena node to convert.
     * @return Equivalent Corese node or null if Jena node is null.
     */
    public static Node JenaNodeToCoreseNode(org.apache.jena.graph.Node jena_node) {
        if (jena_node == null) {
            return null;
        }

        return NodeImpl.create(JenaRdfNodeToCoreseDatatype.convert(jena_factory.asRDFNode(jena_node)));
    }

    /****************************
     * Node : Corese to Jena *
     ****************************/

    /**
     * Convert a Corese node to a Jena node .
     * 
     * @param corese_node Corese node to convert.
     * @return Equivalent Jena node or null if Corese node is null.
     */
    public static org.apache.jena.graph.Node coreseNodeToJenaNode(Node corese_node) {
        if (corese_node == null) {
            return null;
        }
        return CoreseDatatypeToJenaRdfNode.convert(corese_node.getDatatypeValue()).asNode();
    }

    /*****************************
     * Context : Corese to Jena *
     *****************************/

    /**
     * Convert a Corese context to a Jena node.
     * 
     * @param corese_context Corese context to convert.
     * @return Equivalent Jena node.
     */
    public static org.apache.jena.graph.Node coreseContextToJenaContext(Node corese_context) {

        if (corese_context == null) {
            return null;
        } else if (corese_context.equals(corese_default_context)) {
            return jena_default_context;
        } else {
            return ConvertJenaCorese.coreseNodeToJenaNode(corese_context);
        }
    }

    /*****************************
     * Context : Jena to Corese *
     *****************************/

    /**
     * Convert a jena context to a Corese context.
     * 
     * @param jena_context Jena context to convert.
     * @return Equivalent Corese context.
     */
    public static Node jenaContextToCoreseContext(org.apache.jena.graph.Node jena_context) {

        if (jena_context == null) {
            return null;
        } else if (jena_context.equals(jena_default_context)) {
            return corese_default_context;
        } else {
            return ConvertJenaCorese.JenaNodeToCoreseNode(jena_context);
        }
    }

    /*************************
     * Quad : Corese to Jena *
     *************************/

    /**
     * Convert Corese edge to equivalent Jena quad.
     * 
     * @param edge Corese edge to convert.
     * @return Equivalent Jena quad.
     */
    public static Quad edgeToQuad(Edge edge) {
        org.apache.jena.graph.Node subject_jena = ConvertJenaCorese.coreseNodeToJenaNode(edge.getNode(0));
        org.apache.jena.graph.Node predicate_jena = ConvertJenaCorese.coreseNodeToJenaNode(edge.getEdgeNode());
        org.apache.jena.graph.Node object_jena = ConvertJenaCorese.coreseNodeToJenaNode(edge.getNode(1));
        org.apache.jena.graph.Node context_jena = ConvertJenaCorese.coreseContextToJenaContext(edge.getGraph());

        return new Quad(context_jena, subject_jena, predicate_jena, object_jena);
    }

    /*************************
     * Quad : Jena to Corese *
     *************************/

    /**
     * Convert jena quad to equivalent Corese edge.
     * 
     * @param quad Jena quad to convert.
     * @return Equivalent Corese edge.
     */
    public static Edge quadToEdge(Quad quad) {
        Node subject_corese = ConvertJenaCorese.JenaNodeToCoreseNode(quad.getSubject());
        Node predicate_corese = ConvertJenaCorese.JenaNodeToCoreseNode(quad.getPredicate());
        Node object_corese = ConvertJenaCorese.JenaNodeToCoreseNode(quad.getObject());
        Node context_corese = ConvertJenaCorese.jenaContextToCoreseContext(quad.getGraph());

        return EdgeImpl.create(context_corese, subject_corese, predicate_corese, object_corese);
    }

    /**
     * Convert a list of Jena quad to equivalent list of Corese edge.
     * 
     * @param jena_quad_list List of Jena quad to convert.
     * @return Equivalent list of Corese edge.
     */
    public static Iterable<Edge> quadsToEdges(Iterable<Quad> jena_quad_list) {

        ArrayList<Edge> corese_edge_list = new ArrayList<>();
        for (Quad jena_quad : jena_quad_list) {
            corese_edge_list.add(ConvertJenaCorese.quadToEdge(jena_quad));
        }
        return corese_edge_list;
    }

}
