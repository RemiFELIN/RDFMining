/**
 * 
 */
package rdfminer;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A wrapper for an axiom test report, to be used, for instance, for XML serialization.
 * @author Andrea G. B. Tettamanzi
 *
 */
@XmlRootElement
public class AxiomTest
{
	public String axiom;
	public int referenceCardinality, numConfirmations, numExceptions;
	public List<String> exceptions, confirmations;
	public double possibility, necessity;
	public long elapsedTime; // the time it took to test the axiom, in ms.
	
	AxiomTest()
	{
		axiom = "";
		referenceCardinality = numConfirmations = numExceptions = 0;
		exceptions = confirmations = null;
		possibility = necessity = 0.0;
		elapsedTime = 0L;
	}
}
