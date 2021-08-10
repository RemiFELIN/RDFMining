/**
 * 
 */
package com.i3s.app.rdfminer.parameters;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import com.i3s.app.rdfminer.Global;

/**
 * A container of command line parameters and options.
 * 
 * @author Andrea G. B. Tettamanzi
 *
 */
public class CmdLineParameters {

	@Option(name = "-h", aliases = { "--help" }, metaVar = "HELP")
	public boolean help;

	@Option(name = "-a", aliases = { "--axioms" }, usage = "test axioms contained in this file", metaVar = "AXIOMFILE")
	public String axiomFile = null;

	@Option(name = "-sa", aliases = { "--single-axiom" }, usage = "test a single axiom given", metaVar = "AXIOM")
	public String axiom = null;
	
	/**
	 * The angular coefficient to be used for dynamic time capping of axiom test.
	 * <p>
	 * If this parameter is zero, time capping is performed using the value of the
	 * {@link #timeOut} parameter.
	 * </p>
	 * <p>
	 * If this parameter is different from zero, its value is taken to mean the
	 * angular coefficient <var>b</var> of the linear equation <var>T</var> =
	 * <var>a</var> + <var>b</var>TP, where <var>a</var> is the value of the
	 * {@link #timeOut} parameter and TP is the <em>time predictor</em>, computed,
	 * for subsumption axioms, as the product of the reference cardinality of the
	 * subclass and of the number of classes sharing at least one instance with it.
	 * </p>
	 */
	@Option(name = "-d", aliases = {
			"--dynamic-timeout" }, usage = "use a dynamic time-out for axiom testing", metaVar = "ANGULAR_COEFF")
	public double dynTimeOut = 0.0;

	@Option(name = "-g", aliases = { "--grammar" }, usage = "use this file as the axiom grammar", metaVar = "GRAMMAR")
	public String grammarFile = System.getenv("HOME") + "code/resources/OWL2Axiom-test.bnf";

	@Option(name = "-o", aliases = {
			"--output" }, usage = "path of output file", metaVar = "RESULTFILE")
	public String resultFile = Global.OUTPUT_PATH + "results.json";

	@Option(name = "-r", aliases = { "--random" }, usage = "test randomly generated axioms")
	public boolean useRandomAxiomGenerator = false;

	@Option(name = "-s", aliases = {
			"--subclasslist" }, usage = "test subClassOf axioms generated from the list of subclasses in the given file", metaVar = "FILE")
	public String subclassList = null;

	@Option(name = "-t", aliases = {
			"--timeout" }, usage = "use this time-out (in seconds) for axiom testing", metaVar = "SECONDS")
	public long timeOut = 0;
	
	//**************************************************//
	// List of parameters as the input for GE operation //
	//**************************************************//
	
	@Option(name = "-ge", aliases = {
			"--grammatical-evolution" }, usage = "activate the grammatical evolution for the axiom's extraction", metaVar = "GRAMMATICAL_EVOLUTION")
	public boolean grammaticalEvolution;
	
	@Option(name = "-ps", aliases = {
			"--population-size" }, usage = "use as this value as the initial size of population", metaVar = "POPULATION_SIZE")
	public int populationsize = 200;
	
	@Option(name = "-ngen", aliases = {
			"--n-generation" }, usage = "use as this value as the number of generation", metaVar = "GENERATION_NUMBER")
	public int numGeneration = 5;
	
	@Option(name = "-init", aliases = {
			"--initlen" }, usage = "use as this value as the initial length of chromosome", metaVar = "INITLEN_CHROMOSOME")
	public int initlenChromosome = 20;

	@Option(name = "-mxw", aliases = {
			"--maxwrapp" }, usage = "use as this value as the max number of wrapping", metaVar = "MAX_WRAPP")
	public int maxWrapp = 1;

	@Option(name = "-mxc", aliases = {
			"--maxcodon" }, usage = "use as this value as the max value of codon", metaVar = "MAX_CODON")
	public int maxvalCodon = Integer.MAX_VALUE;

	@Option(name = "-tinit", aliases = {
			"--typeinit" }, usage = "use as this value as the type of initialization", metaVar = "TYPE_INITIALIZATION")
	public int typeInitialization = 1; // 1- random initialization ; 2 - ....... - need TO DO

