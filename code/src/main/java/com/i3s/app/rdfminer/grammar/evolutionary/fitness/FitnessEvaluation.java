package com.i3s.app.rdfminer.grammar.evolutionary.fitness;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.log4j.Logger;

import com.i3s.app.rdfminer.axiom.Axiom;
import com.i3s.app.rdfminer.axiom.AxiomFactory;
import com.i3s.app.rdfminer.grammar.evolutionary.individual.GEIndividual;
import Individuals.FitnessPackage.BasicFitness;
import Mapper.Symbol;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * FitnessEvaluation - is the class to setup the fitness value for Axioms in the
 * specified population
 *
 * @author NGUYEN Thu Huong Oct.18
 */
public class FitnessEvaluation {
	private static Logger logger = Logger.getLogger(FitnessEvaluation.class.getName());

	protected Axiom a;
	protected long t0, t;
	// protected int numSuccessAxioms=0;
	double referenceCardinality = 0.0;
	double possibility = 0.0;
	double necessity = 0.0;
	double generality = 0.0;
	double complexity_penalty = 0.0;

	public FitnessEvaluation() {

	}

	/*
	 * Updates the fitness values based on the possibility and necessity degrees
	 * fitness= cardinality * (possibility + necessity)/2
	 * 
	 */

	void update(ArrayList<GEIndividual> population, int curGeneration, int totalGeneration, WritableSheet sheet)
			throws JAXBException, IOException, SQLException, Exception {
		int i = 0;
		if (sheet == null) {
		} else {
			logger.info("EVALUATING AXIOMS AGAINST TO THE RDF DATA oF THE WHOLE DBPEDIA");
		}
		String arg1 = "";
		String arg2 = "";
		String axiom = "";
		double f = 0;
		// String result_collate=""; // contains the result of the collation of Axioms
		// with Gold standard matrix
		int Popsize = population.size();
		int Row = 1;
		while (i < Popsize) {

			GEIndividual indivi = (GEIndividual) population.get(i);
			if (population.get(0).getPhenotype() == null)
				break;
			axiom = population.get(i).getPhenotype().getStringNoSpace();
			logger.info(
					"....................................................................................................................................................");
			logger.info(axiom);
			if (indivi.isMapped()) {

				try {

					a = AxiomFactory.create(indivi.getPhenotype());
					List<List<Symbol>> arguments = a.argumentClasses;
					// logger.info("Argument of axioms:"+ arguments.get(0) + " and " +
					// arguments.get(1));
					List<String> complexClass1 = new ArrayList<String>();
					int SizeArg0 = arguments.get(0).size();
					for (int k = 0; k < SizeArg0; k++) {
						arg1 = arguments.get(0).get(k).getSymbolString();
						if (!arg1.equals("(") && !arg1.equals(")") && !arg1.equals(" ")) {
							complexClass1.add(arg1);
						}
					}

					List<String> complexClass2 = new ArrayList<String>();
					int SizeArg1 = arguments.get(1).size();
					for (int l = 0; l < SizeArg1; l++) {
						arg2 = arguments.get(1).get(l).getSymbolString();
						if (!arg2.equals("(") && !arg2.equals(")") && !arg2.equals(" ")) {
							complexClass2.add(arg2);
						}
					}

					/*
					 * checkGoldStandard=
					 * GoldStandardComparison.CheckDisjointnessComplexClasses(complexClass1,
					 * complexClass2, goldstandard.getGoldStandard());
					 * 
					 * logger.info("GoldStandard collation: " + checkGoldStandard ); if
					 * (Integer.parseInt(checkGoldStandard)>0) { result_collate = "disjointness";
					 * numTrueGoldStandard++; indivi.setEvaluated(true); } else { result_collate =
					 * "not disjointness"; indivi.setEvaluated(false); }
					 * writer.println("Gold standard collation: " + result_collate + "(" +
					 * checkGoldStandard + ")");
					 */

					// t=System.currentTimeMillis();
				} // try
				catch (QueryExceptionHTTP httpError) {
					logger.error("HTTP Error " + httpError.getMessage() + " making a SPARQL query.");
					httpError.printStackTrace();
					System.exit(1);
				} // catch 1
				catch (JenaException jenaException) {
					logger.error("Jena Exception " + jenaException.getMessage() + " making a SPARQL query.");
					jenaException.printStackTrace();
					System.exit(1);
				} // catch 2
				generality = a.generality;
				if (a != null) {
					// Save an XML report of the test:

					possibility = a.possibility().doubleValue();
					// necessity = a.necessity().doubleValue();

					// logger.info("Time to evaluate fitness: " + dt + "ms");

					logger.info("Reference Cardinality: " + a.referenceCardinality + "			num_Confirmation: "
							+ a.numConfirmations + "			Exception: " + a.numExceptions);
					logger.info("Generality: " + a.generality);
					logger.info("Possibility " + possibility);
					// logger.info("time testing: " + a.timeTesting);
					if (sheet == null) {
						// complexity_penalty=a.costGP();
						// logger.info("Complexity penalty: " + complexity_penalty);
						// f=a.possibility().doubleValue() *
						// Math.pow(a.generality,2)*10/complexity_penalty;
						f = a.possibility().doubleValue() * a.generality;
						logger.info("Fitness: " + f);
					}

					/*
					 * report.elapsedTime = dt; report.referenceCardinality =
					 * a.referenceCardinality; report.numConfirmations = a.numConfirmations;
					 * report.numExceptions = a.numExceptions; report.possibility = possibility;
					 * report.necessity = necessity;
					 */
					// int midpoint = (totalGeneration/2) + 1;
					// if (curGeneration < midpoint)

					// f=a.generality/a.timeTesting;
					// else

					// f = a.generality*(a.possibility().doubleValue() +
					// a.necessity().doubleValue())/2;
					// indivi.setAxiom(a);
				} // if

				// }
				else {

					f = 0;
					// complexity_penalty=0;
				}
			} else {

				f = 0;
				// complexity_penalty=0;
			}
			/*
			 * FitnessAxioms fit2 = new FitnessAxioms(f,possibility,
			 * necessity,population.get(i)); fit2.setIndividual(population.get(i));
			 * fit2.getIndividual().setValid(true); population.get(i).setFitness(fit2);
			 */
			BasicFitness fit = new BasicFitness(f, population.get(i));
			fit.setIndividual(population.get(i));
			fit.getIndividual().setValid(true);
			population.get(i).setFitness(fit);

			if (sheet != null) {

				// Row= sheet.getRows();
				// sheet.addCell(new Label(0, Row, axiom));
				if (indivi.isMapped()) {

					// double ARI = a.possibility().doubleValue() + a.necessity().doubleValue() - 1;
					sheet.addCell(new Number(7, Row, a.possibility().doubleValue()));
					// sheet.addCell(new Number(10, Row, a.necessity().doubleValue()));
					// sheet.addCell(new Number(11, Row, ARI));
					sheet.addCell(new Number(8, Row, a.referenceCardinality));
					sheet.addCell(new Number(9, Row, a.generality));
					// sheet.addCell(new Number(11, Row, a.costGP()));
				}

				// sheet.addCell(new Number(12, Row, indivi.getFitness().getDouble()));
				Row++;
			}
			// if (f>0) {numSuccessAxioms ++;}
			// marshaller.marshal(report, xmlStream);
			// xmlStream.flush();
			i++;
		}

		// st.StopVirtuoso();

	}

