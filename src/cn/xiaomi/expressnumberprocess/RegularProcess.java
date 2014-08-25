package cn.xiaomi.expressnumberprocess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import cn.xiaomi.common.ConfigProcess;
import cn.xiaomi.common.Encoding;
import cn.xiaomi.common.FileOperator;
import cn.xiaomi.common.StringDouble;
import cn.xiaomi.common.StringProcess;

public class RegularProcess {

	public RegularProcess(){

		HashMap<String,String> configs=ConfigProcess.readConfig("ExpressNumberProcess.config");
		if(configs==null){
			System.out.println("Config Read Error!!!");
			System.out.println("Program will start with default parameters!");
			return;
		}
		if(configs.containsKey("patMaxStartTag")){
			patMaxStartTag=Integer.parseInt(configs.get("patMaxStartTag"));
		}else{
			patMaxStartTag=3;
		}
		
		if(configs.containsKey("patMaxEndTag")){
			patMaxEndTag=Integer.parseInt(configs.get("patMaxEndTag"));
		}else{
			patMaxEndTag=2;
		}
		
		if(configs.containsKey("kuaidiNumberMinLength")){
			kuaidiNumberMinLength=Integer.parseInt(configs.get("kuaidiNumberMinLength"));
		}else{
			kuaidiNumberMinLength=6;
		}
		if(configs.containsKey("topNAccuraty")){
			topNAccuraty=Integer.parseInt(configs.get("topNAccuraty"));
		}else{
			topNAccuraty=1;
		}
		
		if(configs.containsKey("maxReturnNum")){
			maxReturnNum=Integer.parseInt(configs.get("maxReturnNum"));
		}else{
			maxReturnNum=5;
		}
		
		if(configs.containsKey("rateSingle")){
			rateSingle=Double.valueOf(configs.get("rateSingle"));
		}else{
			rateSingle=0.15;
		}
		
		
		if(configs.containsKey("patCoverMinRate")){
			patCoverMinRate=Double.valueOf(configs.get("patCoverMinRate"));
		}else{
			patCoverMinRate=0.01;
		}
		
		
		if(configs.containsKey("proportionOfMaxScore")){
			proportionOfMaxScore=Double.valueOf(configs.get("proportionOfMaxScore"));
		}else{
			proportionOfMaxScore=0.5;
		}
		
		if(configs.containsKey("lengthMinRate")){
			lengthMinRate=Double.valueOf(configs.get("lengthMinRate"));
		}else{
			lengthMinRate=0.0001;
		}
		
		if(configs.containsKey("lengthMinCount")){
			lengthMinCount=Integer.parseInt(configs.get("lengthMinCount"));
		}else{
			lengthMinCount=4;
		}
		
		if(configs.containsKey("similarWeight")){
			similarWeight=Double.valueOf(configs.get("similarWeight"));
		}else{
			similarWeight=0.7;
		}
		
		if(configs.containsKey("eachTrainDataSetMaxCount")){
			eachTrainDataSetMaxCount=Integer.parseInt(configs.get("eachTrainDataSetMaxCount"));
		}else{
			eachTrainDataSetMaxCount=10000;
		}

		if(configs.containsKey("corpusPath")){
			corpusPath=configs.get("corpusPath");
		}
		
		if(configs.containsKey("modelOutputDir")){
			modelOutputDir=configs.get("modelOutputDir");
		}
	}



	private int patMaxStartTag=3;//生成模式时，前缀的最长长度
	private int patMaxEndTag=2;//生成模式时，后缀的最长长度
	private int kuaidiNumberMinLength=6;//快递单号最短长度。低于此长度，则被判定为噪声
	private int maxReturnNum=5;//查询后，返回的结果数量
	private double rateSingle=0.12;//生成模式时，每位字符最少覆盖比例
	private int topNAccuraty=1;//统计前topNAccuraty个结果的准确率
	private double testRate=0.9;//用于测试的数据占总数据的比例
	private double patCoverMinRate=0.01;//每条模式最少应覆盖<给定快递公司，给定长度>的比例
	private boolean resultMerge=false;//返回的结果是否合并（得分累加）
	private double proportionOfMaxScore=0.5;//返回结果得分应至少达到最高分的比例
	
	private double lengthMinRate=0.0001;//某个长度在此公司的快递总数中最少应占的比例
	private int lengthMinCount=4;//某个长度在此公司的快递总数中最少应有的个数
	
	private double similarWeight=0.7;//匹配相似性在最后结果得分中所占的权重
	private int eachTrainDataSetMaxCount=10000;//每个公司的快递最多收集的训练语料数
	
