package github.javaguide.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 获取单例对象的工厂类
 *
 * @author shuang.kou
 * @createTime 2020年06月03日 15:04:00
 */
public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();
    private SingletonFactory() {
    }

    public static <T> T getInstance(Class<T> c) {
        if (c == null) {
            throw new IllegalArgumentException();
        }
        String key = c.toString();
        Object instance = OBJECT_MAP.get(key);
        
        if (instance == null) {
            synchronized (c) {
                instance = OBJECT_MAP.get(key);
                if (instance == null) {
                    try {
                        instance = c.getDeclaredConstructor().newInstance();
                        OBJECT_MAP.put(key, instance);
                    } catch (IllegalAccessException
                            | InstantiationException
                            | NoSuchMethodException
                            | InvocationTargetException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                }
            }
        }
        return c.cast(instance);
    }
}
