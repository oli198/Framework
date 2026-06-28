package framework.servlet;

import framework.annotation.RequestMapping;
import framework.util.ClassScanner;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

@WebServlet("/*")
public class FrontControllerServlet extends HttpServlet {
    private List<Class<?>> controllers = new ArrayList<>();
    private Map<String, MethodRoute> routes = new HashMap<>();

    @Override
    public void init() throws ServletException {
        String packageName = getServletContext().getInitParameter("controllerPackage");
        if (packageName == null || packageName.isEmpty()) {
            throw new ServletException("Le paramètre 'controllerPackage' est manquant dans web.xml");
        }
        
        try {
            controllers = ClassScanner.getControllers(packageName);
            scanRoutes();
        } catch (Exception e) {
            throw new ServletException("Erreur lors du scan des controllers", e);
        }
    }

    private void scanRoutes() throws Exception {
        for (Class<?> controllerClass : controllers) {
            Object instance = controllerClass.getDeclaredConstructor().newInstance();
            
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                    String url = mapping.value();
                    routes.put(url, new MethodRoute(instance, method));
                }
            }
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
        String contextPath = req.getContextPath();
        String cleanUrl = url.replace(contextPath, "");
        if (cleanUrl.isEmpty()) {
            cleanUrl = "/";
        }

        if (routes.containsKey(cleanUrl)) {
            MethodRoute route = routes.get(cleanUrl);
            try {
                route.method.invoke(route.instance, req, resp);
            } catch (Exception e) {
                out.println("<h1>Erreur lors de l'exécution</h1>");
                out.println("<p>" + e.getMessage() + "</p>");
            }
        } else {
            out.println("<h1>URL non trouvée: " + cleanUrl + "</h1>");
            out.println("<h2>URL disponibles:</h2>");
            out.println("<ul>");
            for (String routeUrl : routes.keySet()) {
                MethodRoute route = routes.get(routeUrl);
                out.println("<li>" + routeUrl + " : " + route.method.getName() + "</li>");
            }
            out.println("</ul>");
        }
    }

    private static class MethodRoute {
        Object instance;
        Method method;

        MethodRoute(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }
    }
}
