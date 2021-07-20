package com.i3s.app.rdfminer.grammar.evolutionary;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author NGUYEN Thu Huong Dec 18, 2017
 */
@XmlRootElement

public class Parameters_GE {

	public int PopulationSize, GenerationSize, MaximumWraping;
	public double proCrossover, proMutation;
	public String grammar;
	public String Chromosome, Individual;

	public Parameters_GE() {
		PopulationSize = 0;
		GenerationSize = 0;
		MaximumWraping = 0;
		proCrossover = 0.0;
		proMutation = 0.0;

	}

}
