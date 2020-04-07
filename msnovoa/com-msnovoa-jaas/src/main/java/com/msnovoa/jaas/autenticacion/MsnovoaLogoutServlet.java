package com.msnovoa.jaas.autenticacion;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "msnovoaLogoutServlet", urlPatterns = {"/logout"})
public class MsnovoaLogoutServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Invalidamos la sesión HTTP actual. Al llamar a este logout, también se llama al JAAS LoginModule logout().
        request.logout();

        //request.getSession().invalidate(); //Esta sería la opción en el caso de usar un LoginModule predefinido.

        // Redirigimos al usuario al inicio nuevamente.
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }
}
