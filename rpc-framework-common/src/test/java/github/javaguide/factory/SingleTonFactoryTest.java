package github.javaguide.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author: Zekun Fu
 * @date: 2025/5/10 21:37
 * @Description:
 */
@Slf4j
public class SingleTonFactoryTest {
    public static Queue<SingletonBean> beans = new LinkedBlockingQueue<>();



    @Test
    public void test() {
        SingletonBean beanOnly = SingletonFactory.getInstance(
                        ()-> SingletonBean.builder().msg("你好fzk").build(),
                        SingletonBean.class);
        Thread[] threads = new Thread[10];
        for (int i = 0; i < 10; i++) {
            // 开启十个线程，创建单例的对象，存储到beans中，结果beans中所有对象的hash值应该一致，并且msg都是你好fzk
            threads[i] = new Thread(new SingletonBean());
            threads[i].start();
        }
        try {
            for (int i = 0; i < 10; i++) {
                // 等待10个线程运行完成
                threads[i].join();
                log.info("第{}线程运行完成", i);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("共计创建{}对象", beans.size());
        for (SingletonBean bean: beans) {
            log.info("对象的hash为:{}, 消息为:{}", bean.hashCode(), bean.getMsg());
            assert bean.hashCode() == beanOnly.hashCode();
            assert bean.getMsg().equals(beanOnly.getMsg());
        }
    }

}

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class SingletonBean implements Runnable {

    private String msg;
    @Override
    public void run() {
        // 创建一个Singleton对象,存访到类的数组里面
        log.info("开始创建对象");
        SingleTonFactoryTest.beans.add(SingletonFactory.getInstance(
                ()-> SingletonBean.builder().msg("你好fzk").build(),
                SingletonBean.class));
        SingleTonFactoryTest.beans.add(SingletonFactory.getInstance((bean)-> bean.setMsg("你好fjh"),
                SingletonBean.class));
        SingletonBean bean = SingletonFactory.getInstance(SingletonBean.class);
        SingleTonFactoryTest.beans.add(bean);
        bean.setMsg("你好中国");
    }
}
