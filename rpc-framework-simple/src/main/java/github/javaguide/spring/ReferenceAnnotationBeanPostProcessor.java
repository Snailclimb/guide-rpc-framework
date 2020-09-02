package github.javaguide.spring;

import github.javaguide.annotation.RpcReference;
import github.javaguide.entity.RpcServiceProperties;
import github.javaguide.extension.ExtensionLoader;
import github.javaguide.proxy.RpcClientProxy;
import github.javaguide.registry.ServiceDiscovery;
import github.javaguide.remoting.transport.ClientTransport;
import github.javaguide.remoting.transport.netty.client.NettyClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author smile2coder
 * @
 */
@Slf4j
public class ReferenceAnnotationBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter implements
        MergedBeanDefinitionPostProcessor, ApplicationContextAware {

    public static final String BEAN_NAME = "referenceAnnotationBeanPostProcessor";

    private String version = "version";
    private String group = "group";

    private ApplicationContext applicationContext;

    private ClientTransport rpcClient;

    private final Set<Class<? extends Annotation>> annotationTypes = new LinkedHashSet<>(1);

    private final Map<String, InjectionMetadata> injectionMetadataCache = new ConcurrentHashMap<>(256);

    public ReferenceAnnotationBeanPostProcessor() {
        this.annotationTypes.add(RpcReference.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(ClientTransport.class).getExtension("nettyClientTransport");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        InjectionMetadata metadata = findAutowiringMetadata(beanName, beanType, null);
        metadata.checkConfigMembers(beanDefinition);
    }

    private InjectionMetadata findAutowiringMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    metadata = buildAutowiringMetadata(clazz);
                    this.injectionMetadataCache.put(cacheKey, metadata);
                }
            }
        }
        return metadata;
    }

    private InjectionMetadata buildAutowiringMetadata(final Class<?> clazz) {

        List<InjectionMetadata.InjectedElement> elements = new ArrayList<>();
        Class<?> targetClass = clazz;

        do {
            final List<InjectionMetadata.InjectedElement> currElements = new ArrayList<>();

            ReflectionUtils.doWithLocalFields(targetClass, field -> {
                MergedAnnotation<?> ann = findReferenceAnnotation(field);
                if (ann != null) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        if (log.isInfoEnabled()) {
                            log.info("Autowired annotation is not supported on static fields: " + field);
                        }
                        return;
                    }
                    AnnotationAttributes annotationAttributes = (AnnotationAttributes)
                            ann.asMap(mergedAnnotation -> new AnnotationAttributes(mergedAnnotation.getType()));
                    String version = annotationAttributes.getString(this.version);
                    String group = annotationAttributes.getString(this.group);
                    currElements.add(new ReferenceFieldElement(field, version, group));
                }
            });

            elements.addAll(0, currElements);
            targetClass = targetClass.getSuperclass();
        }
        while (targetClass != null && targetClass != Object.class);

        return InjectionMetadata.forElements(elements, clazz);
    }

    @Nullable
    private MergedAnnotation<?> findReferenceAnnotation(AccessibleObject ao) {
        MergedAnnotations annotations = MergedAnnotations.from(ao);
        for (Class<? extends Annotation> type : this.annotationTypes) {
            MergedAnnotation<?> annotation = annotations.get(type);
            if (annotation.isPresent()) {
                return annotation;
            }
        }
        return null;
    }

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findAutowiringMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of reference dependencies failed", ex);
        }
        return pvs;
    }

    private class ReferenceFieldElement extends InjectionMetadata.InjectedElement {

        private String version;
        private String group;

        protected ReferenceFieldElement(Member member, String version, String group) {
            super(member, null);
            this.version = version;
            this.group = group;
        }

        @Override
        protected void inject(Object target, String requestingBeanName, PropertyValues pvs) throws Throwable {
            Field field = (Field) this.member;
            RpcServiceProperties rpcServiceProperties = RpcServiceProperties.builder()
                    .group(group).version(version).build();
            RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceProperties);
            Object value = rpcClientProxy.getProxy(field.getType());
            if (value != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(target, value);
            }
        }
    }
}
