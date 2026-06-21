package framework.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)        // utilisable sur les CLASSES seulement
@Retention(RetentionPolicy.RUNTIME) // lisible au runtime par reflection
public @interface Controller {
}