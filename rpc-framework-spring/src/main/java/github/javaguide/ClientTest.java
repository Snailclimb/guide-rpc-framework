package github.javaguide;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @description:
 * @author:lvxuhong
 * @date:2020/6/18
 */
public class ClientTest {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.register(Config.class);
        applicationContext.refresh();
        applicationContext.start();



    }
}
