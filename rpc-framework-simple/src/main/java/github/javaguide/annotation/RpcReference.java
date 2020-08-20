package github.javaguide.annotation;


import github.javaguide.spring.ReferenceAnnotationBeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * RPC reference annotation, autowire the service implementation class
 * @author smile2coder
 * @see ReferenceAnnotationBeanPostProcessor
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
@Component
public @interface RpcReference {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}
