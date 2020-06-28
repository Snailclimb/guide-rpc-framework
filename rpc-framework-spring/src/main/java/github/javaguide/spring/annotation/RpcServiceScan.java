package github.javaguide.spring.annotation;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @description:
 * @author:lvxuhong
 * @date:2020/6/18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RpcServiceScannerRegistrar.class)
public @interface RpcServiceScan {

    String value();
}
