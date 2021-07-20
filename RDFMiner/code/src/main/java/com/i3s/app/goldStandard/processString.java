/**
 * 
 */
package com.i3s.app.goldStandard;

/**
 * @author NGUYEN Thu Huong
 *
 * 
 */
public class processString {

	/**
	 * 
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String inputString = "{ ?x a <http://dbpedia.org/ontology/StillImage> . } UNION { ?x a <http://dbpedia.org/ontology/Book> .\n"
				+ "INTERSECTION\n" + "?x a <http://dbpedia.org/ontology/Cartoon> .\n" + "INTERSECTION\n"
				+ "?x a <http://dbpedia.org/ontology/Image> .\n" + "\n" + "INTERSECTION\n"
				+ "?x a <http://dbpedia.org/ontology/Article> .\n" + "\n" + "}";
		String newString = remove(inputString);

		System.out.println(newString);
	}

	static String remove(String inputString) {
		String newString = inputString.replaceAll("[?]x a", "");
		String newString2 = newString.replaceAll("INTERSECTION", "*");
		String newString3 = newString2.replaceAll("UNION", "+");
		return newString3.replaceAll("[.]", "");

	}
}
