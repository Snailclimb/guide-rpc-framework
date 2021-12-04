package github.javaguide.compiler;

import github.javaguide.extension.SPI;

/**
 * 自适应扩展点字节码编译器
 * 默认使用javassist实现
 * @author yuli.net
 * @createTime 2021/12/3
 */
@SPI
public interface RpcCompiler {
    Class<?> compile(String code, ClassLoader loader);
}
