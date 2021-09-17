/**
 * 
 */
package com.i3s.app.goldStandard;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.log4j.*;

import com.i3s.app.rdfminer.sparql.SparqlEndpoint;

/**
 * GoldStandard Matrix provides the standard in the assessment of the
 * disjointness between two classes. A set of classes in this standard is
 * extracted from OWL2 classes in DBPedia (newest version 2016-10 at the current
 * time) Because of the huge number of classes in the version (736 classes),
 * currently, in the first step we only focus on experiment based on group of
 * classes belongs <http://dbpedia.org/Work> (with 63 subclasses) The
 * construction of the standard is combined by two ways: automatically and
 * manually and is performed alternately until getting the final gold standard
 * matrix. In the case of manually settings, the supports of experts in the
 * specific domain knowledge are required to evaluate the disjointness between
 * two classes.
 * 
 * @author NGUYEN Thu Huong Oct. 2018
 */
public class GoldStandardMatrix {

	/**
	 * @param args
	 */
	private static Logger logger = Logger.getLogger(GoldStandardMatrix.class.getName());
	private int i, j;
	public static int n;
	public static String[][] M = new String[n][n]; // Matrix Gold standard contained in this array.
	static String dbpedia2 = "http://localhost:8890/sparql"; //
	// static String dbpedia2 = "http://dbpedia.org/sparql"; // approach to online
	// DBPedia
	static String PREFIXES = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
			+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n" + "PREFIX dc: <http://purl.org/dc/elements/1.1/>\n"
			+ "PREFIX dbr: <http://dbpedia.org/resource/>\n" + "PREFIX dbp: <http://dbpedia.org/property/>\n"
			+ "PREFIX dbo: <http://dbpedia.org/ontology>\n";
	public static SparqlEndpoint endpoint;
	private static Scanner sc;
	public static String rootClass = "http://dbpedia.org/ontology/Work";

	public static void main(String[] args) throws IOException {

		// Configure the log4j loggers:
		PropertyConfigurator.configure("log4j.properties");
		sc = new Scanner(System.in);
		endpoint = new SparqlEndpoint(dbpedia2, PREFIXES);
		GoldStandardMatrix A = new GoldStandardMatrix();
		setMatrix(LoadMatrix());
		logger.info("Matrix size: " + M.length + "*" + M.length);
		A.Show();
		for (int i = 1; i < M.length; i++) {
			SetupSubClassOf(M[0][i]);
		}
		// String rootClass = "http://www.w3.org/2002/07/owl#Thing";
		GoldStandardGenerator(rootClass);
		A.Show();
		A.checkZero(getMatrix());
		A.WriteExcelFile(getMatrix());
	}

	public void Show() {
		for (i = 0; i < M.length; i++) {
			for (j = 0; j < M.length; j++) {
				System.out.print(M[i][j] + " ");
			}
			System.out.println();
		}

	}

	public static String[][] LoadMatrix() throws IOException {

		String sparql_countClasses = "(count(distinct ?class) AS ?count) where {?class a owl:Class. ?class rdfs:subClassOf+ <"
				+ rootClass + ">}";
		logger.info("Querying SPARQL endpoint for the number of subclasses of the class : <" + rootClass
				+ "> with query:\nSELECT " + SparqlEndpoint.prettyPrint(sparql_countClasses));
		ResultSet result1 = endpoint.select(sparql_countClasses, 0);
		QuerySolution solution1 = result1.next();
		Iterator<String> t = solution1.varNames();
		String varName1 = t.next();
		RDFNode node1 = solution1.get(varName1);
		n = Integer.parseInt(node1.toString().split("\\^")[0]) + 1;

		String sparql_listClasses = "distinct ?class where {?class a owl:Class. ?class rdfs:subClassOf+ <" + rootClass
				+ ">}";
		logger.info("Querying SPARQL endpoint for symbol <Class> with query:\nSELECT "
				+ SparqlEndpoint.prettyPrint(sparql_listClasses));
		ResultSet result2 = endpoint.select(sparql_listClasses, 0);

		int dem = 0;
		String[][] M = new String[n][n];
		M[0][0] = "GOLD STANDARD";
		while (result2.hasNext() && dem < n) {
			QuerySolution solution = result2.next();
			Iterator<String> i = solution.varNames();
			// String separator = "";
			while (i.hasNext()) {
				String varName = i.next();
				RDFNode node = solution.get(varName);
				dem++;
				M[dem][0] = node.toString();
				M[0][dem] = node.toString();
			}

		}
		for (int i1 = 1; i1 < n; i1++) {
			for (int j1 = 1; j1 < n; j1++) {
				if (i1 == j1)
					M[i1][j1] = "0";
				else
					M[i1][j1] = "*";
			}

		}
		return M;
	}

	public static String cacheName(String symbol, String sparql) {
		return String.format("%s%08x.cache", symbol, sparql.hashCode());
	}

