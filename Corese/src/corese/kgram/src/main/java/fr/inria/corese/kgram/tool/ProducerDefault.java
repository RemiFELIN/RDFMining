package fr.inria.corese.kgram.tool;

import fr.inria.corese.kgram.api.core.DatatypeValue;
import fr.inria.corese.kgram.api.core.DatatypeValueFactory;
import java.util.ArrayList;
import java.util.List;

import fr.inria.corese.kgram.api.core.Graph;
import fr.inria.corese.kgram.api.core.Node;
import fr.inria.corese.kgram.api.core.Regex;
import fr.inria.corese.kgram.api.query.Environment;
import fr.inria.corese.kgram.api.query.Producer;
import fr.inria.corese.kgram.core.Exp;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.kgram.api.core.Edge;

/**
 *
 * @author corby
 *
 */
public class ProducerDefault implements Producer {

    int mode = Producer.DEFAULT;
    Node graphNode;

    public void setMode(int n) {
        mode = n;
    }

    @Override
    public Iterable<Edge> getEdges(Node node, List<Node> from, Edge edge,
            Environment env) {
        // TODO Auto-generated method stub
        ArrayList<Edge> list = new ArrayList<Edge>();
        //list.add( EntityImpl.create(null, edge));
        return list;
    }

    @Override
    public Iterable<Edge> getEdges(Node gNode, List<Node> from, Edge edge, Environment env, Regex exp,
            Node src, Node start,
            int index) {
        // TODO Auto-generated method stub
        return new ArrayList<Edge>();
    }

    @Override
    public Iterable<Node> getGraphNodes(Node node, List<Node> from,
            Environment env) {
        // TODO Auto-generated method stub
        return new ArrayList<Node>();
    }

    @Override
    public void init(Query q) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initPath(Edge edge, int index) {
        // TODO Auto-generated method stub

    }

    @Override
    public Node getNode(Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Node> toNodeList(Object obj) {
        // TODO Auto-generated method stub
        return new ArrayList<Node>();
    }

    @Override
    public Mappings map(List<Node> nodes, Object object) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isGraphNode(Node node, List<Node> from, Environment env) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isBindable(Node node) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Iterable<Node> getNodes(Node gNode, List<Node> from, Edge edge, Environment env, List<Regex> exp, int index) {
        return new ArrayList<Node>();
    }

    @Override
    public boolean isProducer(Node node) {
        return false;
    }

    @Override
    public Producer getProducer(Node node, Environment env) {
        return null;
    }

    @Override
    public Query getQuery() {
        return null;
    }

    @Override
    public Graph getGraph() {
        return null;
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void setGraphNode(Node n) {
        graphNode = n;
    }

    @Override
    public Node getGraphNode() {
        return graphNode;
    }

    @Override
    public Mappings getMappings(Node gNode, List<Node> from, Exp exp, Environment env) {
        //create a new Mappings: empty
        Mappings maps = new Mappings();
        return maps;
    }

    @Override
    public Object getValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatatypeValue getDatatypeValue(Object value) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Edge copy(Edge ent) {
        return ent;
    }

    @Override
    public void close() {
    }

    ;

    @Override
    public void start(Query q) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void finish(Query q) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatatypeValueFactory getDatatypeValueFactory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
