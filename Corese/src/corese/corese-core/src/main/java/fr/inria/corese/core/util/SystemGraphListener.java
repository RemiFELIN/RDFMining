package fr.inria.corese.core.util;

import fr.inria.corese.sparql.api.IDatatype;
import fr.inria.corese.kgram.api.core.Edge;
import fr.inria.corese.kgram.api.core.ExpType;
import fr.inria.corese.kgram.core.Mappings;
import fr.inria.corese.kgram.core.Query;
import fr.inria.corese.core.api.GraphListener;
import fr.inria.corese.core.Graph;
import fr.inria.corese.kgram.api.core.Edge;

/**
 * Listener for the named graph kg:system
 * Tune the system:
 * insert data { graph kg:system { 
 *   kg:kgram kg:skolem true 
 * }}
 * 
 * @author Olivier Corby, Wimmics Inria I3S, 2013
 *
 */
class SystemGraphListener implements GraphListener {
    
    private static final String SKOLEM  = ExpType.KGRAM + "skolem";
    private static final String LISTEN  = ExpType.KGRAM + "listen";
    private static final String HELP    = ExpType.KGRAM + "help";
    private static final String STORE   = ExpType.KGRAM + "store";

    Graph graph;
    DefaultGraphListener gl;
    
    
    SystemGraphListener(Graph g){
        graph = g;
    }

        @Override
        public void addSource(Graph g) {

        }

        @Override
        public boolean onInsert(Graph g, Edge ent) {
            return true;
        }

        @Override
        public void insert(Graph g, Edge ent) {
            System.out.println("** Listen: " + ent);
            process(ent);
        }
        
       
    

        @Override
        public void delete(Graph g, Edge ent) {
        }

        @Override
        public void start(Graph g, Query q) {
        }

        @Override
        public void finish(Graph g, Query q, Mappings m) {
        }

    @Override
    public void load(String path) {
    }
    
    
    
    
     /**
         * Some edges may tune kgram:
         * kg:kgram kg:skolem true
         */
        void process(Edge edge){
            String subject = edge.getNode(0).getLabel();
            String predicate = edge.getLabel();
            IDatatype dt =  edge.getNode(1).getValue();

            if (predicate.equals(SKOLEM)){
                graph.setSkolem(dt.booleanValue());
            }
            else if (predicate.equals(LISTEN)){
                if (dt.booleanValue()){
                    init();
                }
                else if (gl != null){
                    graph.removeListener(gl);
                }
            }
            else if (subject.equals(LISTEN)){
                init();
                gl.setProperty(predicate, dt.booleanValue());
            }           
            else if (subject.equals(STORE)){
                init();
                gl.setProperty(predicate, dt.booleanValue());
            }
            else if (predicate.equals(HELP)){
                init();
                gl.help();
            }
        }
    
    
        
        void init(){
            if (gl == null){
                gl = DefaultGraphListener.create();
                graph.addListener(gl);
            }
        }
        
    }