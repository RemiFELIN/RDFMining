/**
 * 
 */
package com.i3s.app.rdfminer;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A container of settings
 * This is a singleton !
 * @author Andrea G. B. Tettamanzi & Rémi Felin
 */
public class Parameters {

	private static Parameters instance = null;

	@JsonProperty("username")
	private String userID = "admin";

	@JsonProperty("projectName")
	private String projectName = "results";

	@JsonProperty("mod")
	private int mod;

	@JsonProperty("prefixes")
	private String prefixes;

	@JsonProperty("namedDataGraph")
	private String namedDataGraph;

	@JsonProperty("bnf")
	private String grammar;

	@JsonProperty("populationSize")
	private int populationSize = 100;

	@JsonProperty("stopCriterionType")
	private int stopCriterion = 1;

	@JsonProperty("effort")
	private int effort = 5000;

	@JsonProperty("time")
	private int maxMiningTime = 30;

	@JsonProperty("sizeChromosome")
	private int sizeChromosome = 20;

	@JsonProperty("maxWrap")
	private int maxWrap = 1000;

	// Selection

	@JsonProperty("eliteSelectionRate")
	private double eliteSelectionRate = 0.2;

	@JsonProperty("tournamentSelectionRate")
	private double tournamentSelectionRate = 0.1;

	@JsonProperty("selectionType")
	private int selectionType = 3;

	@JsonProperty("selectionRate")
	private double selectionRate = 0.5;

	// Operators

	@JsonProperty("crossoverType")
	private int crossoverType = 1;

	@JsonProperty("crossoverRate")
	private double proCrossover = 0.8;

	@JsonProperty("mutationType")
	private int mutationType = 1;

	@JsonProperty("mutationRate")
	private double proMutation = 0.01;

	// Shapes or axioms to evaluate

	@JsonProperty("shapes")
	private String shapes;

	@JsonProperty("axioms")
	private String axioms;

	// Probabilistic SHACL

	@JsonProperty("shaclProb")
	private double probShaclP = 0.05;

	@JsonProperty("shaclAlpha")
	private double probShaclAlpha = 0.05;

	/**
	 * The timeout used for SPARQL queries <i>(default value: 600,000 ms)</i>
	 */
	@JsonProperty("sparqlTimeOut")
	private long sparqlTimeOut = 600000;

	// @TODO: map it on json input when the implementation is finished
	private boolean useNoveltySearch = false;


//	@Option(name = "-cs", aliases = { "--classic-shacl" }, usage = "use classic SHACL validation", metaVar = "CLASSIC_SHACL")
	public boolean useClassicShaclMode = false;

//	@Option(name = "-psh", aliases = { "--probabilistic-shacl" }, usage = "use classic SHACL validation", metaVar = "CLASSIC_SHACL")
	public boolean useProbabilisticShaclMode = false;


//	@Option(name = "-ns", aliases = { "--novelty-search" }, usage = "use Novelty Search approach", metaVar = "NOVELTY_SEARCH")
//	public boolean useNoveltySearch = false;

	// GENERATOR
//	@Option(name = "-ra", aliases = { "--random-axiom" }, usage = "use the random axiom generator")
	public boolean useRandomAxiomGenerator = false;

//	@Option(name = "-rs", aliases = { "--random-shapes" }, usage = "enable SHACL Shapes mining", metaVar = "SHAPES")
	public boolean useRandomShaclShapeGenerator = false;

//	@Option(name = "-st", aliases = {
//				"--sparql-timeout" }, usage = "time-out (in ms.) for SPARQL Query", metaVar = "TIMEOUT")


//	@Option(name = "-tc", aliases = {
//			"--time-cap" }, usage = "time-cap (in min.) for axiom mining assessment", metaVar = "TIMECAP")
	public long timeCap = 0;

//	@Option(name = "-l", aliases = {
//			"--loop" }, usage = "Launch SubClassOf assessment with loop operator from Corese", metaVar = "LOOP_CORESE")
	public boolean loop = false;

	// **************************************************//
	// List of parameters as the input for GE operation //
	// **************************************************//

//	@Option(name = "-ge", aliases = {
//			"--grammatical-evolution" }, usage = "activate the grammatical evolution for the entities extraction", metaVar = "GRAMMATICAL_EVOLUTION")
	public boolean grammaticalEvolution;

//	@Option(name = "-mxc", aliases = {
//			"--max-codon" }, usage = "use as this value as the max value of codon", metaVar = "MAX_CODON")
	public int maxValCodon = Integer.MAX_VALUE;

//	@Option(name = "-ckp", aliases = { "--Checkpoint" }, usage = "Checkpoint", metaVar = "CHECK_POINT")
	public int checkpoint = 1;
//
//	// receives other command line parameters than options
//	@Argument
//	public List<String> arguments = new ArrayList<>();


	public String getUserID() {
		return userID;
	}

	public String getProjectName() {
		return projectName;
	}

	public int getMod() {
		return mod;
	}

	public String getPrefixes() {
		return prefixes;
	}

	public String getNamedDataGraph() {
		return namedDataGraph;
	}

	public String getGrammar() {
		return grammar;
	}

	public int getPopulationSize() {
		return populationSize;
	}

	public int getStopCriterion() {
		return stopCriterion;
	}

	public int getEffort() {
		return effort;
	}

	public int getMaxMiningTime() {
		return maxMiningTime;
	}

	public int getSizeChromosome() {
		return sizeChromosome;
	}

	public int getMaxWrap() {
		return maxWrap;
	}

	public double getEliteSelectionRate() {
		return eliteSelectionRate;
	}

	public double getTournamentSelectionRate() {
		return tournamentSelectionRate;
	}

	public int getSelectionType() {
		return selectionType;
	}

	public double getSelectionRate() {
		return selectionRate;
	}

	public int getCrossoverType() {
		return crossoverType;
	}

	public double getProCrossover() {
		return proCrossover;
	}

	public int getMutationType() {
		return mutationType;
	}

	public double getProMutation() {
		return proMutation;
	}

	public String getShapes() {
		return shapes;
	}

	public String getAxioms() {
		return axioms;
	}

	public double getProbShaclP() {
		return probShaclP;
	}

	public double getProbShaclAlpha() {
		return probShaclAlpha;
	}

	public long getSparqlTimeOut() {
		return sparqlTimeOut;
	}

	public boolean isUseNoveltySearch() {
		return useNoveltySearch;
	}

	public static Parameters getInstance() {
		if(Parameters.instance == null) {
			Parameters.instance = new Parameters();
		}
		return Parameters.getInstance();
	}

}
