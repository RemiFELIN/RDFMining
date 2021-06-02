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
	public List<String> exceptions, confirmations;
	
	public AxiomTestXML()
	{
		axiom = "";
		exceptions = confirmations = null;
	}
}
