package framework.servlet;

import framework.util.ClassScanner;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.util.*;

@WebServlet("/*")
public class FrontControllerServlet extends HttpServlet {

    // Liste de tous les controllers détectés au démarrage
    private List<Class<?>> controllers = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        // Lire le package depuis web.xml
        String packageName = getServletContext().getInitParameter("controllerPackage");

        if (packageName == null || packageName.isEmpty()) {
            throw new ServletException("Le paramètre 'controllerPackage' est manquant dans web.xml");
        }

        try {
            controllers = ClassScanner.getControllers(packageName);
            System.out.println("Controllers trouvés : " + controllers.size());
            for (Class<?> c : controllers) {
                System.out.println("  -> " + c.getName());
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des controllers", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp);
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        String url = req.getRequestURI();
        out.println("<h1>URL: " + url + "</h1>");

        
        out.println("<h2>Controllers enregistrés :</h2><ul>");
        for (Class<?> c : controllers) {
            out.println("<li>" + c.getName() + "</li>");
        }
        out.println("</ul>");
    }
}