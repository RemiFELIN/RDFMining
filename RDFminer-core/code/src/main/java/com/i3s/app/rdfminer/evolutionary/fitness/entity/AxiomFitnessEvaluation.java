package com.i3s.app.rdfminer.evolutionary.fitness.entity;

import com.i3s.app.rdfminer.Global;
import com.i3s.app.rdfminer.Parameters;
import com.i3s.app.rdfminer.entity.Entity;
import com.i3s.app.rdfminer.entity.axiom.Axiom;
import com.i3s.app.rdfminer.entity.axiom.AxiomFactory;
import com.i3s.app.rdfminer.evolutionary.fitness.novelty.NoveltySearch;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.FitnessPackage.BasicFitness;
import com.i3s.app.rdfminer.evolutionary.geva.Individuals.GEIndividual;
import com.i3s.app.rdfminer.evolutionary.tools.EATools;
import com.i3s.app.rdfminer.sparql.corese.CoreseEndpoint;
import org.apache.log4j.Logger;

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

//	public List<JSONObject> evaluatedAxioms = new ArrayList<>();

	@Override
	public ArrayList<Entity> initializePopulation(ArrayList<GEIndividual> individuals) {
		Parameters parameters = Parameters.getInstance();
		Set<Callable<Entity>> callables = new HashSet<>();
		ArrayList<Entity> entities = new ArrayList<>();
		logger.info("The axioms will be intialized using the target SPARQL Endpoint : " + Global.SPARQL_ENDPOINT);
		logger.info("Begin updating population ...");
		for(GEIndividual individual : individuals) {
//			System.out.println(individual.getPhenotype());
			if (individual.getPhenotype() == null)
				break;
			if (individual.isMapped()) {
				callables.add(() -> {
					Axiom axiom = AxiomFactory.create(individual, individual.getPhenotype(),
						new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES));
					// compute fitness
					axiom.computeFitness();
					return axiom;
				});
			} else {
				individual.setFitness(new BasicFitness(0, individual));
			}
		}
		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		List<Future<Entity>> futureEntities = new ArrayList<>();
		logger.info(callables.size() + " axioms are ready to be evaluate ...");
		// submit callables in order to assess them
		for(Callable<Entity> call : callables) {
			futureEntities.add(executor.submit(call));
		}
		// We recover our axioms
		for (Future<Entity> entity : futureEntities) {
			try {
				if(parameters.timeCap != 0)
					entities.add(entity.get(parameters.timeCap, TimeUnit.MINUTES));
				else
					entities.add(entity.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				logger.warn("Time-cap reached !");
			}
		}
		// Log how many axioms has been evaluated
		logger.info(entities.size() + " entities has been evaluated !");
		executor.shutdown();
		try {
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				logger.debug("force the shutdown of executor ...");
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}
		// set new population
		ArrayList<Entity> newPopulation = new ArrayList<>();
		// manage not evaluated axioms
		if(parameters.timeCap != 0) {
			newPopulation.addAll(EATools.getTimeCappedIndividuals(individuals, entities));
		}
		// Check if Novelty Search is enabled
		if(parameters.isUseNoveltySearch()) {
		 	// Compute the similarities of each axiom between them, and update the population
			NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES));
			try {
				entities = noveltySearch.update(entities);
			} catch (URISyntaxException | IOException e) {
				logger.error("Error during the computation of similarities ...");
				e.printStackTrace();
			}
		}
		// Update fitness of population
		for (Entity entity : entities) {
//			ObjectivesFitness.setFitness(axiom);
//			if(RDFMiner.parameters.useNoveltySearch)
//				NoveltyFitness.updateFitness(axiom);
//			entity.setFitness(new BasicFitness(entity.fitness, entity));
			newPopulation.add(entity);
			logger.debug("i: " + entity.individual.getGenotype() + " ~ F(i)= " + entity.individual.getFitness().getDouble());
		}
		return newPopulation;
	}

	@Override
	public ArrayList<Entity> updatePopulation(ArrayList<Entity> population) {
		Parameters parameters = Parameters.getInstance();
		logger.info("Update the current population using the following RDF data graph: " + parameters.getNamedDataGraph());
		Set<Callable<Entity>> callables = new HashSet<>();
		List<Entity> entities = new ArrayList<>();
		for(Entity entity : population) {
			callables.add(() -> {
				Axiom axiom = AxiomFactory.create(entity.individual, entity.individual.getPhenotype(),
						new CoreseEndpoint(parameters.getNamedDataGraph(), parameters.getPrefixes()));
				// compute fitness
				axiom.computeFitness();
				// the generation in which this axiom was discovered
//				axiom.generation = entity.generation;
				return axiom;
			});
		}
		// We have a set of threads to compute each axioms
		ExecutorService executor = Executors.newFixedThreadPool(Global.NB_THREADS);
		// Submit tasks
		List<Future<Entity>> futureEntities = new ArrayList<>();

		logger.info(callables.size() + " axioms are ready to be evaluate ...");
		// submit callables in order to assess them
		for(Callable<Entity> call : callables) {
			futureEntities.add(executor.submit(call));
		}
		// We recover our axioms
		for (Future<Entity> entity : futureEntities) {
			try {
				if(parameters.timeCap != 0)
					entities.add(entity.get(parameters.timeCap, TimeUnit.MINUTES));
				else
					entities.add(entity.get());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				logger.warn("Time-cap reached for the current entity !");
			}
		}
		// Log how many axioms has been evaluated
		logger.info(entities.size() + " entities has been evaluated !");
		executor.shutdown();
		try {
			if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				logger.debug("force the shutdown of executor ...");
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}
		// Check if Novelty Search is enabled
//		if(parameters.isUseNoveltySearch()) {
//			// Compute the similarities of each axiom between them, and update the population
//			NoveltySearch noveltySearch = new NoveltySearch(new CoreseEndpoint(Global.SPARQL_ENDPOINT, Global.PREFIXES));
//			try {
//				entities = noveltySearch.update(entities);
//			} catch (URISyntaxException | IOException e) {
//				logger.error("Error during the computation of similarities ...");
//				e.printStackTrace();
//			}
//		}
		// set new population
		ArrayList<Entity> newPopulation = new ArrayList<>(entities);
		// manage not evaluated axioms
		if(parameters.timeCap != 0) {
			newPopulation.addAll(EATools.getTimeCappedEntities(population, entities));
		}
		// Update fitness of individuals from a population
		//			entity.setFitness(new BasicFitness(entity.fitness, entity));
		return newPopulation;
	}

	@Override
	public Entity updateIndividual(GEIndividual individual) throws URISyntaxException, IOException {
		Parameters parameters = Parameters.getInstance();
		// in a case of new individual, we need to compute it as a new axiom
		double f;
		// instance of axiom
//		if (individual.isMapped()) {
		Axiom axiom = AxiomFactory.create(individual, individual.getPhenotype(),
				new CoreseEndpoint(parameters.getNamedDataGraph(), parameters.getPrefixes()));
		// compute fitness
		axiom.computeFitness();
		f = axiom.individual.getFitness().getDouble();
//		} else {
//			logger.warn(individual.getPhenotype() + " is not mapped !");
//			f = 0;
//		}
		individual.setFitness(new BasicFitness(f, individual));
		return axiom;
	}

}