package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
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
	public ArrayList<Entity> updatePopulation(ArrayList<Entity> population) {
		logger.info("Update the current population ...");
		Set<Callable<Entity>> callables = new HashSet<>();
		List<Entity> entities = new ArrayList<>();
//		logger.info("The axioms will be evaluated using the following SPARQL Endpoint : " + url);
//		logger.info("Begin updating population ...");
		int i = 0;
		while (i < population.size()) {
//			if (population.get(0).individual.getPhenotype() == null)
//				break;
			if (population.get(i).individual.isMapped()) {
				final int idx = i;
				callables.add(() -> {
					Axiom axiom = AxiomFactory.create(population.get(idx).individual,
							population.get(idx).individual.getPhenotype(),
							new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT,
									Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES));
					evaluatedAxioms.add(axiom.toJSON());
					return axiom;
				});
				callables.add(() -> AxiomFactory.create(
						population.get(idx).individual,
						population.get(idx).individual.getPhenotype(),
						new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT,
								Global.TARGET_SPARQL_ENDPOINT, Global.PREFIXES)));
			} else {
				BasicFitness fit = new BasicFitness(0, population.get(i).individual);
				fit.setIndividual(population.get(i).individual);
				fit.getIndividual().setValid(true);
				population.get(i).individual.setFitness(fit);
			}
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
		// Update fitness of population
		for (Entity entity : entities) {
			BasicFitness fit = new BasicFitness(entity.fitness, entity.individual);
			fit.getIndividual().setValid(true);
			entity.individual.setFitness(fit);
			newPopulation.add(entity);
		}
		return newPopulation;
	}

	@Override
	public Entity updateIndividual(Entity entity) throws URISyntaxException, IOException {
		// in a case of new individual, we need to compute it as a new axiom
		double f = 0;
		if (entity.individual.isMapped()) {
			Entity axiom = AxiomFactory.create(entity.individual, entity.individual.getPhenotype(),
					new CoreseEndpoint(Global.CORESE_SPARQL_ENDPOINT, Global.TRAINING_SPARQL_ENDPOINT, Global.PREFIXES));
			f = axiom.fitness;
		} else {
			f = 0;
		}
		BasicFitness fit = new BasicFitness(f, entity.individual);
		fit.setIndividual(entity.individual);
		fit.getIndividual().setValid(true);
		entity.individual.setFitness(fit);
		return entity;
	}

	public List<JSONObject> getEvaluatedAxioms() {
		return evaluatedAxioms;
	}

}