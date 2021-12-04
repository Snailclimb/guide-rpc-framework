package github.javaguide.compiler.service.impl;

import github.javaguide.compiler.service.Fruit;

/**
 * @author yuli.net
 * @createTime 2021/12/4
 */
public class FruitApple implements Fruit {
    @Override
    public String classification(String extName) {
        return "apple";
    }

    @Override
    public void printClassification(String extName) {
        System.out.println("adaptive annotation test: " + classification(extName));
    }
}
