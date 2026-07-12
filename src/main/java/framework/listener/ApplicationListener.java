package framework.listener;

import framework.annotation.Url;
import framework.util.ClassScanner;
import framework.util.UrlMethod;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebListener
public class ApplicationListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            String packageName = sce.getServletContext().getInitParameter("controllerPackage");
            if (packageName == null || packageName.isEmpty()) {
                throw new Exception("Le paramètre 'controllerPackage' est manquant");
            }
            
            List<Class<?>> controllers = ClassScanner.getControllers(packageName);
            Map<UrlMethod, Object[]> routes = new HashMap<>();
            
            for (Class<?> controllerClass : controllers) {
                Object instance = controllerClass.getDeclaredConstructor().newInstance();
                
                for (Method method : controllerClass.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(Url.class)) {
                        Url urlAnnotation = method.getAnnotation(Url.class);
                        
                        UrlMethod urlMethod = new UrlMethod();
                        urlMethod.setUrl(urlAnnotation.value());
                        urlMethod.setMethod(urlAnnotation.method());
                        
                        if (routes.containsKey(urlMethod)) {
                            throw new Exception("Route déjà existante: " + urlMethod);
                        }
                        
                        routes.put(urlMethod, new Object[]{instance, method});
                    }
                }
            }
            
            sce.getServletContext().setAttribute("routes", routes);
            System.out.println("ApplicationListener: Routes chargées au démarrage");
            System.out.println("Nombre de routes: " + routes.size());
            
        } catch (Exception e) {
            System.err.println("Erreur au démarrage de l'application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application arrêtée");
    }
}
