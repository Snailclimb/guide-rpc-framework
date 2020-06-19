package github.javaguide.spring.annotation;

import github.javaguide.spring.rpcservice.RpcServiceScanner;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * @description:
 * @author:lvxuhong
 * @date:2020/6/18
 */

public class RpcServiceScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;


    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annoAttrs = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(RpcServiceScan.class.getName()));
        RpcServiceScanner scanner = new RpcServiceScanner(registry);
        String value = annoAttrs.getString("value");
        if (resourceLoader != null) {
            scanner.setResourceLoader(resourceLoader);
        }

        //所有的接口全部注入
        scanner.addIncludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {
                return true;
            }
        });
        scanner.doScan(value);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
