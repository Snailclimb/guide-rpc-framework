package github.javaguide.compiler.service;

import github.javaguide.extension.Adaptive;
import github.javaguide.extension.SPI;

/**
 * @author yuli.net
 * @createTime 2021/12/4
 */
@SPI
public interface Fruit {
    @Adaptive
    String classification(String extName);

    @Adaptive
    void printClassification(String extName);
}
