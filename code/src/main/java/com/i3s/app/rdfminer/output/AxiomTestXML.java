/**
 * 
 */
package com.i3s.app.rdfminer.output;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A wrapper for an axiom test report, to be used for XML serialization.
 * @author Andrea G. B. Tettamanzi, Rémi FELIN
 *
 */
@XmlRootElement
public class AxiomTestXML
{
	public String axiom;
	// public int referenceCardinality, numConfirmations, numExceptions;
	public List<String> exceptions, confirmations;
	// public double possibility, necessity;
	// public long elapsedTime; // the time it took to test the axiom, in ms.
	
	public AxiomTestXML()
	{
		axiom = "";
		// referenceCardinality = numConfirmations = numExceptions = 0;
		exceptions = confirmations = null;
		// possibility = necessity = 0.0;
		// elapsedTime = 0L;
	}
}
