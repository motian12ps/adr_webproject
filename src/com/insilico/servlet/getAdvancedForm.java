package com.insilico.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javassist.bytecode.Descriptor.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.insilico.dao.QueryFromMysql;
import com.insilico.dao.QueryVariables;
import com.insilico.util.printTool;

public class getAdvancedForm extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public getAdvancedForm() {
		super();
	}

	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	/**
	 * The doGet method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to get.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		this.doPost(request, response);
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag valueSymtom method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		
		QueryVariables queryvar=new QueryVariables();
		

		String year=request.getParameter("adyear");
		//out.println(year);
		//System.out.println(year);
		String[] quarters=request.getParameterValues("quarter");
		String drug=request.getParameter("Drug");
		String symtom=request.getParameter("Symtom");
		String measure=request.getParameter("measure");
		String numofcount=request.getParameter("ad_output");
		String numk=request.getParameter("K");
		String[] ages=request.getParameterValues("age");
		
//		System.out.println(drug+" "+symtom);
//		

//		System.out.println(measure);
//		System.out.println("numofcount"+numofcount);
	
		
//		
		queryvar.setAge(ages);
		

		queryvar.setAd_timeperiod(quarters, year);
		queryvar.setDrug(drug);
		queryvar.setSymtom(symtom);
		queryvar.setMeasure(measure);
		if(numofcount.equals("all")){
			queryvar.setNumOutput(numofcount);
		}else if(numofcount.contains("topk")){
			queryvar.setNumOutput(numofcount+"_"+numk);
		}

		queryvar.setType("advanced");
		QueryFromMysql.query(queryvar);
		
//		System.out.println("year="+queryvar.getAd_timeperiod().get("Year"));
//		System.out.println("quarters="+queryvar.getAd_timeperiod().get("Quarters"));
//		System.out.println("ages="+queryvar.getAge());
//		System.out.println("drug="+queryvar.getDrug());
//		System.out.println("symtom="+queryvar.getSymtom());
//		System.out.println("measure="+queryvar.getMeasure());
//		System.out.println("numouput="+queryvar.getNumOutput());
//		System.out.println("type="+queryvar.getType());
//		System.out.println("====================");
		
		List outputList;
		outputList=QueryFromMysql.query(queryvar);
		
		printTool.printResultsToHTML(out,queryvar,outputList);
		
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