	@Option(name = "-pc", aliases = {
			"--probcross" }, usage = "use as this value as the probability of crossover operation", metaVar = "PROB_CROSSOVER")
	public double proCrossover = 0.8;

	@Option(name = "-pm", aliases = {
			"--probmut" }, usage = "use as this value as the probability of mutation operation", metaVar = "PROB_MUTATION")
	public double proMutation = 0.01;

	@Option(name = "-twi", aliases = {
			"--twin" }, usage = "use as this value as the chose of twin acception", metaVar = "TWIN_SELECTION")
	public int twin = 1; // 0 - the twin individuals are not accepted; 1 - accepted

	@Option(name = "-shf", aliases = {
			"--shuffle" }, usage = "use as this value as the chose of shuffle list", metaVar = "SHUFFLE_SELECTION")
	public int shuffle = 1; // 1- shuffle list of chromosomes; other numbers -needn't to shuffle list of
							// chromosomes;

	@Option(name = "-se", aliases = {
			"--typeselect" }, usage = "use as this value as the type of parent selection operation", metaVar = "TYPE_SELECTION")
	public int typeselect = 2; // 1- Roulette wheel; 2- truncation; 3- Tournament ; other numbers- normal way

	@Option(name = "-cr", aliases = {
			"--typeCrossover" }, usage = "use as this value as the type of parent selection operation", metaVar = "TYPE_CROSSOVER")
	public int typecrossover = 2; // 1- Single-point Crossover; 2- Subtree Crossover

	@Option(name = "-sez", aliases = {
			"--sizeselect" }, usage = "use as this value as the size of parent selection operation", metaVar = "SIZE_SELECTION")
	public double sizeSelection = 0.7;

	/*
	 * @Option (name="-sezm", aliases= {"--sizeMutationselect"} , usage=
	 * "use as this value as the size of mutation selection operation",
	 * metaVar="SIZE_MUTATIONSELECTIOn") public double sizeMutationSelection=0.4;
	 */

	@Option(name = "-el", aliases = {
			"--elitism" }, usage = "use as this value as the choose of elitism selection", metaVar = "ELITISM_SELECTION")
	public int elitism = 1; // 0- not applying etilism method; 1- applying etilism method

	@Option(name = "-seez", aliases = {
			"--sizeelitie" }, usage = "use as this value as the size of elitism selection", metaVar = "TYPE_SELECTION")
	public double sizeElite = 0.02;

	@Option(name = "-div", aliases = {
			"--diversity" }, usage = "use as this value as the chose of diversity method", metaVar = "DIVER_METHOD")
	public int diversity = 1; // 0- not use; 1- crowding method

	// Matrix Gold Standard
	@Option(name = "-gsd", aliases = {
			"--GoldStandard" }, usage = "use as this value as the input Goldstandard file", metaVar = "GOLD_STANDARD")
	public String GoldStandard = "GoldStandard.xlsx";

	// Results
	@Option(name = "-bf", aliases = {
			"--BufferFile" }, usage = "use as this value as the name of buffer file of chromosome for next generation", metaVar = "BUFFER FILE")
	public String Bufferfile = "buffer";

	@Option(name = "-sre", aliases = {
			"--StatisticsResult" }, usage = "use as this value as the name of output statistics result file", metaVar = "STATISTICS_RESULT")
	public String StatisticsResult = "StatisticsResult.json";

	@Option(name = "-fax", aliases = {
			"--FileAxioms" }, usage = "use as this value as the name of output statistics axioms", metaVar = "STATISTICS_AXIOMS")
	public String FileAxioms = "AxiomsStatistics";

	@Option(name = "-kb", aliases = { "--K_Base" }, usage = "KBase", metaVar = "K_BASE")
	public int k_base = 5000;

	@Option(name = "-ckp", aliases = { "--Checkpoint" }, usage = "Checkpoint", metaVar = "CHECK_POINT")
	public int checkpoint = 3;
	
	// receives other command line parameters than options
	@Argument
	public List<String> arguments = new ArrayList<String>();
	
}
