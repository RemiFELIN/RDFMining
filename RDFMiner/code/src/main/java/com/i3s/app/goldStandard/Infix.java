/**
 * 
 */
package com.i3s.app.goldStandard;

import java.util.Stack;

/**
 * @author NGUYEN Thu Huong
 *
 * 
 */
public class Infix {

	/**
	 * 
	 */
	public static String evaluate(String expression) {
		char[] tokens = expression.toCharArray();

		// Stack for numbers: 'values'
		Stack<String> values = new Stack<String>();

		// Stack for Operators: 'ops'
		Stack<Character> ops = new Stack<Character>();

		for (int i = 0; i < tokens.length; i++) {
			// Current token is a whitespace, skip it
			if (tokens[i] == ' ')
				continue;
			if (tokens[i] == '.')
				continue;
			// Current token is a number, push it to stack for numbers
			if (tokens[i] == '<') {
				StringBuffer sbuf = new StringBuffer();
				// There may be more than one digits in number
				while (i < tokens.length && tokens[i] != '>')
					sbuf.append(tokens[i++]);
				values.push((sbuf.toString() + ">"));
			}

			// Current token is an opening brace, push it to 'ops'
			else if (tokens[i] == '{')
				ops.push(tokens[i]);

			// Closing brace encountered, solve entire brace
			else if (tokens[i] == '}') {
				if (!ops.empty()) {
					while (ops.peek() != '{') {
						String a1 = values.pop();
						String a2 = values.pop();
						char a3 = ops.pop();
						// values.push(applyOp(ops.pop(), values.pop(), values.pop()));
						values.push(applyOp(a3, a1, a2));
					}
					ops.pop();
				} else
					continue;

			}

			// Current token is an operator.
			else if (tokens[i] == '*' || tokens[i] == '+') {
				// While top of 'ops' has same or greater precedence to current
				// token, which is an operator. Apply operator on top of 'ops'
				// to top two elements in values stack
				while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
					values.push(applyOp(ops.pop(), values.pop(), values.pop()));

				// Push current token to 'ops'.
				ops.push(tokens[i]);
			}
		}

		// Entire expression has been parsed at this point, apply remaining
		// ops to remaining values
		while (!ops.empty())
			values.push(applyOp(ops.pop(), values.pop(), values.pop()));

		// Top of 'values' contains result, return it
		return values.pop();
	}

	// Returns true if 'op2' has higher or same precedence as 'op1',
	// otherwise returns false.
	public static boolean hasPrecedence(char op1, char op2) {
		if (op2 == '{' || op2 == '}')
			return false;
		if ((op1 == '*') && (op2 == '+'))
			return false;
		else
			return true;
	}

	// A utility method to apply an operator 'op' on operands 'a'
	// and 'b'. Return the result.
	public static String applyOp(char op, String b, String a) {
		switch (op) {
		case '+':
			return a + b;
		case '*':
			return a + "*" + b;
		// other case - plus + 0
		}
		return "";
	}

	static String remove(String inputString) {
		String newString = inputString.replaceAll("[?]x a", "");
		String newString2 = newString.replaceAll("INTERSECTION", "*");
		String newString3 = newString2.replaceAll("UNION", "+");
		return newString3;

	}

	// Driver method to test above methods
	public static void main(String[] args) {
		String inputString = "{ ?x a <http://dbpedia.org/ontology/StillImage> . } UNION { ?x a <http://dbpedia.org/ontology/Book> .\n"
				+ "INTERSECTION\n" + "{ ?x a <http://dbpedia.org/ontology/Cartoon> .\n" + "UNION\n"
				+ "?x a <http://dbpedia.org/ontology/Image> .\n" + "} \n" + "INTERSECTION\n"
				+ "?x a <http://dbpedia.org/ontology/Article> .\n" + "\n" + "}";
		String inputExpression = remove(inputString);
		System.out.println("input Exp: " + inputExpression);
		System.out.println(Infix.evaluate(inputExpression));

	}

}
