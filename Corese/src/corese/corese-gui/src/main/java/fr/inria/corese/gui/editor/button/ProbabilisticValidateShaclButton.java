package fr.inria.corese.gui.editor.button;

import fr.inria.corese.core.Graph;
import fr.inria.corese.core.load.Load;
import fr.inria.corese.core.load.LoadException;
import fr.inria.corese.core.shacl.Shacl;
import fr.inria.corese.core.transform.Transformer;
import fr.inria.corese.gui.core.MainFrame;
import fr.inria.corese.gui.editor.pane.EditorPane;
import fr.inria.corese.gui.editor.pane.ResultPane;
import fr.inria.corese.sparql.exceptions.EngineException;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * SHACL Validate Button : validate SHACL Shapes using probabilistic approach
 * @author Rémi FELIN
 */
public class ProbabilisticValidateShaclButton extends Button {

    private EditorPane editorPane;
    private ResultPane resultPane;
    private MainFrame mainFrame;

    public ProbabilisticValidateShaclButton(EditorPane editorPane, ResultPane resultPane, final MainFrame mainFrame) {
        super("Prob. Validate");
        this.editorPane = editorPane;
        this.resultPane = resultPane;
        this.mainFrame = mainFrame;
    }

    @Override
    protected ActionListener action() {

        ActionListener buttonProbValidateListener = e -> {

            String editorShaclContent = editorPane.getContent();
            Graph coreseGraph = mainFrame.getMyCorese().getGraph();

            // Test if empty
            if (editorShaclContent.strip().isEmpty()) {
                resultPane.setContent("Error : SHACL document is empty.");
                return;
            }

            // Try to load SHACL file
            Graph shapeGraph = Graph.create();
            Load ld = Load.create(shapeGraph);
            try {
                ld.loadString(editorShaclContent, Load.TURTLE_FORMAT);
            } catch (LoadException e1) {
                resultPane.setContent("Error : malformed SHACL document.");
                e1.printStackTrace();
                return;
            }

            // Eval
            Shacl shacl = new Shacl(coreseGraph, shapeGraph);
            Graph result;
            try {
                // run the probabilistic evaluation
                result = shacl.eval(Shacl.PROBABILISTIC_MODE);
            } catch (EngineException e2) {
                resultPane.setContent("Error : engine exception.");
                e2.printStackTrace();
                return;
            }

            // Format and export result
            Transformer transformer = Transformer.create(result, Transformer.TURTLE);
            resultPane.setContent(transformer.toString());
        };
        return buttonProbValidateListener;
    }
}
