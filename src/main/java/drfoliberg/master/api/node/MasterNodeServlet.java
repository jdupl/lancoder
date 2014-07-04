package main.java.drfoliberg.master.api.node;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MasterNodeServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	private static final long serialVersionUID = 8887369974516325892L;

	MasterNodeServletListener listener;

	public MasterNodeServlet(MasterNodeServletListener listener) {
		this.listener = listener;
	}
	
	
}
