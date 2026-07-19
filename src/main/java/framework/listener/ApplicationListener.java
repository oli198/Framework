package framework.listener;

import framework.annotation.Url;
import framework.util.ClassScanner;
import framework.util.UrlMethod;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebListener
public class ApplicationListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            AnnotationConfigApplicationContext springContext = 
                new AnnotationConfigApplicationContext("controller", "service", "repository");
            
            sce.getServletContext().setAttribute("springContext", springContext);
            System.out.println("Spring Context initialisé");
            
            String packageName = sce.getServletContext().getInitParameter("controllerPackage");
            if (packageName == null || packageName.isEmpty()) {
                throw new Exception("Le paramètre 'controllerPackage' est manquant");
            }
            
            List<Class<?>> controllers = ClassScanner.getControllers(packageName);
            Map<UrlMethod, Object[]> routes = new HashMap<>();
            
            for (Class<?> controllerClass : controllers) {
                Object instance = springContext.getBean(controllerClass);
                
                injectDependencies(instance, springContext);
                
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
    
    private void injectDependencies(Object instance, AnnotationConfigApplicationContext springContext) {
        Class<?> clazz = instance.getClass();
        
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                try {
                    Object bean = springContext.getBean(field.getType());
                    field.set(instance, bean);
                    System.out.println("Injection: " + field.getName() + " dans " + clazz.getSimpleName());
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'injection de " + field.getName());
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AnnotationConfigApplicationContext context = 
            (AnnotationConfigApplicationContext) sce.getServletContext().getAttribute("springContext");
        
        if (context != null) {
            context.close();
            System.out.println("Spring Context fermé");
        }
    }
}
