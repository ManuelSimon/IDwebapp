package com.msnovoa.jaas.autenticacion;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "msnovoaLogoutServlet", urlPatterns = {"/logout"})
public class MsnovoaLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Invalidate current HTTP session.
        // Will call JAAS LoginModule logout() method
        request.getSession().invalidate();

        // Redirect the user to the secure web page.
        // Since the user is now logged out the
        // authentication form will be shown
        response.sendRedirect(request.getContextPath()
                + "/index.jsp");

    }
}
