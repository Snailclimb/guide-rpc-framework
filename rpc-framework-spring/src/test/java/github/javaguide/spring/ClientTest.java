package github.javaguide.spring;

import github.javaguide.api.Hello;
import github.javaguide.api.HelloService;
import github.javaguide.spring.annotation.RpcServiceScan;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author:lvxuhong
 * @date:2020/6/19
 */
public class ClientTest {

    @Test
    public void test() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(TestConfig.class);
        applicationContext.refresh();
        applicationContext.start();

        HelloService helloService = applicationContext.getBean(HelloService.class);
        Hello hello = Hello.builder().message("test message").description("test description").build();
        String res = helloService.hello(hello);
        String expectedResult = "Hello description is " + hello.getDescription();
        Assert.assertEquals(expectedResult, res);

    }

    //@Configuration
    @RpcServiceScan("github.javaguide.api")
    public static class TestConfig {

    }
}
