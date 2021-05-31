package com.i3s.app.rdfminer.output;

import com.opencsv.bean.CsvBindByPosition;

/**
 * A wrapper for an axiom test report, to be used for CSV serialization.
 * @author Rémi FELIN
 *
 */
public class AxiomTestCSV {
	
	public final static String COLUMNS_NAME = "axiom,referenceCardinality,numConfirmations,numExceptions,possibility,necessity,elapsedTime\n";
	
	@CsvBindByPosition(position = 0)
	public String axiom;
	
	@CsvBindByPosition(position = 1)
	public int referenceCardinality;
	
	@CsvBindByPosition(position = 2)
	public int numConfirmations;
	
	@CsvBindByPosition(position = 3)
	public int numExceptions;
	
	@CsvBindByPosition(position = 4)
	public double possibility;
	
	@CsvBindByPosition(position = 5)
	public double necessity;
	
	@CsvBindByPosition(position = 6)
	public long elapsedTime; // the time it took to test the axiom, in ms.
	
	public AxiomTestCSV()
	{
		axiom = "";
		referenceCardinality = numConfirmations = numExceptions = 0;
		possibility = necessity = 0.0;
		elapsedTime = 0L;
	}
	
}
