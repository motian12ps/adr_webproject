package com.insilico.datastructure;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.insilico.dao.QueryVariables;
import com.insilico.parameter.parameter;
import com.insilico.util.compressDataset;
import com.insilico.util.printTool;
import com.insilico.util.sortTool;



public class OLAPcube {
	
	//Map label index of each cell, to the value of each corresponding value(count)
	public static Map<Integer, Integer> cellset=new TreeMap<Integer, Integer>();  
	
	public static Map<Integer, Integer> cellset_b=new TreeMap<Integer,Integer>();
	
	public static Map<Integer, Integer> cellset_c=new TreeMap<Integer,Integer>();
	
	public static Map<Integer, Integer> cellset_d=new TreeMap<Integer, Integer>();
	
	
	public static List labelList=new ArrayList();
	
	public static List labelList_b=new ArrayList();
	
	public static List labelList_c=new ArrayList();
	
	public static List labelList_d=new ArrayList();
	
	//map the index with the label 
	public static Map<Integer, Map> labelMap=new TreeMap<Integer, Map>();
	
	//map the index with the label for b
	public static Map<Integer,Map> labelMap_b=new TreeMap<Integer, Map>();

	//map the index with the label for c
	public static Map<Integer,Map> labelMap_c=new TreeMap<Integer, Map>();
	
	//map the index with the label for d
	public static Map<Integer,Map> labelMap_d=new TreeMap<Integer, Map>();
	
	//map with each labelindex with each corresponding measure value 
	public static Map<Integer,Double> measureMap=new TreeMap<Integer, Double>();
	
	
	//labelcount for a
	public static int labelcount=0;	
	
	//labelcount for b
	public static int labelcount_b=0;
	
	//labelcount for c
	public static int labelcount_c=0;
	
	//labelcount for d
	public static int labelcount_d=0;
	
	//map the label index to a list of a b c d.
	public static Map<Integer,List<Integer>> abcdMap=new TreeMap<Integer, List<Integer>>(); 
	
	//map the label index to a list of a
	public static Map<Integer, Integer> sublabela=new TreeMap<Integer, Integer>();
	
	//map the label index to its corresponding b
	public static Map<Integer, Integer> sublabelb=new TreeMap<Integer, Integer>();
	
	//map the label index to its corresponding c
	public static Map<Integer, Integer> sublabelc=new TreeMap<Integer, Integer>();
	
	//map the label index to its corresponding d
	public static Map<Integer, Integer> sublabeld=new TreeMap<Integer, Integer>();
	
	public OLAPcube(){
	}
	
