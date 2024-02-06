package com.i3s.app.costGP;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.topbraid.spin.system.SPINModuleRegistry;

/*
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;
*/
/**
 * @author NGUYEN Thu Huong Converts between textual SPARQL representation and
 *         SPIN RDF model.
 */

public class SPARQL2SPINConverter {
	
	public static String readFile(String path, Charset encoding) {
		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new String(encoded, encoding);
	}

	public static void main(String[] args) {
		// Register system functions
		SPINModuleRegistry.get().init();

		// Create an empty OntModel
		final Model model = ModelFactory.createDefaultModel();

		// FileChooser UI
		JFrame frame = new JFrame("SPARQL2SPIN Converter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = frame.getContentPane();

		JFileChooser fileChooser = new JFileChooser(".");
		fileChooser.setControlButtonsAreShown(false);
		contentPane.add(fileChooser, BorderLayout.CENTER);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				model.removeAll();
				//JFileChooser theFileChooser = (JFileChooser) actionEvent.getSource();
				String command = actionEvent.getActionCommand();
				if (command.equals(JFileChooser.APPROVE_SELECTION)) {
					//File selectedFile = theFileChooser.getSelectedFile();

					// read rule
					//String query = readFile(selectedFile.getAbsolutePath(), Charset.defaultCharset());

					// Query arqQuery = ARQFactory.get().createQuery( model, query );
					// ARQ2SPIN arq2SPIN = new ARQ2SPIN( model );
					// Select sparqlQuery = (Select) arq2SPIN.createQuery( arqQuery, null );

					// System.out.println( "SPARQL Query:\n" + sparqlQuery );
					System.out.println("\nSPIN Representation:");
					model.write(System.out, FileUtils.langTurtle);
				}
			}
		};

		fileChooser.addActionListener(actionListener);

		frame.pack();
		frame.setVisible(true);
	}
}