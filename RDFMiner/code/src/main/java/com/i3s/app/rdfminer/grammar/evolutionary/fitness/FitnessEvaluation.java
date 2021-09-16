package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

//import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

//import org.apache.jena.shared.JenaException;
//import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
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

//	protected Axiom axiom;
	protected long t0, t;
	// protected int numSuccessAxioms=0;
	double referenceCardinality = 0.0;
	double possibility = 0.0;
	double necessity = 0.0;
	double generality = 0.0;
	double complexity_penalty = 0.0;

	public void updatePopulation(ArrayList<GEIndividual> population, int curGeneration,
			boolean evaluate, List<JSONObject> axioms) throws InterruptedException, ExecutionException {
		
		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Log the size of executor
//		logger.info("n thread(s) ready to be launched");
		
		Set<Callable<Axiom>> callables = new HashSet<Callable<Axiom>>();
		List<Axiom> axiomList = new ArrayList<>();
		
		int i = 0;
		while (i < population.size()) {
			if (population.get(0).getPhenotype() == null)
				break;
			if (population.get(i).isMapped()) {
				final int idx = i;
				callables.add(new Callable<Axiom>() {
					public Axiom call() throws Exception {
//						logger.info("Thread-ID: " + Thread.currentThread().getId());
//						logger.info("Starting update axiom ...");
						SparqlEndpoint endpoint;
						if (evaluate) {
							endpoint = new SparqlEndpoint(Global.REMOTE_SPARQL_ENDPOINT, Global.REMOTE_PREFIXES);
						} else {
							endpoint = new SparqlEndpoint(Global.LOCAL_SPARQL_ENDPOINT, Global.LOCAL_PREFIXES);
						}
						Axiom axiom = AxiomFactory.create(population.get(idx), population.get(idx).getPhenotype(), endpoint);
//						logger.info("Axiom successfully evaluated !");
						return axiom;
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
		
		// Submit tasks
		List<Future<Axiom>> futureAxioms = executor.invokeAll(callables);
		
		// We recover our axioms
		for (Future<Axiom> axiom : futureAxioms) {
//			System.out.println(axiom.toString() + " added !");
			axiomList.add(axiom.get());
		}
		
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		
		// population.clear();
//		try {
//			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//		} catch (InterruptedException e) {
//			logger.warn("Executor service has been interrupted !");
//			System.exit(1);
//		}
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
				if(axiom.getIndividual().isMapped()) {
					dbpedia.possibility = axiom.possibility().doubleValue();
					dbpedia.referenceCardinality = axiom.referenceCardinality;
					dbpedia.generality = axiom.generality;
					dbpedia.necessity = axiom.necessity().doubleValue();
					for(JSONObject json : axioms) {
						if(json.get("axiom").equals(axiom.axiomId)) {
							axioms.get(axioms.indexOf(json)).put("resultsFromDBPedia", dbpedia);
						}
					}
				} else {
					for(JSONObject json : axioms) {
						if(json.get("axiom").equals(axiom.axiomId)) {
							axioms.get(axioms.indexOf(json)).put("resultsFromDBPedia", dbpedia);
						}
					}
				}
			}
		}
	}

	public void updateIndividual(GEIndividual indivi) {
		
		double f = 0;
		if (indivi.isMapped()) {
			Axiom axiom = AxiomFactory.create(indivi, indivi.getPhenotype(), new SparqlEndpoint(Global.LOCAL_SPARQL_ENDPOINT, Global.LOCAL_PREFIXES));
			if (axiom != null) {
				f = setFitness(axiom);
			} else {
				f = 0;
			}
		} else {
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


	public void display(ArrayList<GEIndividual> population, List<JSONObject> axioms, int ngen) throws InterruptedException {
		int index = population.size();
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
		for (int i = 0; i < index; i++) {
			final int idx = i;
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					GEIndividual indivi = (GEIndividual) population.get(idx);
//					if (population.get(0).getPhenotype() == null)
//						break;
					if (indivi.isMapped()) {
						Axiom a = AxiomFactory.create(indivi, indivi.getPhenotype(), new SparqlEndpoint(Global.LOCAL_SPARQL_ENDPOINT, Global.LOCAL_PREFIXES));
						a.generation = ngen;
						axioms.add(a.toJSON());
					}
					return null;
				}
			});
		}
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		executor.invokeAll(callables);
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
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