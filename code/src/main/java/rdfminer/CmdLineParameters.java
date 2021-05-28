/**
 * 
 */
package rdfminer;

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
public class CmdLineParameters
{
	
	@Option(name = "-h", aliases = { "--help" },
			metaVar = "HELP")
	public boolean help;
	
	@Option(name = "-a", aliases = { "--axioms" },
			usage = "test axioms contained in this file",
			metaVar = "AXIOMFILE")
	public String axiomFile = null;

	/**
	 *  The angular coefficient to be used for dynamic time capping of axiom test.
	 *  <p>If this parameter is zero, time capping is performed using the value of the {@link #timeOut} parameter.</p>
	 *  <p>If this parameter is different from zero, its value is taken to mean the angular coefficient <var>b</var>
	 *  of the linear equation <var>T</var> = <var>a</var> + <var>b</var>TP,
	 *  where <var>a</var> is the value of the {@link #timeOut} parameter
	 *  and TP is the <em>time predictor</em>, computed, for subsumption axioms,
	 *  as the product of the reference cardinality of the subclass and of the number
	 *  of classes sharing at least one instance with it.</p>
	 */
	@Option(name = "-d", aliases = { "--dynamic-timeout" },
			usage = "use a dynamic time-out for axiom testing",
			metaVar = "ANGULAR_COEFF")
	public double dynTimeOut = 0.0;
	
	@Option(name = "-g", aliases = { "--grammar" },
			usage = "use this file as the axiom grammar",
			metaVar = "GRAMMAR")
	public String grammarFile = System.getenv("HOME") + "code/resources/OWL2Axiom-test.bnf";

	@Option(name = "-o", aliases = { "--output" },
			usage = "name of output results files: a XML and CSV file",
			metaVar = "RESULTFILE")
	public String resultFile = System.getenv("HOME") + "data/results";
	
	@Option(name = "-r", aliases = { "--random"},
			usage = "test randomly generated axioms")
	public boolean useRandomAxiomGenerator = false;

	@Option(name = "-s", aliases = { "--subclasslist"},
			usage = "test subClassOf axioms generated from the list of subclasses in the given file",
			metaVar = "FILE")
	public String subclassList = null;

	@Option(name = "-t", aliases = { "--timeout" },
			usage = "use this time-out (in minutes) for axiom testing",
			metaVar = "MINUTES")
	public long timeOut = 0;
	
    // receives other command line parameters than options
    @Argument
    public List<String> arguments = new ArrayList<String>();
}
