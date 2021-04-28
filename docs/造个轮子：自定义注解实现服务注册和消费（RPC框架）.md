我们这里借用了 Spring 框架来简化该功能的实现。当然了，不通过 Spring 框架也可以实现，不过会麻烦一些。

先来看一下最终效果。

**1.通过注解注册服务：**

```java
@Slf4j
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {

    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}

```

**2.通过注解消费远程服务：**

```java
@Component
public class HelloController {

    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("111", "222"));
    }
}
```

**学会了如何自定义注解之后，不光可以加深我们对于 Spring 底层原理的认识，而且能够在很多方面优化自己的项目代码。**

首先，我们要实现一个包扫描器，来扫描我们自定义的注解。

### 实现自定义包扫描

```java
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {

    String[] basePackage();

}

```



### 自定义服务注册注解 `RpcService`

为此，我们自定义了一个注解 `RpcService`，并且这个注解有两个属性：

1. `version` :服务版本。主要是为后续不兼容升级提供可能
2. `group` : 服务所在的组。主要用于处理一个接口有多个类实现的情况。

并且， `RpcService`这个注解还使用了 Spring 提供的 `@Component` 注解。这样的话，使用 `RpcService`注解的类就会交由 Spring 管理（前提是被被`@ComponentScan`注解扫描到 ）。

```java
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC service annotation, marked on the service implementation class
 *
 * @author shuang.kou
 * @createTime 2020年07月21日 13:11:00
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Component
public @interface RpcService {

    /**
     * Service version, default value is empty string
     */
    String version() default "";

    /**
     * Service group, default value is empty string
     */
    String group() default "";

}
```

接下来，我们实现 `BeanPostProcessor` 接口，Spring Bean 在实例化之前会调用 `BeanPostProcessor` 接口的 `postProcessBeforeInitialization()`方法。

被我们使用 `RpcService`注解的类也算是 Spring Bean，所以，我们可以在`postProcessBeforeInitialization()`方法中去判断类上是否有`RpcService` 注解，如果有的话，就取出 `group` 和 `version` 的值。然后，再调用 `ServiceProvider` 的 `publishService()` 方法发布服务即可！

```java
import github.javaguide.annotation.RpcService;
import github.javaguide.config.RpcServiceConfig;
import github.javaguide.factory.SingletonFactory;
import github.javaguide.provider.ServiceProvider;
import github.javaguide.provider.impl.ZkServiceProviderImpl;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * call this method before creating the bean to see if the class is annotated
 *
 * @author shuang.kou
 * @createTime 2020年07月14日 16:42:00
 */
@Component
@Slf4j
public class SpringBeanPostProcessor implements BeanPostProcessor {


    private final ServiceProvider serviceProvider;

    public SpringBeanPostProcessor() {
        serviceProvider = SingletonFactory.getInstance(ServiceProviderImpl.class);
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with  [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // 获取注解
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            RpcServiceProperties rpcServiceConfig = RpcServiceProperties.builder()
                    .group(rpcService.group()).version(rpcService.version()).build();
            // 发布服务
            serviceProvider.publishService(bean, rpcServiceConfig);
        }
        return bean;
    }

}

```

