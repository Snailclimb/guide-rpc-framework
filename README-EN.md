# guide-rpc-framework

[中文](./README.md)|English

Sorry, I did not fully translate the Chinese readme. I have translated the important parts. You can translate the rest by yourself through Google.

## Preface

Although the principle of RPC is not difficult, I encountered many problems in the process of implementation. [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework)  implements only the most basic features of the RPC framework, and some of the optimizations are mentioned below for those interested.

With this simple wheel, you can learn the underlying principles and principles of RPC  framework as well as various Java coding practices.

You can even use the  [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework)  as a choice for your graduation/project experience, which is very great! Compared to other job seekers whose project experience is based on a variety of systems, building wheels is a sure way to win an interviewer's favor.

If you're going to use the  [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework)  as your graduation/project experience, I want you to understand it rather than just copy and paste my ideas. You can fork my project and then optimize it. If you think the optimization is valuable, you can submit PR to me, and I will deal with it as soon as possible.

##  Introduction

 [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework) is an RPC framework based on Netty+Kyro+Zookeeper. Detailed code comments, clear structure, and integrated Check Style specification code structure make it ideal for reading and learning.

Due to the limited energy and ability of me, if you think there is something to be improved and perfected, welcome to fork this project, then clone it to local, and submit PR to me after local modification, I will Review your code as soon as possible.

Let's start with a basic RPC framework design idea!

> **note** ：The RPC framework we mentioned here refers to a framework that allows clients to directly call server-side methods as simple as calling local methods, similar to the Dubbo, Motan, and gRPC I introduced earlier. If you need to deal with the HTTP protocol, parse and encapsulate HTTP requests and responses. Type frameworks are not considered "RPC frameworks", such as Feign.

A schematic diagram of the simplest RPC framework usage is shown in the figure below, which is also the current architecture of [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework):

![](./images/rpc-architure.png)

The service provider Server registers the service with the registry, and the service consumer Client gets the service-related information through the registry, and then requests the service provider Server through the network.