	/*
	 * int getnumSuccessAxioms() // return the number of axioms being mapped
	 * successfully. { return numSuccessAxioms; }
	 */
	/*
	 * int getnumTrueGoldStandard() // return the number of axioms being
	 * disjointness following to the Gold Standard { return numTrueGoldStandard; }
	 */

	public void update2(GEIndividual indivi, /* GoldStandardComparison goldstandard, */ int curGeneration,
			int totalGeneration) throws IOException, InterruptedException // evaluation fitness for each individual

	{
		String arg1 = "";
		String arg2 = "";
		// String checkGoldStandard="";
		double f = 0;
		// int midpoint= (totalGeneration/2) + 1;
		logger.info("axiom: " + indivi.getPhenotype().getStringNoSpace());
		logger.info("chromosome:" + indivi.getGenotype().toString());

		// st.StopVirtuoso();

		if (indivi.isMapped()) {

			try {
				// st.StartVirtuoso();

				a = AxiomFactory.create(indivi.getPhenotype());

				// st.StopVirtuoso();

				List<List<Symbol>> arguments = a.argumentClasses;
				// logger.info("Argument of axioms:"+ arguments.get(0) + " and " +
				// arguments.get(1));
				List<String> complexClass1 = new ArrayList<String>();
				int SizeArg0 = arguments.get(0).size();
				for (int k = 0; k < SizeArg0; k++) {
					arg1 = arguments.get(0).get(k).getSymbolString();
					if (!arg1.equals("(") && !arg1.equals(")") && !arg1.equals(" ")) {
						complexClass1.add(arg1);
					}
				}

				List<String> complexClass2 = new ArrayList<String>();
				int SizeArg1 = arguments.get(1).size();
				for (int l = 0; l < SizeArg1; l++) {
					arg2 = arguments.get(1).get(l).getSymbolString();
					if (!arg2.equals("(") && !arg2.equals(")") && !arg2.equals(" ")) {
						complexClass2.add(arg2);
					}
				}

				/*
				 * checkGoldStandard=
				 * GoldStandardComparison.CheckDisjointnessComplexClasses(complexClass1,
				 * complexClass2, goldstandard.getGoldStandard());
				 * 
				 * // logger.info("GoldStandard collation: " + checkGoldStandard ); if
				 * (Integer.parseInt(checkGoldStandard)>0) { indivi.setEvaluated(true); } else {
				 * indivi.setEvaluated(false); }
				 */
			} // try
			catch (QueryExceptionHTTP httpError) {
				logger.error("HTTP Error " + httpError.getMessage() + " making a SPARQL query.");
				httpError.printStackTrace();
				System.exit(1);
			} // catch 1
			catch (JenaException jenaException) {
				logger.error("Jena Exception " + jenaException.getMessage() + " making a SPARQL query.");
				jenaException.printStackTrace();
				System.exit(1);
			} // catch 2

			if (a != null) {
				// logger.info("mapped=YES");
				referenceCardinality = a.referenceCardinality;
				// logger.info("referenceCardinality: " + referenceCardinality);
				// logger.info("confirmation:" + a.numConfirmations);
				// logger.info("numExceptions:" + a.numExceptions);
				possibility = a.possibility().doubleValue();
				// logger.info("possibility " + possibility);
				necessity = a.necessity().doubleValue();
				// logger.info("necessity : " + necessity );
				// indivi.setAxiom(a);
				generality = a.generality;
				// logger.info("generality " + generality);
				// logger.info("hereeeee");
				// complexity_penalty=a.costGP();
				// logger.info("complexity: " + complexity_penalty);
				// if (curGeneration < midpoint)
				// f=generality/a.timeTesting;
				// else
				// f=Math.pow(generality,2)*possibility*10/complexity_penalty;
				f = generality * possibility;

			} // if(a!=null)

			else {
				// dt=t-t0;
				referenceCardinality = 0;
				// complexity_penalty=0;
				generality = 0;
				possibility = 0;
				// necessity=0;
				f = 0;

			} // end else

		} // if(indivi.isMapped ==true
		else // else (indivi.isMapped =false)
		{
			referenceCardinality = 0;
			// complexity_penalty=0;
			possibility = 0;
			necessity = 0;
			generality = 0;
			f = 0;
		}

		BasicFitness fit = new BasicFitness(f, indivi);
		fit.setIndividual(indivi);
		fit.getIndividual().setValid(true);
		indivi.setFitness(fit);
		// logger.info("fitness: " + f);

		// logger.info("Fitness:" + population.get(i).getFitness().getDouble());
		// logger.info("Fitness:" + fit.getDouble());

	}

