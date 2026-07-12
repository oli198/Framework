package framework.util;

import framework.annotation.Controller;
import java.io.*;
import java.net.*;
import java.util.*;

public class ClassScanner {

    /**
     * Liste toutes les classes dans un package donné
     */
    public static List<Class<?>> getClassesInPackage(String packageName) throws Exception {
        List<Class<?>> classes = new ArrayList<>();

        String path = packageName.replace('.', '/');
        URL resource = Thread.currentThread()
                             .getContextClassLoader()
                             .getResource(path);

        if (resource == null) {
            throw new Exception("Package introuvable : " + packageName);
        }

        File directory = new File(resource.toURI());

        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".class")) {
                String className = packageName + "." 
                                 + file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }

        return classes;
    }

   
    public static boolean hasControllerAnnotation(Class<?> clazz) {
        return clazz.isAnnotationPresent(Controller.class);
    }

    
    public static List<Class<?>> getControllers(String packageName) throws Exception {
        List<Class<?>> controllers = new ArrayList<>();

        for (Class<?> clazz : getClassesInPackage(packageName)) {
            if (hasControllerAnnotation(clazz)) {
                controllers.add(clazz);
            }
        }

        return controllers;
    }
}