	static boolean checkSibling(String Class, String Class2) {
		String sparql = "DISTINCT ?superClass WHERE { <" + Class
				+ "> rdfs:subClassOf ?superClass. FILTER regex(?superClass,<http://dbpedia.org/ontology*>)  }";
		String sparql2 = "DISTINCT ?superClass WHERE { <" + Class2
				+ "> rdfs:subClassOf ?superClass. FILTER regex(?superClass,<http://dbpedia.org/ontology*>) }";
		ArrayList<RDFNode> k1 = ResultQuery(endpoint, sparql);
		ArrayList<RDFNode> k2 = ResultQuery(endpoint, sparql2);
		if (k1.size() != 0 && k2.size() != 0 && (k1.get(0).toString().equals(k2.get(0).toString())))
			return true;
		else
			return false;
	}

	static boolean checkSubclass(String Class, String Class2) {

		String sparql = "DISTINCT ?superClass WHERE { <" + Class
				+ "> rdfs:subClassOf ?superClass. FILTER regex(?superClass,<http://dbpedia.org/ontology*>)  }";
		ArrayList<RDFNode> k1 = ResultQuery(endpoint, sparql);
		if ((k1.size() != 0) && (Class2.equals(k1.get(0).toString())))
			return true;
		else
			return false;
	}

	static ArrayList<String> ListSubClasses(String superClass) {
		String sparql = "DISTINCT ?Class WHERE { ?Class rdfs:subClassOf+ <" + superClass
				+ "> .FILTER regex(?Class,<http://dbpedia.org/ontology*>)}";
		ArrayList<RDFNode> k1 = ResultQuery(endpoint, sparql);
		ArrayList<String> List = new ArrayList<String>();
		for (int i = 0; i < k1.size(); i++) {
			List.add(k1.get(i).toString());
		}
		return List;
	}

	static ArrayList<RDFNode> ResultQuery(SparqlEndpoint endpoint, String sparql) {
		ArrayList<RDFNode> arrRDFNode = new ArrayList<RDFNode>();
		ResultSet result = endpoint.select(sparql, 0);
		while (result.hasNext()) {
			QuerySolution solution = result.next();
			Iterator<String> i = solution.varNames();

			while (i.hasNext()) {
				String varName = i.next();
				RDFNode node = solution.get(varName);
				arrRDFNode.add(node);
			}
		}
		return arrRDFNode;
	}

