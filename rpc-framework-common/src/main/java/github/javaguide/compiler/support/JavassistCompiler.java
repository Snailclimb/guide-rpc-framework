package github.javaguide.compiler.support;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javassist.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 利用 javassist 通过代码字符串生成字节码，并加载到vm中 ，具体参考
 * 1. http://www.javassist.org/tutorial/tutorial.html
 * 2. https://github.com/apache/dubbo/blob/3.0/dubbo-common/src/main/java/org/apache/dubbo/common/compiler/support/JavassistCompiler.java
 * @author yuli.net
 * @createTime 2021/12/3
 */
@Slf4j
public class JavassistCompiler extends AbstractCompiler {
    private static final Pattern IMPORT_PATTERN = Pattern.compile("import\\s+([\\w.*]+);\n");

    private static final Pattern EXTENDS_PATTERN = Pattern.compile("\\s+extends\\s+([\\w.]+)[^{]*\\{\n");

    private static final Pattern IMPLEMENTS_PATTERN = Pattern.compile("\\s+implements\\s+([\\w.]+)\\s*\\{\n");

    private static final Pattern METHODS_PATTERN = Pattern.compile("\n(private|public|protected)\\s+");

    private static final Pattern FIELD_PATTERN = Pattern.compile("[^\n]+=[^\n]+;");

    private final List<String> importList = Lists.newArrayList();

    private final Map<String, String> fullNames = Maps.newHashMap();

    private final List<String> interfaceList = Lists.newArrayList();

    private final List<String> constructorList = Lists.newArrayList();

    private final List<String> fieldList = Lists.newArrayList();

    private final List<String> methodList = Lists.newArrayList();

    /**
     * 真正执行编译的部分了，因为在父类中加了锁，所以这里是线程安全的
     * @param classLoader 类加载器
     * @param className 类名
     * @param source 类的源码
     * @return 编译之后类型
     */
    @Override
    public Class<?> doCompile(ClassLoader classLoader, String className, String source) throws Throwable {
        String superClassName = "java.lang.Object";

        // 首先 处理一下import语句，这里要记录一下import进来的类对应的包，以及类的全称类名
        Matcher matcher = IMPORT_PATTERN.matcher(source);
        while (matcher.find()) {
            String originPkg = matcher.group(1).trim();
            int pos = originPkg.lastIndexOf(".");
            if (pos > 0) {
                String pkg = originPkg.substring(0, pos);
                importList.add(pkg);
                if (!originPkg.endsWith(".*")) {
                    fullNames.put(originPkg.substring(pos + 1), pkg);
                }
            }
        }

        // 然后添加一下继承的超类，默认超类为 Object 类
        matcher = EXTENDS_PATTERN.matcher(source);
        while (matcher.find()) {
            superClassName = getFullClassName(matcher.group(1).trim());
        }

        // 处理要实现的接口，一般来讲，实现多接口都是用 "," 分隔的
        // 注意，这里的接口要存放全名，也就是 "package.interface" 这种形式
        matcher = IMPLEMENTS_PATTERN.matcher(source);
        while (matcher.find()) {
            String[] interfaces = matcher.group(1).trim().split(",");
            Arrays.stream(interfaces).forEach(face -> interfaceList.add(getFullClassName(face.trim())));
        }

        // 处理构造函数、私有方法、对外暴露的方法
        String body = source.substring(source.indexOf("{") + 1, source.length() - 1);
        String[] methods = METHODS_PATTERN.split(body);
        String classSimpleName = getSimpleClassName(className);
        Arrays.stream(methods).map(String::trim)
                              .filter(m -> !m.isEmpty())
                              .forEach(method -> {
                                  if (method.startsWith(classSimpleName)) {
                                      constructorList.add("public " + method);
                                  } else if (FIELD_PATTERN.matcher(method).matches()) {
                                      fieldList.add("private " + method);
                                  } else {
                                      methodList.add("public " + method);
                                  }
                              });

        ClassPool classPool = new ClassPool(true);
        classPool.insertClassPath(new LoaderClassPath(classLoader));
        // 编译生成目标类
        CtClass ctClass = classPool.makeClass(className, classPool.get(superClassName));

        // 生成import包
        importList.forEach(classPool::importPackage);

        // add implemented interfaces
        for (String face : interfaceList) {
            ctClass.addInterface(classPool.get(face));
        }

        // add constructors
        for (String constructor : constructorList) {
            ctClass.addConstructor(CtNewConstructor.make(constructor, ctClass));
        }

        // add fields
        for (String field : fieldList) {
            ctClass.addField(CtField.make(field, ctClass));
        }

        // add methods
        for (String method : methodList) {
            ctClass.addMethod(CtNewMethod.make(method, ctClass));
        }
        return ctClass.toClass();
    }

    /**
     * 实现的功能就是将 simpleName 转化为 fullName，也就是实现类似于 Class 下的 getName 方法
     * 只不过我们穿的不是Class对象，而是一个 simpleName的字符串
     * @param className 类或接口名
     * @return 全称类名或接口名
     */
    private String getFullClassName(String className) {
        if (className.contains(".")) {
            return className;
        }

        if (fullNames.containsKey(className)) {
            return fullNames.get(className);
        }

        // 以上缓存中都没找到超类的全称类名，那就要手动拼接一个了
        try {
            return classForName(className).getName();
        } catch (ClassNotFoundException e) {
            if (!importList.isEmpty()) {
                for (String pkg : importList) {
                    try {
                        return classForName(pkg + "." + className).getName();
                    } catch (ClassNotFoundException ignore) {
                    }
                }
            }
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * 这个是Dubbo给出的方法，先尝试所有已经存在的类型，看看能不能匹配上
     * 不得不感叹，这也太猛了
     */
    private Class<?> classForName(String className) throws ClassNotFoundException {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "boolean[]":
                return boolean[].class;
            case "byte[]":
                return byte[].class;
            case "char[]":
                return char[].class;
            case "short[]":
                return short[].class;
            case "int[]":
                return int[].class;
            case "long[]":
                return long[].class;
            case "float[]":
                return float[].class;
            case "double[]":
                return double[].class;
            default:
        }
        try {
            return arrayForName(className);
        } catch (ClassNotFoundException e) {
            // try to load from java.lang package
            if (className.indexOf('.') == -1) {
                try {
                    return arrayForName("java.lang." + className);
                } catch (ClassNotFoundException ignore) {
                    // ignore, let the original exception be thrown
                }
            }
            throw e;
        }
    }

    private Class<?> arrayForName(String className) throws ClassNotFoundException {
        return Class.forName(className.endsWith("[]")
                ? "[L" + className.substring(0, className.length() - 2) + ";"
                : className, true, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 将full class name转化为simple class name，和 getFullClassName 方法实现的功能正好相反
     * @param className full class name
     * @return simple class name
     */
    private static String getSimpleClassName(String className) {
        if (null == className) {
            return null;
        }
        int i = className.lastIndexOf('.');
        return i < 0 ? className : className.substring(i + 1);
    }
}
