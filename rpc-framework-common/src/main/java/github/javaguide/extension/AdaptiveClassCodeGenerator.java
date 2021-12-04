package github.javaguide.extension;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 类代码生成器, 参考dubbo
 * 这个是简易的实现，考虑扩展名查找的问题，这里规定要实现的子类型的方法最末端一定要穿一个扩展名字符串
 * 或者在 Adaptive 注解中传入扩展名，两者都没有会抛异常
 * @see AdaptiveClassCodeGenerator#generateExtNameAssignment
 * @author yuli.net
 * @createTime 2021/12/1
 */
@Slf4j
public class AdaptiveClassCodeGenerator {
    private static final String CODE_PACKAGE = "package %s;\n";

    private static final String CODE_IMPORTS = "import %s;\n";

    private static final String CODE_CLASS_DECLARATION = "public class %s$Adaptive implements %s {\n";

    private static final String CODE_METHOD_DECLARATION = "public %s %s(%s) %s {\n%s}\n";

    private static final String CODE_METHOD_ARGUMENT = "%s arg%d";

    private static final String CODE_METHOD_THROWS = "throws %s";

    private static final String CODE_UNSUPPORTED = "throw new UnsupportedOperationException(\"The method %s of interface %s is not adaptive method!\");\n";

    private static final String CODE_EXT_NAME_ASSIGNMENT = "String extName = %s;\n";

    private static final String CODE_EXTENSION_ASSIGNMENT = "%s extension = (%s) ExtensionLoader.getExtensionLoader(%s.class).getExtension(extName);\n";

    private static final String CODE_EXT_NAME_NULL_CHECK = "if(extName == null) "
            + "throw new IllegalStateException(\"Failed to get extension (%s) name from extName use keys(%s)\");\n";

    private static final String CODE_EXTENSION_METHOD_INVOKE_ARGUMENT = "arg%d";

    private final Class<?> type;

    public AdaptiveClassCodeGenerator(Class<?> type) {
        this.type = type;
    }

    public String generate() {
        return this.generate(false);
    }

    public String generate(boolean sort) {
        if (!hasAdaptiveMethod()) {
            throw new IllegalStateException("no need to generate adaptive class");
        }
        StringBuilder code = new StringBuilder();
        code.append(generatePackage());
        code.append(generateImport());
        code.append(generateClassDeclaration());

        Method[] methods = type.getMethods();
        if (sort) {
            Arrays.sort(methods, Comparator.comparing(Method::toString));
        }
        for (Method method : methods) {
            code.append(generateMethod(method));
        }
        code.append('}');

        if (log.isDebugEnabled()) {
            log.debug(code.toString());
        }
        return code.toString();
    }

    private String generateMethod(Method method) {
        String methodReturnType = method.getReturnType().getCanonicalName();
        String methodName = method.getName();
        String methodContent = generateMethodContent(method);
        String methodArgs = generateMethodArguments(method);
        String methodThrows = generateMethodThrows(method);
        return String.format(CODE_METHOD_DECLARATION, methodReturnType, methodName, methodArgs, methodThrows, methodContent);
    }

    /**
     * 生成方法内部处理异常的代码，如果没有抛出过异常，那么直接返回空串就可以了
     * 否则将异常用 "," 分割，拼接起来
     * @param method 方法体
     * @return 拼接好的代码字符串
     */
    private String generateMethodThrows(Method method) {
        Class<?>[] exceptionClasses = method.getExceptionTypes();
        if (exceptionClasses.length > 0) {
            String exceptionString = Arrays.stream(exceptionClasses)
                                           .map(Class::getCanonicalName)
                                           .collect(Collectors.joining(", "));
            return String.format(CODE_METHOD_THROWS, exceptionString);
        } else {
            return "";
        }
    }

    /**
     * 方法内容的生成实现，这里注意，最终生成的服务只有加了Adaptive注解的方法是
     * 可以正常使用的，否则就会抛异常，具体原因可以看看这部分代码实现
     * @see AdaptiveClassCodeGenerator#generateUnsupported
     * @param method 要填充的方法
     * @return 最终填充之后的具体内容的字符串形式
     */
    private String generateMethodContent(Method method) {
        Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
        StringBuilder code = new StringBuilder(512);
        if (adaptiveAnnotation == null) {
            return generateUnsupported(method);
        } else {
            String[] value = getMethodAdaptiveValue(adaptiveAnnotation);
            // 如果没有值直接就抛异常，这里对比Dubbo的处理策略，没有默认选项
            code.append(generateExtNameAssignment(value, method));
            code.append(generateExtensionAssignment());
            // return statement
            code.append(generateReturnAndInvocation(method));
        }
        return code.toString();
    }

