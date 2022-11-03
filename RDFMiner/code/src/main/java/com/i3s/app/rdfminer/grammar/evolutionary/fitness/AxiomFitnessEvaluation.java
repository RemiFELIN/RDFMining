package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.i3s.app.rdfminer.RDFMiner;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.novelty.NoveltyFitness;
import com.i3s.app.rdfminer.grammar.evolutionary.NoveltySearch;
import com.i3s.app.rdfminer.grammar.evolutionary.fitness.objectives.ObjectivesFitness;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;

import Individuals.FitnessPackage.BasicFitness;

/**
 * It is the class to setup the fitness value for Axioms in the
 * specified population
 *
 * @author NGUYEN Thu Huong Oct.18 & Rémi FELIN
 */
public class AxiomFitnessEvaluation {

	private static final Logger logger = Logger.getLogger(AxiomFitnessEvaluation.class.getName());

	/**
	 * Update a given population by using evaluation of each individual and fill data
	 * @param axioms       	a given population
	 * @param axiomsJSON	the list of OWL 2 Axioms in JSON format (used to return results).
	 */
	public static ArrayList<Axiom> assessAxioms(ArrayList<Axiom> axioms, List<JSONObject> axiomsJSON) throws URISyntaxException, IOException {

		Set<Callable<Axiom>> callables = new HashSet<>();
		ArrayList<Axiom> axiomList = new ArrayList<>();

		int i = 0;

		logger.info("The axioms will be evaluated using the following SPARQL Endpoint : " + Global.SPARQL_ENDPOINT);
		logger.info("Begin updating population ...");

		for(Axiom phi : axioms) {
			if (phi.individual.getPhenotype() == null)
				break;
			if (phi.individual.isMapped()) {
				callables.add(() -> {
					Axiom axiom = AxiomFactory.create(phi.individual, phi.individual.getPhenotype(),
							new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES));
					axiomsJSON.add(axiom.toJSON());
					return axiom;
				});
			} else {
				logger.error("The axiom " + phi.individual.getGenotype() + " is not mapped !");
				BasicFitness fit = new BasicFitness(0, phi.individual);
				fit.setIndividual(phi.individual);
				fit.getIndividual().setValid(true);
				phi.individual.setFitness(fit);
			}
		}

		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		List<Future<Axiom>> futureAxioms = null;
		logger.info(callables.size() + " axioms are ready to be evaluate ...");
		try {
			// Submit tasks
			futureAxioms = executor.invokeAll(callables);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// We recover our axioms
		assert futureAxioms != null;
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

		// Check if Novelty Search is enabled
		if(RDFMiner.parameters.useNoveltySearch) {
			// Compute the similarities of each axiom between them, and update the population
			NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.SPARQL_ENDPOINT, Global.PREFIXES));
			axiomList = noveltySearch.updateSimilarities(axiomList);
		}

		ArrayList<Axiom> newPopulation = new ArrayList<>();
		// Update fitness of population
		for (Axiom axiom : axiomList) {
			ObjectivesFitness.setFitness(axiom);
			if(RDFMiner.parameters.useNoveltySearch)
				NoveltyFitness.updateFitness(axiom);
			BasicFitness fit = new BasicFitness(axiom.fitness, axiom.individual);
			fit.getIndividual().setValid(true);
			axiom.individual.setFitness(fit);
			newPopulation.add(axiom);
		}
		return newPopulation;
	}

	/**
	 * initialize a given population by using evaluation of each individual
	 * @param individuals	a given population
	 * @return 				a list of assessed axioms
	 */
	public static ArrayList<Axiom> initializePopulation(ArrayList<GEIndividual> individuals) throws URISyntaxException, IOException {

		Set<Callable<Axiom>> callables = new HashSet<>();
		ArrayList<Axiom> axiomList = new ArrayList<>();

		logger.info("The axioms will be intialized using the following SPARQL Endpoint : " + Global.TRAINING_SPARQL_ENDPOINT);
		logger.info("Begin updating population ...");
		for(GEIndividual individual : individuals) {
			if (individual.getPhenotype() == null)
				break;
			if (individual.isMapped()) {
				callables.add(() -> {
					CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES);
					Axiom axiom = AxiomFactory.create(individual, individual.getPhenotype(),
							endpoint);
					return axiom;
				});
			} else {
				BasicFitness fit = new BasicFitness(0, individual);
				fit.setIndividual(individual);
				fit.getIndividual().setValid(true);
				individual.setFitness(fit);
			}
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

		// Check if Novelty Search is enabled
		if(RDFMiner.parameters.useNoveltySearch) {
			// Compute the similarities of each axiom between them, and update the population
			NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES));
			axiomList = noveltySearch.updateSimilarities(axiomList);
		}

		ArrayList<Axiom> newPopulation = new ArrayList<>();
		// Update fitness of population
		for (Axiom axiom : axiomList) {
			ObjectivesFitness.setFitness(axiom);
			if(RDFMiner.parameters.useNoveltySearch)
				NoveltyFitness.updateFitness(axiom);
			BasicFitness fit = new BasicFitness(axiom.fitness, axiom.individual);
			fit.getIndividual().setValid(true);
			axiom.individual.setFitness(fit);
			newPopulation.add(axiom);
		}
		return newPopulation;
	}

	/**
	 * Update a given individual by using evaluation (possibility, fitness, ...)
	 * @param axiom a given individual
	 * @return the evaluated individual
	 */
	public static Axiom updateIndividual(Axiom axiom, ArrayList<Axiom> axioms) throws URISyntaxException, IOException {
//		logger.info("Update the GEIndividual instance of this axiom ...");
		// Define a Corese endpoint
		CoreseEndpoint endpoint = new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES);
		double f;
		if (axiom.individual.isMapped()) {
			// The axiom is already evaluated !
//			axiom = AxiomFactory.create(axiom.individual, axiom.individual.getPhenotype(), endpoint);
			ObjectivesFitness.setFitness(axiom);
			if(RDFMiner.parameters.useNoveltySearch)
				axiom = NoveltySearch.updateSimilarity(endpoint, axiom, axioms);
				NoveltyFitness.updateFitness(axiom);
			f = axiom.fitness;
		} else {
			f = 0;
		}
		BasicFitness fit = new BasicFitness(f, axiom.individual);
		fit.setIndividual(axiom.individual);
		fit.getIndividual().setValid(true);
		axiom.individual.setFitness(fit);
//		logger.info("Done ! axiom.individual.fitness = " + axiom.individual.getFitness().getDouble());
		return axiom;
	}

}