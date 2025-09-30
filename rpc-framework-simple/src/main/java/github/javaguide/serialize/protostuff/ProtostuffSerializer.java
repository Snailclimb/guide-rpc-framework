package github.javaguide.serialize.protostuff;

import github.javaguide.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @author TangMinXuan
 * @createTime 2020年11月09日 20:13
 */
public class ProtostuffSerializer implements Serializer {

    /**
     * 使用 ThreadLocal 为每个线程分配独立的 LinkedBuffer 实例。
     * <p>
     * 由于 LinkedBuffer 在多线程环境下是非线程安全的，因此通过 ThreadLocal
     * 确保每个线程都有自己专属的缓冲区，避免并发访问导致的数据竞争或序列化异常
     * 线程管理的多个channel调用序列化算法是安全的
     */
    private static final ThreadLocal<LinkedBuffer> BUFFER =
            ThreadLocal.withInitial(() -> LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));

    @Override
    public byte[] serialize(Object obj) {
        Class<?> clazz = obj.getClass();
        Schema schema = RuntimeSchema.getSchema(clazz);
        // 每个线程第一次访问LinkedBuffer时才会创建ThreadLocal对象
        LinkedBuffer buffer = BUFFER.get();
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();// 清空复用
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        return obj;
    }
}
