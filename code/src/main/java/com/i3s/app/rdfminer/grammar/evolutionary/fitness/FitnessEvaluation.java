package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.jena.atlas.lib.Timer;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.launcher.LaunchWithGE;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import Individuals.FitnessPackage.BasicFitness;
import Mapper.Symbol;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * FitnessEvaluation - is the class to setup the fitness value for Axioms in the
 * specified population
 *
 * @author NGUYEN Thu Huong Oct.18
 */
public class FitnessEvaluation {
	private static Logger logger = Logger.getLogger(FitnessEvaluation.class.getName());

	protected Axiom a;
	protected long t0, t;
	// protected int numSuccessAxioms=0;
	double referenceCardinality = 0.0;
	double possibility = 0.0;
	double necessity = 0.0;
	double generality = 0.0;
	double complexity_penalty = 0.0;

	public void update(ArrayList<GEIndividual> population, int curGeneration, int totalGeneration, WritableSheet sheet)
			throws JAXBException, IOException, SQLException, Exception {
		int i = 0;
		SparqlEndpoint endpoint;
		if (sheet != null) {
			endpoint = new SparqlEndpoint(Global.SPARQL_ENDPOINT, LaunchWithGE.PREFIXES);
			logger.info("Evaluating axioms against to the RDF Data of the whole DBPedia");
		} else {
			endpoint = RDFMiner.endpoint;
		}
		String arg1 = "";
		String arg2 = "";
		String axiom = "";
		double f = 0;
		int popSize = population.size();
		int Row = 1;
		Timer timer = new Timer();
		long finalTime = 0;
		while (i < popSize) {
			timer.startTimer();
			GEIndividual indivi = (GEIndividual) population.get(i);
			if (population.get(0).getPhenotype() == null)
				break;
			axiom = population.get(i).getPhenotype().getStringNoSpace();
			// logger.info(" ");
			// logger.info(axiom);
			if (indivi.isMapped()) {
				try {
					a = AxiomFactory.create(indivi.getPhenotype(), endpoint);
					List<List<Symbol>> arguments = a.argumentClasses;
					List<String> complexClass1 = new ArrayList<String>();
					int SizeArg0 = arguments.get(0).size();
					for (int k = 0; k < SizeArg0; k++) {
						arg1 = arguments.get(0).get(k).getSymbolString();
						if (!arg1.equals("(") && !arg1.equals(")") && !arg1.equals(" ")) {
							complexClass1.add(arg1);
						}
					}

					List<String> complexClass2 = new ArrayList<String>();
					int SizeArg1 = arguments.get(1).size();
					for (int l = 0; l < SizeArg1; l++) {
						arg2 = arguments.get(1).get(l).getSymbolString();
						if (!arg2.equals("(") && !arg2.equals(")") && !arg2.equals(" ")) {
							complexClass2.add(arg2);
						}
					}

				} // try
				catch (QueryExceptionHTTP httpError) {
					logger.error("HTTP Error " + httpError.getMessage() + " making a SPARQL query.");
					httpError.printStackTrace();
					System.exit(1);
				} // catch 1
				catch (JenaException jenaException) {
					logger.error("Jena Exception " + jenaException.getMessage() + " making a SPARQL query.");
					jenaException.printStackTrace();
					System.exit(1);
				} // catch 2
				generality = a.generality;
				if (a != null) {
					possibility = a.possibility().doubleValue();
					// logger.info("Reference Cardinality: " + a.referenceCardinality);
					// logger.info("N. confirmation: " + a.numConfirmations);
					// logger.info("N. exception: " + a.numExceptions);
					// logger.info("Generality: " + a.generality);
					// logger.info("Possibility: " + possibility);
					if (sheet == null) {
						f = a.possibility().doubleValue() * a.generality;
						// logger.info("Fitness: " + f);
					}
				} else {
					f = 0;
				}
			} else {
				f = 0;
			}
			
			BasicFitness fit = new BasicFitness(f, population.get(i));
			fit.setIndividual(population.get(i));
			fit.getIndividual().setValid(true);
			population.get(i).setFitness(fit);
			if (sheet != null) {
				if (indivi.isMapped()) {
					sheet.addCell(new Number(7, Row, a.possibility().doubleValue()));
					sheet.addCell(new Number(8, Row, a.referenceCardinality));
					sheet.addCell(new Number(9, Row, a.generality));
				}
				Row++;
			}
			i++;
			System.out.print("### Progress: " + (i * 100) / popSize + "%	Request time: " + timer.read() + "ms " + "###\r");
			finalTime += timer.endTimer();
		}
		logger.info("Done ! duration: " + finalTime + "ms");
	}

