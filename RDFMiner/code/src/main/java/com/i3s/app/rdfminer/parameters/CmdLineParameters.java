/**
 * 
 */
package com.i3s.app.rdfminer.parameters;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import java.util.ArrayList;
import java.util.List;

/**
 * A container of command line parameters and options.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class CmdLineParameters {

	@Option(name = "-prod", aliases = { "--production" }, usage = "use RDFMiner in production", metaVar = "PRODUCTION")
	public boolean production = false;

	@Option(name = "-h", aliases = { "--help" }, metaVar = "HELP")
	public boolean help;

	@Option(name = "-user", aliases = { "--username" }, usage = "Specify the username", metaVar = "SHAPES_FILE")
	public String username = "admin";

	@Option(name = "-sa", aliases = { "--single-axiom" }, usage = "test a single axiom given", metaVar = "AXIOM")
	public String singleAxiom = null;

	// FILES (evaluator part)
	@Option(name = "-sf", aliases = { "--shapes-file" }, usage = "test shapes contained in this file", metaVar = "SHAPES_FILE")
	public String shapeFile = null;

	// Probabilistic SHACL
	@Option(name = "-shacl-p", aliases = { "--shacl-probability" }, usage = "set the value of p for SHACL probabilistic mode", metaVar = "SHACL_PROB_P")
	public String probShaclP = String.valueOf(0.05);

	@Option(name = "-shacl-a", aliases = { "--shacl-alpha" }, usage = "set the value of alpha for SHACL probabilistic mode (Hypothesis testing)", metaVar = "SHACL_PROB_A")
	public double alpha = 0.05;
	
	@Option(name = "-af", aliases = { "--axioms-file" }, usage = "test axioms contained in this file", metaVar = "AXIOM_FILE")
	public String axiomFile = null;

	@Option(name = "-p", aliases = { "--prefixes" }, usage = "use this file as the prefixes to be used in SPARQL queries", metaVar = "PREFIXES")
	public String prefixesFile = "prefixes.txt";

	@Option(name = "-cs", aliases = { "--classic-shacl" }, usage = "use classic SHACL validation", metaVar = "CLASSIC_SHACL")
	public boolean useClassicShaclMode = false;

	@Option(name = "-psh", aliases = { "--probabilistic-shacl" }, usage = "use classic SHACL validation", metaVar = "CLASSIC_SHACL")
	public boolean useProbabilisticShaclMode = false;

	@Option(name = "-target", aliases = { "--target-endpoint" }, usage = "specify the SPARQL endpoint to be used for sending requests", metaVar = "TARGET")
	public String targetSparqlEndpoint = null;

	@Option(name = "-train", aliases = { "--train-endpoint" }, usage = "specify the SPARQL endpoint to be used as a training dataset", metaVar = "TRAIN")
	public String trainSparqlEndpoint = null;

	@Option(name = "-ns", aliases = { "--novelty-search" }, usage = "use Novelty Search approach", metaVar = "NOVELTY_SEARCH")
	public boolean useNoveltySearch = false;

	/**
	 * The angular coefficient to be used for dynamic time capping of axiom test.
	 * <p>
	 * If this parameter is zero, time capping is performed using the value of the
	 * {@link #sparqlTimeOut} parameter.
	 * </p>
	 * <p>
	 * If this parameter is different from zero, its value is taken to mean the
	 * angular coefficient <var>b</var> of the linear equation <var>T</var> =
	 * <var>a</var> + <var>b</var>TP, where <var>a</var> is the value of the
	 * {@link #sparqlTimeOut} parameter and TP is the <em>time predictor</em>, computed,
	 * for subsumption axioms, as the product of the reference cardinality of the
	 * subclass and of the number of classes sharing at least one instance with it.
	 * </p>
	 */
	@Option(name = "-d", aliases = {
			"--dynamic-timeout" }, usage = "use a dynamic time-out for axiom testing", metaVar = "ANGULAR_COEFF")
	public double dynTimeOut = 0.0;

	@Option(name = "-g", aliases = { "--grammar" }, usage = "use this file as the axiom grammar", metaVar = "GRAMMAR")
	public String grammarFile = "grammar.bnf";

	@Option(name = "-dir", aliases = { "--directory" }, usage = "path of output folder", metaVar = "RESULTFOLDER")
	public String directory = "results";

	// GENERATOR
	@Option(name = "-ra", aliases = { "--random-axiom" }, usage = "use the random axiom generator")
	public boolean useRandomAxiomGenerator = false;

	@Option(name = "-rs", aliases = { "--random-shapes" }, usage = "enable SHACL Shapes mining", metaVar = "SHAPES")
	public boolean useRandomShaclShapeGenerator = false;

	@Option(name = "-s", aliases = {
			"--subclassof-list" }, usage = "test subClassOf axioms generated from the list of subclasses in the given file", metaVar = "FILE")
	public String subClassList = null;

	@Option(name = "-st", aliases = {
				"--sparql-timeout" }, usage = "time-out (in ms.) for SPARQL Query", metaVar = "TIMEOUT")
	public long sparqlTimeOut = 0;

	@Option(name = "-tc", aliases = {
			"--time-cap" }, usage = "time-cap (in min.) for axiom mining assessment", metaVar = "TIMECAP")
	public long timeCap = 0;

	@Option(name = "-l", aliases = {
			"--loop" }, usage = "Launch SubClassOf assessment with loop operator from Corese", metaVar = "LOOP_CORESE")
	public boolean loop = false;

	// **************************************************//
	// List of parameters as the input for GE operation //
	// **************************************************//

	@Option(name = "-ge", aliases = {
			"--grammatical-evolution" }, usage = "activate the grammatical evolution for the entities extraction", metaVar = "GRAMMATICAL_EVOLUTION")
	public boolean grammaticalEvolution;

	@Option(name = "-ps", aliases = {
			"--population-size" }, usage = "use as this value as the initial size of population", metaVar = "POPULATION_SIZE")
	public int populationSize = 200;

	@Option(name = "-init", aliases = {
			"--init-len" }, usage = "use as this value as the initial length of chromosome", metaVar = "INITLEN_CHROMOSOME")
	public int initLenChromosome = 20;

	@Option(name = "-mxw", aliases = {
			"--max-wrapp" }, usage = "use as this value as the max number of wrapping", metaVar = "MAX_WRAPP")
	public int maxWrapp = 1000;

	@Option(name = "-mxc", aliases = {
			"--max-codon" }, usage = "use as this value as the max value of codon", metaVar = "MAX_CODON")
	public int maxValCodon = Integer.MAX_VALUE;

	@Option(name = "-pc", aliases = {
			"--prob-cross" }, usage = "use as this value as the probability of crossover operation", metaVar = "PROB_CROSSOVER")
	public double proCrossover = 0.8;

	@Option(name = "-pm", aliases = {
			"--prob-mut" }, usage = "use as this value as the probability of mutation operation", metaVar = "PROB_MUTATION")
	public double proMutation = 0.01;

	@Option(name = "-se", aliases = {
			"--type-select" }, usage = "use as this value as the type of parent selection operation (1: Proportional Roulette Wheel; 2: Scaled Roulette Wheel; 3: Tournament)", metaVar = "TYPE_SELECTION")
	public int typeSelection = 3;

	@Option(name = "-cr", aliases = {
			"--type-crossover" }, usage = "use as this value as the type of crossover operation (1: Single Point; 2: Two Point; 3: Sub tree; 4: Customized Swap)", metaVar = "TYPE_CROSSOVER")
	public int typeCrossover = 1;

	@Option(name = "-mu", aliases = {
			"--type-mutation" }, usage = "use as this value as the type of mutation operation (1: Int Flip; 2: Int Flip Byte; 3: Nodal; 4: Sub tree)", metaVar = "TYPE_MUTATION")
	public int typeMutation = 1;

	@Option(name = "-er", aliases = {
			"--elite-rate" }, usage = "use as this value as the proportion of elited individuals to select", metaVar = "ELITE_RATE")
	public double eliteSelectionRate = 0.2;

	@Option(name = "-sr", aliases = {
			"--selection-rate" }, usage = "use as this value as the proportion of individuals to select for operations", metaVar = "SELECTION_RATE")
	public double selectionRate = 0.5;

	@Option(name = "-tr", aliases = {
			"--tournament-rate" }, usage = "use as this value as the proportion of individuals to select for operations (Only used if you have choosen the tournament selection)", metaVar = "SIZE_TOURNAMENT")
	public double tournamentSelectionRate = 0.1;

//	@Option(name = "-sezm", aliases = {
//			"--sizeMutationselect" }, usage = "use as this value as the size of mutation selection operation", metaVar = "SIZE_MUTATIONSELECTIOn")
//	public double sizeMutationSelection = 0.4;

	@Option(name = "-div", aliases = {
			"--diversity" }, usage = "use as this value as the chose of diversity method", metaVar = "DIVER_METHOD")
	public int diversity = 0; // 0- not use; 1- crowding method

	@Option(name = "-kb", aliases = { "--K_Base" }, usage = "KBase", metaVar = "K_BASE")
	public int kBase = 5000;

	@Option(name = "-time", aliases = { "--time" }, usage = "time allocated for the mining (in min)", metaVar = "TIME")
	public int maxTime = 180;

	@Option(name = "-ckp", aliases = { "--Checkpoint" }, usage = "Checkpoint", metaVar = "CHECK_POINT")
	public int checkpoint = 1;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<>();

}
