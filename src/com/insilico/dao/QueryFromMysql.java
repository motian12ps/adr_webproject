package com.insilico.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.insilico.datastructure.OLAPcube;
import com.insilico.parameter.parameter;
import com.insilico.util.printTool;


public class QueryFromMysql {
	
	public static List query(QueryVariables queryvar){
		
		Connection conn=QueryFromMysql.getConnection();
		String drugname=queryvar.getDrug();
		String symtom=queryvar.getSymtom();
		String type=queryvar.getType();

		//There exists two mode, the first mode is we have known the specific drug and specific symptom
		if((drugname!=null&&(!drugname.equals("")))&&(symtom!=null&&(!symtom.equals("")))&&type.equals("advanced")){
			Map resultMap=QueryFromMysql.queryUnderDS(queryvar,conn);
			List list=new ArrayList();
			list.add(resultMap);
			return list;
		}else{
			QueryFromMysql.generateOLAPCube(conn, queryvar);
			OLAPcube.computeABCD();
			OLAPcube.setMeasurevalue(queryvar);
			List outputList;
			outputList=OLAPcube.outputK(queryvar);
			//printTool.printResults(outputList);
			return outputList;
		}


		
	}
	
	public static Connection getConnection(){
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url="jdbc:mysql://192.168.188.110:3306/faers";
			
			String username="root";
			String password="";
			
			conn=DriverManager.getConnection(url,username,password);
			
			System.out.println("conn----"+conn);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	
	
	// the query can be divided into four phase:(1) the contingency cube extraction phase;
	//(2) the candidate rule generation phase;(3) the measure calculation phase;(4) the signal ranking and output phase
	public static OLAPcube generateOLAPCube(Connection conn,QueryVariables queryvar){
		
		Statement stmt=null;
		try {
			stmt=conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String sql=QueryFromMysql.generateSQL(queryvar);
			ResultSet rs=stmt.executeQuery(sql);
			ResultSetMetaData rsMD=rs.getMetaData();	
			System.out.println("complete sql");

			OLAPcube olcube=new OLAPcube(rs,rsMD);
			return olcube;
			//System.out.println(count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
		
	}
	
	
	//generate the sql language by using Queryvariables queryvar.
	public static String generateSQL(QueryVariables queryvar){
		String sql = null;
		//System.out.println(queryvar.getType());
		if(queryvar.getType().equals("simple")){

			String startdate=QueryFromMysql.setDatetype((String)queryvar.getTimeperiod().get("startTime"), "start");
			String enddate=QueryFromMysql.setDatetype((String)queryvar.getTimeperiod().get("endTime"), "end");
			
			
			String selectpart="select drug_name,pt,";
			String[] part=new String[5];
			
			System.out.println(queryvar.getMiningAttributes());
			
			Iterator it=queryvar.getMiningAttributes().keySet().iterator();
			
			while(it.hasNext()){
				String key=(String) it.next();
				if(key.equals("Year") && ((Boolean)queryvar.getMiningAttributes().get("Year"))==true){
					selectpart=selectpart+"time,";
					part[0]=" (time between '"+startdate+"' and '"+enddate+"') and";
				}else if(key.equals("Age") && ((Boolean) queryvar.getMiningAttributes().get("Age"))==true){
					selectpart=selectpart+"age, ";
					part[1]=" (age between 0 and 150 or age is null) and";
				}else if(key.equals("Weight") && ((Boolean) queryvar.getMiningAttributes().get("Weight"))==true){
					selectpart=selectpart+"weight,";
					part[2]=" (weight between 0 and 10000 or weight is null) and";
				}else if(key.equals("Gender") && ((Boolean) queryvar.getMiningAttributes().get("Gender"))==true){
					selectpart=selectpart+"gender,";
					part[3]=" (gender is null or gender='F' or gender='M') and";
				}else if(key.equals("Country") && ((Boolean) queryvar.getMiningAttributes().get("Country"))==true){
					selectpart=selectpart+"country,";
					part[4]=" (country is null or country is not null) and";
				}
			}
			String tablepart=" from Denormalized_ADR";
			
			//delete the last comma
			selectpart=(String) selectpart.subSequence(0, selectpart.length()-1);
			
			String wherepart=" where";
			for(int i=0;i<5;i++){
				if(part[i]!=null){
					wherepart=wherepart+part[i];
				}
			}
			wherepart=wherepart.substring(0, wherepart.length()-4);
			sql=selectpart+tablepart+wherepart+";";
			System.out.println(sql);
			
		}else if(queryvar.getType().equals("advanced")){
			String selectpart="select drug_name,pt,";
			String tablepart=" from Denormalized_ADR ";
			List<String> part=new ArrayList<String>();
			
			
			//set the time constraint
			String timeconstraint=null;
			
			String year=(String) queryvar.getAd_timeperiod().get("Year");
			List quarterList=(List) queryvar.getAd_timeperiod().get("Quarters");
			
			selectpart=selectpart+" time,";
			
			for(int i=0;i<quarterList.size();i++){
				
				String starttime=QueryFromMysql.setDatetype(year+"_"+quarterList.get(i),"start");
				String endtime=QueryFromMysql.setDatetype(year+"_"+quarterList.get(i), "end");
				
				timeconstraint=timeconstraint+" time between '"+starttime+"' and '"+endtime+"' or";
			}
			timeconstraint=timeconstraint.substring(5, timeconstraint.length()-2);
			timeconstraint=" ("+timeconstraint+") ";
			part.add(timeconstraint);
			
			//System.out.println(timeconstraint);
			
			//set the age constraint
			String ageconstraint=" ";
			
			List ageList=(List) queryvar.getAge();
			if(!ageList.isEmpty()){
				selectpart=selectpart+" age,";
			}
			for(int i=0;i<ageList.size();i++){
				
				String[] ageline=((String) ageList.get(i)).split("_");
					
				String startage=ageline[0];
				String endage=ageline[1];
				
				ageconstraint=ageconstraint+" age between "+startage+" and "+endage+" or";
			}
			ageconstraint=" ("+ageconstraint.substring(0, ageconstraint.length()-2)+") ";
			part.add(ageconstraint);
			
			
			//System.out.println(ageconstraint);
			
			String drugconstraint=" ";
			String drugname=queryvar.getDrug();

			
			String symtomconstraint=" ";
			String symtomname=queryvar.getSymtom();
			
			if(drugname.equals("")&&symtomname.equals("")){
				String whereconstraint=" where ";
				for(int i=0;i<part.size();i++){
					whereconstraint=whereconstraint+part.get(i)+" and ";
				}
				whereconstraint=whereconstraint.substring(0, whereconstraint.length()-4);
				//System.out.println(whereconstraint);
				
				selectpart=selectpart.substring(0, selectpart.length()-1);
				//System.out.println(selectpart);
				
				sql=selectpart+tablepart+whereconstraint+";";
			}
			
			
//			if(drugname.length()!=0){
//				selectpart=selectpart+" drug_name,";
//				drugconstraint="(drug_name='"+drugname+"')";
//				part.add(drugconstraint);
//			}
//
//			
//			//System.out.println(drugconstraint);
//			

//			if(symtomname.length()!=0){
//				selectpart=selectpart+" pt,";
//				symtomconstraint="(pt='"+symtomname+"')";
//				part.add(symtomconstraint);
//			}
			
			
			//System.out.println(symtomconstraint);
//			String whereconstraint=" where ";
//			for(int i=0;i<part.size();i++){
//				whereconstraint=whereconstraint+part.get(i)+" and ";
//			}
//			whereconstraint=whereconstraint.substring(0, whereconstraint.length()-4);
//			//System.out.println(whereconstraint);
//			
//			selectpart=selectpart.substring(0, selectpart.length()-1);
//			//System.out.println(selectpart);
//			
//			sql=selectpart+tablepart+whereconstraint+";";
			System.out.println(sql);
			
		
		}
		return sql;
	}
	
	public static String setDatetype(String datepair,String type){
		String[] line=datepair.split("_");
		String date=line[0];
		if(line[1].equals("all") &&type.equals("start")){
			date=date+"-01-01";
		}else if(line[1].equals("Q1") &&type.equals("start")){
			date=date+"-01-01";
		}else if(line[1].equals("Q2") &&type.equals("start")){
			date=date+"-04-01";
		}else if(line[1].equals("Q3") &&type.equals("start")){
			date=date+"-07-01";
		}else if(line[1].equals("Q4") &&type.equals("start")){
			date=date+"-10-01";
		}else if(line[1].equals("all") &&type.equals("end")){
			date=date+"-12-31";
		}else if(line[1].equals("Q1") &&type.equals("end")){
			date=date+"-03-31";
		}else if(line[1].equals("Q2") &&type.equals("end")){
			date=date+"-06-30";
		}else if(line[1].equals("Q3") &&type.equals("end")){
			date=date+"-09-30";
		}else if(line[1].equals("Q4") &&type.equals("end")){
			date=date+"-12-31";
		}
		return date;
	}
	
	//query under when we have the specific drug and symptom
	public static Map queryUnderDS(QueryVariables queryvar,Connection conn){
		

		String sql_a;
		String sql_b;
		String sql_c;
		String sql_d;
		
		String selectpart="select count(*) ";
		String tablepart="from Denormalized_ADR ";
		String whereconstraint_a="where ";
		String whereconstraint_b="where ";
		String whereconstraint_c="where ";
		String whereconstraint_d="where ";
		
			
		List<String> part_a=new ArrayList<String>();
		List<String> part_b=new ArrayList<String>();
		List<String> part_c=new ArrayList<String>();
		List<String> part_d=new ArrayList<String>();
		
		//set the time constraint
		String timeconstraint=null;
		
		String year=(String) queryvar.getAd_timeperiod().get("Year");
		List quarterList=(List) queryvar.getAd_timeperiod().get("Quarters");
		
		
		for(int i=0;i<quarterList.size();i++){
			
			String starttime=QueryFromMysql.setDatetype(year+"_"+quarterList.get(i),"start");
			String endtime=QueryFromMysql.setDatetype(year+"_"+quarterList.get(i), "end");
			
			timeconstraint=timeconstraint+" time between '"+starttime+"' and '"+endtime+"' or";
		}
		timeconstraint=timeconstraint.substring(5, timeconstraint.length()-2);
		timeconstraint=" ("+timeconstraint+") ";
		part_a.add(timeconstraint);
		part_b.add(timeconstraint);
		part_c.add(timeconstraint);
		part_d.add(timeconstraint);
		
		//System.out.println(timeconstraint);
		
		//set the age constraint
		String ageconstraint=" ";
		
		List ageList=(List) queryvar.getAge();

		for(int i=0;i<ageList.size();i++){
			
			String[] ageline=((String) ageList.get(i)).split("_");
				
			String startage=ageline[0];
			String endage=ageline[1];
			
			ageconstraint=ageconstraint+" age between "+startage+" and "+endage+" or";
		}
		ageconstraint=" ("+ageconstraint.substring(0, ageconstraint.length()-2)+") ";
		part_a.add(ageconstraint);
		part_b.add(ageconstraint);
		part_c.add(ageconstraint);
		part_d.add(ageconstraint);
		//System.out.println(ageconstraint);
		
		String drugname=queryvar.getDrug();
		String symptom=queryvar.getSymtom();
		String lastconstraint_a="( drug_name='"+drugname+"' and pt='"+symptom+"')";
		String lastconstraint_b="( drug_name='"+drugname+"')";
		String lastconstraint_c="( pt='"+symptom+"')";
		part_a.add(lastconstraint_a);
		part_b.add(lastconstraint_b);
		part_c.add(lastconstraint_c);
		

		for(int i=0;i<part_a.size();i++){
			whereconstraint_a=whereconstraint_a+part_a.get(i)+" and ";
		}
		whereconstraint_a=whereconstraint_a.substring(0, whereconstraint_a.length()-4);
		//System.out.println(whereconstraint_a);

		for(int i=0;i<part_b.size();i++){
			whereconstraint_b=whereconstraint_b+part_b.get(i)+" and ";
		}
		whereconstraint_b=whereconstraint_b.substring(0, whereconstraint_b.length()-4);
		//System.out.println(whereconstraint_b);
		
		for(int i=0;i<part_c.size();i++){
			whereconstraint_c=whereconstraint_c+part_c.get(i)+" and ";
		}
		whereconstraint_c=whereconstraint_c.substring(0, whereconstraint_c.length()-4);
		//System.out.println(whereconstraint_c);
		
		for(int i=0;i<part_d.size();i++){
			whereconstraint_d=whereconstraint_d+part_d.get(i)+" and ";
		}
		whereconstraint_d=whereconstraint_d.substring(0, whereconstraint_d.length()-4);
		//System.out.println(whereconstraint_d);

		//selectpart=selectpart.substring(0, selectpart.length()-1);
		//System.out.println(selectpart);
		sql_a=selectpart+tablepart+whereconstraint_a+";";
		sql_b=selectpart+tablepart+whereconstraint_b+";";
		sql_c=selectpart+tablepart+whereconstraint_c+";";
		sql_d=selectpart+tablepart+whereconstraint_d+";";
		System.out.println(sql_a);
		System.out.println(sql_b);
		System.out.println(sql_c);
		System.out.println(sql_d);

		Statement stmt1=null;
		Statement stmt2=null;
		Statement stmt3=null;
		Statement stmt4=null;
		
		Map resultMap=new HashMap();

		try {
			stmt1=conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			stmt2=conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			stmt3=conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			stmt4=conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
			ResultSet rs_a=stmt1.executeQuery(sql_a);
			ResultSet rs_b=stmt2.executeQuery(sql_b);
			ResultSet rs_c=stmt3.executeQuery(sql_c);
			ResultSet rs_d=stmt4.executeQuery(sql_d);

			double a=0;
			double b=0;
			double c=0;
			double d=0;
			while(rs_a.next()){
				a=rs_a.getInt(1);
				//System.out.println("a="+a);
				resultMap.put("a", a);
			}
			while(rs_b.next()){
				b=rs_b.getInt(1)-a;
				//System.out.println("b="+b);
				resultMap.put("b", b);
			}
			while(rs_c.next()){
				c=rs_c.getInt(1)-a;
				//System.out.println("c="+c);
				resultMap.put("c", c);
			}
			while(rs_d.next()){
				d=rs_d.getInt(1)-a-b-c;
				//System.out.println("d="+d);
				resultMap.put("d", d);
			}
			
			String measure=queryvar.getMeasure();
			if(measure.equals("ROR")){
				double ROR=0;
				if(b==0||c==0){
					ROR=parameter.INFINITY;
				}else{
					ROR=a*d/(c*b);
				}
				resultMap.put("ROR", ROR);

			}
			
			double PRR=0;
			if(measure.equals("PRR")){
				if((a+b)==0||c==0){
					PRR=parameter.INFINITY;
				}else{
					PRR=a*(c+b)/(a+b)/c;
				}

				resultMap.put("PRR", PRR);
				
			}
			
			double IC=0;
			
			if(measure.equals("IC")){
				if((a+b)==0||(a+c)==0){
					IC=parameter.INFINITY;
				}else if((a*(a+b+c+d)/(a+b)/(a+c))==0){
					IC=parameter.MINUSINFINITY;
				}else{
					double express=a*(a+b+c+d)/(a+b)/(a+c);
					IC=Math.log(express)/Math.log(2);
				}
				resultMap.put("IC", IC);
			}
			System.out.println(resultMap);
			return resultMap;
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
}
