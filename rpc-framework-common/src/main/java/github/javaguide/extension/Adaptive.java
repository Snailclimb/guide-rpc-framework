package github.javaguide.extension;

import java.lang.annotation.*;

/**
 * 自适应扩展点注解
 * @author yuli.net
 * @createTime 2021/12/1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Adaptive {
    String[] value() default {};
}
