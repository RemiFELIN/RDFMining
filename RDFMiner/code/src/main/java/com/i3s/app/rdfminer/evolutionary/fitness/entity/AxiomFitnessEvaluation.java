package com.i3s.app.rdfminer.evolutionary.fitness.entity;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.evolutionary.individual.GEIndividual;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

/**
 * It is the class to setup the fitness value for Axioms in the
 * specified population
 *
 * @author NGUYEN Thu Huong Oct.18 & Rémi FELIN
 */
public class AxiomFitnessEvaluation implements FitnessEvaluation {

	private static final Logger logger = Logger.getLogger(AxiomFitnessEvaluation.class.getName());

	public List<JSONObject> evaluatedAxioms = new ArrayList<>();

	@Override
	public ArrayList<Entity> initializePopulation(ArrayList<GEIndividual> individuals) {
		Set<Callable<Axiom>> callables = new HashSet<>();
		ArrayList<Axiom> axiomList = new ArrayList<>();
		logger.info("The axioms will be intialized using the target SPARQL Endpoint : " + Global.TARGET_SPARQL_ENDPOINT);
		logger.info("Begin updating population ...");
		for(GEIndividual individual : individuals) {
			System.out.println(individual.getPhenotype());
			if (individual.getPhenotype() == null)
				break;
			if (individual.isMapped()) {
				callables.add(() -> AxiomFactory.create(individual, individual.getPhenotype(),
						new CoreseEndpoint(Global.CORESE_IP, Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES)));
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
//		if(RDFMiner.parameters.useNoveltySearch) {
			// Compute the similarities of each axiom between them, and update the population
//			NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES));
//			axiomList = noveltySearch.updateSimilarities(axiomList);
//		}

		ArrayList<Entity> newPopulation = new ArrayList<>();
		// Update fitness of population
		for (Axiom axiom : axiomList) {
//			ObjectivesFitness.setFitness(axiom);
//			if(RDFMiner.parameters.useNoveltySearch)
//				NoveltyFitness.updateFitness(axiom);
			BasicFitness fit = new BasicFitness(axiom.fitness, axiom.individual);
			fit.getIndividual().setValid(true);
			axiom.individual.setFitness(fit);
			newPopulation.add(axiom);
		}
		return newPopulation;
	}

	@Override
	public ArrayList<Entity> updatePopulation(ArrayList<Entity> population) {
		logger.info("Update the current population using the target SPARQL Endpoint : " + Global.TARGET_SPARQL_ENDPOINT);
		Set<Callable<Entity>> callables = new HashSet<>();
		List<Entity> entities = new ArrayList<>();
//		logger.info("The axioms will be evaluated using the following SPARQL Endpoint : " + url);
//		logger.info("Begin updating population ...");
		int i = 0;
		while (i < population.size()) {
//			if (population.get(0).individual.getPhenotype() == null)
//				break;
//			if (population.get(i).individual.isMapped()) {
			final int idx = i;
			callables.add(() -> {
				Axiom axiom = AxiomFactory.create(population.get(idx).individual,
						population.get(idx).individual.getPhenotype(),
						new CoreseEndpoint(Global.CORESE_IP,
								Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES));
				// the generation in which this axiom was discovered
				axiom.generation = population.get(idx).generation;
//				assert axiom != null;
//				evaluatedAxioms.add(axiom.toJSON());
				return axiom;
			});
//			} else {
//				logger.warn(population.get(i).individual.getPhenotype() + " is not correctly mapped !");
//				BasicFitness fit = new BasicFitness(0, population.get(i).individual);
//				fit.setIndividual(population.get(i).individual);
//				fit.getIndividual().setValid(true);
//				population.get(i).individual.setFitness(fit);
//			}
			i++;
		}
		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		List<Future<Entity>> futureEntities = null;

		logger.info(callables.size() + " axioms are ready to be evaluate ...");
		try {
			futureEntities = executor.invokeAll(callables);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// We recover our axioms
		assert futureEntities != null;
		for (Future<Entity> entityFuture : futureEntities) {
			try {
				entities.add(entityFuture.get());
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

		ArrayList<Entity> newPopulation = new ArrayList<>();
		// Update fitness of individuals from a population
		for (Entity entity : entities) {
			BasicFitness fit = new BasicFitness(entity.fitness, entity.individual);
			fit.getIndividual().setValid(true);
			entity.individual.setFitness(fit);
			newPopulation.add(entity);
		}
		return newPopulation;
	}

	@Override
	public Entity updateIndividual(GEIndividual individual) throws URISyntaxException, IOException {
		// in a case of new individual, we need to compute it as a new axiom
		double f = 0;
		// instance of axiom
		Entity axiom = null;
		if (individual.isMapped()) {
			axiom = AxiomFactory.create(individual, individual.getPhenotype(),
					new CoreseEndpoint(Global.CORESE_IP, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES));
			assert axiom != null;
			f = axiom.fitness;
		} else {
			logger.warn(individual.getPhenotype() + " is not mapped !");
			f = 0;
		}
		BasicFitness fit = new BasicFitness(f, individual);
		fit.setIndividual(individual);
		fit.getIndividual().setValid(true);
		individual.setFitness(fit);
//		// set axiom individual as mapped !
//		assert axiom != null;
//		axiom.individual.setMapped(true);
		return axiom;
	}

//	public List<JSONObject> getEvaluatedAxioms() {
//		return evaluatedAxioms;
//	}

}