	static int Search(String Class, String[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[0][i].equals(Class)) {
				return i; // i here is the position of the specific class in the matrix
			}
		}
		return -1; // Cannot find the position of the specific class in the matrix
	}

	static void SetupSubClassOf(String Class) {
		int pos, pos1;
		ArrayList<String> ListSubClass = ListSubClasses(Class);
		pos1 = Search(Class, M);
		if (ListSubClass.size() != 0) {
			for (int j = 0; j < ListSubClass.size(); j++) {
				pos = Search(ListSubClass.get(j).toString(), M);
				if (pos >= 0) {
					M[pos1][pos] = "0";
					M[pos][pos1] = "0";
				}
			}
		}
	}

	static void SetUpSiblingDisjoint(String Class1, String Class2) {
		int pos11, pos22;
		int pos1 = Search(Class1, M);
		int pos2 = Search(Class2, M);
		;
		ArrayList<String> ListSubClass1;
		ArrayList<String> ListSubClass2;

		if (ListSubClasses(Class1).size() == 0) {
			ListSubClass1 = ListSubClasses(Class2);
			ListSubClass2 = ListSubClasses(Class1);
			pos1 = Search(Class2, M);
			pos2 = Search(Class1, M);

		} else

		{
			ListSubClass1 = ListSubClasses(Class1);
			ListSubClass2 = ListSubClasses(Class2);
			pos1 = Search(Class1, M);
			pos2 = Search(Class2, M);

		}

		for (int i = 0; i < ListSubClass1.size(); i++) {
			pos11 = Search(ListSubClass1.get(i).toString(), M);
			M[pos2][pos11] = "1";
			M[pos11][pos2] = "1";
			for (int j = 0; j < ListSubClass2.size(); j++) {
				pos22 = Search(ListSubClass2.get(j).toString(), M);
				M[pos1][pos22] = "1";
				M[pos22][pos1] = "1";
				if (pos11 >= 0 && pos22 >= 0) {
					M[pos11][pos22] = "1";
					M[pos22][pos11] = "1";
				}
			}
		}
	}

	static String getSuperClass(String Class) {
		String sparql = "DISTINCT ?superClass WHERE { <" + Class
				+ "> rdfs:subClassOf ?superClass .FILTER regex(?superClass,<http://dbpedia.org/ontology*>) }";
		ArrayList<RDFNode> k1 = ResultQuery(endpoint, sparql);
		if (k1.size() != 0) {
			String superClass = k1.get(0).toString();
			return superClass;
		} else {
			return "";
		}
	}

	static ArrayList<String> ListSibling(String Class) {
		String superClass = getSuperClass(Class);
		String sparql = "DISTINCT ?Class WHERE { ?Class  rdfs:subClassOf <" + superClass
				+ "> .FILTER regex(?Class,<http://dbpedia.org/ontology*>)}";
		ArrayList<RDFNode> k1 = ResultQuery(endpoint, sparql);
		ArrayList<String> List = new ArrayList<String>();
		if (k1.size() != 0) {
			for (int i = 0; i < k1.size(); i++) {
				if (!k1.get(i).toString().equals(Class))
					List.add(k1.get(i).toString());
			}
		}
		return List;
	}

	static boolean Checked(String Class1, String Class2) {
		int pos1 = Search(Class1, M);
		int pos2 = Search(Class2, M);
		if (!M[pos1][pos2].equals("*"))
			return true; // cell in matrix containing values
		else
			return false; // otherwise
	}

	static int countUnChecked(String Class1, ArrayList<String> ListClasses) {
		int dem = 0;
		for (int i = 0; i < ListClasses.size(); i++) {
			if (!Checked(Class1, ListClasses.get(i).toString()))
				dem = dem + 1;
		}
		return dem;

	}

	static void GoldStandardGenerator(String Class) {
		String n = "y";
		int pos1, pos2, countUnChecked;
		String c2;
		String m = "y";
		sc = new Scanner(System.in);
		ArrayList<String> Siblings = ListSibling(Class);
		ArrayList<String> ListSubclasses = ListSubClasses(Class);
		String check = "";
		if (ListSubclasses.size() != 0) {
			System.out.println();
			System.out.println(ListSubclasses.size() + " SUBCLASSES OF THE CLASS " + Class);
			System.out.println("*****************************************");
			System.out.println();
			for (int t2 = 0; t2 < ListSubclasses.size(); t2++) {
				System.out.println(ListSubclasses.get(t2).toString());
			}
		}
		countUnChecked = countUnChecked(Class, Siblings);

		if ((Siblings.size() > 0) && (countUnChecked > 0))

		{
			System.out.println();
			System.out.println("****************************************");
			System.out.println(Siblings.size() + " LIST SIBLING OF THE CLASS " + Class);
			System.out.println("****************************************");
			System.out.println();
			for (int t1 = 0; t1 < Siblings.size(); t1++) {
				c2 = Siblings.get(t1).toString();
				if (!c2.equals(Class))
					if (Checked(Class, c2))
						check = "Checked";
					else
						check = "Unchecked";

				System.out.println(Siblings.get(t1).toString() + "  " + check);
			}
			System.out.println("*****************************************************************");
			System.out.println("List unchecked:" + countUnChecked);
			System.out.println("*****************************************************************");
			System.out.println("Setup automatically disjointness between sibling class group of the class " + Class
					+ "? Press 'y' - Yes or Press any keys - Manually");
			m = sc.nextLine().toString();
			if (m.equals("y"))
				System.out.println("Automatically setting up......");
			else
				System.out.println("Manually setting up ......");
			for (int i = 0; i < Siblings.size(); i++) {

				c2 = Siblings.get(i).toString();
				if (!Checked(Class, c2)) {
					pos1 = Search(Class, M);
					pos2 = Search(c2, M);
					if (pos1 >= 0 & pos2 >= 0) {
						if (M[pos1][pos2].equals("*")) {
							if (!m.equals("y")) {
								System.out.println(Class + " and " + c2
										+ " are disjointness or not? Press 'y' - yes or Press any keys - no");
								n = sc.nextLine().toString();
							} else {
								n = "y";
							}
							if (n.equals("y")) {
								M[pos1][pos2] = "1";
								M[pos2][pos1] = "1";
								SetUpSiblingDisjoint(Class, c2);
							} else {
								M[pos1][pos2] = "0";
								M[pos2][pos1] = "0";
							}
						}

					}
				}
			}

		}
		for (int t = 0; t < ListSubClasses(Class).size(); t++)
			GoldStandardGenerator(ListSubClasses(Class).get(t).toString());

	}

	void WriteExcelFile(String[][] arr) {
		System.out.println("Recording Gold Standard Matrix to Excel file.....");
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("GoldStandard");
		for (int rowNum = 0; rowNum < arr.length; rowNum++) {
			Row row = sheet.createRow(rowNum);
			for (int cellNum = 0; cellNum < arr.length; cellNum++) {
				Cell cell = row.createCell(cellNum, CellType.STRING);
				cell.setCellValue(arr[rowNum][cellNum].toString());
			}
		}
		try {
			FileOutputStream outputStream = new FileOutputStream("GoldStandard.xlsx");
			workbook.write(outputStream);
			workbook.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done");
	}

	static String[][] getMatrix() {
		return M;
	}

	static void setMatrix(String[][] matrix) {
		M = matrix;
	}

	void checkZero(String[][] arr) {
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println("Check the remaining unchecked pair classes....");
		sc = new Scanner(System.in);
		String n = "";
		for (int i = 0; i < arr.length; i++)
			for (int j = 0; j < arr.length; j++) {
				if (arr[i][j].equals("*")) {
					System.out.println(arr[i][0] + " and " + arr[0][j]
							+ " are disjointness or not? Press 'y' - yes or Press any keys - no");
					n = sc.nextLine().toString();

					if (n.equals("y")) {
						arr[i][j] = "1";
						arr[j][i] = "1";
					} else {
						arr[i][j] = "0";
						arr[j][i] = "0";
					}
				}
			}

	}

}
