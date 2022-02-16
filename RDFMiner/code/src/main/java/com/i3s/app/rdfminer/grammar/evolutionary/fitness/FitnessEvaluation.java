package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

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

import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.output.DBPediaJSON;
import com.i3s.app.rdfminer.sparql.virtuoso.SparqlEndpoint;

import Individuals.FitnessPackage.BasicFitness;

/**
 * FitnessEvaluation - is the class to setup the fitness value for Axioms in the
 * specified population
 *
 * @author NGUYEN Thu Huong Oct.18 & Rémi FELIN
 */
public class FitnessEvaluation {

	/**
	 * Update a given population by using evaluation (possibility, fitness, ...) of
	 * each individual and fill data
	 * 
	 * @param population       a given population
	 * @param curGeneration    the current generation
	 * @param evaluateOnFullDB true if the given population are evaluated of full
	 *                         instance of DB, else false
	 * @param axioms           the list of axioms in JSON format (used to return
	 *                         results)
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void updatePopulation(ArrayList<GEIndividual> population, int curGeneration, boolean evaluateOnFullDB,
			List<JSONObject> axioms) throws InterruptedException, ExecutionException {

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
						SparqlEndpoint endpoint;
						if (evaluateOnFullDB) {
							endpoint = new SparqlEndpoint(Global.VIRTUOSO_REMOTE_SPARQL_ENDPOINT, Global.VIRTUOSO_REMOTE_PREFIXES);
						} else {
							endpoint = new SparqlEndpoint(Global.VIRTUOSO_LOCAL_SPARQL_ENDPOINT, Global.VIRTUOSO_LOCAL_PREFIXES);
						}
						Axiom axiom = AxiomFactory.create(population.get(idx), population.get(idx).getPhenotype(),
								endpoint);
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

		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		List<Future<Axiom>> futureAxioms = executor.invokeAll(callables);
		// We recover our axioms
		for (Future<Axiom> axiom : futureAxioms) {
			axiomList.add(axiom.get());
		}
		// Shut down the executor
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

		// Update fitness of population
		for (Axiom axiom : axiomList) {
			BasicFitness fit = new BasicFitness(setFitness(axiom), axiom.individual);
			fit.getIndividual().setValid(true);
			axiom.individual.setFitness(fit);
			population.add(axiom.individual);
			// Now, we can fill our JSONObject
			if (evaluateOnFullDB) {
				// data about full database of DBPedia
				DBPediaJSON dbpedia = new DBPediaJSON();
				if (axiom.getIndividual().isMapped()) {
					dbpedia.possibility = axiom.possibility().doubleValue();
					dbpedia.referenceCardinality = axiom.referenceCardinality;
					dbpedia.generality = axiom.generality;
					dbpedia.necessity = axiom.necessity().doubleValue();
					dbpedia.ari = axiom.ARI();
					dbpedia.elapsedTime = axiom.elapsedTime;
					dbpedia.isTimeOut = axiom.isTimeout;
				}
				for (JSONObject json : axioms) {
					if (json.get("axiom").equals(axiom.axiomId)) {
						axioms.get(axioms.indexOf(json)).put("resultsFromFullDB", dbpedia.toJSON());
					}
				}
			}
		}
	}

	/**
	 * Update a given individual by using evaluation (possibility, fitness, ...)
	 * @param indivi a given individual
	 * @return the evaluated individual
	 */
	public static GEIndividual updateIndividual(GEIndividual indivi) {
		if(indivi.getFitness() != null) {
			// this individual has already been evaluated
			return indivi;
		}
		// in a case of new individual, we need to compute it as a new axiom
		double f = 0;
		if (indivi.isMapped()) {
			Axiom axiom = AxiomFactory.create(indivi, indivi.getPhenotype(),
					new SparqlEndpoint(Global.VIRTUOSO_LOCAL_SPARQL_ENDPOINT, Global.VIRTUOSO_LOCAL_PREFIXES));
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
		return indivi;
	}

	/**
	 * display a given population
	 * 
	 * @param population a given population
	 * @param axioms     the list of axioms in JSON format (used to return results)
	 * @param generation the current generation
	 * @throws InterruptedException
	 */
	public void display(ArrayList<GEIndividual> population, List<JSONObject> axioms, int generation)
			throws InterruptedException {
		int index = population.size();
		Set<Callable<Void>> callables = new HashSet<Callable<Void>>();
		for (int i = 0; i < index; i++) {
			final int idx = i;
			callables.add(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					// Compute axiom values and fill the list of axioms
					GEIndividual indivi = (GEIndividual) population.get(idx);
					// if indivi is correctly formed
					if (indivi.isMapped()) {
						Axiom a = AxiomFactory.create(indivi, indivi.getPhenotype(),
								new SparqlEndpoint(Global.VIRTUOSO_LOCAL_SPARQL_ENDPOINT, Global.VIRTUOSO_LOCAL_PREFIXES));
						a.generation = generation;
						axioms.add(a.toJSON());
					}
					return null;
				}
			});
		}
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		executor.invokeAll(callables);
		// Shut down the executor
		executor.shutdown();
		executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	/**
	 * Compute the fitness of a given axiom by using generality (if it's not equal
	 * to 0) or the {@link Axiom#possibility() possibility} and
	 * {@link Axiom#necessity() necessity} values.
	 * 
	 * @param axiom a given {@link Axiom axiom}
	 * @return the value of fitness
	 */
	public static double setFitness(Axiom axiom) {
		// Evaluate axioms with generality formula or (initial) formula with necessity
		if (axiom.generality != 0) {
			axiom.fitness = axiom.possibility().doubleValue() * axiom.generality;
			return axiom.possibility().doubleValue() * axiom.generality;
		} else {
			axiom.fitness = axiom.referenceCardinality
					* ((axiom.possibility().doubleValue() + axiom.necessity().doubleValue()) / 2);
			return axiom.referenceCardinality
					* ((axiom.possibility().doubleValue() + axiom.necessity().doubleValue()) / 2);
		}
	}

//	public static String removeCharAt(String s, int pos) {
//		return s.substring(0, pos) + s.substring(pos + 1);
//	}

}