package com.insilico.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class sortTool {
	
	
	//sort the map by values in descending order
	public static List sortByValue(Map map){
		
		List<Map.Entry<Integer, Double>> list=new ArrayList<Map.Entry<Integer,Double>>(map.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>(){

			@Override
			public int compare(Entry<Integer, Double> arg0,
					Entry<Integer, Double> arg1) {
				// TODO Auto-generated method stub
				return arg1.getValue().compareTo(arg0.getValue());
				//return arg0.getValue().compareTo(arg1.getValue());
			}
			
		});

		return list;
	}
	
	public static List sortByValue(Map map,int numofoutput){
		
		List<Map.Entry<Integer, Double>> list=new ArrayList<Map.Entry<Integer,Double>>(map.entrySet());
		
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>(){

			@Override
			public int compare(Entry<Integer, Double> arg0,
					Entry<Integer, Double> arg1) {
				// TODO Auto-generated method stub
				return arg1.getValue().compareTo(arg0.getValue());
			}
			
		});
		
		List sublist=new ArrayList();
		sublist=list.subList(0, numofoutput);

		return sublist;
	}

}
