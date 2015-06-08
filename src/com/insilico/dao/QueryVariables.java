package com.insilico.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QueryVariables {
//	
//	public List<Integer> year=new ArrayList<Integer>();
//	public List<String> quarter=new ArrayList<String>();
	
	//key:startTime, endTime for simple query
	private Map<String,String> timeperiod=new HashMap<String,String>();
	
	
	//key:Year, Quarters, for advanced query
	private Map ad_timeperiod=new HashMap();
	

	//key:Year, Age,Gender,Weight,Country
	private Map<String,Boolean> miningAttributes=new HashMap<String,Boolean>();
	
	private Map<String, Boolean> miningAdAttraibutes=new HashMap<String,Boolean>();
	
	private String Drug;
	
	private String Symtom;
	

	private String measure=null;
	
	private List<String> gender=new ArrayList<String>();
	
	private List<String> age=new ArrayList<String>();
	
	private List<Double> weight=new ArrayList<Double>();
	
	private List<String> country=new ArrayList<String>();
	
	private int numOutput=5; //default K=5 to output the top 5 most likely relationship
	
	private String type;
	
	public QueryVariables(){
		this.miningAttributes.put("Year", false);
		this.miningAttributes.put("Age", false);
		this.miningAttributes.put("Gender", false);
		this.miningAttributes.put("Weight", false);
		this.miningAttributes.put("Country", false);
		this.miningAdAttraibutes.put("Time", false);
		this.miningAdAttraibutes.put("Age", false);
		this.miningAdAttraibutes.put("Drug", false);
		this.miningAdAttraibutes.put("PT", false);
		
	}
	
	public void setTimeperiod(String startyear,String endyear,String startquarter, String endquarter){
		
		this.timeperiod.put("startTime", startyear+"_"+startquarter);
		this.timeperiod.put("endTime", endyear+"_"+endquarter);
		
	} 
	
	public Map getTimeperiod() {
		return timeperiod;
	}
	
	public void setMiningAttributes(String[] miningAttributes){
		for(int i=0;i<miningAttributes.length;i++){
			this.miningAttributes.put(miningAttributes[i], true);
		}
		
	}
	
	public Map getMiningAttributes(){
		return miningAttributes;
	}

	public String getMeasure() {
		return measure;
	}

	public void setMeasure(String measure) {
		this.measure = measure;
	}

	public List<String> getGender() {
		return gender;
	}

	public void setGender(List<String> gender) {
		this.gender = gender;
	}

	public List<String> getAge() {
		return age;
	}

	public void setAge(String[] ages) {
		for(int i=0;i<ages.length;i++){
			//System.out.println(ages[i]);
			this.age.add(ages[i]);
		}
	}

	public List<Double> getWeight() {
		return weight;
	}

	public void setWeight(List<Double> weight) {
		this.weight = weight;
	}

	public List<String> getCountry() {
		return country;
	}

	public void setCountry(List<String> country) {
		this.country = country;
	}

	public int getNumOutput() {
		return numOutput;
	}

	public void setNumOutput(String numofcount) {
		
		if(numofcount.equals("all")){
			this.numOutput=-1; //-1 means all
		}else if(numofcount.contains("topk")){
			String[] linesplit=numofcount.split("_");
			if(linesplit.length<2){
				this.numOutput=5;
			}else if(linesplit.length==2){
				this.numOutput=Integer.parseInt(linesplit[1]);
			}
			//System.out.println(this.numOutput);
		}
	}
	
	public Map getAd_timeperiod() {
		return ad_timeperiod;
	}

	public void setAd_timeperiod(String[] quarters,String year) {
		this.ad_timeperiod.put("Year", year);
		List quatersList=new ArrayList();
		for(int i=0;i<quarters.length;i++){
			quatersList.add(quarters[i]);
		}
		this.ad_timeperiod.put("Quarters", quatersList);
	}
	
	public String getDrug() {
		return Drug;
	}

	public void setDrug(String drug) {
		Drug = drug;
	}

	public String getSymtom() {
		return Symtom;
	}

	public void setSymtom(String symtom) {
		Symtom = symtom;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
}
