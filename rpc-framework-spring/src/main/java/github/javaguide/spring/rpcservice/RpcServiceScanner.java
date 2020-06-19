package github.javaguide.spring.rpcservice;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Arrays;
import java.util.Set;

/**
 * @description:
 * @author:lvxuhong
 * @date:2020/6/18
 */
public class RpcServiceScanner extends ClassPathBeanDefinitionScanner {

    private RpcServiceFactoryBean<Object> rpcServiceFactoryBean = new RpcServiceFactoryBean<>();

    public RpcServiceScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        //父类在扫描的时候 beanDefinition 会通过 registry 注册，我们需要修改 beanDefinition 的 beanClass
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        processBeanDefinitions(beanDefinitions);
        return beanDefinitions;
    }

    /**
     * 主要是将beanDefinition 的beanClass 设置成我们自定义的FactoryBean
     *
     * @param beanDefinitions
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        GenericBeanDefinition definition;
        for (BeanDefinitionHolder beanDefinitionHolder : beanDefinitions) {
            definition = (GenericBeanDefinition) beanDefinitionHolder.getBeanDefinition();

            definition.getConstructorArgumentValues().addGenericArgumentValue(definition.getBeanClassName());

            definition.setBeanClass(rpcServiceFactoryBean.getClass());

            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
    }
}
