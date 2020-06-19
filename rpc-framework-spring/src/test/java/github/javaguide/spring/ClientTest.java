package github.javaguide.spring;

import github.javaguide.spring.annotation.RpcServiceScan;
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



    }

    @Configuration
    @RpcServiceScan("github.javaguide.spring.service")
    public static class TestConfig {

    }
}
