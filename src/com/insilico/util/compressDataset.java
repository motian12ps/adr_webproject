package com.insilico.util;

public class compressDataset {
	
	public static String compressDateIntoQuarter(String time){
		
		String[] line=time.split("-");
		String year=line[0];
		int month=Integer.parseInt(line[1]);
		String quarter="";
		if(1<=month&&month<=3){
			quarter="Q1";
		}else if(4<=month&&month<=6){
			quarter="Q2";
		}else if(7<=month&&month<=9){
			quarter="Q3";
		}else if(10<=month&&month<=12){
			quarter="Q4";
		}
		String compressedTime=year+"_"+quarter;
		return compressedTime;
	}
	
	public static String compressAgeIntoAgeRange(String Age){
		if(!Age.equals("null")){
			int age=Integer.parseInt(Age);
			String agerange="";
			if(0<=age&&age<=1){
				agerange="0_1";
			}else if(1<age&&age<=2){
				agerange="1_2";
			}else if(2<age&&age<=3){
				agerange="2_3";
			}else if(3<age&&age<=4){
				agerange="3_4";
			}else if(4<age&&age<=7){
				agerange="4_7";
			}else if(7<age&&age<=14){
				agerange="7_14";
			}else if(14<age&&age<=20){
				agerange="14_20";
			}else if(20<age&&age<=60){
				agerange="20_60";
			}else if(60<age){
				agerange="60_150";
			}
			return agerange;
		}else if(Age.equals("null")){
			//System.out.println("age is null");
			return "60_150";
		}
		return "";
		
	}

}
