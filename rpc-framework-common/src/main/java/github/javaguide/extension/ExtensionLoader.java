package github.javaguide.extension;

import github.javaguide.factory.SingletonFactory;
import github.javaguide.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * refer to dubbo spi: https://dubbo.apache.org/zh-cn/docs/source_code_guide/dubbo-spi.html
 */
@Slf4j
public final class ExtensionLoader<T> {

    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";

    /**
     * 扩展类加载器的缓存，每一个类都有一个扩展类加载器。
     * 需要考虑多线程的问题
     * */
    private static final Map<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();

    /**
     * 扩展类实力的缓存，根据全类名进行缓存
     * */
//    private static final Map<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    private final Class<?> type;

    /**
     * 实力缓存，根据名字进行缓存
     * 保证可见性的 Holder。
     * */
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    /**
     * 类缓存，根据名称进行缓存，从文件中进行读取的key，value
     * */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    /**
     * 获取扩展类加载器
     * */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type) {
        if (type == null) {
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if (!type.isInterface()) {
            // 需要是接口
            throw new IllegalArgumentException("Extension type must be an interface.");
        }
        if (type.getAnnotation(SPI.class) == null) {
            // 类上需要包含SPI注解
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        // 创建类加载器，直接就是使用ConcurrentHashMap进行创建的，每一个类有一个自己的类加载器
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if (extensionLoader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }

        return extensionLoader;
    }

    public T getExtension(String name) {
        if (StringUtil.isBlank(name)) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        // 创建一个对象，如果没有的情况下，创建一个新的
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<>());
            holder = cachedInstances.get(name);
        }
        // 单例模式创建对象，双检测锁。没有只是使用ConcurrentHashMap
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 使用SigletonFactory创建单例bean
     * */
    private T createExtension(String name) {
        // 1. 首先获取扩展类加载器
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw new RuntimeException("扩展类不存在:  " + name);
        }
        // 2. 获取实例
        return (T) SingletonFactory.getInstance(clazz);
    }

    private Map<String, Class<?>> getExtensionClasses() {
        // 1. 从缓存中获取所有的类
        Map<String, Class<?>> classes = cachedClasses.get();
        // 2. 缓存中没有，进行双检测锁
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = new HashMap<>();
                    // 3. 从文件夹中加载所有的扩展类
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    /**
     * java的SPI机制
     * */
    private void loadDirectory(Map<String, Class<?>> extensionClasses) {
        // 1. 构建配置文件的路径
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            // 2. Java的SPI，扩展类加载器，然后设置文件的URl
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    // 3. 加载并解析
                    loadResource(extensionClasses, classLoader, resourceUrl);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // 读取配置文件的没一行
            while ((line = reader.readLine()) != null) {
                // 1. 过滤掉注释
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    line = line.substring(0, ci);
                }
                // 2. 去掉空格
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        // 3. = 实现的key value对解析，存入到map中
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        if (name.length() > 0 && clazzName.length() > 0) {
                            // 4. Java的SPI的具体实现
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
