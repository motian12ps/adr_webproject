package com.insilico.util;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.insilico.dao.QueryVariables;
import com.insilico.datastructure.OLAPcube;

public class printTool {
	
	//print List
	public static void printList(List list){
		for(int i=0;i<list.size();i++){
			System.out.println(list.get(i));
		}
	}
	
	
	//print Map
	public static void printMap(Map map){
		Iterator it=map.keySet().iterator();
		while(it.hasNext()){
			Object key=it.next();
			System.out.println("key:"+key+", value:"+map.get(key));
		}
	}
	
	//output the results into Console
	public static void printResults(List<Entry<Integer,Double>> outputList){
		for(int i=0;i<outputList.size();i++){
			int labelindex=outputList.get(i).getKey();
			Map label=OLAPcube.labelMap.get(labelindex);
			double measure=outputList.get(i).getValue();
			System.out.println("label="+label+" ,measure="+measure);
		}
	}
	
	//output the results into HTML under the case without specific drug and symptom
	public static void printResultsToHTML(PrintWriter out,QueryVariables queryvar,List<Entry<Integer, Double>> outputList){
		
		//printTool.printList(outputList);
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.println("    The results of ADR detection using measure:"+queryvar.getMeasure()+" is the following: <br />");
		//<table border="1" cellspacing="0">
		out.println("<table border=\"1\" cellspacing=\"0\">");
		
		//output the first row-label of the table.
		out.println("<tr>");
		out.println("<td>Drug</td>");
		out.println("<td>Symptom</td>");
		Map tmplabel=OLAPcube.labelMap.get(0);
		Iterator it=tmplabel.keySet().iterator();
		while(it.hasNext()){
			String labelname=(String) it.next();
			if((!labelname.equals("drug_name"))&&(!labelname.equals("pt"))){
				out.println("<td>"+labelname+"</td>");
			}
			
		}
		out.println("<td> Count</td>");
		out.println("<td>"+queryvar.getMeasure()+"</td>");
		
		out.println("<td>a</td>");
		out.println("<td>b</td>");
		out.println("<td>c</td>");
		out.println("<td>d</td>");
		
		out.println("</tr>");
		for(int i=0;i<outputList.size();i++){
			out.println("<tr>");
			
			int labelindex=outputList.get(i).getKey();
			double measure=outputList.get(i).getValue();
			Map label=OLAPcube.labelMap.get(labelindex);
			out.println("<td>"+label.get("drug_name")+"</td>");
			out.println("<td>"+label.get("pt")+"</td>");
			Iterator it1=label.keySet().iterator();
			while(it1.hasNext()){
				String labelname=(String) it1.next();
				if((!labelname.equals("drug_name"))&&(!labelname.equals("pt"))){
					out.println("<td>"+label.get(labelname)+"</td>");
				}
			}	
			out.println("<td>"+OLAPcube.cellset.get(labelindex)+"</td>");
			out.println("<td>"+measure+"</td>");
			out.println("<td>"+OLAPcube.abcdMap.get(labelindex).get(0)+"</td>");
			out.println("<td>"+OLAPcube.abcdMap.get(labelindex).get(1)+"</td>");
			out.println("<td>"+OLAPcube.abcdMap.get(labelindex).get(2)+"</td>");
			out.println("<td>"+OLAPcube.abcdMap.get(labelindex).get(3)+"</td>");
			out.println("</tr>");
		}
		
		out.println("  </BODY>");
		out.println("</HTML>");
		
		out.flush();
		out.close();
	}
	
	//output the results to the html under the case with specific drug and specific symptom
	public static void printHTMLDS(Map resultMap, QueryVariables queryvar, PrintWriter out){
		
		String measure=queryvar.getMeasure();
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		out.println("<HTML>");
		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
		out.println("  <BODY>");
		out.println("    The results of ADR detection using measure:"+queryvar.getMeasure()+" is the following: <br />");
		//<table border="1" cellspacing="0">
		out.println("<table border=\"1\" cellspacing=\"0\">");
		
		//output the first row-label of the table.
		out.println("<tr>");
		out.println("<td>Drug</td>");
		out.println("<td>Symptom</td>");
		out.println("<td>Year</td>");
		out.println("<td>Count</td>");
		out.println("<td>Measure:"+measure+"</td>");
		out.println("<td>a</td>");
		out.println("<td>b</td>");
		out.println("<td>c</td>");
		out.println("<td>d</td>");
		out.println("</tr>");
		
		
		out.println("<tr>");
		out.println("<td>"+queryvar.getDrug()+"</td>");
		out.println("<td>"+queryvar.getSymtom()+"</td>");
		out.println("<td>"+queryvar.getAd_timeperiod().get("Year")+" "+queryvar.getAd_timeperiod().get("Quarters")+"</td>");
		out.println("<td>"+resultMap.get("a")+"</td>");
		if(measure.equals("ROR")){
			out.println("<td>"+resultMap.get("ROR")+"</td>");
		}else if(measure.equals("PRR")){
			out.println("<td>"+resultMap.get("PRR")+"</td>");
		}else if(measure.equals("IC")){
			out.println("<td>"+resultMap.get("IC")+"</td>");
		}
		out.println("<td>"+resultMap.get("a")+"</td>");
		out.println("<td>"+resultMap.get("b")+"</td>");
		out.println("<td>"+resultMap.get("c")+"</td>");
		out.println("<td>"+resultMap.get("d")+"</td>");
		
		out.println("</tr>");
		
		out.flush();
		out.close();
		
	}

}
