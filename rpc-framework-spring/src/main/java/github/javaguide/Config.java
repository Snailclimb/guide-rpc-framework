package github.javaguide;

import github.javaguide.spring.annotation.RpcServiceScan;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author:lvxuhong
 * @date:2020/6/18
 */

@Configuration
@RpcServiceScan("github.javaguide.spring.service")
//@Import(RpcServiceScannerRegistrar.class)
public class Config {

}