	public void update2(GEIndividual indivi, int curGeneration, int totalGeneration)
			throws IOException, InterruptedException {
		// evaluation fitness for each individual
		String arg1 = "";
		String arg2 = "";
		double f = 0;
		// logger.info("axiom: " + indivi.getPhenotype().getStringNoSpace());
		// logger.info("chromosome: " + indivi.getGenotype().toString());
		if (indivi.isMapped()) {
			try {
				a = AxiomFactory.create(indivi.getPhenotype(), RDFMiner.endpoint);
				List<List<Symbol>> arguments = a.argumentClasses;
				List<String> complexClass1 = new ArrayList<String>();
				int SizeArg0 = arguments.get(0).size();
				for (int k = 0; k < SizeArg0; k++) {
					arg1 = arguments.get(0).get(k).getSymbolString();
					if (!arg1.equals("(") && !arg1.equals(")") && !arg1.equals(" ")) {
						complexClass1.add(arg1);
					}
				}

				List<String> complexClass2 = new ArrayList<String>();
				int SizeArg1 = arguments.get(1).size();
				for (int l = 0; l < SizeArg1; l++) {
					arg2 = arguments.get(1).get(l).getSymbolString();
					if (!arg2.equals("(") && !arg2.equals(")") && !arg2.equals(" ")) {
						complexClass2.add(arg2);
					}
				}
			} // try
			catch (QueryExceptionHTTP httpError) {
				logger.error("HTTP Error " + httpError.getMessage() + " making a SPARQL query.");
				httpError.printStackTrace();
				System.exit(1);
			} // catch 1
			catch (JenaException jenaException) {
				logger.error("Jena Exception " + jenaException.getMessage() + " making a SPARQL query.");
				jenaException.printStackTrace();
				System.exit(1);
			} // catch 2

			if (a != null) {
				referenceCardinality = a.referenceCardinality;
				possibility = a.possibility().doubleValue();
				necessity = a.necessity().doubleValue();
				generality = a.generality;
				f = generality * possibility;
			} else {
				referenceCardinality = 0;
				generality = 0;
				possibility = 0;
				f = 0;
			}
		} else {
			referenceCardinality = 0;
			possibility = 0;
			necessity = 0;
			generality = 0;
			f = 0;
		}
		BasicFitness fit = new BasicFitness(f, indivi);
		fit.setIndividual(indivi);
		fit.getIndividual().setValid(true);
		indivi.setFitness(fit);
	}

	public static String removeCharAt(String s, int pos) {
		return s.substring(0, pos) + s.substring(pos + 1);
	}

	public void display(ArrayList<GEIndividual> Population, int curGeneration, WritableSheet sheet)
			throws IOException, RowsExceededException, WriteException {

		String axiom = "";
		String chromosome = "";
		// logger.info(" ");
		// logger.info("CANDIDATE POPULATION IN GENERATION: " + curGeneration);
		int index = Population.size();
		int Row;
		for (int i = 0; i < index; i++) {
			axiom = Population.get(i).getPhenotype().getStringNoSpace();
			chromosome = Population.get(i).getGenotype().toString();
			GEIndividual indivi = (GEIndividual) Population.get(i);
			if (Population.get(0).getPhenotype() == null)
				break;
			// logger.info(axiom);
			// logger.info(chromosome.substring(1, chromosome.length() - 1));
			// logger.info("Mapped: " + String.valueOf(indivi.isMapped()));
			// logger.info("Fitness: " + indivi.getFitness().getDouble());
			if (sheet != null) {
				Row = sheet.getRows();
				sheet.addCell(new Label(0, Row, axiom));
				if (indivi.isMapped()) {
					Axiom a = AxiomFactory.create(indivi.getPhenotype(), RDFMiner.endpoint);
					sheet.addCell(new Number(1, Row, a.possibility().doubleValue()));
					// sheet.addCell(new Number(2, Row, a.necessity().doubleValue()));
					// sheet.addCell(new Number(3, Row, ARI));
					sheet.addCell(new Number(2, Row, a.referenceCardinality));
					sheet.addCell(new Number(3, Row, a.generality));
					// sheet.addCell(new Number(4, Row, complexity_penalty));
					// logger.info("complexity_penalty:" + complexity_penalty);
				}

				sheet.addCell(new Number(5, Row, indivi.getFitness().getDouble()));
				sheet.addCell(new Label(6, Row, Boolean.toString(indivi.isMapped())));
			}
		}

	}

}