As a leader in the field of RPC framework [Dubbo](https://github.com/apache/dubbo), the architecture is shown in the figure below, which is roughly the same as what we drew above.

<img src="./images/dubbo-architure.jpg" style="zoom:80%;" />

**Under normal circumstances, the RPC framework must not only provide service discovery functions, but also provide load balancing, fault tolerance and other functions. Such an RPC framework is truly qualified. ** 

**Please let me simply talk about the idea of designing a most basic RPC framework:**

![](./images/rpc-architure-detail.png)

1. **Registration Center**: The registration center is required first, and Zookeeper is recommended. The registration center is responsible for the registration and search of service addresses, which is equivalent to a directory service. When the server starts, the service name and its corresponding address (ip+port) are registered in the registry, and the service consumer finds the corresponding service address according to the service name. With the service address, the service consumer can request the server through the network.
2. **Network Transmission**: Since you want to call a remote method, you must send a request. The request must at least include the class name, method name, and related parameters you call! Recommend the Netty framework based on NIO.
3. **Serialization**: Since network transmission is involved, serialization must be involved. You can't directly use the serialization that comes with JDK! The serialization that comes with the JDK is inefficient and has security vulnerabilities. Therefore, you have to consider which serialization protocol to use. The more commonly used ones are hession2, kyro, and protostuff.
4. **Dynamic Proxy**: In addition, a dynamic proxy is also required. Because the main purpose of RPC is to allow us to call remote methods as easy as calling local methods, the use of dynamic proxy can shield the details of remote method calls such as network transmission. That is to say, when you call a remote method, the network request will actually be transmitted through the proxy object. Otherwise, how could it be possible to call the remote method directly?
2. **Load Balancing**: Load balancing is also required. Why? For example, a certain service in our system has very high traffic. We deploy this service on multiple servers. When a client initiates a request, multiple servers can handle the request. Then, how to correctly select the server that processes the request is critical. If you need one server to handle requests for the service, the meaning of deploying the service on multiple servers no longer exists. Load balancing is to avoid a single server responding to the same request, which is likely to cause server downtime, crashes and other problems. We can clearly feel its meaning from the four words of load balancing.

## Run the project

### Import the project

Fork the project to your own repository, then clone the project to its own locale: `git clone git@github.com:username/guide-rpc-framework.git`  use java IDE such as IDEA to open and wait for the project initialization to complete.

### Initialize git hooks

**This step is mainly to run Check Style before submitting the code to ensure that the code format is correct. If there is a problem, it cannot be submitted. **

>The following demonstrates the operation corresponding to Mac/Linux. Window users need to manually copy the `pre-commit` file under the `config/git-hooks` directory to the `.git/hooks/` directory under the project.

Execute these commands:

```bash
➜guide-rpc-framework git: (master)✗chmod + x ./init.sh
➜guide-rpc-framework git: (master)✗./init.sh
```

The main function of the `init.sh` script is to copy the git commit hook to the `.git/hooks/` directory under the project so that it will be executed every time you commit.

### CheckStyle plug-in download and configuration

IntelliJ IDEA-> Preferences->Plugins-> search to download CheckStyle plug-in, and then configure it as follows.

![CheckStyle plug-in download and configuration](./images/setting-check-style.png)

After the configuration is complete, use this plugin as follows!

![How to use the plug-in](./images/run-check-style.png)

### Download and run zookeeper

Docker is used here to download and install.

download:

```shell
docker pull zookeeper:3.5.8
```

运行：

```shell
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.5.8
```

## Use

### Server(service provider)

Implementing the interface：

```java
@Slf4j
@RpcService(group = "test1", version = "version1")
public class HelloServiceImpl implements HelloService {
    static {
        System.out.println("HelloServiceImpl被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl返回: {}.", result);
        return result;
    }
}
	
@Slf4j
public class HelloServiceImpl2 implements HelloService {

    static {
        System.out.println("HelloServiceImpl2被创建");
    }

    @Override
    public String hello(Hello hello) {
        log.info("HelloServiceImpl2收到: {}.", hello.getMessage());
        String result = "Hello description is " + hello.getDescription();
        log.info("HelloServiceImpl2返回: {}.", result);
        return result;
    }
}
```

Publish services (transport using Netty) :

```java
/**
 * Server: Automatic registration service via @RpcService annotation
 *
 * @author shuang.kou
 * @createTime 2020年05月10日 07:25:00
 */
@RpcScan(basePackage = {"github.javaguide.serviceimpl"})
public class NettyServerMain {
    public static void main(String[] args) {
        // Register service via annotation
        new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyServer nettyServer = new NettyServer();
        // Register service manually
        HelloService helloService2 = new HelloServiceImpl2();
        RpcServiceProperties rpcServiceConfig = RpcServiceProperties.builder()
                .group("test2").version("version2").build();
        nettyServer.registerService(helloService2, rpcServiceConfig);
        nettyServer.start();
    }
}
```

### Client(srvice consumer)


```java
@Component
public class HelloController {

    @RpcReference(version = "version1", group = "test1")
    private HelloService helloService;

    public void test() throws InterruptedException {
        String hello = this.helloService.hello(new Hello("111", "222"));
        //如需使用 assert 断言，需要在 VM options 添加参数：-ea
        assert "Hello description is 222".equals(hello);
        Thread.sleep(12000);
        for (int i = 0; i < 10; i++) {
            System.out.println(helloService.hello(new Hello("111", "222")));
        }
    }
}
```

```java
ClientTransport rpcRequestTransport = new SocketRpcClient();
RpcServiceProperties rpcServiceConfig = RpcServiceProperties.builder()
        .group("test2").version("version2").build();
RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcRequestTransport, rpcServiceConfig);
HelloService helloService = rpcClientProxy.getProxy(HelloService.class);
String hello = helloService.hello(new Hello("111", "222"));
System.out.println(hello);
```