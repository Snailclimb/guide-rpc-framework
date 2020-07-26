package github.javaguide;

import github.javaguide.extension.SPI;

/**
 * @author shuang.kou
 * @createTime 2020年05月10日 07:03:00
 */
@SPI
public interface HelloService {
    String hello(Hello hello);
}