	private String corpusPath=null;//输入的训练语料的路径
	private String modelOutputDir=null;//输出模型的文件夹路径
	private static Pattern patForCleanNum = Pattern.compile("[a-z]*[0-9a-z]{6,}[a-z]*");//快递单号的格式
	private static String cleanNumberRegex="[　 \\s\\-\\_\\+(\\\\t)(\\\\r)(\\\\n)(\\.)\\*,，\\(\\)\\（\\）]+";

	
	//快递公司名2比例
	private HashMap<String, Double> expName2Rate=new HashMap<String, Double>();
	
	//规则（字符串格式）2快递公司
	private HashMap<String, ArrayList<StringDouble>> pattern2ExpNames=new HashMap<String, ArrayList<StringDouble>>();
	
	//索引2规则
	private HashMap<Integer, ArrayList<RegularExpress>> index2RegularExpress=new HashMap<Integer, ArrayList<RegularExpress>>();

	/**
	 * 快递单号规范化
	 * @param target
	 * @return
	 */
	private String cleanNumber(String target) {
//		target="370  344*667  173,大家看法角度看";
		target=target.toLowerCase().trim().replaceAll(cleanNumberRegex, "");
		Matcher numMatcher = patForCleanNum
				.matcher(target);
		if (numMatcher.find()) {
			return numMatcher.group();
		}
		return "";
	}
	
	/**
	 * 根据单号生成规则模板
	 * @param num
	 * @return
	 */
	private ArrayList<RegularExpress> getRegexFromNum(String num){
		ArrayList<RegularExpress> returnValue=new ArrayList<RegularExpress>();
		boolean endEnglish=false;
		for(int i=0;i<=patMaxStartTag;i++){
			for(int temp=num.length()-1;temp>=num.length()-patMaxEndTag;temp--){
				if(num.charAt(temp)>='a'&&num.charAt(temp)<='z'){
					endEnglish=true;
					break;
				}
			}
			int endLength=patMaxEndTag;
			if(!endEnglish){
				endLength=0;
			}
			for(int j=0;j<=endLength;j++){
				int varLength=num.length()-i-j;
				StringBuffer sb=new StringBuffer();
				sb.append('^');
				if(i>0){
					sb.append(num.substring(0, i));
				}
				boolean containEnglish=false;
				for(int m=i;m<num.length()-j;m++){
					if(num.charAt(m)>='a'&&num.charAt(m)<='z'){
						containEnglish=true;
						break;
					}
				}
				if(containEnglish){
					sb.append("[0-9a-z]{").append(varLength).append("}");
				}else{
					sb.append("[0-9]{").append(varLength).append("}");
				}
				if(j>0){
					sb.append(num.substring(num.length()-j));
				}
				sb.append('$');
				returnValue.add(new RegularExpress(i+j,num.length(),sb.toString()));
			}
		}
		return returnValue;
	}

