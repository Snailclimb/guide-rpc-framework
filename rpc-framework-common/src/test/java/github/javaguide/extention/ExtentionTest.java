package github.javaguide.extention;

import github.javaguide.extension.ExtensionLoader;
import github.javaguide.extension.SPI;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author: Zekun Fu
 * @date: 2025/5/10 22:55
 * @Description:类加载器测试
 */
public class ExtentionTest {

    @Test
    public void test() {
        // 接口的类加载器
        ExtensionLoader<HelloServiceBean>classLoader = ExtensionLoader.getExtensionLoader(HelloServiceBean.class);

        // 实现特定的类加载器
        HelloServiceBean bean = classLoader.getExtension("helloService1");
        HelloServiceBean bean2 = classLoader.getExtension("helloService2");

        // 测试
        bean.test();
        bean2.test();

    }

}

@SPI
interface HelloServiceBean {
    public void test();
}

@Slf4j
class HelloServiceBeanImp implements HelloServiceBean{
    @Override
    public void test() {
        log.info("你好我是hello1");
    }
}

@Slf4j
class HelloServiceBeanImp2 implements HelloServiceBean {
    @Override
    public void test() {
        log.info("你好我是hello2");
    }
}