	public OLAPcube(ResultSet rs,ResultSetMetaData rsMD){
		try {
			int columnsize=rsMD.getColumnCount();

			intilize();

			//we have constraint of the number of reference when parameter.countnum!=-1
			//otherwise, we will iterate on all of the data
			if(parameter.countnum!=-1){
				int count=0;
				while(rs.next()&&count<parameter.countnum){
					
					//OLAPcell olapcell=new OLAPcell();
					Map label=new TreeMap();
					for(int i=1;i<=columnsize;i++){
						String entryname=rsMD.getColumnName(i);
						
						String entryclass=rsMD.getColumnClassName(i);
						Object value=rs.getObject(entryname);
						if(entryname.equals("age")){
							value=String.valueOf(value);
							value=compressDataset.compressAgeIntoAgeRange((String)value);
						}
						if(entryname.equals("time")){
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							String time=df.format(value);
							value=compressDataset.compressDateIntoQuarter(time);
						}
						label.put(rsMD.getColumnName(i), value);
						
//						System.out.println(rsMD.getColumnName(i)+" "+value);
//						System.out.println(rs.getObject(entryname));

					}
					
					//if this label is a new label, put label into cellset and initialize the count=1
					//else count++ for the exiting label
					if(OLAPcube.addLabel(label,labelList,labelMap,"a")){
						//System.out.println(label);
						cellset.put(labelcount-1, 1);
					}else{
						//System.out.println(label);
						int labelindex=labelList.indexOf(label);
						int oldcount=cellset.get(labelindex);
						int newcount=oldcount+1;
						cellset.put(labelindex, newcount);
					}
					
					count++;
					
				}
			}else if(parameter.countnum==-1){
				double count=0;
				int rowCount=0;
				double ratio=0;
				try{
					rs.last();
					rowCount=rs.getRow();
				}catch(Exception e){
					e.printStackTrace();
				}
				rs.first();
				double rssize=(double)rowCount;
				System.out.println(rssize);
				while(rs.next()){
				
					//OLAPcell olapcell=new OLAPcell();
					Map label=new TreeMap();
					for(int i=1;i<=columnsize;i++){
						String entryname=rsMD.getColumnName(i);
					
						String entryclass=rsMD.getColumnClassName(i);
						Object value=rs.getObject(entryname);
						if(entryname.equals("age")){
							value=String.valueOf(value);
							value=compressDataset.compressAgeIntoAgeRange((String)value);
						}
						if(entryname.equals("time")){
							DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
							String time=df.format(value);
							value=compressDataset.compressDateIntoQuarter(time);
						}
						label.put(rsMD.getColumnName(i), value);
						//System.out.println(rsMD.getColumnName(i)+" "+value);
						//System.out.println(rs.getObject(entryname));

					}
				
					//if this label is a new label, put label into cellset and initialize the count=1
					//else count++ for the exiting label
					if(OLAPcube.addLabel(label,labelList,labelMap,"a")){
						//System.out.println(label);
						
						cellset.put(labelcount-1, 1);
					}else{
						//System.out.println(label);
						int labelindex=labelList.indexOf(label);
						int oldcount=cellset.get(labelindex);
						int newcount=oldcount+1;
						cellset.put(labelindex, newcount);
					}
					
					count++;
					if((count/rssize)-ratio>=0.002){
						ratio=count/rssize;
						System.out.println("progess:"+ratio+"%");
					}
					
					
					
				}
				
				//System.out.println("Complete generate cellset");
			}
			//System.out.println("Complete generate cellset");


						

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	//add label into labelList, the put<index, label> into labelmap
	public static boolean addLabel(Map label,List labelList,Map labelmap,String type){
		if(!labelList.contains(label)){
			labelList.add(label);
			if(type.equals("a")){
				labelmap.put(labelcount, label);
				labelcount++;
			}else if(type.equals("b")){
				labelmap.put(labelcount_b, label);
				labelcount_b++;
			}else if(type.equals("c")){
				labelmap.put(labelcount_c, label);
				labelcount_c++;
			}else if(type.equals("d")){
				labelmap.put(labelcount_d, label);
				labelcount_d++;
			}

			return true;
		}
		return false;
	}
	
	//compute a, b, c, d
	public static void computeABCD(){
		
		labelMap_b.clear();
		labelMap_c.clear();
		labelMap_d.clear();
		
		sublabela.clear();
		sublabelb.clear();
		sublabelc.clear();
		sublabeld.clear();
		
		
		cellset_b.clear();
		cellset_c.clear();
		cellset_d.clear();
		
		//generate the map between labelindex and a
		for(int i=0;i<labelList.size();i++){
			Map label=(Map) labelList.get(i);
			int labelindex=i;
			int a=cellset.get(labelindex);	
			sublabela.put(labelindex, a);
			//System.out.println(label+" "+a);			
		}
		
		//generate the labelList for b,c,d
		generateSublabelList();
		
		//compute abcd sunday code
		generateAbcdMap();
		
		
		
		
	}
	
	//generate sublabelList of b,c,d
	public static void generateSublabelList(){
		
		for(int i=0;i<labelList.size();i++){
			
			
			Map tmplabel_b=new TreeMap();
			
			//copy label[i] to tmplabel_b
			tmplabel_b=(Map) ((TreeMap)labelList.get(i)).clone();
			
			//remove key=pt from tmplabel_b
			tmplabel_b.remove("pt");
			
			
			OLAPcube.addLabel(tmplabel_b, labelList_b,labelMap_b,"b");
			
			Map tmplabel_c=new TreeMap();
			
			////copy label[i] to tmplabel_c
			tmplabel_c=(Map) ((TreeMap)labelList.get(i)).clone();
			
			//remove key=drug_name from tmplabel_b
			tmplabel_c.remove("drug_name");
			
			OLAPcube.addLabel(tmplabel_c, labelList_c,labelMap_c,"c");
			
			Map tmplabel_d=new TreeMap();
			
			////copy label[i] to tmplabel_d
			tmplabel_d=(Map) ((TreeMap)labelList.get(i)).clone();
			
			//remove key=drug_name from tmplabel_d
			tmplabel_d.remove("drug_name");
			tmplabel_d.remove("pt");
			
			OLAPcube.addLabel(tmplabel_d, labelList_d,labelMap_d,"d");
			
		
		}
//		printTool.printList(labelList);
//		System.out.println("========");
//		printTool.printList(labelList_b);
//		System.out.println("========");
//		printTool.printList(labelList_c);
//		System.out.println("========");
//		printTool.printList(labelList_d);
//		System.out.println("========");
//		printTool.printMap(labelMap_b);
//		System.out.println("========");
//		printTool.printMap(labelMap_c);
//		System.out.println("========");
//		printTool.printMap(labelMap_d);
//		System.out.println("========");
		
		
	}
	
	public static void generateAbcdMap(){
		//set cellset_b,c,d
		setCellset_bcd(labelList_b,"b");
		setCellset_bcd(labelList_c,"c");
		setCellset_bcd(labelList_d,"d");
		
		//printTool.printMap(cellset_b);
		//printTool.printMap(cellset_c);
		//printTool.printMap(cellset_d);
		
		for(int i=0;i<labelList.size();i++){
			List abcdList=new ArrayList();
			int labelindex=i;
			double a=cellset.get(labelindex);
			double b=0;
			double c=0;
			double d=0;
			abcdList.add(a);
			Map label=(Map) labelList.get(labelindex);
			for(int i1=0;i1<labelList_b.size();i1++){
				int labelindex_b=i1;
				Map label_b=(Map) labelList_b.get(i1);
				if(label.entrySet().containsAll(label_b.entrySet())){
					b=cellset_b.get(labelindex_b)-a;
					abcdList.add(b);					
					break;
				}
			}
			for(int i2=0;i2<labelList_c.size();i2++){
				int labelindex_c=i2;
				Map label_c=(Map) labelList_c.get(i2);
				if(label.entrySet().containsAll(label_c.entrySet())){
					c=cellset_c.get(labelindex_c)-a;
					abcdList.add(c);
					break;
				}
			}
			for(int i3=0;i3<labelList_d.size();i3++){
				int labelindex_d=i3;
				Map label_d=(Map) labelList_d.get(i3);
				if(label.entrySet().containsAll(label_d.entrySet())){
					d=cellset_d.get(labelindex_d)-a-b-c;
					abcdList.add(d);
					break;
				}
			}
			abcdMap.put(labelindex, abcdList);
		}
		//print abcd count for each cell;
		//printTool.printMap(abcdMap);
		//printTool.printMap(labelMap);
		
	}
	
	
	
	public static void setCellset_bcd(List curlabelist,String type){
		
		//printTool.printList(curlabelist);
		for(int i=0;i<curlabelist.size();i++){
			Map sublabel=(Map)curlabelist.get(i);
			//printTool.printMap(sublabel);
			int sumcount=0;
			for(int j=0;j<labelList.size();j++){
				int labelindex_a=j;
				Map label_a=(Map)labelList.get(labelindex_a);
				if(label_a.entrySet().containsAll(sublabel.entrySet())){
					sumcount=sumcount+cellset.get(labelindex_a);
				}
			}	
			if(type.equals("b")){
				cellset_b.put(i, sumcount);
			}else if(type.equals("c")){
				cellset_c.put(i, sumcount);
			}else if(type.equals("d")){
				cellset_d.put(i, sumcount);
			}
		}		
		
	}
	
	
	public static void setMeasurevalue(QueryVariables queryvar){
		measureMap.clear();
		String measuretype=queryvar.getMeasure();
		System.out.println(measuretype);
		Iterator it=abcdMap.keySet().iterator();
		while(it.hasNext()){
			int labelindex=(Integer) it.next();
			List abcdList=abcdMap.get(labelindex);
			double a=(Double) abcdList.get(0);
			double b=(Double) abcdList.get(1);
			double c=(Double) abcdList.get(2);
			double d=(Double) abcdList.get(3);
			//System.out.println(a+" "+b+" "+c+" "+d);
			if(measuretype.equals("PRR")&&a>=3.0){

				double PRR=0;
				if((a+b)==0||c==0){
					PRR=parameter.INFINITY;
				}else{
					PRR=a*(c+b)/(a+b)/c;
				}
				if(PRR>=parameter.threshold_prr){
					measureMap.put(labelindex, PRR);
				}

				
				
			}else if(measuretype.equals("ROR")&&a>=3.0){
				double ROR=0;
				if(b==0||c==0){
					ROR=parameter.INFINITY;
				}else{
					ROR=a*d/(c*b);
				}
				if(ROR>=parameter.threshold_ror){
					measureMap.put(labelindex, ROR);
				}
				
			}else if(measuretype.equals("IC")&&a>=3.0){
				double IC=0;
				if((a+b)==0||(a+c)==0){
					IC=parameter.INFINITY;
				}else if((a*(a+b+c+d)/(a+b)/(a+c))==0){
					IC=parameter.MINUSINFINITY;
				}else{
					double express=a*(a+b+c+d)/(a+b)/(a+c);
					IC=Math.log(express)/Math.log(2);
				}
				if(IC>=parameter.threshold_ic){
					measureMap.put(labelindex, IC);
				}
				
								
			}
		}
		//printTool.printMap(measureMap);

	}
	
	public static List outputK(QueryVariables queryvar){
		int numofoutput=queryvar.getNumOutput();
		//System.out.println(queryvar.getNumOutput());
		
		List outputList=new ArrayList();
		
		if(numofoutput==-1){
			outputList=sortTool.sortByValue(measureMap);
			
		}else if(numofoutput!=-1){
			outputList=sortTool.sortByValue(measureMap,numofoutput);
			
		}
		//outputList=sortTool.sortByValue(measureMap, 3);
		//printTool.printList(outputList);
		//printTool.printResults(outputList);
		
		return outputList;
		
		
	}
	
	public static void intilize(){
		
		//clear previous results
		cellset.clear();
		labelList.clear();
		labelMap.clear();
		abcdMap.clear();
		
		labelMap_b.clear();
		labelMap_c.clear();
		labelMap_d.clear();
		
		sublabela.clear();
		sublabelb.clear();
		sublabelc.clear();
		sublabeld.clear();
		
		labelcount=0;
		labelcount_b=0;
		labelcount_c=0;
		labelcount_d=0;
	}

}