    private String generateExtensionAssignment() {
        return String.format(CODE_EXTENSION_ASSIGNMENT, type.getName(), type.getName(), type.getName());
    }

    /**
     * 生成返回代码并且执行相应的方法
     * 这里传入的参数都是 arg + (number)的形式，例如：arg0
     * @param method 要生成的方法
     * @return 返回类型和执行方法体的代码拼接字符串
     */
    private String generateReturnAndInvocation(Method method) {
        String returnStatement = method.getReturnType().equals(void.class) ? "" : "return ";

        String args = IntStream.range(0, method.getParameters().length)
                .mapToObj(i -> String.format(CODE_EXTENSION_METHOD_INVOKE_ARGUMENT, i))
                .collect(Collectors.joining(", "));

        return returnStatement + String.format("extension.%s(%s);\n", method.getName(), args);
    }

    /**
     * 这里是要生成扩展名赋值部分的实现，Dubbo中要提前判断方法中是否含有URL参数，从而判断当前方法的扩展名到底是哪个
     * 为了便于理解和实现，现在更改为要求两种方式给出扩展名
     * 1. Adaptive注解直接给出相应扩展名，如果传入多个扩展名的值，默认使用第一个传入的扩展名实现
     * 2. 如果Adaptive注解没有扩展名，则需要实现的方法的最后一个参数给出扩展名（参数的名称可以任意定义）
     *
     * 方法的参数是否符合规范会在 methodParamsCheck 方法中检查，不符合规范则抛出异常
     * @see AdaptiveClassCodeGenerator#methodParamsCheck
     * @param value 扩展名数组
     * @param method 要扩展的方法
     * @return 扩展名生成部分的代码
     */
    private String generateExtNameAssignment(String[] value, Method method) {
        Class<?>[] argTypes = method.getParameterTypes();
        String lastArgName = method.getParameters()[argTypes.length - 1].getName();
        if ((value == null || value.length == 0) && !methodParamsCheck(argTypes)) {
            throw new IllegalStateException("extension name not found");
        }
        String extName = ((value == null || value.length == 0) ? lastArgName : "\"" + value[0] + "\"");
        return String.format(CODE_EXT_NAME_ASSIGNMENT, extName)
                + String.format(CODE_EXT_NAME_NULL_CHECK, type.getName(), extName);
    }

    /**
     * 自适应扩展方法参数合法性检查，方法的参数至少要有扩展名(规定作为最后一个参数)，并且类型为String类型
     * @param argTypes 要生成的方法的所有类型数组
     * @return 当前方法所带的参数是否合法
     */
    private boolean methodParamsCheck(Class<?>[] argTypes) {
        int n = argTypes.length;
        return (n != 0) && argTypes[n - 1].equals(String.class);
    }

    private String[] getMethodAdaptiveValue(Adaptive adaptiveAnnotation) {
        return adaptiveAnnotation.value();
    }

    private String generateUnsupported(Method method) {
        return String.format(CODE_UNSUPPORTED, method, type.getName());
    }

    private String generateClassDeclaration() {
        return String.format(CODE_CLASS_DECLARATION, type.getSimpleName(), type.getCanonicalName());
    }

    private String generateImport() {
        return String.format(CODE_IMPORTS, ExtensionLoader.class.getName());
    }

    private String generatePackage() {
        return String.format(CODE_PACKAGE, type.getPackage().getName());
    }

    private boolean hasAdaptiveMethod() {
        return Arrays.stream(type.getMethods()).anyMatch(m -> m.isAnnotationPresent(Adaptive.class));
    }

    /**
     * 生成方法的参数，注意要和 generateReturnAndInvocation 方法返回的参数的对应上
     * @param method 方法
     * @return 传入参数的代码拼接字符串
     */
    private String generateMethodArguments(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return IntStream.range(0, paramTypes.length)
                        .mapToObj(index -> String.format(CODE_METHOD_ARGUMENT, paramTypes[index].getCanonicalName(), index))
                        .collect(Collectors.joining(", "));
    }
}
