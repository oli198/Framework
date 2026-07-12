package framework.servlet;

import framework.model.ModelAndView;
import framework.util.UrlMethod;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class FrontControllerServlet extends HttpServlet {
    
    private String viewPrefix;
    private String viewSuffix;
    
    @Override
    public void init() throws ServletException {
        viewPrefix = getServletContext().getInitParameter("viewPrefix");
        viewSuffix = getServletContext().getInitParameter("viewSuffix");
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processRequest(req, resp, "POST");
    }

    private void processRequest(HttpServletRequest req, HttpServletResponse resp, String httpMethod)
            throws ServletException, IOException {
        
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        String cleanUrl = url.replace(contextPath, "");
        
        if (cleanUrl.isEmpty()) {
            cleanUrl = "/";
        }

        Map<UrlMethod, Object[]> routes = (Map<UrlMethod, Object[]>) getServletContext().getAttribute("routes");
        
        UrlMethod urlMethod = new UrlMethod();
        urlMethod.setUrl(cleanUrl);
        urlMethod.setMethod(httpMethod);
        
        if (routes.containsKey(urlMethod)) {
            Object[] routeData = routes.get(urlMethod);
            Object instance = routeData[0];
            Method method = (Method) routeData[1];
            
            printMethodInfo(method);
            
            try {
                Object result = method.invoke(instance, req, resp);
                
                if (result instanceof ModelAndView) {
                    ModelAndView mav = (ModelAndView) result;
                    
                    for (String key : mav.getModel().keySet()) {
                        req.setAttribute(key, mav.getModel().get(key));
                    }
                    
                    String viewPath = viewPrefix + mav.getUrl() + viewSuffix;
                    RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(viewPath);
                    dispatcher.forward(req, resp);
                }
            } catch (Exception e) {
                resp.setContentType("text/html; charset=UTF-8");
                PrintWriter out = resp.getWriter();
                out.println("<h1>Erreur lors de l'exécution</h1>");
                out.println("<p>" + e.getMessage() + "</p>");
                e.printStackTrace(out);
            }
        } else {
            resp.setContentType("text/html; charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.println("<h1>URL non trouvée: " + cleanUrl + " [" + httpMethod + "]</h1>");
            out.println("<h2>Routes disponibles:</h2>");
            out.println("<ul>");
            for (UrlMethod um : routes.keySet()) {
                Object[] routeData = routes.get(um);
                Method method = (Method) routeData[1];
                out.println("<li>" + um + " : " + method.getName() + "</li>");
            }
            out.println("</ul>");
        }
    }

    private void printMethodInfo(Method method) {
        System.out.println("=== Méthode Appelée ===");
        System.out.println("Nom: " + method.getName());
        System.out.println("Classe: " + method.getDeclaringClass().getName());
        System.out.println("Retour: " + method.getReturnType().getName());
        System.out.println("Paramètres:");
        
        Parameter[] parameters = method.getParameters();
        for (Parameter param : parameters) {
            System.out.println("  - " + param.getType().getName() + " " + param.getName());
        }
        System.out.println("========================");
    }
}
