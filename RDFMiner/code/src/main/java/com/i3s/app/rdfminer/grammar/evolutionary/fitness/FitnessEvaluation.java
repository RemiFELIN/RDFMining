package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

//import org.apache.jena.shared.JenaException;
//import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.launcher.LaunchWithGE;
import com.i3s.app.rdfminer.output.AxiomJSON;
import com.i3s.app.rdfminer.output.DBPediaJSON;
//import com.i3s.app.rdfminer.output.DBPediaJSON;
import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

import Individuals.FitnessPackage.BasicFitness;
//import jxl.write.Label;
//import jxl.write.Number;
//import jxl.write.WritableSheet;
//import jxl.write.WriteException;
//import jxl.write.biff.RowsExceededException;

/**
 * FitnessEvaluation - is the class to setup the fitness value for Axioms in the
 * specified population
 *
 * @author NGUYEN Thu Huong Oct.18
 */
public class FitnessEvaluation {

	private static Logger logger = Logger.getLogger(FitnessEvaluation.class.getName());

	protected Axiom axiom;
	protected long t0, t;
	// protected int numSuccessAxioms=0;
	double referenceCardinality = 0.0;
	double possibility = 0.0;
	double necessity = 0.0;
	double generality = 0.0;
	double complexity_penalty = 0.0;

	public void updatePopulation(ArrayList<GEIndividual> population, int curGeneration,
			boolean evaluate, List<JSONObject> axioms) {
		
		SparqlEndpoint endpoint;
		if (evaluate) {
			endpoint = RDFMiner.REMOTE_ENDPOINT;
			logger.info("Evaluating axioms against to the RDF Data of the whole DBPedia.");
			logger.info("Endpoint URL: " + endpoint.endpoint);
		} else {
			endpoint = RDFMiner.LOCAL_ENDPOINT;
			logger.info("Evaluating axioms against to the RDF Data of the minimized DBPedia");
			logger.info("Endpoint URL: " + endpoint.endpoint);
		}

		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(4);
		// Log the size of executor
		logger.info("[n] thread(s) ready to be launched");
		List<Future<Axiom>> futureAxioms = new ArrayList<>();
		List<Callable<Axiom>> callables = new ArrayList<>();
		List<Axiom> axiomList = new ArrayList<>();
		
		int i = 0;
		while (i < population.size()) {
			if (population.get(0).getPhenotype() == null)
				break;
			if (population.get(i).isMapped()) {
				final int idx = i;
				callables.add(new Callable<Axiom>() {
					public Axiom call() throws Exception {
						return AxiomFactory.create(population.get(idx), population.get(idx).getPhenotype(), endpoint);
					};
				});
			} else {
				BasicFitness fit = new BasicFitness(0, population.get(i));
				fit.setIndividual(population.get(i));
				fit.getIndividual().setValid(true);
				population.get(i).setFitness(fit);
			}
			i++;
		}
		
		for(Callable<Axiom> callable : callables) {
			executor.submit(callable);
		}
		// launch all the threads
//		try {
//			futureAxioms = executor.invokeAll(callables);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		// We recover our axioms
//		for (Future<Axiom> axiom : futureAxioms) {
//			try {
//				axiomList.add(axiom.get());
//			} catch (InterruptedException | ExecutionException e) {
//				e.printStackTrace();
//				System.exit(0);
//			}
//		}
		// After that, we can stop the executor
		// population.clear();
		executor.shutdown();
		while(!executor.isTerminated()) {
			System.out.print("Closing executor service ...\r");
		}
		// Update fitness of population
		for(Axiom axiom : axiomList) {
			BasicFitness fit = new BasicFitness(setFitness(axiom), axiom.individual);
			fit.getIndividual().setValid(true);
			axiom.individual.setFitness(fit);
			population.add(axiom.individual);
			// Now, we can fill our JSONObject
			if(evaluate) {
				// data about DBPedia
				DBPediaJSON dbpedia = new DBPediaJSON();
				dbpedia.possibility = axiom.possibility().doubleValue();
				dbpedia.referenceCardinality = axiom.referenceCardinality;
				dbpedia.generality = axiom.generality;
				dbpedia.necessity = axiom.necessity().doubleValue();
				for(JSONObject json : axioms) {
					if(json.get("axiom") == axiom.axiomId) {
						axioms.get(axioms.indexOf(json)).put("resultsFromDBPedia", dbpedia);
					}
				}
			}
		}
	}

	public void updateIndividual(GEIndividual indivi) {
		
		double f = 0;
		
		if (indivi.isMapped()) {
			
			axiom = AxiomFactory.create(indivi, indivi.getPhenotype(), RDFMiner.LOCAL_ENDPOINT);

			if (axiom != null) {
				referenceCardinality = axiom.referenceCardinality;
				possibility = axiom.possibility().doubleValue();
				necessity = axiom.necessity().doubleValue();
				generality = axiom.generality;
//				f = generality * possibility;
				f = setFitness(axiom);
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
//		System.out.print("Evaluation>	" + indivi.getPhenotype().getStringNoSpace().substring(0, 60)
//				+ "... - Request time: " + timer.endTimer() + "ms   \r");
	}

	public static String removeCharAt(String s, int pos) {
		return s.substring(0, pos) + s.substring(pos + 1);
	}


	public void display(ArrayList<GEIndividual> population, boolean fill, List<JSONObject> axioms, int k) {

		int index = population.size();
		for (int i = 0; i < index; i++) {
			GEIndividual indivi = (GEIndividual) population.get(i);
			if (population.get(0).getPhenotype() == null)
				break;
			if (fill && indivi.isMapped()) {
				Axiom a = AxiomFactory.create(indivi, indivi.getPhenotype(), RDFMiner.LOCAL_ENDPOINT);
				axioms.add(a.toJSON());
			}
		}

	}

	public double setFitness(Axiom axiom) {
		// Evaluate axioms with generality formula or (initial) formula with necessity
		if (generality != 0) {
			axiom.fitness = axiom.possibility().doubleValue() * axiom.generality;
			return axiom.possibility().doubleValue() * axiom.generality;
		} else {
			axiom.fitness = axiom.referenceCardinality
					* ((axiom.possibility().doubleValue() + axiom.necessity().doubleValue()) / 2);
			return axiom.referenceCardinality
					* ((axiom.possibility().doubleValue() + axiom.necessity().doubleValue()) / 2);
		}
	}

}