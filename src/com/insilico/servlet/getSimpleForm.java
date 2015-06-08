package com.insilico.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.insilico.dao.QueryFromMysql;
import com.insilico.dao.QueryVariables;
import com.insilico.util.printTool;

public class getSimpleForm extends HttpServlet {

	/**
	 * Constructor of the object.
	 */
	public getSimpleForm() {
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
//		response.setContentType("text/html");
//		PrintWriter out = response.getWriter();
//		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
//		out.println("<HTML>");
//		out.println("  <HEAD><TITLE>A Servlet</TITLE></HEAD>");
//		out.println("  <BODY>");
//		out.print("    This is ");
//		out.print(this.getClass());
//		out.println(", using the GET method");
//		out.println("  </BODY>");
//		out.println("</HTML>");
//		out.flush();
//		out.close();
	}

	/**
	 * The doPost method of the servlet. <br>
	 *
	 * This method is called when a form has its tag value method equals to post.
	 * 
	 * @param request the request send by the client to the server
	 * @param response the response send by the server to the client
	 * @throws ServletException if an error occurred
	 * @throws IOException if an error occurred
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		//set response type
		response.setContentType("text/html");
		
		PrintWriter out = response.getWriter();
		
		QueryVariables queryvar=new QueryVariables();
		
		String startyear=request.getParameter("startyear");
		String endyear=request.getParameter("endyear");
		String startquarter=request.getParameter("startquarter");
		String endquarter=request.getParameter("endquarter");
		
		String[] miningAttributes=request.getParameterValues("MiningAttributes");
		
		String measure=request.getParameter("Measure");
		
		String numofcount=request.getParameter("numberofoutput");

		queryvar.setTimeperiod(startyear, endyear, startquarter, endquarter);
		queryvar.setMiningAttributes(miningAttributes);
		queryvar.setMeasure(measure);
		queryvar.setNumOutput(numofcount);
		queryvar.setType("simple");

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
