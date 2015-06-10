package com.insilico.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

		List outputList;
		outputList=QueryFromMysql.query(queryvar);
		if(outputList.size()>1){
			printTool.printResultsToHTML(out,queryvar,outputList);
		}else if(outputList.size()==1){
			Map resultMap=(Map) outputList.get(0);
			printTool.printHTMLDS(resultMap, queryvar, out);
		}
		
		
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
