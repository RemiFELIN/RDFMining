/**
 * 
 */
package com.i3s.app.rdfminer.parameters;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

/**
 * A container of command line parameters and options.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class CmdLineParameters {

	@Option(name = "-h", aliases = { "--help" }, metaVar = "HELP")
	public boolean help;

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
	public String prefixesFile = null;

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
	public String grammarFile = System.getenv("HOME") + "code/resources/OWL2Axiom-test.bnf";

	@Option(name = "-dir", aliases = { "--directory" }, usage = "path of output folder", metaVar = "RESULTFOLDER")
	public String resultFolder = "results";

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
	public int maxWrapp = 1;

	@Option(name = "-mxc", aliases = {
			"--max-codon" }, usage = "use as this value as the max value of codon", metaVar = "MAX_CODON")
	public int maxValCodon = Integer.MAX_VALUE;

	@Option(name = "-tinit", aliases = {
			"--type-init" }, usage = "use as this value as the type of initialization", metaVar = "TYPE_INITIALIZATION")
	public int typeInitialization = 1; // 1- random initialization ; 2 - ....... - TODO

	@Option(name = "-pc", aliases = {
			"--prob-cross" }, usage = "use as this value as the probability of crossover operation", metaVar = "PROB_CROSSOVER")
	public double proCrossover = 0.8;

	@Option(name = "-pm", aliases = {
			"--prob-mut" }, usage = "use as this value as the probability of mutation operation", metaVar = "PROB_MUTATION")
	public double proMutation = 0.01;

	@Option(name = "-se", aliases = {
			"--type-select" }, usage = "use as this value as the type of parent selection operation", metaVar = "TYPE_SELECTION")
	public int typeSelect = 2; // 1- Roulette wheel; 2- truncation; 3- Tournament ; other numbers- normal way

	@Option(name = "-cr", aliases = {
			"--type-crossover" }, usage = "use as this value as the type of parent selection operation", metaVar = "TYPE_CROSSOVER")
	public int typeCrossover = 0; // 0-; 1- Single-point Crossover; 2- Subtree Crossover

	@Option(name = "-sez", aliases = {
			"--size-select" }, usage = "use as this value as the size of parent selection operation", metaVar = "SIZE_SELECTION")
	public double sizeSelection = 0.7;

//	@Option(name = "-sezm", aliases = {
//			"--sizeMutationselect" }, usage = "use as this value as the size of mutation selection operation", metaVar = "SIZE_MUTATIONSELECTIOn")
//	public double sizeMutationSelection = 0.4;

	@Option(name = "-el", aliases = {
			"--elitism" }, usage = "use as this value as the choose of elitism selection", metaVar = "ELITISM_SELECTION")
	public int elitism = 1; // 0- not applying elitism method; 1- applying elitism method

	@Option(name = "-seez", aliases = {
			"--size-elite" }, usage = "use as this value as the size of elitism selection", metaVar = "SIZE_ELITE")
	public double sizeElite = 0.02;

	@Option(name = "-div", aliases = {
			"--diversity" }, usage = "use as this value as the chose of diversity method", metaVar = "DIVER_METHOD")
	public int diversity = 1; // 0- not use; 1- crowding method

	@Option(name = "-kb", aliases = { "--K_Base" }, usage = "KBase", metaVar = "K_BASE")
	public int kBase = 5000;

	@Option(name = "-ckp", aliases = { "--Checkpoint" }, usage = "Checkpoint", metaVar = "CHECK_POINT")
	public int checkpoint = 1;

	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<>();

}
