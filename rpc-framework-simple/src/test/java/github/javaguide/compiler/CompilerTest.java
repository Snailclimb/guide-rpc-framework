package github.javaguide.compiler;

import github.javaguide.compiler.service.Fruit;
import github.javaguide.extension.ExtensionLoader;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @author yuli.net
 * @createTime 2021/12/4
 */
@Slf4j
public class CompilerTest {
    @Test
    public void doCompile() {
        Fruit fruit = ExtensionLoader.getExtensionLoader(Fruit.class).getAdaptiveExtension();
        fruit.printClassification("banana");
    }
}
