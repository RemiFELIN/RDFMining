package fr.inria.corese.compiler.federate;

import fr.inria.corese.sparql.triple.parser.ASTQuery;
import fr.inria.corese.sparql.triple.parser.BasicGraphPattern;
import fr.inria.corese.sparql.triple.parser.Exp;
import fr.inria.corese.sparql.triple.parser.Expression;
import fr.inria.corese.sparql.triple.parser.Triple;
import fr.inria.corese.sparql.triple.parser.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class SelectorFilter {
    static HashMap<String, Boolean> map;
    static String[] ope = {"=", "regex", "contains", "strstarts"};
    
    ASTQuery ast;
    ArrayList<BasicGraphPattern> res;
    
    static {
        init();
    }
    
    SelectorFilter(ASTQuery ast) {
        this.ast = ast;
        res = new ArrayList<>();
    }
    
    static void init() {
        map = new HashMap<>();
        for (String ope : ope) {
            map.put(ope, true);
        }
    }
    
    /**
     * Return list of all triples with possibly filters bound by triples
     * One BGP per triple/filters
     * Focus on filter (exp = exp)
     */
    List<BasicGraphPattern> process() {
        process(ast);       
        return res;
    }
    
    void process(ASTQuery ast) {
        for (Expression exp : ast.getModifierExpressions()) {
            process(exp);
        }
        process(ast.getBody());
    }
    
    
    void process(Exp body) {
        if (body.isBGP()) {
            processBGP(body);
        }
        else if (body.isQuery()) {
            process(body.getAST());
        }
        else for (Exp exp : body) {
            process(exp);
        }
    }
    
    void processBGP(Exp body) {
        for (Exp exp : body) {
            if (exp.isFilter() || exp.isBind()) {
                process(exp.getFilter());
            }
            else if (exp.isTriple()) {
                process(exp.getTriple(), body);
            } else {
                process(exp);
            }
        }
    }
    
    void process (Expression exp) {
        if (exp.isTermExist()) {
            process(exp.getTerm().getExistBGP());
        }
        else if (exp.isTerm()) {
            for (Expression e : exp.getArgs()) {
                process(e);
            }
        }
    }
        
    void process(Triple t, Exp body) {
        BasicGraphPattern bgp = ast.bgp(t);
        res.add(bgp);
        List<Variable> list = t.getVariables();
        if (!list.isEmpty()) {
            for (Exp exp : body) {
                if (exp.isFilter() && accept(exp.getFilter())) {
                    if (exp.getFilter().isBound(list)) {
                        bgp.add(exp);
                    }
                }
            }
        }
    }
    
    boolean accept(Expression exp) {
        Boolean b = map.get(exp.getName().toLowerCase());
        return b!= null && b;
    }
    
    
}
