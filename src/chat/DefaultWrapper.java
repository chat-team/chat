package chat;

import java.io.*;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/**
 * Created by He Tao on 2015/12/13.
 */
@WebServlet(name = "DefaultWrapper")
public class DefaultWrapper extends HttpServlet  {
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher rd = getServletContext().getNamedDispatcher("default");
        HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
            public String getServletPath() { return req.getServletPath(); }
        };
        rd.forward(wrapped, resp);
    }
}
