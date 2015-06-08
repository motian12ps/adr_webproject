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
import com.insilico.util.printTool;


public class QueryFromMysql {
	
	public static List query(QueryVariables queryvar){
		
		Connection conn=QueryFromMysql.getConnection();
		QueryFromMysql.generateOLAPCube(conn, queryvar);
		OLAPcube.computeABCD();
		OLAPcube.setMeasurevalue(queryvar);
		List outputList;
		outputList=OLAPcube.outputK(queryvar);
		//printTool.printResults(outputList);
		return outputList;

		
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
//			int count=0;
//			while(rs.next()){
//				//System.out.println(rs.getDate("time"));
//				count++;
//			}
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
			if(drugname.length()!=0){
				selectpart=selectpart+" drug_name,";
				drugconstraint="(drug_name='"+drugname+"')";
				part.add(drugconstraint);
			}

			
			//System.out.println(drugconstraint);
			
			String symtomconstraint=" ";
			String symtomname=queryvar.getSymtom();
			if(symtomname.length()!=0){
				selectpart=selectpart+" pt,";
				symtomconstraint="(pt='"+symtomname+"')";
				part.add(symtomconstraint);
			}
			
			
			//System.out.println(symtomconstraint);
			String whereconstraint=" where ";
			for(int i=0;i<part.size();i++){
				whereconstraint=whereconstraint+part.get(i)+" and ";
			}
			whereconstraint=whereconstraint.substring(0, whereconstraint.length()-4);
			//System.out.println(whereconstraint);
			
			selectpart=selectpart.substring(0, selectpart.length()-1);
			//System.out.println(selectpart);
			
			sql=selectpart+tablepart+whereconstraint+";";
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
	
	public static void computeABCD(OLAPcube olapbuc){
		
	}
}
