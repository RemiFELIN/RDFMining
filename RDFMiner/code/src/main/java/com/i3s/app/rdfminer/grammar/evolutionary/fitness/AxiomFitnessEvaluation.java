package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.i3s.app.rdfminer.grammar.evolutionary.Fitness;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.output.axiom.DBPediaJSON;
import com.i3s.app.rdfminer.sparql.virtuoso.VirtuosoEndpoint;

import Individuals.FitnessPackage.BasicFitness;

/**
 * It is the class to setup the fitness value for Axioms in the
 * specified population
 *
 * @author NGUYEN Thu Huong Oct.18 & Rémi FELIN
 */
public class AxiomFitnessEvaluation extends Fitness {

	private static final Logger logger = Logger.getLogger(AxiomFitnessEvaluation.class.getName());

	@Override
	public ArrayList<GEIndividual> updatePopulation(ArrayList<GEIndividual> population, String url, String prefixes, List<JSONObject> axioms) {

		Set<Callable<Axiom>> callables = new HashSet<>();
		List<Axiom> axiomList = new ArrayList<>();

		int i = 0;

		logger.info("Begin updating population ...");
		while (i < population.size()) {
			if (population.get(0).getPhenotype() == null)
				break;
			if (population.get(i).isMapped()) {
				final int idx = i;
				callables.add(() -> {
					VirtuosoEndpoint endpoint = new VirtuosoEndpoint(url, prefixes);
					Axiom axiom = AxiomFactory.create(population.get(idx), population.get(idx).getPhenotype(),
							endpoint);
					// TODO : num of generation (look if it miss...)
					if(axioms != null)
						axioms.add(axiom.toJSON());
					return axiom;
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
		List<Future<Axiom>> futureAxioms = null;

		logger.info(callables.size() + " axioms are ready to be evaluate ...");
		try {
			futureAxioms = executor.invokeAll(callables);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// We recover our axioms
		for (Future<Axiom> axiom : futureAxioms) {
			try {
				axiomList.add(axiom.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		// Shut down the executor
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ArrayList<GEIndividual> newPopulation = new ArrayList<>();
		// Update fitness of population
		for (Axiom axiom : axiomList) {
			BasicFitness fit = new BasicFitness(setFitness(axiom), axiom.individual);
			fit.getIndividual().setValid(true);
			axiom.individual.setFitness(fit);
			newPopulation.add(axiom.individual);
			// Now, we can fill our JSONObject
			if (Objects.equals(url, Global.VIRTUOSO_REMOTE_SPARQL_ENDPOINT)) {
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
		return newPopulation;
	}

	@Override
	public GEIndividual updateIndividual(GEIndividual indivi) {
		if(indivi.getFitness() != null) {
			// this individual has already been evaluated
			return indivi;
		}
		// in a case of new individual, we need to compute it as a new axiom
		double f = 0;
		if (indivi.isMapped()) {
			Axiom axiom = AxiomFactory.create(indivi, indivi.getPhenotype(),
					new VirtuosoEndpoint(Global.VIRTUOSO_LOCAL_SPARQL_ENDPOINT, Global.VIRTUOSO_LOCAL_PREFIXES));
			f = setFitness(axiom);
		} else {
			f = 0;
		}
		BasicFitness fit = new BasicFitness(f, indivi);
		fit.setIndividual(indivi);
		fit.getIndividual().setValid(true);
		indivi.setFitness(fit);
		return indivi;
	}

	@Override
	public void display(ArrayList<GEIndividual> population, List<JSONObject> axioms, int generation) {
		int index = population.size();
		Set<Callable<Void>> callables = new HashSet<>();
		for (int i = 0; i < index; i++) {
			final int idx = i;
			callables.add(() -> {
				// Compute axiom values and fill the list of axioms
				GEIndividual ind = population.get(idx);
				// if indivi is correctly formed
				if (ind.isMapped()) {
					Axiom a = AxiomFactory.create(ind, ind.getPhenotype(),
							new VirtuosoEndpoint(Global.VIRTUOSO_LOCAL_SPARQL_ENDPOINT, Global.VIRTUOSO_LOCAL_PREFIXES));
					a.generation = generation;
					axioms.add(a.toJSON());
				}
				return null;
			});
		}
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		try {
			executor.invokeAll(callables);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Shut down the executor
		executor.shutdown();
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

}