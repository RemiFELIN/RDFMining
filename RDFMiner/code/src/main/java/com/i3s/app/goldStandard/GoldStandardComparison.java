/**
 * 
 */
package com.i3s.app.goldStandard;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * A class of the assessment of disjointness classes axioms based on the
 * reference to the created GoldStandard Matrix
 * 
 * @author NGUYEN Thu Huong Oct. 2018
 */
public class GoldStandardComparison {
	
	protected String GoldStandardFile;

	protected String[][] GoldStandardArr;

	public GoldStandardComparison(String GoldStandardFile) {
		this.GoldStandardFile = GoldStandardFile;
	}

	static int Search(String Class, String[][] arr) {
		int arrlen = arr.length;
		for (int i = 0; i < arrlen; i++) {
			if (arr[0][i].equals(Class)) {
				return i; // i là vị trí của s
			}
		}
		return -1; // không có trả về -1 vì 0 là vị trí đầu tiên trong mảng
	}

	public static String CheckDisjointnessAtomicClasses(String class1, String class2, String[][] GoldStandardArr) {
		int pos1 = Search(class1, GoldStandardArr);
		int pos2 = Search(class2, GoldStandardArr);
		// logger.info ("Position in GoldStandard matrix: row: " + pos1 + " column: " +
		// pos2);
		return GoldStandardArr[pos1][pos2];
	}

	@SuppressWarnings("deprecation")
	public String[][] ConvertExcelResultToArray() throws IOException {
		int n = 63;
		String[][] arr = new String[n][n];
		int i = 0;
		String excelFilePath = GoldStandardFile;
		FileInputStream inputStream = new FileInputStream(new File(excelFilePath));
		Workbook workbook = new XSSFWorkbook(inputStream);
		Sheet firstSheet = workbook.getSheetAt(0);
		Iterator<Row> iterator = firstSheet.iterator();
		Row nextRow;// = iterator.next();
		Iterator<Cell> cellIterator;// = nextRow.cellIterator();
		Cell cell; // = cellIterator.next();
		while (iterator.hasNext()) {
			nextRow = iterator.next();
			cellIterator = nextRow.cellIterator();
			int j = 0;
			while (cellIterator.hasNext()) {
				// TODO : resolve deprecated 
				cell = cellIterator.next();
				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					arr[i][j] = cell.getStringCellValue();
					System.out.print(arr[i][j]);
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					System.out.print(cell.getBooleanCellValue());
					break;
				case Cell.CELL_TYPE_NUMERIC:
					System.out.print(cell.getNumericCellValue());
					break;
				}
				System.out.print("  ");
				j++;
			}

			System.out.println();
			i++;
		}
		workbook.close();
		inputStream.close();
		return arr;
	}

	void setGoldStandard(String[][] GoldStandardArr) {
		this.GoldStandardArr = GoldStandardArr;
	}

	String[][] getGoldStandard() {
		return GoldStandardArr;
	}

	static Boolean isOperand(String c) {
		// If the character is a digit
		// then it must be an operand
		if (c.contains("<") && c.contains(">"))
			return true;
		else
			return false;
	}

	static String CheckDisjointnessComplexClasses(List<String> arg1, List<String> arg2, String[][] GoldStandardArr) {
		Stack<String> Stack = new Stack<String>();
		String Class1 = "";
		String Class2 = "";

		if ((arg1.size() == 1) && (arg2.size() == 1)) {
			Class1 = arg1.get(0).toString();
			Class2 = arg2.get(0).toString();
			return CheckDisjointnessAtomicClasses(Class1.substring(1, Class1.length() - 1),
					Class2.substring(1, Class2.length() - 1), GoldStandardArr);
		} else {
			List<String> tmp;
			if (arg1.size() < arg2.size()) {
				tmp = arg1;
				arg1 = arg2;
				arg2 = tmp;
			}
			int arg1size = arg1.size();
			for (int j = arg1size - 1; j >= 0; j--) {

				// Push operand to Stack
				if (isOperand(arg1.get(j).toString())) {
					Stack.push((arg1.get(j).toString()));
				}

				else {
					// Operator encountered
					// Pop two elements from Stack
					List<String> o1 = new ArrayList<String>();
					o1.add(Stack.peek());
					Stack.pop();
					List<String> o2 = new ArrayList<String>();
					o2.add(Stack.peek());
					Stack.pop();
					// Use switch case to operate on o1
					// and o2 and perform o1 O o2.
					// Operators here are ObjectUnionOf and ObjectIntersectionOf
					switch (arg1.get(j).toString()) {
					case "ObjectIntersectionOf":
						if (!o1.get(0).toString().contains("<") && !o1.get(0).toString().contains((">"))) {
							if (!o2.get(0).toString().contains("<") && !o2.get(0).toString().contains((">")))

								Stack.push(String.valueOf(Integer.parseInt(o1.get(0).toString())
										+ Integer.parseInt(o2.get(0).toString())));
							else
								Stack.push(String.valueOf(Integer.parseInt(o1.get(0).toString()) + Integer
										.parseInt(CheckDisjointnessComplexClasses(arg2, o2, GoldStandardArr))));
						} else {

							if (!o2.get(0).toString().contains("<") && !o2.get(0).toString().contains((">")))

								Stack.push(String.valueOf(
										Integer.parseInt(CheckDisjointnessComplexClasses(arg2, o1, GoldStandardArr))
												+ Integer.parseInt(o2.get(0).toString())));

							else
								Stack.push(String.valueOf(
										Integer.parseInt(CheckDisjointnessComplexClasses(arg2, o1, GoldStandardArr))
												+ Integer.parseInt(
														CheckDisjointnessComplexClasses(arg2, o2, GoldStandardArr))));

						}

						break;

					case "ObjectUnionOf":

						if (!o1.get(0).toString().contains("<") && !o1.get(0).toString().contains((">"))) {
							if (!o2.get(0).toString().contains("<") && !o2.get(0).toString().contains((">")))

								Stack.push(String.valueOf(Integer.parseInt(o1.get(0).toString())
										* Integer.parseInt(o2.get(0).toString())));
							else
								Stack.push(String.valueOf(Integer.parseInt(o1.get(0).toString()) * Integer
										.parseInt(CheckDisjointnessComplexClasses(arg2, o2, GoldStandardArr))));
						} else {

							if (!o2.get(0).toString().contains("<") && !o2.get(0).toString().contains((">")))

								Stack.push(String.valueOf(
										Integer.parseInt(CheckDisjointnessComplexClasses(arg2, o1, GoldStandardArr))
												* Integer.parseInt(o2.get(0).toString())));

							else
								Stack.push(String.valueOf(
										Integer.parseInt(CheckDisjointnessComplexClasses(arg2, o1, GoldStandardArr))
												* Integer.parseInt(
														CheckDisjointnessComplexClasses(arg2, o2, GoldStandardArr))));

						}

						break;
					}
				}
			}

			return Stack.peek();
		}
	}
}
