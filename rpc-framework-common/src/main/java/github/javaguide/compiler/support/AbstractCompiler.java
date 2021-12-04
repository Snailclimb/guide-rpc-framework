package github.javaguide.compiler.support;

import github.javaguide.compiler.RpcCompiler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模版方法模式，实现基本的编译方法，通过自适应扩展点，手动加载整个类
 * 具体的 doCompile 方法交给具体的子类去执行
 * @author yuli.net
 * @createTime 2021/12/3
 */
public abstract class AbstractCompiler implements RpcCompiler {

    /**
     * 这里正则匹配我改过了，原来是这样的：package\s+([$_a-zA-Z][$_a-zA-Z0-9//.]*);
     * 中括号里的 '.' 应该是不需要转译的，所以我就把双斜杠去掉了
     */
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([$_a-zA-Z][$_a-zA-Z0-9.]*);");

    private static final Pattern CLASS_PATTERN = Pattern.compile("class\\s+([$_a-zA-Z][$_a-zA-Z0-9]*)\\s+");

    private static final Map<String, Lock> CLASS_IN_CREATION_MAP = new ConcurrentHashMap<>();

    /**
     * 编译生成类之前所做的必要的检查工作，参考Dubbo的源码：https://github.com/apache/dubbo下的dubbo-common中，检查的步骤如下：
     * 1. 先检查package是否存在
     * 2. 检查是否存在合法的类名
     * 3. 拼接一下全称类名：包路径 + 类的simple name
     * 4. 这里处理编译逻辑，只能有一个线程在编译当前类型，所以要加锁，为了能释放锁资源，要把对应的Lock存到map里
     * 5. 调用子类实现的方法进行编译
     *
     * @param code 代码拼接字符串
     * @param classLoader 类加载器
     * @return 加载之后的类型
     */
    @Override
    public Class<?> compile(String code, ClassLoader classLoader) {
        code = code.trim();
        Matcher matcher = PACKAGE_PATTERN.matcher(code);
        String pkg;
        if (matcher.find()) {
            pkg = matcher.group(1);
        } else {
            pkg = "";
        }
        matcher = CLASS_PATTERN.matcher(code);
        String cls;
        if (matcher.find()) {
            cls = matcher.group(1);
        } else {
            throw new IllegalArgumentException("No such class name in " + code);
        }
        String className = pkg != null && pkg.length() > 0 ? pkg + "." + cls : cls;
        Lock lock = CLASS_IN_CREATION_MAP.get(className);
        if (lock == null) {
            CLASS_IN_CREATION_MAP.putIfAbsent(className, new ReentrantLock());
            lock = CLASS_IN_CREATION_MAP.get(className);
        }
        try {
            lock.lock();
            return Class.forName(className, true, classLoader);
        } catch (ClassNotFoundException e) {
            if (!code.endsWith("}")) {
                throw new IllegalStateException("The java code not endsWith \"}\", code: \n" + code + "\n");
            }
            try {
                return doCompile(classLoader, className, code);
            } catch (RuntimeException t) {
                throw t;
            } catch (Throwable t) {
                throw new IllegalStateException("Failed to compile class, cause: " + t.getMessage() + ", class: " + className + ", code: \n" + code + "\n, stack: " + t);
            }
        } finally {
            lock.unlock();
        }
    }

    protected abstract Class<?> doCompile(ClassLoader classLoader, String name, String source) throws Throwable;
}