	/**
	 * 输出快递名及其在语料中的频率和概率
	 * @param expName2ExpCount
	 * @param allCount
	 * @param outputModelDir
	 * @throws IOException
	 */
	private void outputExpNameAndCount(HashMap<String, Integer> expName2ExpCount,int allCount) throws IOException{
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(modelOutputDir+"/Model_ExpNameAndCount.txt"), Encoding.utf8));
		for (Map.Entry<String, Integer> entry : expName2ExpCount.entrySet()) {
			bw.write(entry.getKey()+"\t"+entry.getValue()+"\t"+(double)entry.getValue()/allCount);
			bw.newLine();
		}
		bw.close();
	}
	
	/**
	 * 输出每个公司的每个长度的快递单号数量统计
	 * @param expName2ExpNumLength
	 * @param expName2ExpCount
	 * @param expName2ShortExpNum
	 * @throws IOException
	 */
	private void outputExpName2Length(HashMap<String, HashMap<Integer, Integer>> expName2ExpNumLength,HashMap<String, Integer> expName2ExpCount,ArrayList<Express> exps) throws IOException{
		
		HashMap<String, HashMap<Integer, ArrayList<String>>> expName2ShortExpNum=new HashMap<String, HashMap<Integer,ArrayList<String>>>();
		for (Express express : exps) {
			int length = express.getNumber().length();
			if(expName2ExpNumLength.get(express.getName()).get(length)<0){
				if(!expName2ShortExpNum.containsKey(express.getName())){
					expName2ShortExpNum.put(express.getName(),new HashMap<Integer, ArrayList<String>>());
				}
				if(!expName2ShortExpNum.get(express.getName()).containsKey(length)){
					expName2ShortExpNum.get(express.getName()).put(length, new ArrayList<String>());
				}
				expName2ShortExpNum.get(express.getName()).get(length).add(express.getNumber());
			}
		}
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(modelOutputDir+"/expName2Length.txt"), Encoding.utf8));
		for (Map.Entry<String, HashMap<Integer, Integer>> expName2Length : expName2ExpNumLength
				.entrySet()) {
			for (Map.Entry<Integer, Integer> length : expName2Length.getValue()
					.entrySet()) {
				int valueTrue=Math.abs(length.getValue());
				double rate=(double) valueTrue
						/ expName2ExpCount.get(
								expName2Length.getKey());
				bw.write(expName2Length.getKey()
						+ "\t"
						+ length.getKey()
						+ "\t"
						+ valueTrue
						+ "\t"
						+ String.format(
								"%.4f",
								rate));
				if(length.getValue()<0){
					for (String num : expName2ShortExpNum.get(expName2Length.getKey()).get(length.getKey())) {
						bw.write("\t");bw.write(num);
					}
				}
				bw.newLine();
			}
		}
		bw.close();
		
	}
	
	/**
	 * 对所有快递单号预处理，统计相关频率
	 * @param exps
	 * @param expName2ExpNumLength
	 * @param expName2ExpCount
	 * @throws IOException
	 */
	private void preprocessExpress(ArrayList<Express> exps,HashMap<String, HashMap<Integer, Integer>> expName2ExpNumLength,HashMap<String, Integer> expName2ExpCount) throws IOException{
		for (Express express : exps) {
			if (!expName2ExpCount.containsKey(express.getName())) {
				expName2ExpCount.put(express.getName(), 1);
				expName2ExpNumLength.put(express.getName(),
						new HashMap<Integer, Integer>());
			} else {
				expName2ExpCount.put(express.getName(),
						expName2ExpCount.get(express.getName()) + 1);
			}
			int length = express.getNumber().length();
			if (!expName2ExpNumLength.get(express.getName())
					.containsKey(length)) {
				expName2ExpNumLength.get(express.getName()).put(length, 1);
			} else {
				expName2ExpNumLength.get(express.getName()).put(
						length,
						1 + expName2ExpNumLength.get(express.getName()).get(
								length));
			}
		}
		
		for (Map.Entry<String, HashMap<Integer, Integer>> expName2Length : expName2ExpNumLength
				.entrySet()) {
			for (Map.Entry<Integer, Integer> length : expName2Length.getValue()
					.entrySet()) {
				double rate=(double) length.getValue()
						/ expName2ExpCount.get(
								expName2Length.getKey());
			
				//如果频率小于一定阈值，则作为噪声，标记上，以后不参与训练
				if(rate<=lengthMinRate||length.getValue()<=lengthMinCount){
					length.setValue(length.getValue()*-1);
				}
			}
		}
		outputExpName2Length(expName2ExpNumLength,expName2ExpCount,exps);
		
	}
	
	/**
	 * 从快递单号中学习规则模板
	 * @param exps
	 * @param outputModelDir
	 * @throws IOException
	 */
	private void studyRegexFromExpress(ArrayList<Express> exps) throws IOException {
		
		//快递名：长度：数量
		HashMap<String, HashMap<Integer, Integer>> expName2ExpNumLength = new HashMap<String, HashMap<Integer, Integer>>();
		//快递名：总数
		HashMap<String, Integer> expName2ExpCount = new HashMap<String, Integer>();
		
		preprocessExpress(exps,expName2ExpNumLength,expName2ExpCount);
		
		HashMap<String, Integer> pattern2Count=new HashMap<String, Integer>();
		HashMap<String, ArrayList<StringDouble>> pattern2ExpNames=new HashMap<String, ArrayList<StringDouble>>();
		HashMap<String, RegularExpress> final_regStr2RegPat=new HashMap<String, RegularExpress>();
		HashMap<String, RegularExpress> temp_regStr2RegPat=new HashMap<String, RegularExpress>();
		String lastExpName="";
		for (Express express : exps) {
			if(expName2ExpNumLength.get(express.getName()).get(express.getNumber().length())<0){
				continue;
			}
			if(!lastExpName.equals(express.getName())&&!lastExpName.equals("")){
				if(pattern2Count.size()>0){
					for (Map.Entry<String, Integer> entry : pattern2Count.entrySet()) {
						double rate=(double)entry.getValue()/Math.abs(expName2ExpNumLength.get(lastExpName).get(temp_regStr2RegPat.get(entry.getKey()).getLength()));
						if(rate>=Math.pow(rateSingle,temp_regStr2RegPat.get(entry.getKey()).getFixNum())){
							if(!pattern2ExpNames.containsKey(entry.getKey())){
								pattern2ExpNames.put(entry.getKey(), new ArrayList<StringDouble>());
							}
							pattern2ExpNames.get(entry.getKey()).add(new StringDouble((double)entry.getValue()/expName2ExpCount.get(lastExpName),lastExpName));
							final_regStr2RegPat.put(entry.getKey(), temp_regStr2RegPat.get(entry.getKey()));
						}
					}
				}
				temp_regStr2RegPat.clear();
				pattern2Count.clear();
			}
			lastExpName=express.getName();
			 ArrayList<RegularExpress> regexs=getRegexFromNum(express.getNumber());
			 for (RegularExpress regularExpress : regexs) {
				String patString=regularExpress.getRegex().toString();
				if(!pattern2Count.containsKey(patString)){
					pattern2Count.put(patString, 1);
					temp_regStr2RegPat.put(patString, regularExpress);
				}else{
					pattern2Count.put(patString, 1+pattern2Count.get(patString));
				}
			}
		}
		if(pattern2Count.size()>0){
			for (Map.Entry<String, Integer> entry : pattern2Count.entrySet()) {
				double rate=(double)entry.getValue()/Math.abs(expName2ExpNumLength.get(lastExpName).get(temp_regStr2RegPat.get(entry.getKey()).getLength()));
				if(rate>=Math.pow(rateSingle,temp_regStr2RegPat.get(entry.getKey()).getFixNum())){
					if(!pattern2ExpNames.containsKey(entry.getKey())){
						pattern2ExpNames.put(entry.getKey(), new ArrayList<StringDouble>());
					}
					pattern2ExpNames.get(entry.getKey()).add(new StringDouble((double)entry.getValue()/expName2ExpCount.get(lastExpName),lastExpName));
					final_regStr2RegPat.put(entry.getKey(), temp_regStr2RegPat.get(entry.getKey()));
				}
			}
		}
		temp_regStr2RegPat.clear();
		pattern2Count.clear();
		
		
		//输出规则模型
		String outputPatFileName=modelOutputDir+"/Model_Pattern2kuaidi.txt";
		BufferedWriter bw2 = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputPatFileName), Encoding.utf8));
		for (Map.Entry<String, ArrayList<StringDouble>> express : pattern2ExpNames.entrySet()) {
			bw2.write(final_regStr2RegPat.get(express.getKey()).toString());
			for (StringDouble expNames : pattern2ExpNames.get(express.getKey())) {
				bw2.write("\t"+expNames.toString());
			}
			bw2.newLine();
		}
		bw2.close();
		
	}
	
	/**
	 * 计算每家快递应该随机选择的比例
	 * @param inputFileName
	 * @return
	 * @throws IOException
	 */
	private HashMap<String, Double> getExpName2SelectRate() throws IOException{
		int allCount=0;
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(corpusPath),Encoding.utf8));
		HashMap<String, Integer> expName2AllExpCount=new HashMap<String, Integer>();
		String aline;
		while ((aline = br.readLine()) != null) {
			aline = aline.toLowerCase().trim();
			if (aline.equals("")) {
				continue;
			}
			if (aline.startsWith("bizcode")) {
				continue;
			}
			String[] temp = aline.split("\t");
			if (temp.length < 2) {
				continue;
			}
			allCount++;
			if(expName2AllExpCount.containsKey(temp[0])){
				expName2AllExpCount.put(temp[0], expName2AllExpCount.get(temp[0])+1);
			}else{
				expName2AllExpCount.put(temp[0], 1);
			}
			
		}
		br.close();
		
		//输出真实语料中快递名和数量、比例
		outputExpNameAndCount(expName2AllExpCount, allCount);
		
		HashMap<String, Double> returnValue=new HashMap<String, Double>();
		for (Map.Entry<String,Integer> entry : expName2AllExpCount.entrySet()) {
			returnValue.put(entry.getKey(), (double)eachTrainDataSetMaxCount/entry.getValue());
		}
		return returnValue;
	}
	
	/**
	 * 清理原始数据
	 * @param inputFileName
	 * @return
	 * @throws IOException
	 */
	private ArrayList<Express> cleanData() throws IOException{
		HashMap<String, Double> expName2SelectRate=getExpName2SelectRate();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(corpusPath),Encoding.utf8));
		 ArrayList<Express> exps=new ArrayList<Express>();
			String cleanFileName=corpusPath+".clean";
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(cleanFileName), Encoding.utf8));
			String aline;
			while ((aline = br.readLine()) != null) {
				aline = aline.toLowerCase().trim();
				if (aline.equals("")) {
					continue;
				}
				if (aline.startsWith("bizcode")) {
					continue;
				}
//				aline="zhongtong	地方大幅度\\t761249607458";
				String[] temp = aline.split("\t");
				if (temp.length < 2) {
//					System.out.println("Number Format Error:\t"+aline);
					continue;
				}
			

				String cleanedNum=cleanNumber(temp[1]);
				if(!temp[1].equals(cleanedNum)){
//					System.out.println("Number Format Error,try to change:\t"+aline+"\tto\t"+cleanedNum);
				}

				if(cleanedNum.length()<kuaidiNumberMinLength){
//					System.out.println("Number Format Error,change Error:\t"+aline);
					continue;
				}
				bw.write(temp[0]+"\t"+cleanedNum);
				bw.newLine();
				if(!expName2SelectRate.containsKey(temp[0])){
					System.out.println("Do Not Find:\t"+temp[0]+"  in expName2SelectRate!");
					continue;
				}
				//随机选择是否将当前数据加入到训练集
				double k=Math.random();
				if(k>expName2SelectRate.get(temp[0])){
					continue;
				}
				exps.add(new Express(temp[0], cleanedNum));
			}
			bw.close();
			br.close();
			Collections.sort(exps);
			System.out.println("Select num : "+exps.size());
			FileOperator.OutputCollectionResult(exps, modelOutputDir+"/selectForTrain.txt");
			return exps;
	}
	
	/**
	 * 从配置文件中读取输入输出文件路径，生成规则模板，并输出
	 * @return
	 */
	public boolean buildModel(){
		if(corpusPath==null||modelOutputDir==null||corpusPath.equals("")||modelOutputDir.equals("")){
			System.out.println("corpusPath or modelOutputDir did not assigned!!!");
			return false;
		}
		try{
			ArrayList<Express> exps=cleanData();
			
			studyRegexFromExpress(exps);
		}catch(Exception exp){
			System.out.print(exp.getMessage());
			return false;
		}
		System.out.println("Build finish.");
		return true;
	}
	
	/**
	 * 根据输入原始单号文件，生成规则模板，并输出
	 * @param inputFileName
	 * @param outputModelDir
	 * @return
	 * @throws IOException
	 */
	public boolean buildModel(String inputFileName,String outputModelDir){
		corpusPath=inputFileName;
		modelOutputDir=outputModelDir;
		return buildModel();
	}
	
	/**
	 * 根据单号列表，生成规则模板，并输出
	 * @param exps
	 * @param outputModelDir
	 * @return
	 * @throws IOException
	 */
	private boolean buildModel(ArrayList<Express> exps,String outputModelDir){
		try {
			studyRegexFromExpress(exps);
		} catch (IOException e) {
			System.out.print(e.getMessage());
			return false;
		}
		System.out.println("Build finish.");
		return true;
	}
	/**
	 * 加载快递公司名及其对应的概率
	 * @param path
	 * @return
	 */
	private boolean loadExpNameAndCount(String path) {
		expName2Rate.clear();
		ArrayList<String> ExpNameAndCount;
		try{
			ExpNameAndCount=FileOperator.readFile(path);
		}catch(Exception exp){
			return false;
		}
		if(ExpNameAndCount==null||ExpNameAndCount.size()==0){
			return false;
		}
		for (String string : ExpNameAndCount) {
			String[] temp=string.split("\t");
			if(temp.length!=3){
				continue;
			}
			expName2Rate.put(temp[0], Double.parseDouble(temp[2]));
		}
		return true;
	}
	/**
	 * 加载模板文件，并生成索引文件
	 * @param path
	 * @return
	 */
	private boolean loadPattern2kuaidi(String path){
		pattern2ExpNames.clear();
//		regStr2RegPat.clear();
		index2RegularExpress.clear();
		ArrayList<String> patternAndExpNames;
		try{
			patternAndExpNames=FileOperator.readFile(path);
		}catch(Exception exp){
			return false;
		}
		
		for (String string : patternAndExpNames) {
			String[] temp=string.split("\t");
			if(temp.length<4){
				continue;
			}
			ArrayList<StringDouble> expNames=new ArrayList<StringDouble>();
			for(int i=3;i<temp.length;i++){
				if(temp[i].equals("")){
					continue;
				}
				StringDouble sd=new StringDouble(temp[i]);
				if(sd.getScore()<patCoverMinRate){
					continue;
				}
				try{
					sd.setScore(sd.getScore()*expName2Rate.get(sd.getName()));//p(y/x)=p(y)*p(x/y)/p(x)~p(y)*p(x/y);x:单号满足此形式;y:属于y快递
				}catch(Exception exp){
					System.out.println(exp.getMessage());
					return false;
				}
				expNames.add(sd);
			}
			if(expNames.size()==0){
				continue;
			}
			pattern2ExpNames.put(temp[2], expNames);
			RegularExpress regularExpress=new RegularExpress(Integer.parseInt(temp[0]), Integer.parseInt(temp[1]), temp[2]);
			int index=generatePatIndex(temp[2],regularExpress.getLength());
			if(!index2RegularExpress.containsKey(index)){
				index2RegularExpress.put(index, new ArrayList<RegularExpress>());
			}
			index2RegularExpress.get(index).add(regularExpress);
		}
		System.out.println("All index2RegularExpress num："+index2RegularExpress.size());
	
		return true;
	}
	/**
	 * 根据规则生成索引号
	 * @param pat
	 * @param length
	 * @return
	 */
	private int generatePatIndex(String pat,int length){
		pat=StringProcess.trim(pat, '^','$');
		return generatePatStartIndex(pat)*100+length;
	}
	/**
	 * 根据单号和长度生成索引号
	 * @param number
	 * @param startMaxCount
	 * @param length
	 * @return
	 */
	private int generateNumberIndex(String number,int startMaxCount,int length){
		return generateNumberStartIndex(number,startMaxCount)*100+length;
	}
	/**
	 * 根据单号，生成全部索引号
	 * @param number
	 * @param length
	 * @return
	 */
	private ArrayList<Integer> generateAllNumberIndexs(String number,int length){
		ArrayList<Integer> returnValue=new ArrayList<Integer>();
		int tempIndex=Integer.MAX_VALUE-1000;
		returnValue.add(tempIndex*100+length);
		for(int i=0;i<number.length()&&i<patMaxStartTag;i++){
			if(tempIndex!=Integer.MAX_VALUE-1000){
				tempIndex=tempIndex*5+number.charAt(i);
				returnValue.add(tempIndex*100+length);
			}else{
				tempIndex=number.charAt(i);
				returnValue.add(tempIndex*100+length);
			}
		}
		return returnValue;
	}
	
	/**
	 * 根据单号和长度生成前缀索引
	 * @param number
	 * @param startMaxCount
	 * @return
	 */
	private int generateNumberStartIndex(String number,int startMaxCount){
		int returnValue=Integer.MAX_VALUE-1000;
		for(int i=0;i<number.length()&&i<startMaxCount;i++){
			if(returnValue!=Integer.MAX_VALUE-1000){
				returnValue=returnValue*5+number.charAt(i);
			}else{
				returnValue=number.charAt(i);
			}
		}
		return returnValue;
	}
	
	/**
	 * 根据规则生成前缀索引
	 * @param pat
	 * @return
	 */
	private int generatePatStartIndex(String pat){
		int returnValue=Integer.MAX_VALUE-1000;
		for(int i=0;i<pat.length();i++){
			if(pat.charAt(i)=='['){
				break;
			}
			if(returnValue!=Integer.MAX_VALUE-1000){
				returnValue=returnValue*5+pat.charAt(i);
			}else{
				returnValue=pat.charAt(i);
			}
		}
		return returnValue;
	}
	


	
	/**
	 * 从配置文件中加载模型
	 * @return
	 */
	public boolean loadModel(){
		if(modelOutputDir==null||modelOutputDir.equals("")){
			System.out.println("corpusPath or modelOutputDir did not assigned!!!");
			return false;
		}
		if(!(new File(modelOutputDir)).exists()){
			System.out.println("Model path is not exists.");
			return false;
		}
		String expNameAndCountPath=modelOutputDir+"/Model_ExpNameAndCount.txt";
		String pattern2kuaidiPath=modelOutputDir+"/Model_Pattern2kuaidi.txt";
		if(!(new File(expNameAndCountPath)).exists()){
			System.out.println("Model_ExpNameAndCount.txt is not exists.");
			return false;
		}
		if(!(new File(pattern2kuaidiPath)).exists()){
			System.out.println("Pattern2kuaidiPath.txt is not exists.");
			return false;
		}
		if(!loadExpNameAndCount(expNameAndCountPath)){
			System.out.println("Load Model_ExpNameAndCount Error!");
			return false;
		}
		if(!loadPattern2kuaidi(pattern2kuaidiPath)){
			System.out.println("Load Model_Pattern2kuaidi Error!");
			return false;
		}
		return true;
	}
	
	/**
	 * 从参数目录中加载模型
	 * @param path
	 * @return
	 */
	public boolean loadModel(String path){
		modelOutputDir=path;
		return loadModel();
	}
	/**
	 * 根据快递单号查询快递公司
	 * @param kuaidiNumber
	 * @return
	 */
	public ArrayList<String> queryKuaidi(String kuaidiNumber){
		return queryKuaidi(kuaidiNumber,false);
	}
	
	/**
	 * 根据快递单号查询快递公司
	 * @param kuaidiNumber
	 * @param outputScore：是否输出每条结果的得分
	 * @return
	 */
	private ArrayList<String> queryKuaidi(String kuaidiNumber,boolean outputScore){
		kuaidiNumber=cleanNumber(kuaidiNumber);
		if(kuaidiNumber.length()<kuaidiNumberMinLength){
			System.out.println("Number Format Error,change Error:\t"+kuaidiNumber);
			return new ArrayList<String>();
		}
		ArrayList<Integer> allNumberIndexs=generateAllNumberIndexs(kuaidiNumber,kuaidiNumber.length());
		ArrayList<RegularExpress> rightMatch=new ArrayList<RegularExpress>();
		for(int i=allNumberIndexs.size()-1;i>=0;i--){
			if(!index2RegularExpress.containsKey(allNumberIndexs.get(i))){
				continue;
			}
			for (RegularExpress regularExpress : index2RegularExpress.get(allNumberIndexs.get(i))) {
				if(regularExpress.getRegex().matcher(kuaidiNumber).find()){
					rightMatch.add(regularExpress);
				}
			}
		}
		Collections.sort(rightMatch);
		ArrayList<StringDouble> results=new ArrayList<StringDouble>();
		for(int i=rightMatch.size()-1;i>=0;i--){
			for (StringDouble stringDouble : pattern2ExpNames.get(rightMatch.get(i).getRegex().pattern())) {
				double score=stringDouble.getScore()*(1-similarWeight)+similarWeight*rightMatch.get(i).getRate();
				results.add(new StringDouble(score,stringDouble.getName()));
			}
//			if(results.size()>=maxReturnNum){
//				break;
//			}
		}
		
		if (resultMerge) {
			Collections.sort(results,new Comparator<StringDouble>() {
				@Override
				public int compare(StringDouble o1, StringDouble o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			ArrayList<StringDouble> tempResults=new ArrayList<StringDouble>();
			
			for (int i = 0; i <results.size(); i++) {
				if(tempResults.size()==0){
					tempResults.add(new StringDouble(results.get(i).getScore(),results.get(i).getName()));
					continue;
				}
				if(tempResults.get(tempResults.size()-1).getName().equals(results.get(i).getName())){
					tempResults.get(tempResults.size()-1).setScore(tempResults.get(tempResults.size()-1).getScore()+results.get(i).getScore());
				}else{
					tempResults.add(new StringDouble(results.get(i).getScore(),results.get(i).getName()));
				}
			}
			
			results=tempResults;
		}
		
		
		Collections.sort(results);
		if(outputScore){
			for(int i=results.size()-1;i>=0;i--){
				System.out.println(results.get(i));
			}
		}
		ArrayList<String> returnValue=new ArrayList<String>();
		HashSet<String> returnValueNoRepeat=new HashSet<String>();
		double maxScore=0;
		for(int i=results.size()-1;i>=0;i--){
			if(i==results.size()-1){
				maxScore=results.get(i).getScore();
			}
			//如果当前分数比最高分小得多，则不返回此结果
			else if(results.get(i).getScore()<maxScore*proportionOfMaxScore){
				break;
			}
			if(!returnValueNoRepeat.contains(results.get(i).getName())){
				returnValue.add(results.get(i).getName());
				returnValueNoRepeat.add(results.get(i).getName());
			}
			if(returnValue.size()>=maxReturnNum){
				break;
			}
		}
		return returnValue;
	}
	

	/**
	 * 将单号根据比例分成训练集和测试集
	 * @param exps
	 * @param toTrain
	 * @param toTest
	 * @param outputDir
	 * @throws IOException
	 */
	private void split2trainAndTest(ArrayList<Express> exps,ArrayList<Express> toTrain,ArrayList<Express> toTest,String outputDir) throws IOException{
		for (Express express : exps) {
			double k=Math.random() ;
			if(k<testRate){
				toTest.add(express);
			}else{
				toTrain.add(express);
			}
		}
		FileOperator.OutputCollectionResult(toTrain, outputDir+"/toTain.txt");
		FileOperator.OutputCollectionResult(toTest, outputDir+"/toTest.txt");
	}
	
	/**
	 * 从路径中加载快递单号
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private ArrayList<Express> loadCorpus(String path) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(path),Encoding.utf8));
		 ArrayList<Express> exps=new ArrayList<Express>();
			
			String aline;
			while ((aline = br.readLine()) != null) {
				aline = aline.toLowerCase().trim();
				if (aline.equals("")) {
					continue;
				}
				if (aline.startsWith("bizcode")) {
					continue;
				}
//				aline="zhongtong	地方大幅度\\t761249607458";
				String[] temp = aline.split("\t");
				if (temp.length < 2) {
					System.out.println("Number Format Error:\t"+aline);
					continue;
				}
				exps.add(new Express(temp[0], temp[1]));
			}
			br.close();
			Collections.sort(exps);
			return exps;
	}

	/**
	 * 验证查询结果的正确性
	 * @param expNumber
	 * @param trueResult
	 * @param result
	 * @return
	 */
	private ArrayList<Integer> testResult(String expNumber,String trueResult,ArrayList<String> result){

		ArrayList<Integer> returnValue=new ArrayList<Integer>();
		boolean find=false;
		for(int i=0;i<result.size()&&i<topNAccuraty;i++){
			if(result.get(i).equals(trueResult)){
				returnValue.add(1);
				find=true;
				break;
			}else{
				returnValue.add(0);
			}
		}
		while(returnValue.size()<topNAccuraty){
			if(find){
				returnValue.add(1);
			}else{
				returnValue.add(0);
			}
		}
		return returnValue;
	}

	//单号中重复的比例
	private HashSet<String> repeatNumber=new HashSet<String>();
	
	/**
	 * 得到单号到快递公司名的索引
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private  HashMap<String, String> getNumber2ExpName(String path) throws IOException{
		HashMap<String, String> number2ExpName=new HashMap<String, String>();
		ArrayList<String> corpuStrings=FileOperator.readFile(path);
		int repeatCount=0;
		for (String string : corpuStrings) {
			String[] temp=string.split("\t");
			if(temp.length==2){
				if(number2ExpName.containsKey(temp[1])){
//					System.out.println("getNumber2ExpName repeat:\t"+temp[1]);
					repeatNumber.add(temp[1]);
					repeatCount++;
				}else{
					number2ExpName.put(temp[1],temp[0]);
				}
			}
		}
//		System.out.println("Repeat Count:"+repeatCount+". Repeat Rate:"+String.format("%.5f", (double)repeatCount/(number2ExpName.size()+repeatCount)));
		return number2ExpName;
	}
	
//	public static void  main(String[] args)
	public static void main_test(String[] args)
			throws IOException, InterruptedException{

		RegularProcess re=new RegularProcess();
		
		ArrayList<Express> exps=re.cleanData();
		ArrayList<Express> toTrain=new ArrayList<Express>();
		ArrayList<Express> toTest=new ArrayList<Express>();
		re.split2trainAndTest(exps,toTrain,toTest,"./data");
		re.buildModel(toTrain,"./data");

		System.out.println("Begin Load mode!");
		
		re.loadModel("./data");

		
		ArrayList<String> results=re.queryKuaidi("123456789012",true);
		System.out.println(results);
	}
//	public static void  main(String[] args)
	public static void  main2(String[] args)
//	public static void main_test(String[] args) 
			throws IOException {

		RegularProcess re=new RegularProcess();
//		if(!re.buildModel()){
//			System.out.println("Build Model Error!!!");
//			return ;
//		}

		System.out.println("Begin Load mode!");
		
		if(!re.loadModel()){
			System.out.println("Load Model Error!!!");
			return ;
		}
		System.out.println(re.queryKuaidi("8071019138 ",true));
		System.in.read();
	}
	
	
	public static void  main(String[] args)
//	public static void  main2(String[] args)
//	public static void main_test(String[] args) 
			throws IOException {

		RegularProcess re=new RegularProcess();
		if(!re.buildModel()){
			System.out.println("Build Model Error!!!");
			return ;
		}

		System.out.println("Begin Load mode!");
		
		if(!re.loadModel()){
			System.out.println("Load Model Error!!!");
			return ;
		}

		HashMap<String, String> number2ExpName=re.getNumber2ExpName("./data/spb_kuaidi.csv.clean");
		Date dt=new Date();
		int allResultsCount=0;
		ArrayList<Integer> allRightCount=new ArrayList<Integer>();
		for(int i=0;i<re.topNAccuraty;i++){
			allRightCount.add(0);
		}
		for (Map.Entry<String, String> entry : number2ExpName.entrySet()) {
			ArrayList<String> results=re.queryKuaidi(entry.getKey());
			ArrayList<Integer> tempResults=re.testResult(entry.getKey(),entry.getValue(),results);
			for(int i=0;i<re.topNAccuraty;i++){
				allRightCount.set(i,allRightCount.get(i)+tempResults.get(i));		
			}
			allResultsCount+=results.size();
		}
		long totalSeconds=(long)((new Date()).getTime()-dt.getTime())/1000;

		System.out.println("Test Num:"+number2ExpName.size()+".  Total Time:"+totalSeconds+" Seconds.  Average Time:"+String.format("%.3f", (double)1000*totalSeconds/number2ExpName.size())+"  millSeconds");
		System.out.println("Average Results Count:"+String.format("%.3f", (double)allResultsCount/number2ExpName.size())+". ");

		for(int i=0;i<re.topNAccuraty;i++){
			System.out.println("Top "+(i+1)+" Average Precision:"+String.format("%.3f", (double)allRightCount.get(i)/number2ExpName.size()));
		}
	}
}
