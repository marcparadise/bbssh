/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bbssh.onlinehelp.servlets;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.bbssh.onlinehelp.da.DataManager;

/**
 *
 * @author Marc A. Paradise
 */
public class HelpProvider extends HttpServlet {
	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String screen = request.getParameter("s");
		String field = request.getParameter("f");
		//	System.out.println("s: " + screen + " f: " + field);
		request.setAttribute("HelpTopic", DataManager.getInstance().getHelpTopic(screen, field));
		String ua = request.getHeader("user-agent");
		if (ua == null || !ua.toLowerCase().contains("blackberry")) {
			request.getRequestDispatcher("/helphtml.jsp").forward(request, response);
		} else {
			request.getRequestDispatcher("/helpwml.jsp").forward(request, response);
		}
	}
	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods.">

	/**
	 * Handles the HTTP <code>GET</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>
}
