package cn.xiaomi.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;

public class FileOperator {

	public static <K, V> void outputDictionary(
			java.util.HashMap<K, V> toBeOutput, String fileName)
			throws IOException {
		CreateDir(fileName);
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, false), Encoding.utf8));
		// TextWriter sw = new StreamWriter(fileName,false, Encoding.Default);
		// C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit
		// typing in Java:
		for (K k : toBeOutput.keySet()) {
			tw.write(k + "\t" + toBeOutput.get(k));
			tw.newLine();
		}
		tw.close();
	}
	
	public static <K, V> void outputDictionary_new(
			java.util.HashMap<K, V> toBeOutput, String fileName)
			throws IOException {
		CreateDir(fileName);
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, false), Encoding.utf8));

		for (K k : toBeOutput.keySet()) {
			tw.write("/////////////////////////////////////////////////////////////////////////////");
			tw.newLine();
			tw.write(k.toString());
			tw.newLine();
			tw.write(toBeOutput.get(k).toString());
			tw.newLine();
			tw.newLine();
		}
		tw.close();
	}

	/**
	 * 得到文件的完整路径和文件�?
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileFullName(String path) {
		File fi = new File(path);
		return fi.getAbsolutePath();
	}

	/**
	 * 得到不含路径的文件名
	 * 
	 * @param path
	 * @return
	 */
	public static String getFileName(String path) {
		File fi = new File(path);
		return fi.getName();
	}

	public static String getFileNameWithoutExtend(String source) {
		String returnValue = source.replace("\\", "/")
				.replaceAll("^[/\" ]", "").replaceAll("[/\" ]$", "");
		int i = returnValue.lastIndexOf('.');
		if (i >= 0) {
			returnValue = returnValue.substring(0, i);
		}
		i = returnValue.lastIndexOf("/");
		if (i >= 0) {
			returnValue = returnValue.substring(i + 1);
		}
		return returnValue;
	}

	
	
	public static <T> void OutputCollectionResult(
			java.util.Collection<T> toBeOutput, String fileName)
			throws IOException {

		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, false), Encoding.utf8));
		for (T a : toBeOutput) {
			tw.write(a.toString());
			tw.newLine();
		}
		tw.close();
	}




	public static void Output(java.util.Collection<String> toBeOutput,
			String fileName) throws IOException {
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, false), Encoding.utf8));
		for (String a : toBeOutput) {
			tw.write(a.toString());
			tw.newLine();
		}
		tw.close();
	}

	public static void Output(String toBeOutput, String fileName)
			throws IOException {
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, false), Encoding.utf8));
		tw.write(toBeOutput);
		tw.close();
	}

	public static void OutputLineAddMode(String toBeOutput, String fileName)
			throws IOException {
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, true), Encoding.utf8));
		tw.write(toBeOutput);
		tw.newLine();
		tw.close();
	}
	
	public static void OutputAddModeAddTitle(String title,java.util.Collection<String> toBeOutput,
			String fileName) throws IOException {
//		if (toBeOutput.size() == 0) {
//			return;
//		}
		boolean writeTitle=false;
		if(!(new File(fileName)).exists()){
			writeTitle=true;
		}
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, true), Encoding.utf8));
		//第一行加上Title
		if(writeTitle){
			tw.write(title);
			tw.newLine();
		}
		for (String a : toBeOutput) {
			tw.write(a.toString());
			tw.newLine();
		}
		tw.close();
	}
	
	public static void OutputAddMode(java.util.Collection<String> toBeOutput,
			String fileName) throws IOException {
//		if (toBeOutput.size() == 0) {
//			return;
//		}
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, true), Encoding.utf8));
		for (String a : toBeOutput) {
			tw.write(a.toString());
			tw.newLine();
		}
		tw.close();
	}
	
	public static void OutputAddSegModeWithTitle(java.util.Collection<String> toBeOutput,
			String fileName,String title,int seg) throws IOException {
//		if (toBeOutput.size() == 0) {
//			return;
//		}
		boolean addMode = false;
		if (seg > 0) {
			addMode = true;
		}
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, addMode), Encoding.utf8));
		if (!addMode) {
			tw.write(title);
			tw.newLine();
		}
		for (String a : toBeOutput) {
			tw.write(a.toString());
			tw.newLine();
		}
		tw.close();
	}
	public static void OutputAddSegMode(java.util.Collection<String> toBeOutput,
			String fileName,int seg) throws IOException {
//		if (toBeOutput.size() == 0) {
//			return;
//		}
		boolean addMode=false;
		if(seg>0){
			addMode=true;
		}
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, addMode), Encoding.utf8));
		for (String a : toBeOutput) {
			tw.write(a.toString());
			tw.newLine();
		}
		tw.close();
	}

	public static <T> void OutputCollectionResultAddMode(
			java.util.Collection<T> toBeOutput, String fileName)
			throws IOException {
		if (toBeOutput.size() == 0) {
			return;
		}
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName, true), Encoding.utf8));
		for (T a : toBeOutput) {
			tw.write(a.toString());
			tw.newLine();
		}
		tw.close();
	}

	// public static void
	// OutputCollectionResultAddMode(java.util.Collection<String> toBeOutput,
	// String fileName)
	// {
	//
	// CreateDir(fileName);
	// BufferedWriter tw=new BufferedWriter(new OutputStreamWriter(new
	// FileOutputStream(fileName,true)));
	// //TextWriter tw = new StreamWriter(fileName, true, Encoding.Default);
	// for (String a : toBeOutput)
	// {
	// tw.write(a.toString());
	// tw.newLine();
	// }
	// tw.close();
	// }

	/**
	 * 创建目录
	 * 
	 * @param fileName
	 * @return
	 */
	public static boolean CreateDir(String fileName) {
		fileName= fileName.replace("\\", "/").replaceAll("^[/\" ]", "")
				.replaceAll("[/\" ]$", "").replaceAll("//", "/");
		int pos=fileName.lastIndexOf("/");
		if (pos <= 0) {
			return true;
		}
		String dir = fileName.substring(0, pos);

		// 如果日志目录不存在，则创�?
		if (!(new File(dir)).exists()) {
			(new File(dir)).mkdirs();
			// Directory.CreateDirectory(dir);
		}
		return true;
	}

	
	public static java.util.ArrayList<String> readFile(String fileName)
			throws IOException {

		java.util.ArrayList<String> returnValue = new java.util.ArrayList<String>();
		// TextReader tw = new StreamReader(fileName, Encoding.Default);
		BufferedReader tr = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Encoding.utf8));
		String aline;
		while ((aline = tr.readLine()) != null) {
			//aline = aline.trim();
			if (!aline.equals("")) {
				returnValue.add(aline);
			}
		}
		tr.close();
		return returnValue;
	}

	/*
	 * 不踢掉空�?
	 */
	public static java.util.ArrayList<String> readFileFull(String fileName)
			throws IOException {

		java.util.ArrayList<String> returnValue = new java.util.ArrayList<String>();
		// TextReader tw = new StreamReader(fileName, Encoding.Default);
		BufferedReader tr = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Encoding.utf8));
		String aline;
		while ((aline = tr.readLine()) != null) {
			returnValue.add(aline);
		}
		tr.close();
		return returnValue;
	}

	public static HashSet<String> readToSet(String fileName)
			throws IOException {

		HashSet<String> returnValue = new HashSet<String>();
		BufferedReader tr = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Encoding.utf8));
		String aline;
		while ((aline = tr.readLine()) != null) {
			//aline = aline.trim();
			if (!aline.equals("")) {
				returnValue.add(aline);
			}
		}
		tr.close();
		return returnValue;
	}
	
	public static java.util.HashMap<String, String> readToCompair(String fileName)
			throws IOException {

		if(!(new File(fileName)).exists()){
			return new HashMap<String, String>();
		}
		HashMap<String, String> returnValue = new HashMap<String, String>();
		BufferedReader tr = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Encoding.utf8));
		String aline;
		while ((aline = tr.readLine()) != null) {
			aline = aline.trim();
			if (!aline.equals("")) {
				int position = aline.lastIndexOf("\t");
				if (position > 0&&position<aline.length()-1) {
					returnValue.put(aline.substring(0, position),
							aline.substring(position + 1));
				}
			}
		}
		tr.close();
		return returnValue;
	}
	
	public static java.util.HashMap<String, String> readToMap(String fileName)
			throws IOException {

		HashMap<String, String> returnValue = new HashMap<String, String>();
		BufferedReader tr = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Encoding.utf8));
		String aline;
		while ((aline = tr.readLine()) != null) {
			// aline = aline.trim();
			if (!(aline.trim()).equals("")) {
				int position = aline.indexOf("\t");
				if (position > 0&&position<aline.length()-1) {
					returnValue.put(aline.substring(0, position),
							aline.substring(position + 1));
				}
			}
		}
		tr.close();
		return returnValue;
	}

	public static String readFileToString(String fileName) throws IOException {
		BufferedReader tr = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileName), Encoding.utf8));
		// TextReader tw = new StreamReader(fileName, Encoding.Default);
		String lineString;
		StringBuffer text = new StringBuffer();
		while (null != (lineString = tr.readLine())) {
			text.append(lineString + "\r\n");
		}
		tr.close();
		return text.toString();
	}

	public static void OutputDictionaryResult(
			java.util.HashMap<String, java.util.HashSet<String>> toBeOutput,
			String fileName) throws IOException {

		CreateDir(fileName);
		BufferedWriter tw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fileName), Encoding.utf8));
		// TextWriter tw = new StreamWriter(fileName, false, Encoding.Default);

		// C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit
		// typing in Java:
		for (String a : toBeOutput.keySet()) {
			for (String b : toBeOutput.get(a)) {
				tw.write(a + "\t" + toBeOutput.get(a).size() + "\t" + b);
				tw.newLine();
			}
		}
		tw.close();
	}

	public static void compairFiles(String dir1, String dir2)
			throws IOException {
		java.util.ArrayList<String> a = new java.util.ArrayList<String>();
		java.util.ArrayList<String> b = new java.util.ArrayList<String>();
		String[] fis1 = (new File(dir1)).list();
		String[] fis2 = (new File(dir2)).list();
		for (int i = 0; i < fis1.length; i++) {
			String fileName = fis1[i];
			BufferedReader tr = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), Encoding.utf8));
			// TextReader tr = new StreamReader(fileName, Encoding.Default);
			String aline;
			while ((aline = tr.readLine()) != null) {
				a.add(aline);
			}
			tr.close();
		}
		for (int i = 0; i < fis2.length; i++) {
			String fileName = fis2[i];
			BufferedReader tr = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName), Encoding.utf8));
			// TextReader tr = new StreamReader(fileName, Encoding.Default);
			String aline;
			while ((aline = tr.readLine()) != null) {
				b.add(aline);
			}
			tr.close();
		}
		if (a.size() != b.size()) {
			System.out.println("长度不匹配");
			System.out.println(a.size() + "\t" + b.size());
			return;
		}
		boolean yes = true;
		System.out.println(a.size());
		for (int i = 0; i < a.size(); i++) {
			if (i % 10000 == 0) {
				System.out.print(i + "\r");
			}
			if (!a.get(i).equals(b.get(i))) {
				System.out.println(i);
				System.out.println(a.get(i));
				System.out.println(b.get(i));
				yes = false;
				break;
			}
		}
		if (yes) {
			System.out.println("文件完全相同");
		} else {
			System.out.println("文件不完全相同");
		}
	}
}