	public static String removeCharAt(String s, int pos) {
		return s.substring(0, pos) + s.substring(pos + 1);
	}

	public void display(ArrayList<GEIndividual> Population, int curGeneration, WritableSheet sheet)
			throws IOException, RowsExceededException, WriteException {

		String axiom = "";
		String chromosome = "";
		// String result_collate="";
		logger.info("==================================================================");
		logger.info("CANDIDATE POPULATION IN GENERATION: " + curGeneration);
		int index = Population.size();
		int Row;
		for (int i = 0; i < index; i++)

		{
			axiom = Population.get(i).getPhenotype().getStringNoSpace();
			chromosome = Population.get(i).getGenotype().toString();
			GEIndividual indivi = (GEIndividual) Population.get(i);
			if (Population.get(0).getPhenotype() == null)
				break;
			/*
			 * report.generation= population.get(i).getAge(); report.axiom =axiom;
			 * report.chromosome= chromosome;
			 * report.mapped=String.valueOf(indivi.isMapped());
			 */
			// writer.println("................................................................................................................................................");
			// writer.println(axiom);
			// writer.println();

			// writer.println(chromosome.substring(1, chromosome.length()-1));
			// writer.println("Mapped: " + String.valueOf(indivi.isMapped()));

			logger.info(axiom);
			logger.info(chromosome.substring(1, chromosome.length() - 1));
			logger.info("Mapped: " + String.valueOf(indivi.isMapped()));
			/*
			 * if(indivi.isEvaluated()) { result_collate="disjointness";
			 * numTrueGoldStandard++; } else result_collate = "not disjointness";
			 * 
			 * logger.info("GoldStandard collation: " + result_collate );
			 * 
			 * writer.println("Gold standard collation: " + result_collate);
			 */

			logger.info("Fitness: " + indivi.getFitness().getDouble());

			if (sheet != null) {

				Row = sheet.getRows();
				sheet.addCell(new Label(0, Row, axiom));
				if (indivi.isMapped()) {
					Axiom a = AxiomFactory.create(indivi.getPhenotype());
					// double ARI = a.possibility().doubleValue() + a.necessity().doubleValue() - 1;
					// complexity_penalty=a.costGP();
					sheet.addCell(new Number(1, Row, a.possibility().doubleValue()));
					// sheet.addCell(new Number(2, Row, a.necessity().doubleValue()));
					// sheet.addCell(new Number(3, Row, ARI));
					sheet.addCell(new Number(2, Row, a.referenceCardinality));
					sheet.addCell(new Number(3, Row, a.generality));
					// sheet.addCell(new Number(4, Row, complexity_penalty));
					// logger.info("complexity_penalty:" + complexity_penalty);
				}

				sheet.addCell(new Number(5, Row, indivi.getFitness().getDouble()));
				sheet.addCell(new Label(6, Row, Boolean.toString(indivi.isMapped())));
			}
		}

	}

}