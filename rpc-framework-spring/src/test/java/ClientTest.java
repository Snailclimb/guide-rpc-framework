import github.javaguide.Config;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

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
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        for (String s : beanDefinitionNames) {
            System.out.println(s);
        }

        applicationContext.getBean("Service1");
    }
}
