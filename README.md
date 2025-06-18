# guide-rpc-framework

> [该 RPC 框架配套教程已经更新在我的星球，点击此链接了解详情。](https://javaguide.cn/zhuanlan/handwritten-rpc-framework.html)

<div align="center">
  <p> 中文| <a href="./README-EN.md">English</a>
  </p>
  <p>
    <a href="https://github.com/Snailclimb/guide-rpc-framework">Github</a> | <a href="https://gitee.com/SnailClimb/guide-rpc-framework ">Gitee</a>
  </p>
</div>

## 前言

虽说 RPC 的原理实际不难，但是，自己在实现的过程中自己也遇到了很多问题。[guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework) 目前只实现了 RPC 框架最基本的功能，一些可优化点都在下面提到了，有兴趣的小伙伴可以自行完善。

通过这个简易的轮子，你可以学到 RPC 的底层原理和原理以及各种 Java 编码实践的运用。

你甚至可以把 [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework) 当做你的毕设/项目经验的选择，这是非常不错！对比其他求职者的项目经验都是各种系统，造轮子肯定是更加能赢得面试官的青睐。

如果你要将 [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework) 当做你的毕设/项目经验的话，我希望你一定要搞懂，而不是直接复制粘贴我的思想。你可以 fork 我的项目，然后进行优化。如果你觉得的优化是有价值的话，你可以提交 PR 给我，我会尽快处理。

## 介绍

[guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework) 是一款基于 Netty+Kyro+Zookeeper 实现的 RPC 框架。代码注释详细，结构清晰，非常适合阅读和学习。

### 🚀 项目特性

- **高性能网络通信**：基于 Netty 实现高性能的网络传输
- **多种序列化方式**：支持 Kryo、Protostuff、Hessian 等序列化框架
- **服务注册与发现**：集成 Zookeeper 作为注册中心
- **负载均衡**：支持多种负载均衡策略（随机、轮询等）
- **Spring 集成**：通过注解方式简化服务注册和消费
- **心跳检测**：支持客户端和服务端的心跳检测机制
- **异步调用**：基于 CompletableFuture 实现异步调用
- **服务分组和版本控制**：支持服务的分组和版本管理

### 📁 项目结构

```
guide-rpc-framework/
├── rpc-framework-simple/     # RPC 框架核心实现
├── rpc-framework-common/     # 公共工具类和常量
├── hello-service-api/        # 示例服务接口定义
├── example-server/           # 服务提供者示例
├── example-client/           # 服务消费者示例
├── docs/                     # 相关文档
└── images/                   # 项目图片资源
```

由于 Guide 哥自身精力和能力有限，如果大家觉得有需要改进和完善的地方的话，欢迎 fork 本项目，然后 clone 到本地，在本地修改后提交 PR 给我，我会在第一时间 Review 你的代码。

**我们先从一个基本的 RPC 框架设计思路说起！**

### 一个基本的 RPC 框架设计思路

> **注意** ：我们这里说的 RPC 框架指的是：可以让客户端直接调用服务端方法就像调用本地方法一样简单的框架，比如我前面介绍的 Dubbo、Motan、gRPC 这些。 如果需要和 HTTP 协议打交道，解析和封装 HTTP 请求和响应。这类框架并不能算是“RPC 框架”，比如 Feign。

一个最简单的 RPC 框架使用示意图如下图所示,这也是 [guide-rpc-framework](https://github.com/Snailclimb/guide-rpc-framework) 目前的架构 ：

![](./images/rpc-architure.png)

服务提供端 Server 向注册中心注册服务，服务消费者 Client 通过注册中心拿到服务相关信息，然后再通过网络请求服务提供端 Server。

作为 RPC 框架领域的佼佼者[Dubbo](https://github.com/apache/dubbo)的架构如下图所示,和我们上面画的大体也是差不多的。

<img src="./images/dubbo-architure.jpg" style="zoom:80%;" />

**一般情况下， RPC 框架不仅要提供服务发现功能，还要提供负载均衡、容错等功能，这样的 RPC 框架才算真正合格的。**

**简单说一下设计一个最基本的 RPC 框架的思路：**

![](./images/rpc-architure-detail.png)

1. **注册中心** ：注册中心首先是要有的，推荐使用 Zookeeper。注册中心负责服务地址的注册与查找，相当于目录服务。服务端启动的时候将服务名称及其对应的地址(ip+port)注册到注册中心，服务消费端根据服务名称找到对应的服务地址。有了服务地址之后，服务消费端就可以通过网络请求服务端了。
2. **网络传输** ：既然要调用远程的方法就要发请求，请求中至少要包含你调用的类名、方法名以及相关参数吧！推荐基于 NIO 的 Netty 框架。
3. **序列化** ：既然涉及到网络传输就一定涉及到序列化，你不可能直接使用 JDK 自带的序列化吧！JDK 自带的序列化效率低并且有安全漏洞。 所以，你还要考虑使用哪种序列化协议，比较常用的有 hession2、kyro、protostuff。
4. **动态代理** ： 另外，动态代理也是需要的。因为 RPC 的主要目的就是让我们调用远程方法像调用本地方法一样简单，使用动态代理可以屏蔽远程方法调用的细节比如网络传输。也就是说当你调用远程方法的时候，实际会通过代理对象来传输网络请求，不然的话，怎么可能直接就调用到远程方法呢？
5. **负载均衡** ：负载均衡也是需要的。为啥？举个例子我们的系统中的某个服务的访问量特别大，我们将这个服务部署在了多台服务器上，当客户端发起请求的时候，多台服务器都可以处理这个请求。那么，如何正确选择处理该请求的服务器就很关键。假如，你就要一台服务器来处理该服务的请求，那该服务部署在多台服务器的意义就不复存在了。负载均衡就是为了避免单个服务器响应同一请求，容易造成服务器宕机、崩溃等问题，我们从负载均衡的这四个字就能明显感受到它的意义。
6. ......

### 项目基本情况和可优化点

为了循序渐进，最初的是时候，我是基于传统的 **BIO** 的方式 **Socket** 进行网络传输，然后利用 **JDK 自带的序列化机制** 来实现这个 RPC 框架的。后面，我对原始版本进行了优化，已完成的优化点和可以完成的优化点我都列在了下面 👇。

**为什么要把可优化点列出来？** 主要是想给哪些希望优化这个 RPC 框架的小伙伴一点思路。欢迎大家 fork 本仓库，然后自己进行优化。

- [x] **使用 Netty（基于 NIO）替代 BIO 实现网络传输；**
- [x] **使用开源的序列化机制 Kyro（也可以用其它的）替代 JDK 自带的序列化机制；**
- [x] **使用 Zookeeper 管理相关服务地址信息**
- [x] Netty 重用 Channel 避免重复连接服务端
- [x] 使用 `CompletableFuture` 包装接受客户端返回结果（之前的实现是通过 `AttributeMap` 绑定到 Channel 上实现的） 详见：[使用 CompletableFuture 优化接受服务提供端返回结果](./docs/使用CompletableFuture优化接受服务提供端返回结果.md)
- [x] **增加 Netty 心跳机制** : 保证客户端和服务端的连接不被断掉，避免重连。
- [x] **客户端调用远程服务的时候进行负载均衡** ：调用服务的时候，从很多服务地址中根据相应的负载均衡算法选取一个服务地址。ps：目前实现了随机负载均衡算法与一致性哈希算法。
- [x] **处理一个接口有多个类实现的情况** ：对服务分组，发布服务的时候增加一个 group 参数即可。
- [x] **集成 Spring 通过注解注册服务**
- [x] **集成 Spring 通过注解进行服务消费** 。参考： [PR#10](https://github.com/Snailclimb/guide-rpc-framework/pull/10)
- [x] **增加服务版本号** ：建议使用两位数字版本，如：1.0，通常在接口不兼容时版本号才需要升级。为什么要增加服务版本号？为后续不兼容升级提供可能，比如服务接口增加方法，或服务模型增加字段，可向后兼容，删除方法或删除字段，将不兼容，枚举类型新增字段也不兼容，需通过变更版本号升级。
- [x] **对 SPI 机制的运用**
- [ ] **增加可配置比如序列化方式、注册中心的实现方式,避免硬编码** ：通过 API 配置，后续集成 Spring 的话建议使用配置文件的方式进行配置
- [x] **客户端与服务端通信协议（数据包结构）重新设计** ，可以将原有的 `RpcRequest`和 `RpcRequest` 对象作为消息体，然后增加如下字段（可以参考：《Netty 入门实战小册》和 Dubbo 框架对这块的设计）：
  - **魔数** ： 通常是 4 个字节。这个魔数主要是为了筛选来到服务端的数据包，有了这个魔数之后，服务端首先取出前面四个字节进行比对，能够在第一时间识别出这个数据包并非是遵循自定义协议的，也就是无效数据包，为了安全考虑可以直接关闭连接以节省资源。
  - **序列化器编号** ：标识序列化的方式，比如是使用 Java 自带的序列化，还是 json，kyro 等序列化方式。
  - **消息体长度** ： 运行时计算出来。
  - ......
- [ ] **编写测试为重构代码提供信心**
- [ ] **服务监控中心（类似 dubbo admin）**
- [x] **设置 gzip 压缩**

## 运行项目

### 环境要求

- **JDK**: 1.8+
- **Maven**: 3.6+
- **Zookeeper**: 3.5.8+

### 快速开始

#### 1. 启动 Zookeeper

首先需要启动 Zookeeper 作为注册中心：

```bash
# 拉取 zookeeper 镜像
docker pull zookeeper:3.5.8
# 使用 Docker 启动 Zookeeper
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.5.8
```

#### 2. 克隆项目并构建

```bash
git clone https://github.com/Snailclimb/guide-rpc-framework.git
cd guide-rpc-framework
mvn clean install
```

#### 3. 定义服务接口

在 `hello-service-api` 模块中定义服务接口和数据传输对象：

```java
// HelloService.java - 服务接口
public interface HelloService {
    String hello(Hello hello);
}

// Hello.java - 数据传输对象
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class Hello implements Serializable {
    private String message;
    private String description;
}
```

### 服务提供端

#### 1. 实现服务接口

在 `example-server` 模块中实现服务接口，使用 `@RpcService` 注解标记服务：

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
```

#### 2. 启动服务提供者

使用 `@RpcScan` 注解扫描服务，启动 Netty 服务器：

```java
@RpcScan(basePackage = {"github.javaguide"})
public class NettyServerMain {
    public static void main(String[] args) {
        autoRegistry();
    }

    public static void autoRegistry() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyServerMain.class);
        NettyRpcServer nettyRpcServer = (NettyRpcServer) applicationContext.getBean("nettyRpcServer");
        HelloService helloService = applicationContext.getBean(HelloServiceImpl.class);
        helloService.hello(new Hello("你好fzk", "你好服务端"));
        nettyRpcServer.start();
    }
}
```

### 服务消费端

#### 1. 创建服务消费者

使用 `@RpcReference` 注解注入远程服务：

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

#### 2. 启动服务消费者

```java
@RpcScan(basePackage = {"github.javaguide"})
public class NettyClientMain {
    public static void main(String[] args) throws InterruptedException {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(NettyClientMain.class);
        HelloController helloController = (HelloController) applicationContext.getBean("helloController");
        helloController.test();
    }
}
```

### 运行步骤

1. **启动 Zookeeper**：确保 Zookeeper 在 `127.0.0.1:2181` 运行
2. **启动服务提供者**：运行 `NettyServerMain.main()` 方法
3. **启动服务消费者**：运行 `NettyClientMain.main()` 方法

### 核心注解说明

- **`@RpcService`**：标记服务提供者，支持 `group` 和 `version` 属性用于服务分组和版本控制
- **`@RpcReference`**：标记服务消费者，通过 `group` 和 `version` 属性指定要调用的服务
- **`@RpcScan`**：指定要扫描的包路径，自动注册和发现 RPC 服务

### 配置说明

框架支持多种配置方式：

- **注册中心**：默认使用 Zookeeper，地址为 `127.0.0.1:2181`
- **序列化方式**：支持 Kryo、Protostuff、Hessian 等
- **传输协议**：支持 Netty（推荐）和 Socket
- **负载均衡**：支持随机、轮询等策略

## 相关问题

### 为什么要造这个轮子？Dubbo 不香么？

写这个 RPC 框架主要是为了通过造轮子的方式来学习，检验自己对于自己所掌握的知识的运用。

实现一个简单的 RPC 框架实际是比较容易的，不过，相比于手写 AOP 和 IoC 还是要难一点点，前提是你搞懂了 RPC 的基本原理。

我之前从理论层面在我的知识星球分享过如何实现一个 RPC。不过理论层面的东西只是支撑，你看懂了理论可能只能糊弄住面试官。咱程序员这一行还是最需要动手能力，即使你是架构师级别的人物。当你动手去实践某个东西，将理论付诸实践的时候，你就会发现有很多坑等着你。

大家在实际项目上还是要尽量少造轮子，有优秀的框架之后尽量就去用，Dubbo 在各个方面做的都比较好和完善。

### 如果我要自己写的话，需要提前了解哪些知识

**Java** ：

1. 动态代理机制；
2. 序列化机制以及各种序列化框架的对比，比如 hession2、kyro、protostuff。
3. 线程池的使用；
4. `CompletableFuture` 的使用
5. ......

**Netty** ：

1. 使用 Netty 进行网络传输；
2. `ByteBuf` 介绍
3. Netty 粘包拆包
4. Netty 长连接和心跳机制

**Zookeeper** :

1. 基本概念；
2. 数据结构；
3. 如何使用 Netflix 公司开源的 zookeeper 客户端框架 Curator 进行增删改查；

## 教程

Guide 的星球正在更新《从零开始手把手教你实现一个简单的 RPC 框架》。扫描下方二维码关注“**JavaGuide**”后回复 “**星球**”即可。

![JavaGuide 官方公众号](https://oss.javaguide.cn/github/javaguide/gongzhonghaoxuanchuan.png)

