package github.javaguide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shuang.kou
 * @createTime 2020年05月12日 17:36:00
 */
public class HelloServiceImpl2 {
    private static final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    public String hello(Hello hello) {
        logger.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        logger.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
