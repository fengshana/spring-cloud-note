Ribbon 在 Eureka 客户端 服务发现的基础上，实现了 对服务实例的选择策略，从而实现对服务的负载均衡。（Ribbon在后续会进一步讲解，当前暂时只是用一下这个组件；因为Eureka客户端client本身底层有依赖这个组件，然后用它Ribbon来进行消费）

![image-20210520003241478](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520003241478.png)

接下来我们来让 消费者去消费服务（通过注册中心的方式发现服务并通过Ribbon消费服务）：



我们前面搭建了 服务消费者项目，接下来我们就可以使用该 服务消费者 通过 注册中心 去调用 服务提供者，步骤如下：

1. 在该 消费者项目 中添加 eureka 的依赖，因为 服务消费者 从注册中心 获取服务，需要连接 eureka，所以需要 eureka 客户端的支持；

   ```xml
   <!--spring-cloud-starter-netflix-eureka-client-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

2. 激活 Eureka 中的 EnableEurekaClient 功能，在Spring Boot 的入口函数处，通过添加 @EnbaleEurekaClient 注解来表明自己是一个 eureka 客户端，让我的消费者 可以使用 eureka 注册中心（进行发现服务）。

3. 配置服务的名称和注册的地址

   ```properties
   spring.application.name=03-springcloud-web-consumer
   eureka.client.service-url.defaultZone=http://localhost:8761/eureka
   ```

4. 前面我介绍了 服务的发现由eureka 客户端实现，而服务的真正调用由Ribbon 实现，所以我们需要在 调用服务提供者时 使用ribbon来调用：

   ```java
   @LoadBalanced //使用Ribbon实现负载均衡的调用，Ribbon的一个注解叫做负载均衡 LoadBalanced
   @Bean
   public RestTemplate restTemplate(){
       return new RestTemplate();
   }
   ```

   加入了 ribbon的支持，那么在调用时，即可改为使用 服务名称来访问：(后续通过服务的名称去进行调用即可，该名称即相当于服务的标识，通过其标识去调用我们消费者需要的服务，相当于该标识=ip+端口，这个标识等价于ip+端口，这样去进行调用)

   ```java
   restTemplate.getForEntity("http://34-SPRINGCOUD-SERVICE-GOODS/service/goods", String.class).getBody();
   ```

5. 完成上面的步骤后，我们就可以启动 消费者的 SpringBoot程序，main方法

6. 启动成功之后，通过在浏览器地址栏访问我们的消费者，看是否可以正常调用远程服务提供者提供的服务；

   

先将前端项目进行关闭portal项目关闭。

##### Eureka 与 Zookeeper的比较

著名的 CAP 理论指出，一个 分布式系统 不可能同时满足 C（一致性）、A（可用性）和P（分区容错性）。

由于 P（分区容错性）是在 分布式系统 中必须要保证的，因此我们只能在 A（可用性）和C（一致性）之间进行权衡，在此 Zookeeper 保证的是CP（即一致性和分区容错性），而Eureka则是AP（即可用性和分区容错性）。

两种方案：

##### Zookeeper 保证 CP（一致性、分区容错性）

在 Zookeeper中，当 master 结点因为 网络故障 与其他结点失去联系，剩余结点会重新进行 leader选举，但是问题在于，选举leader需要一定时间，且选举期间整个Zookeeper集群都是不可用的，这就导致在选举期间注册服务瘫痪。

在云部署的环境下，因网络问题使得Zookeeper集群失去master结点是大概率事件，虽然服务最终能够恢复，但是在选举时间内导致服务注册长期不可用是难以容忍的。

（在选举期间整个服务是不能够进行注册的，即注册不了，由于选举确实是需要一点时间的，多少毫秒、或者是多少秒的时间。在这个毫秒或者秒的时间之内没有办法进行服务的注册，就相当于是服务的不可用）

即Zookeeper的这种注册方式，在其选举期间是不能够进行注册服务的，那么这个时候就相当于服务进行注册但是报错了，日志当中抛出了异常，服务不可用。

所以Zookeeper保证的是数据的一致性C和分区容错性P。所以Zookeeper是牺牲掉了可用性A（选举的时候没有办法注册，没有可用性，可用性不强，但是其保证了数据的一致性）。

##### Eureka 保证 AP（可用性、分区容错性）

Eureka 优先保证 可用性，Eureka 各个结点是平等的，某几个结点挂掉不会影响 正常结点的工作，剩余的节点 依然可以提供 注册和查询 服务。

而Eureka 的 客户端在向 某个 Eureka 注册或是 如果发现 连接失败（Eureka集群有多个，其中一个Eureka连接不上时会切换到另外一个节点Eureka上），则会自动切换至其他节点，只要有一台Eureka还在，就能保证注册服务可用（保证可用性），只不过查到的信息可能不是最新的（不保证强一致性）。（比如说Eureka部署了三台，其中一台Eureka宕机了，但是依然可以注册服务，通过其余剩下的没有出现宕机的两台Eureka，这一台Eureka服务宕机不行了会自动切换到另外一台Eureka上去，还可以进行服务注册，优先保证了服务的可用性，除非三台Eureka都宕机了，那就没有办法注册服务了。不保证强一致性的原因： 比如说Eureka有三台进行部署了服务，a订单服务注册到了第一台Eureka注册中心当中，[后续会说到Eureka的集群]，有三台Eureka，以及存在一些微服务到这三台Eureka当中进行注册，刚刚说到的a订单服务到第一台Eureka进行注册服务，但是这个时候出现了状况，即a订单服务向第一台Eureka注册了服务之后，第一台Eureka马上就宕机了，而此时另外一个服务正好在第一台Eureka宕机的时候要进行访问a订单服务，而在第一台Eureka服务宕机之后，就会切换到第二台剩余的没有宕机的Eureka服务，另外一个服务需要访问Eureka发现a订单服务肯定不会去访问第一台的Eureka，因为第一台宕机会自动切换到第二台，而第二台Eureka服务注册中心中并没有a 订单服务的服务注册，所以此时另外一个服务在第二台Eureka上是拿不到a 订单服务的，因为Eureka不保证强一致性，即注册到第一台Eureka上去之后，第一台Eureka有可能出现马上宕机的情况，而第二台第三台Eureka是没有服务的注册信息的；而但是如果不是马上宕机的话，第一台Eureka上的服务注册信息是会往第二台第三台Eureka上进行复制服务注册信息的，即稍微等待片刻之后，第二台Eureka和第三台Eureka也会有服务的相关注册信息，第一台Eureka当中的注册信息会复制到第二台和第三台Eureka中来，那么此时第一台Eureka没有马上宕机的话，稍等片刻后，等待第一台的Eureka中的服务注册信息复制到第二台第三条Eureka上之后，第一台Eureka再宕机，这个时候，另外一个服务在第二台或者第三台Eureka上是可以查询得到注册的a 订单服务的相关信息的）

![image-20210520020115665](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520020115665.png)

所以 Eureka在 网络故障导致部分节点失去联系的情况下，只要有一个节点可用，那么注册和查询服务就可以正常使用，而不会像 zookeeper那样使整个注册服务瘫痪，Eureka优先保证了可用性。 

Eureka不保证一致性。如果是Zookeeper的master节点宕机的情况下，之后会有一个选举的过程，master节点宕机之后，即后面两个节点之间都在选举过程中，都是连接不上的，没有办法去连接，因此就没有办法保证可用性了，而是保证的一致性。

不能连接，即不能用。虽然不能使用但是它保证了数据的一致性。即数据的复制是已经复制过来到从结点上，但是由于leader的选举问题从而导致服务不可注册不可用。而且zookeeper就是说在master节点写了数据之后，master将会发一个确认给两个从结点机器，收到超过半数机器的确认以后，master节点机器才会进行持久化操作，不然的话master节点机器写该数据是没有写成功的。比如说master节点在写了数据之后马上宕机了，而该数据并没有持久化，因为写数据的这一个操作并没有得到半数从结点机器的确认，这个时候就相当于master主节点机器写操作失败了。master节点机器需要等两个从结点机器确认之后，才进行写操作，才成功。所以zookeeper从而保证了数据的一致性。

![image-20210520020318256](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520020318256.png)

C（一致性）即数据的一致性，数据不会出现不一致的情况。比如说这个服务的数据是1，那个服务的数据是2，而应该两个服务的数据都是1，这样才表示数据是一致的。

A（可用性）比较好理解，即服务的可用，该服务能不能用，能不能访问，有没有宕机，这个是可用性。

P（分区容错性）不太好理解，分区容错性的意思就是我的服务并没有问题，但是由于网络的原因，网络这个时候中断了，然后另外一个服务来进行访问该服务的时候发现访问不了，但是其实该服务并没有任何问题，只是网络的原因，比如说网络突然的一个抖动、中断等问题，瞬间中断一小会儿，毫秒级别的一个闪断，那么这个情况下就可以叫做分区容错性。一般情况下是由于外部网络等原因造成的问题，不可避免，叫做分区容错性。



以上就是 Eureka和Zookeeper之间的比较。Zookeeper保证数据的一致性，Eureka保证服务的可用性。

而分区容错性P为什么需要保证呢?

即因为需要保证网络没有问题，不能说因为网络的断开，人为的断开。网络原因导致的话是偶尔的情况。人为的断开肯定不行。分区容错性是Eureka和Zookeeper都要进行保证的，网络不能断了，网路断了就没有办法工作了。所以网络不能断。P分区容错性主要是指网络的中断、闪断的这种情况。

协调服务-一致性-zookeeper

可用性-eureka（对生产环境的影响最小化，只要有一个eureka在，服务注册中心即可用，但是数据并不保证强一致性，对于保证数据一致性会有点影响，即服务注册成功之后，第一台Eureka马上宕机后，到第另外一个eureka上面去进行查询的时候发现该服务没有注册，没有该服务的相关信息，即第一台eureka没有同步到第二台第三台eureka上，没有同步过来，也是eureka的一个问题）

##### Eureka 注册中心 高可用集群

这节课先搭建一下eureka集群，下节课再在这个基础之上再进行操作。也就是环境搭建好之后，就直接在项目工程当中进行使用了，后续就直接在项目工程当中写代码了。当前需要做eureka集群，目前的话是只有一台eureka注册中心的。注册中心一般会有集群。后续的注册中心就直接使用集群的模式。

在微服务架构的这种 分布式系统中，我们要充分考虑各个 微服务组件的高可用性问题，不能有单点故障，由于注册中心 eureka 本身也是一个服务，如果它只有一个节点，那么它有可能发生故障，这样我们就不能注册与查询服务了，所以我们需要一个高可用的服务注册中心，这就需要通过注册中心集群来解决。（搭建三台或者几台）

eureka服务注册中心 它本身也是一个服务，它也可以看做是一个提供者，又可以看做是一个消费者，我们之前通过配置：

`eureka.client.register-with-eureka=false` 让注册中心不注册自己，但是我们可以向其他注册中心注册自己。

Eureka Server的 高可用 实际上就是将自己 作为服务 向其他服务注册中心注册自己（自身也是一个服务，eureka有三台的话，那么就是相互进行注册，将每一个都看做是一个服务，eureka除去自身将其他的服务/eureka注册中心都当成服务，向自己注册，其余也是一样；三台eureka服务两两注册），这样就会形成一组互相注册的服务注册中心，进而实现 服务清单的互相同步（数据也会进行复制），往注册中心 A 上注册的服务，可以被复制同步到注册中心B上，所以从任何一台注册中心上都能查询到已经被注册的服务，从而达到高可用的效果。（注册完成之后，数据会同步会复制）

集群思路如下：

![image-20210520143836041](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520143836041.png)

![image-20210520144101550](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520144101550.png)



![image-20210520144118727](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520144118727.png)



eureka server之间进行相互数据复制。

现在搭建一下这个eureka集群环境。

##### Eureka 注册中心 高可用集群搭建

搭建eureka server 注册中心步骤：

1. 添加依赖 spring-cloud-starter-netflix-eureka-server

2. 配文件，配置application.properties有关eureka server的相关内容配置

   ```properties
   #设置该服务注册中心的hostname
   eureka.instance.hostname=localhost
   #由于我们目前创建的应用是一个服务注册中心，而不是普通的应用，默认情况下，这个应用会向自己注册服务
   eureka.client.register-with-eureka=false
   #表示不去从服务端检索其他服务信息，因为自己就是服务端，服务注册中心本身的职责就是维护
   eureka.client.fetch-registry=false
   #指定服务注册中心的位置
   eureka.client.service-url.defualtZone=http://localhost:8761/eureka
   ```

   

3. 写内容，在main方法中添加@EnableEurekaServer注解

当前 34-springcloud-service-eureka已经是一个完整正常的服务注册中心，那么当下需要三份eureka注册中心，即将34-springcloud-service-eureka项目进行复制三份即可。而复制三份这个操作，可以使用多文件来进行实现，之前有在springboot当中讲过多文件的方式，即多环境配置。还有一个办法就是将该项目程序进行拷贝三份（太麻烦了，程序、配置都一样，没有必要，所以使用多配置文件的形式去做eureka集群就可以了）。这是两种做法。

第一种办法多文件环境配置，即复制三份application.properties文件出来，分别命名为application-eureka8761.properties、application-eureka8762.properties、application-eureka8763.properties这三个文件（三个注册中心）



我们知道，Eureka注册中心高可用集群 就是各个 注册中心相互注册，所以：

1. 在8761的配置文件中，让它的 service-url 指向8762，在8762的配置文件中让它的service-url指向 8761。

2. 由于 8761 和 8762 互相指向双方，实际上我们构建了一个 双节点的服务注册中心集群。（在当前的项目当中已经搭建了 34-springcloud-service-eureka）

   ```properties
   eureka.client.service-url.defaultZone=http://eureka8762:8762/eureka,http://eureka8762:8763/eureka/
   
   eureka.client.service-url.defaultZone=http://eureka8761:8761/eureka/
   ```

   （需要进行修改一下 host ip的名字，为什么要改这个host ip名字呢？在host 上面去配置像 eureka8761、eureka8762、eureka8763这种形式的名字，搭建集群的时候才能够进行识别，否则无法进行识别；

   即在eureka-server项目当中的application-eureka8763.properties中的配置 `eureka.client.service-url.defaultZone=http://localhost:8761/eureka,http://localhost:8762/eureka` 中的`localhost`即本地的`127.0.0.1`，但是需要将其替换成 eureka8761、eureka8762这种形式的名字，进行修改hosts文件。搭建eureka集群的时候需要进行修改这个hosts文件，通过eureka8761、eureka8762、eureka8763这种名称去替换掉127.0.0.1以及localhost

   ）

   然后在本地 hosts 文件配置：`C:\Windows\System32\drivers\etc\hosts`

   ```bash
   127.0.0.1 eureka8761
   127.0.0.1 eureka8762
   127.0.0.1 eureka8763
   ```

   通过上述方式进行指定名称，指定名称完成之后如何进行区别，搭建eureka集群需要使用hosts修改名称的这种方式，如果不使用这种方式就将出现问题。（之前做rabbitmq镜像队列的时候，也是进行修改hosts文件，通过修改名称...）

   运行时，在运行配置项目 `Program Arguments` 中配置：

   ```properties
   --spring.profiles.active=eureka8761
   --spring.profiles.active=eureka8762
   --spring.profiles.active=eureka8763  
   ```

   分别启动两个注册中心，访问两个注册中心页面，观察注册中心页面是否正常。

   `--spring.profiles.active` 这是表示激活哪个文件，当前激活的是`applicaition-eureka8761.properties`

   在激活文件的时候讲过，如果要激活`application-eureka8761.properties`文件，则在`--spring.profiles.active=`后面的取值即为`application-`后面的那个单词/内容（除去`.properties`）。即该eureka server运行的时候是通过该文件进行运行服务的。

   通过idea的edit configuration的Spring Boot中复制程序，将EurekaApplication修改为EurekaApplication8761之后，点击它并进行复制两个出来，名为EurekaApplication8762和EurekaApplication8763。

   此处就有三个EurekaApplication了，到时候运行这三个EurekaApplicatin程序即可。

   将之前的EurekaApplication关闭，在重新启动EurekaApplication8761。



##### Eureka 注册中心高可用集群测试

在要进行注册的服务配置：

```properties
#eureka 注册中心的连接地址
eureka.client.service-url.defaultZone=http://eureka8761:8761/eureka,http://eureka8762:8762/eureka,http://eureka8763:8763/eureka
```

启动服务提供者服务/服务消费者服务，然后观察注册中心页面，可以看到服务会在两个注册中心上都注册成功；



##### 集群的注册中心打包发布

在 真实项目 中，需要将 Eureka 发布到具体服务器上进行执行，打包部署其实和 springboot 里面讲的大同小异，对于 properties文件，不同的环境会有不同的配置文件，比如application-dev.properties，application-test.properties，application-pro.properties等；

运行在8761端口上：`java -jar springcloud-eureka-server.java`

运行其他两个 profile 配置：

```bash
java -jar spring-eureka-server.jar --spring.profiles.active=eureka8762

java -jar spring-eureka-server.jar --spring.profiles.active=eureka8763
```

后续在环境当中就不再搭建eureka集群了。就将其他的一些组件之类的编码使用了。



SpringCloud注册中心Eureka注册中心打包（有点小问题后续解决）

将集群的注册中心进行打包一下

打包发布的意思即，注册中心集群已经在本地电脑上已经启动起来了，已经弄好了，但是下次每次要使用eureka服务的时候，都需要启动这三个服务eureka8761、eureka8762、eureka8763，这样很麻烦，而且占用机器的内存，干脆就将这个注册中心集群部署到linux环境当中，下次直接在项目当中访问linux当中的eureka注册中心即可。在linux环境上进行发布部署，该eureka 注册中心 集群都不在需要在本地保留了，这就叫做发布。

注册中心的发布和springboot项目一样，且eureka本身就是一个springboot项目程序。

springboot当中可以进行war包部署，jar包部署。

springboot程序默认打jar包。后面跟着参数激活一下对应的配置文件即可。（`--spring.profiles.active=eureka876X`）

使用Maven工具`Lifecycle`中的`package`即可。

打包时报错：

```bash
Non-resolvable parent POM for com.bjpowernode.springcloud:34-springcloud-service-eureka:1.0.0: Failure to find com.bjpowernode.springcloud:34-springcloud-service-parent:pom:1.0.0 in https://repo.maven.apache.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced and 'parent.relativePath' points at wrong local POM @ line 12, column 13 -> [Help 2]
```

报错原因在于：当前使用了父项目即34-springcloud-service-parent，父项目并没有打包。

eureka-server项目上有统一的父依赖即`34-springcloud-service-parent`

```xml
<!--
    该当前项目依然是SpringBoot项目，和之前一样
    1. 首先继承父依赖 parent,有一个统一的父依赖 继承统一的父依赖，从goods服务中的pom.xml中粘贴过来或者 portal中的pom.xml粘贴过来即可
    -->
    <!-- 统一继承的父项目 -->
    <parent>
        <groupId>com.bjpowernode.springcloud</groupId>
        <artifactId>34-springcloud-service-parent</artifactId>
        <version>1.0.0</version>
    </parent>
```

在父项目当中进行**聚合操作**，聚合各个子项目。通过module进行聚合一下。

```xml
<modules>
    <module>../34-springcloud-service-commons</module>
    <module>../34-springcloud-service-eureka</module>
    <module>../34-springcloud-service-goods</module>
    <module>../34-springcloud-service-portal</module>
</modules>
```

经此之后，该父项目就变成聚合项目了，将四个子服务聚合起来。

在Idea当中的左侧maven中可以看到 34-springcloud-service-parent后面是跟着(root)字符的，表示这是一个父项目。

以及在父项目当中添加pom打包方式

```xml
<!--打包方式：在父项目当中需要指定pom，接着import Changes-->
    <packaging>pom</packaging>
```

接着再次进行eureka的package打包。

此时打包仍然报错；

报错内容如下：

```bash
Non-resolvable parent POM for com.bjpowernode.springcloud:34-springcloud-service-eureka:1.0.0: Failure to find com.bjpowernode.springcloud:34-springcloud-service-parent:pom:1.0.0 in https://repo.maven.apache.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced and 'parent.relativePath' points at wrong local POM @ line 12, column 13
```



#### Spring Cloud Eureka 注册中心打包发布与集群部署

微服务专题-一站式微服务架构 SpringCloud（开发框架开发架构）

##### 1、快速回顾

1. 分布式与微服务架构的理论梳理；

2. 什么是Spring Cloud？

   Spring Cloud，一站式进行微服务开发的一套架构、组件，工具。

3. Spring Cloud的整体架构

   存在有服务提供者、服务消费者，但是角色并不固定，即服务提供者也可以是服务消费者，服务消费者也可以是服务提供者。然后还有一个注册中心。整体上来说还是三个部分。（提供者、消费者、注册中心）与dubbo相类似，因为dubbo也是类似远程调用的一种方式。

4. 服务消费者Controller直连调用服务提供者Controller（controller调用controller，可以使用restTemplate，spring提供的这样一个工具类。也可以使用HTTPClient，或者是java.util，java.net下的一些包进行使用 HTTP URL等一些类进行调用，存在有很多方式，这是直接调用）

5. Spring Cloud注册中心Eureka（可以将服务放到Eureka上面去，后续在服务注册中心上获取得到服务列表，然后再去进行调用，那么这样就不在需要写服务提供者提供过来的连接地址、接口地址了）

6. Spring Cloud Eureka与 Zookeeper比较（eureka保证可用性和分区容错性即AP，zookeeper保证一致性和分区容错性即CP，分区容错性在分布式当中都会要求进行保证即网络的通道，即网络不要出现网络不同，这种说的分区容错性；eureka保证的可用性，zookeeper保证数据的一致性。它们的取舍不一样；eureka实现AP，zookeeper实现CP）

7. Spring Cloud Eureka高可用集群（使用eureka搭建了集群，高可用的注册中心，即三个节点（搭建了三个注册中心），每个节点两两相互的复制数据，两两相互的进行注册，相互复制，那么这样的话就实现了eureka高可用注册中心，通过多配置文件的方式进行搭建。）

##### 2、Spring Cloud Eureka集群注册中心打包发布

在实际项目中，需要将Eureka发布到具体服务器上进行部署，打包部署其实和SpringBoot里面的一样，我们可以将其打成jar包，启动时对于properties文件，不同的环境激活不同的配置文件。

运行：

```bash
java -jar springcloud-service-eureka.jar --spring.profiles.active=eureka8761

java -jar springcloud-service-eureka.jar --spring.profiles.active=eureka8762

java -jar springcloud-service-eureka.jar --spring.profiles.active=eureka8763
```

通过父项目 34-springcloud-service-parent 进行打包，其他的子服务项目commons、eureka、goods、portal都是继承了父项目，在commons项目当中也要继承父项目，所以在父项目当中复制 gav坐标到commons项目当中的pom.xml中

即在commons项目当中粘贴如下内容即可：

```xml
<!--
    该当前项目依然是SpringBoot项目，和之前一样
    1. 首先继承父依赖 parent,有一个统一的父依赖 继承统一的父依赖，从goods服务中的pom.xml中粘贴过来或者 portal中的pom.xml粘贴过来即可
    统一继承的父项目 
    -->
    <parent>
        <groupId>com.bjpowernode.springcloud</groupId>
        <artifactId>34-springcloud-service-parent</artifactId>
        <version>1.0.0</version>
    </parent>
```

所有的子服务项目都继承了父项目，

之前的报错内容当中告诉我们

```bash
[ERROR]   The project com.bjpowernode.springcloud:34-springcloud-service-eureka:1.0.0 (F:\Project\UserProject\fsn\34-springcloud-service-eureka\pom.xml) has 1 error
[ERROR]     Non-resolvable parent POM for com.bjpowernode.springcloud:34-springcloud-service-eureka:1.0.0: Failure to find com.bjpowernode.springcloud:34-springcloud-service-parent:pom:1.0.0 in https://repo.maven.apache.org/maven2 was cached in the local repository, resolution will not be reattempted until the update interval of central has elapsed or updates are forced and 'parent.relativePath' points at wrong local POM @ line 12, column 13 -> [Help 2]
```

在继承父项目的时候需要指明parent父项目的相对位置，即

```bash
resolution will not be reattempted until the update interval of central has elapsed or updates are forced and 'parent.relativePath' points at wrong local POM @ line 12, column 13
```

中的：`parent.relativePath`

在每个子服务项目当中的<parent\>结点当中再添加一个<relativePath\>结点，结点内容为 `../34-springcloud-service-parent/pom.xml`即可

即如下内容：

```xml
<!--
    该当前项目依然是SpringBoot项目，和之前一样
    1. 首先继承父依赖 parent,有一个统一的父依赖 继承统一的父依赖，从goods服务中的pom.xml中粘贴过来或者 portal中的pom.xml粘贴过来即可
    统一继承的父项目
    -->
    <parent>
        <groupId>com.bjpowernode.springcloud</groupId>
        <artifactId>34-springcloud-service-parent</artifactId>
        <version>1.0.0</version>
        <relativePath>../34-springcloud-service-parent/pom.xml</relativePath>
    </parent>
```

中间的

```xml
        <relativePath>../34-springcloud-service-parent/pom.xml</relativePath>

```

此处的relativePath即为相对路径

![image-20210520210041944](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520210041944.png)

`../`回到上一层目录。

这四个子项目微服务都需要在parent结点当中添加relativePath相对路径，然后再进行eureka的打包`maven-lifecycle-package`部署。

下面就使BUILD SUCCESS的相关信息

```bash
F:\jdk\jdk1.8202\jdk\bin\java.exe -Dmaven.multiModuleProjectDirectory=F:\Project\UserProject\fsn\34-springcloud-service-eureka "-Dmaven.home=F:\ideaUI\IntelliJ IDEA 2018.3.1\plugins\maven\lib\maven3" "-Dclassworlds.conf=F:\ideaUI\IntelliJ IDEA 2018.3.1\plugins\maven\lib\maven3\bin\m2.conf" "-javaagent:F:\ideaUI\IntelliJ IDEA 2018.3.1\lib\idea_rt.jar=61246:F:\ideaUI\IntelliJ IDEA 2018.3.1\bin" -Dfile.encoding=UTF-8 -classpath "F:\ideaUI\IntelliJ IDEA 2018.3.1\plugins\maven\lib\maven3\boot\plexus-classworlds-2.5.2.jar" org.codehaus.classworlds.Launcher -Didea.version=2018.3.1 package
[INFO] Scanning for projects...
[INFO]                                                                         
[INFO] ------------------------------------------------------------------------
[INFO] Building 34-springcloud-service-eureka 1.0.0
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- maven-resources-plugin:3.1.0:resources (default-resources) @ 34-springcloud-service-eureka ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] Copying 4 resources
[INFO] Copying 0 resource
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:compile (default-compile) @ 34-springcloud-service-eureka ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 1 source file to F:\Project\UserProject\fsn\34-springcloud-service-eureka\target\classes
[INFO] 
[INFO] --- maven-resources-plugin:3.1.0:testResources (default-testResources) @ 34-springcloud-service-eureka ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory F:\Project\UserProject\fsn\34-springcloud-service-eureka\src\test\resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.8.1:testCompile (default-testCompile) @ 34-springcloud-service-eureka ---
[INFO] Changes detected - recompiling the module!
[INFO] 
[INFO] --- maven-surefire-plugin:2.22.2:test (default-test) @ 34-springcloud-service-eureka ---
[INFO] 
[INFO] --- maven-jar-plugin:3.1.2:jar (default-jar) @ 34-springcloud-service-eureka ---
[INFO] Building jar: F:\Project\UserProject\fsn\34-springcloud-service-eureka\target\34-springcloud-service-eureka-1.0.0.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 5.148 s
[INFO] Finished at: 2021-05-20T21:01:55+08:00
[INFO] Final Memory: 47M/294M
[INFO] ------------------------------------------------------------------------

Process finished with exit code 0

```



总结之前打包eureka出错的原因在于，

1. parent父项目当中缺少modules标签，parent没有将其下的子服务进行聚合起来（parent需要聚合另外的四个模块）。

   ```xml
   <modules>
       <module>../34-springcloud-service-commons</module>
       <!-- ... -->
   </modules>
   ```

   

2. parent父项目下的**每一个子服务**（commons、eureka、goods、portal）的`pom.xml`，在依赖父项目的时候，需要进行添加`relativePath`，写出父项目pom.xml的相对路径（`../34-springcloud-service-parent`）。

在上述完成之后是可以进行eureka的打包操作的。 （先进行clean再进行package操作）

在打包成功之后，就将其进行部署了。

部署的包的路径在：34-springcloud-service-eureka工程下的target目录：`34-springcloud-service-eureka-1.0.0.jar`

linux输入命令如下：

```bash
cd /usr/local

mkdir java

tar -zxvf jdk-8u221-linux-x64.tar.gz

yum -y install vim*

vim /etc/profile

#-------------------
  # Java Environment Path
export JAVA_HOME=/usr/jdk1.8.0_221
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
#-------------

source /etc/profile

java -version
java
javac

# 创建springcloud-eureka文件夹，相当于是注册中心服务端
mkdir spring-cloud-eureka

cd spring-cloud-eureka

#安装rz命令
yum install lrzsz -y 

#传jar包到当前目录
rz

#编辑shell脚本进行启动该34-springcloud-service-eureka.jar
vim eureka_server.sh

#34-springcloud-service-eureka-1.0.0.jar
#--------------------------
#/!bin.sh
#后面指定配置参数，使用哪一个配置文件，--spring.profiles.active参数指定
#当前是第一台eureka server服务
#将其日志放到一个文件当中去,nohup来产生一个文件./logs/eureka8761.log
# 后台启动加&，&表示后台启动
nohup java -jar 34-springcloud-service-eureka.jar --spring.profiles.active=eureka8761 > ./logs/eureka8761.log &

#第二台eureka server服务
nohup java -jar 34-springcloud-service-eureka.jar --spring.profiles.active=eureka87
62 > ./logs/eureka8762.log &

#第三条eureka server服务
nohup java -jar 34-springcloud-service-eureka.jar --spring.profiles.active=eureka87
63 > ./logs/eureka8763/log &

#上述都是通过jar包进行启动
# 英文的冒号 :wq 退出vim编辑模式
#--------------------------

#此时还需要进行修改hosts文件，因为代码当中是使用 eureka server不同端口的服务名称去代替了本机host name
#代码中是eureka-server项目中的application.properties配置文件中eureka.client.service-url.defaultZone=http://eureka8762:8762/eureka,http://eureka8763:8763/eureka这种方式，将IP替换成了不同端口的eureka server服务名称
#所以在linux环境当中也需要在hosts文件当中进行配置 内网ip为不同端口的eureka server服务名称，类似在windows系统上的操作，即将ip和不同端口的eureka server服务名称做一个映射处理
vim /etc/hosts
#-----------------
# 配置eureka server不同端口的服务名称 映射本机ip
# 此处的127.0.0.1也可以写成内网ip，linux通过命令 ifconfig 查看内网ip
# 本机的内网ip为 192.168.182.130
127.0.0.1 eureka8761
127.0.0.1 eureka8762
127.0.0.1 eureka8763
#-----------------
#最后 :wq 退出保存

#创建日志脚本文件夹
cd spring-cloud-eureka
mkdir logs

#eureka_server.sh目前没有执行权限，需要对其赋予权限,仅修改root用户执行权限，其他用户不变动
chmod 744 eureka_server.sh

# 查看机器有没有其他的java进程
ps -ef|grep java
#启动脚本
./eureka_server.sh

#返回如下：nohup: redirecting stderr to stdout
#nohup: redirecting stderr to stdout
#nohup: redirecting stderr to stdout

cd logs

#查看日志
cat eureka8761.log
#返回如下：no main manifest attribute, in 34-springcloud-service-eureka.jar，表示没有main属性，打的jar有问题，在打包的时候需要指定编译的方式，因为我们是通过java -jar的方式启动的jar包，现在的日志提示jar包不是那种可执行的jar包，即可能是一个普通的jar包，不是可执行的。在windows环境中可用压缩软件打开这个jar包，发现该jar包不是springboot打出来的那种可执行的jar包的结构，结构不是这样子，这只是一个普通的jar包；普通jar包结构：com、META-INF、application.properties、application-eureka8761.properties、application-eureka8762.properties、application-eureka8763.properties
#所以需要重新打包，需要添加springboot打包的插件，这样子打出来的包才是正常的


```



在eureka-server的pom.xml中添加如下内容后，再次进行打包（通过父项目打包即可，因为父项目当中聚合了所有的子项目clean-package），打包成功之后，将新打的包替换到linux上去

```xml
<!--
    在创建springboot项目的时候，会自动添加上这个插件，通过这个插件打出来的包是可执行的
    在parent中写完modules结点之后，将4个子服务项目进行聚合
    然后在4个子服务项目的pom.xml中在parent结点中添加relativePath parent父依赖的pom.xml的相对路径之后
    通过maven的lifecycle的package打包不正常
    此时需要添加springboot的打包插件进行打包
    使用springboot专门用于打包的插件，打包编译插件
    -->
    <build>
        <plugins>
            <!--SpringBoot提供的编译、打包的Maven插件-->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <!-- TODO  如果是打一个jar运行的话，jar包里面有JSP页面，那么要采用1.4.2.RELEASE 版本来打包，否则JSP无法访问-->
                <!--此时该项目当中没有JSP页面自然也就不需要填写这个版本号了，import Changes
                然后再通过idea左侧的maven-lifecycle-package再次对eureka-server进行打包操作
                -->
                <!--<version>1.4.2.RELEASE</version>-->
            </plugin>
        </plugins>
    </build>
```

通过该插件打出来的可执行jar包，其目录结构为：`BOOT-INF、META-INF、org`，这个jar包是可以通过 java -jar进行运行的。

linux命令

```bash
cd spring-cloud-eureka
#删除原来的不能执行的jar包
rm -rf 34-springcloud-service-eureka.jar

#进行重命名
mv 34-springcloud-service-eureka-1.0.0.jar 34-springcloud-service-eureka.jar 

#通过shell脚本进行启动三台eureka server注册中心
./eureka_server.sh

#查看日志，无返回内容
cat logs/eureka8761.log

#查看java进程
ps -ef|grep java
#返回如下
```

```bash
[root@localhost logs]# ps -ef|grep java
root      18000      1 29 22:48 pts/0    00:00:05 java -jar 34-springcloud-service-eureka.jar --spring.profiles.active=eureka8761
root      18001      1 29 22:48 pts/0    00:00:05 java -jar 34-springcloud-service-eureka.jar --spring.profiles.active=eureka8762
root      18002      1 31 22:48 pts/0    00:00:06 java -jar 34-springcloud-service-eureka.jar --spring.profiles.active=eureka8763
root      18035  17886  2 22:49 pts/1    00:00:00 grep --color=auto java
```

此时就可以看到存在有三个java进程，此时就可以在浏览器当中进行访问三个注册中心eureka server了。

访问地址如下（访问不了的检查防火墙是否开启了这三个端口）

```bash
#查看防火墙状态
systemctl status firewalld 

#如果为开启状态则进行查看防火墙已经开放的端口有哪些；查看所有打开的端口： firewall-cmd --zone=public --list-ports
firewall-cmd --zone=public --list-ports

#添加eureka 8761、8762、8763这三个端口   （--permanent永久生效，没有此参数重启后失效）
firewall-cmd --zone=public --add-port=8761/tcp --permanent

firewall-cmd --zone=public --add-port=8762/tcp --permanent 

firewall-cmd --zone=public --add-port=8763/tcp --permanent   

#重新载入
firewall-cmd --reload

#再次查看防火墙开启的所有端口
firewall-cmd --zone=public --list-ports

#然后进行页面访问eureka server
```

访问地址为：

```bash
eureka server 8761:
http://192.168.182.130:8761/

eureka server 8762:
http://192.168.182.130:8762/

eureka server 8763：
http://192.168.182.130:8763/

#此时可以看到 8761当中的DS Replicas有8762和8763、8762当中的DS Replicas有8761和8763、8763当中的DS Replicas有8762和8761 是正常的
```

此时相当于本地的eureka项目可以进行删除不再使用了。本地项目如果需要使用eureka server注册中心，可以使用linux环境上已经部署好的eureka服务。即直接将本地的服务注册到服务器上。本机上就不再需要启动eureka服务了。

防火墙相关命令：https://www.cnblogs.com/moxiaoan/p/5683743.html

一般公司当中在测试环境当中有一套eureka服务，然后在开发过程当中不需要管测试环境服务器上的eureka，只需要知道它的连接地址即可，配置到项目的application.properties中的eureka.client.service-url.defaultZone即可，主要是开发服务提供者和服务消费者，开发一个个的服务，而注册中心搭建一个即可，在linux环境上部署好就行了。

以上为自己在本机的虚拟机上进行运行部署的。

![image-20210520230602339](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520230602339.png)

![image-20210520230617567](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520230617567.png)

![image-20210520230634381](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520230634381.png)

#### Spring Cloud Eureka注册中心的自我保护机制

自我保护机制 是 Eureka注册中心的重要特性，当Eureka注册中心进入自我保护模式时，在Eureka Server首页会输出如下警告信息：

```bash
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.

```

小写是(字母大小写转换工具地址：https://www.iamwawa.cn/daxiaoxie.html)：

```bash
emergency! eureka may be incorrectly claiming instances are up when they're not. renewals are lesser than threshold and hence the instances are not being expired just to be safe.


翻译：紧急情况！eureka可能错误地声称实例在没有启动的情况下启动了。续订小于阈值，因此实例不会为了安全而过期。
```

在没有 Eureka 自我保护的情况下，如果 Eureka Server在一定时间内没有接收到某个微服务实例的心跳，Eureka Server将会注销该实例，但是当发生网络分区故障时，那么微服务与 Eureka Server之间将无法正常通信，以上行为可能变得非常危险了，因为微服务本身其实是正常的，此时不应该注销这个微服务，如果没有自我保护机制，那么Eureka Server就会将此服务注销掉。

![image-20210520231940881](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520231940881.png)

每个微服务都会与eureka server进行心跳的连接，表示这个微服务还存活着，通过这个机制来保证服务是否存活着。如果有一个服务很久没有给注册中心eureka server发送心跳了，那么eureka server将会注销该服务的实例。网络分区故障的意思是：网络的原因造成访问不同的情况。因为网络原因导致微服务和注册中心eureka连接不上，但是服务本身是没有任何问题是正常的。所以没有这个自我保护机制的话，那么eureka server就将会把这个因为网络原因而导致无法访问的微服务给注销掉。

Eureka通过自我保护模式可以不进行注销该因为网络分区故障原因导致不可用的微服务。

Eureka 通过 “自我保护模式” 来解决这个问题（网络分区故障）----Eureka Server节点在短时间内丢失过多客户端时（在短时间内发现有很多客户端没有向它发送心跳了）（它就判断认为可能发生了网络分区故障），那么就会把这个微服务节点进行保护（也就是该微服务依然在Eureka Server这里被认为是存活着，不将它进行踢出）。

一旦进入自我保护模式，Eureka Server就会保护 服务注册表中的信息，不删除 服务注册表中的数据（也就是不会注销任何微服务）。当网络故障恢复后，该 Eureka Server节点会再自动退出 自我保护模式。（当Eureka Server退出自我保护模式，即红色的字符就会消失掉）

即如果一段时间内没有向Eureka Server发送心跳，它会认为是发生了网络分区故障，然后Eureka Server就进入自我保护模式，将微服务保护起来，该微服务没有被踢出，还在注册中心上。

所以，自我保护模式 是一种应对 网络异常 的安全保护措施，它的 架构哲学 是 宁可同时保留所有的微服务（健康的微服务和不健康的微服务都会被保留），也不盲目注销任何健康的微服务，使用自我保护模式，可以让 Eureka 集群更加的健壮、



```bash
EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.

```

即为Eureka server进入自我保护模式所提示的信息，即当前没有服务给该Eureka Server注册中心发送心跳，此时Eureka Server认为这是由于网络故障原因从而导致的微服务不可用，即一段时间没有服务实例给Eureka Server发送心跳，它就会进入自我保护模式，目前该Eureka Server当中有哪些服务，就将一直保存起来不将它们进行注销掉，不进行踢出，这个就叫做自我保护。

当然Eureka也有可能出现误判断的情况，即微服务确确实实是因为宕机了所以才无法发送心跳，如果这个时候进入自我保护模式，而这个微服务也确实是调不通了，但是Eureka由于进入自我保护模式，从而也还是会有这个不健康的微服务，即宕机的微服务。

存在误判的可能性。



当然也可以使用配置项：`eureka.server.enable-self-preservation=false` #禁止自我保护模式（即该微服务没有发送心跳给Eureka了，那么就让Eureka Server将该微服务进行踢出，禁止自我保护，如需要配置则配置到eureka server 服务端的application.properties 当中）

关闭自我保护模式后会出现红色：

```bash
THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS.
```

该警告表示将自我保护模式关闭了之后也可能会带来一些危险，如果发生网络分区故障了，可能该微服务是健康的是正常的，但是Eureka仍然会将该服务进行踢出，在Eureka当中没有该微服务，由于网络分区故障缘故导致微服务没有发送心跳给Eureka Server注册中心。 

即关闭了Eureka的自我保护模式，也会存在有不足。也有其弊端。如果关闭了也会有提示信息

```bash
翻译：
自我保护模式已关闭。如果出现网络/其他问题，这可能无法保护实例过期。
```

即网络出现问题了，Eureka无法保护该微服务。

EurekaApplication没有配置Program arguments的话则读取默认的配置文件即application.properties

启动该EurekaApplication服务，也会出现如上警告；

访问地址：http://localhost:8761

```bash
THE SELF PRESERVATION MODE IS TURNED OFF. THIS MAY NOT PROTECT INSTANCE EXPIRY IN CASE OF NETWORK/OTHER PROBLEMS.

```

![image-20210520235459168](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210520235459168.png)

即平时开发的时候出现该上述红色警告不要过于担心。



但是 Eureka Server 自我保护模式也会给我们带来一些困扰，如果在保护期内某个服务提供者刚好 非正常 下线了（刚好故障宕机了），此时 服务消费者 就会拿到一个 无效的服务实例，此时会调用失败，对于这个问题需要 服务消费者端 具有一些容错机制，如 重试、断路器等。（即针对的是弊端，微服务确确实实宕机了但是Eureka认为它是因为网络分区故障原因导致，所以进入了自我保护模式，没有进行注销这个不健康的服务实例）

Eureka 的自我保护模式 是有意义的，该模式被激活后，健康的、不健康的微服务不会从 注册列表中 被踢出因长时间没收到心跳导致 注册过期的服务，而是等待修复，直到心跳回复正常之后，它自动退出自我保护模式。

这种模式旨在 避免因 网络分区故障 导致服务不可用的 问题。

例如，两个微服务客户端实例 A 和B 之间有调用关系，A是消费者，B是服务提供者，但是由于网络故障，B未能及时向Eureka发送心跳续约，这时候Eureka不能简单地将B从注册表中踢出，因为如果剔除了，A就无法从Eureka服务器中获取B注册的服务，但是这时候B服务是可用的；

所以，Eureka的自我保护模式最好还是开启它。

假设微服务部署在一个机房，而注册中心部署在另外一个机房，那么这两者就有可能会发生网络分区问题，即使在同一个机房当中，两个服务之间也有可能会出现闪断、故障，也有可能。基于考虑到了这种问题（网络分区故障原因），所以出现有了自我保护机制。

 关于 自我保护 常用的几个配置如下：

服务器端配置(关闭自我保护模式)，在注册中心服务端进行配置，表示关闭自我保护：

```properties
#测试时关闭自我保护机制，保证不可用服务及时踢出
eureka.server.enable-self-preservation=false
```

客户端配置（微服务给Eureka注册中心发送心跳时间间隔以及多久没有发送心跳就让Eureka将其踢出），在客户端微服务当中进行配置，表示多久发送心跳以及多久没有发送心跳将其进行踢出：

```properties
#每间隔2s，向服务端发送一次心跳，证明自己依然“存活”
eureka.instance.lease-renewal-interval-in-seconds=2

#告诉服务端，如果我10s之内没有给你发心跳，就代表我故障了，将我踢出掉
eureka.instance.lease-expiration-duration-in-seconds=10
```



#### 4、Spring Cloud Ribbon

在RestTemplate构造Bean上添加了@LoadBalanced（负载均衡，eureka client底层有依赖这个组件ribbon，通过ribbon知道向Eureka注册过服务的服务名称等信息）

Spring Cloud Ribbon 客户端负载均衡

##### Ribbon是什么？

Spring Cloud Ribbon是基于 Netflix Ribbon实现的一套 客户端负载均衡器；

我们通常说的 负载均衡是指将 一个请求均匀地分摊到不同的节点单元上执行，负载均衡分为 硬件负载均衡和软件负载均衡：

**硬件负载均衡**：比如F5、深信服、Array等；（最著名的就是F5，它是一个设备，买一台就像一个服务器一样，一般大公司会使用到硬件负载均衡）

**软件负载均衡**：比如Nginx、LVS、HAProxy等；（HAProxy，在做rabbitmq镜像队列的时候就是用到过HAProxy这个产品）（这些是由服务器来进行实现的软件负载均衡）

Ribbon也是一个软件负载均衡器。

Ribbon也是Spring Cloud中服务调用的一个组件。

Spring Cloud当中封装了大量Netflix公司的开源项目。

所以当前会分为两块，一块是Spring Cloud Netflix，另外一块是Spring Cloud Alibaba。

因为现在Spring Cloud Alibaba现在也有一套。

在Spring Cloud Netflix当中大量使用了Netflix公司的开源项目、组件。

在Spring Cloud Alibaba当中则大量使用了Alibaba公司的开源项目、组件。

所以目前分有两套。

当前Ribbon 客户端负载均衡器是Netflix公司提供的开源组件。

Ribbon 是 Netflix公司发布的 开源项目（组件、框架、简单理解就是一个jar包，ribbon可以理解为是jar来实现负载均衡），主要功能是 提供客户端的软件负载均衡算法，它会从 eureka 中获取一个可用的 服务端列表，通过心跳检测 来剔除 故障的服务端节点 以保证 清单中都是可以正常访问的服务端节点。

第一步首先从注册中心eureka，将可用的服务列表的服务接口都拿取过来，拿到之后再进行负载均衡，比如说服务端有三个接口，部署了了三份，那就将这三个接口拿到之后，一个个轮询一个个校验服务端接口服务是否可用。这种模式，其实ribbon就是一个jar包。

当客户端发送请求，则 ribbon 负载均衡器按照某种算法（比如轮询、权重、最小连接数等），从维护的可用服务端清单中取出一台服务端的地址，然后进行请求；（因为服务可能有多个请求接口，部署了多份，服务是通过集群的方式进行部署的，协助）

Ribbon非常简单，可以说就是一个jar包，这个 jar包实现了 负载均衡算法，Spring Cloud对 Ribbon做了二次封装，可以让我们使用 RestTemplate的服务请求，自动转换成 客户端负载均衡的服务调用。（RestTemplate对象+添加@LoadBalanced注解即可使用，spring帮助开发人员进行封装了ribbon所以只需添加一个注解即可实现对ribbon负载均衡调用）

Ribbon 支持多种 负载均衡算法，还支持 自定义的负载均衡算法。（还可以自己去扩展它的接口，自己去实现）

##### 客户端负载均衡 vs 服务端负载均衡

![image-20210521003700265](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521003700265.png)

上图就是服务端负载均衡，客户端有一个APP应用，通过80端口访问Nginx，Nginx作为一个负载均衡设备，然后它将请求分发到8100、8200、8300的Web Server服务器，那么上述称之为服务端的负载均衡，应用请求到Nginx服务器，然后再通过Nginx转发请求到分发到不同的web服务器，然后实现负载均衡。

这个是服务器端的负载均衡。Nginx，即请求会经过一个中转站，部署有Nginx的服务器即为一个中转站，通过该中转站来进行转发，那么Nginx即成为了服务端的负载均衡。

 

![image-20210521004133679](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521004133679.png)

上图为客户端的负载均衡。

![image-20210521004202594](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521004202594.png)

客户端负载均衡。

客户端有一个APP应用，服务端有提供应用goods service、goods service、goods service，通过集群部署了三份相同的服务。

现在app端需要调用服务提供者，即goodsservice，那么就通过负载均衡算法去进行调用服务端应用。而ribbon该jar包其实就放在APP当中。

因为首先APP的后端服务首先会连接高可用集群注册中心Eureka8761、8762、8763，首先进行注册服务，再就是发现服务，发现服务就是从eureka注册中心将服务列表即goods service、goods service、goods service 通过集群部署了三份的应用服务的接口地址都拿取到，即发现服务，拿到这些服务列表接口地址之后，在通过ribbon这个jar包当中的负载均衡算法进行调用应用服务goods service。

通过集群进行部署的goods service，应用服务也会到eureka注册中心进行注册服务。三份goods service启动之后都会到注册中心eureka上进行注册服务。

APP端的后端服务启动之后首先也会进行往注册中心eureka进行注册服务，然后就会发现服务，发现服务之后将eureka中 三份goods service的服务列表拿到，拿到之后，当下次需要进行调用的时候，这个时候通过ribbon这个jar包来进行实现负载均衡调用。比如说第一次调用goods service1，第二次调用goods service 2，第三次调用goods service3这样子。

所以这个ribbon，负载均衡是在客户端APP当中实现的，那么就叫做客户端的负载均衡。在APP端进行实现的通过ribbon进行负载均衡算法调用应用服务接口，而不是通过服务器去进行实现的。这就是客户端负载均衡。



#### Spring Cloud Ribbon客户端负载均衡应用测试

##### 采用 Ribbon实现服务调用

1. 首先加入 ribbon的依赖，但是 eureka已经依赖了 ribbon，所以这里不需要再引用 ribbon 的依赖；

2. 要使用 ribbon，只需要一个注解：

   ```java
   @Bean
   @LoadBalanced
   public RestTemplate restTemplate(){
       RestTemplate restTemplate = new RestTemplate();
       return restTemplate;
   }
   ```

   在 RestTemplate 上面加入 @LoadBalanced 注解，这样就可以实现RestTemplate在调用时自动负载均衡。

   看到portal前端项目当中的pom.xml当中进行依赖了 `spring-cloud-stater-netflix-eureka-client` eureka-clietn依赖，而可以看到idea左侧的maven依赖中，该eureka-client依赖底层是有对ribbon进行依赖的。

   `spring-cloud-starter-netflix-ribbon`下包括ribbon-core、ribbon-httpclient、loadbalancer等等。loadbalancer即负载均衡。

   ribbon-loadbalancerjar包当中包括有ribbon-core、netflix-statistics、rxjava、slf4j-api、servo-core、guava、archaius-core、netflix-commons-util等；（netflix公司提供的相关的一些jar包）loadbalancer主要实现负载均衡。eureka client客户端已经自动依赖了loadbalancer这个jar包。

   在netflix-ribbon的起步依赖当中底层已经添加了loadbalancer依赖。spring-cloud-starter-netflix-ribbon中添加有起步器即starter，这个启动器自动配置到时候就会把所有相关的bean准备好，开发人员直接使用即可。

   所以第一步就不再需要进行添加该ribbon的相关依赖了。

   第二步即在restTemplate上添加@LoadBalanced的注解即可。这样就可以自动实现负载均衡了。

   我们这里现在启动了 eureka集群（3个eureka）和服务提供者集群（2个 service-goods）和一个服务调用这（service-portal）

   ```properties
   #告诉服务端，服务实例的唯一id
   eureka.instance.instance-id=34-springcloud-service-portal
   ```

   3个注册中心已经在linux环境上进行部署好了。然后服务提供者提供两份，然后服务调用者提供一份，看自动的负载均衡有没有实现。

   复制goods工程为9200时，需要在新的工程9200工程下删除文件`34-springcloud-service-goods.iml`以及文件夹`target`，然后编辑goods9200工程当中的pom.xml文件。

   ```xml
   <!--gav坐标中进行修改 a,artifactId的取值在后面加一个-9200-->
   <groupId>com.bjpowernode.springcloud</groupId>
       <artifactId>34-springcloud-service-goods-9200</artifactId>
       <version>1.0.0</version>	
   <!--name结点标签内容也需要添加-9200 以及description结点内容-->
   <name>34-springcloud-service-goods-9200</name>
       <description>34-springcloud-service-goods-9200 project for Spring Boot</description>
   ```

   修改完成之后再通过idea的New-Module from Existing Sources...

   ![image-20210521102100082](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521102100082.png)

   接着选中复制的goods9200工程当中的pom.xml文件即可

   ![image-20210521102145409](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521102145409.png)

   OK之后在弹出的页面当中点击Next按钮，然后再次点击Finish按钮即可。

   注册中心目前在linux当中。所以就不需要再关注eureka。

   当前已经提供了两份服务提供者。
   
   微服务启动比较麻烦，服务比较多。
   
   两个GoodsApplication9100和9200服务提供者启动之后，接着启动portal消费端。portal项目当中再RestConfig当中的RestTemplate上面已经添加了@LoadBalanced注解，负载均衡。（使用Ribbon实现负载均衡的调用）
   
   @LoadBalanced即spring cloud在底层对ribbon进行了封装，即spring cloud在底层进行封装了ribbon、eureka、restTemplate都进行了封装。
   
   

##### Ribbon负载均衡策略

Ribbon 是用来做负载均衡使用的。帮助开发者实现负载均衡调用。Ribbon是存在有很多的负载均衡策略的，主要是由IRule接口定义。rule 规则。

Ribbon 的负载均衡策略是由 IRule 接口定义，该接口由如下实现：

在jar 包：`com.netflix.ribbon#ribbon-load-balancer`中：ribbon-loadbalancer该jar包即位于`spring-cloud-starter-netflix-eureka-client`依赖下的`spring-cloud-starter-netflix-ribbon`依赖下的ribbon相关的jar包即`ribbon-loadbalancer`

ribbon的负载均衡就是在ribbon-loadbalancer该jar包当中实现的。

![image-20210521141504395](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521141504395.png)

该ribbon-loadbalancer jar包，它的一个继承体系结构如上图。最上层是IRule接口，然后就是实现接口的抽象类，再就是具体的负载均衡算法。中横线的类即表示已经过时的类。

在idea中的portal项目当中，找到其下的`External Libraries`，找到下面的有关于netflix.ribbon相关的内容；

![image-20210521142102417](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521142102417.png)

找到该jar包，在loadbalancer文件夹下有一个接口IRule

![image-20210521142200081](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521142200081.png)

这是负载均衡的统一的一个接口，负载均衡算法规则接口。该IRule下有多个实现类相当于是多个负载均衡规则算法。

RandomRule 随机算法；

Random随机的，随机的负载均衡。

RoundRobinRule 轮询的负载均衡；

另外还有一些其他的负载均衡算法。WeightedResponseTimeRule即权重负载均衡算法。

```bash
ClientConfigEnabledRoundRobinRule
    BestAvailableRule
    PredicateBaseRule
        ZoneAvoidanceRule
        AvailabilityFilteringRule
RoundRobinRule
WeightedResponseTimeRule
ResponseTimeWeightedRule（该类已过期）
RandomRule
RetryRule
```

![image-20210521143043918](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521143043918.png)

继承结构。在idea中右击IRule类当中的名称，选择Diagrams源码，然后找到`Show Diagram...`之后点击，就生成上面的该图了。

需要观察一下没有使用某一种负载均衡策略，那么它默认的负载均衡策略是什么呢？

如果没有加负载均衡策略，则有一天负载均衡的入口，ILoadBalancer即该负载均衡的入口。

```bash
ILoadBalancer
	AbstractLoadBalancer
		NoOpLoadBalancer
			DynamicServerListLoadBalancer
				ZoneAwareLoadBalancer
```

DynamicServerListLoadBalancer 动态选择，就是默认情况下，负载均衡算法从ILoadBalancer该入口进来之后，

首先从注册中心拿到服务即addServers(List<Server\>):void，将eureka添加服务列表添加进来；

chooseServer(Object):Server，选择服务器，使用使用哪一个服务器；

markServerDown(Server):void，标记哪一台服务器宕机了；

getReachableServer():List<Server\>，获取可以到达的服务器；可到达的服务器表明该服务是通的，可以进行连接上，没有宕机没有报错。

getAllServers():List<Server\>拿到所有服务器列表。





```java
public interface ILoadBalancer {
    void addServers(List<Server> var1);
    
Server chooseServer(Object var1);

void markServerDown(Server var1);

/** @deprecated */
@Deprecated
List<Server> getServerList(boolean var1);

List<Server> getReachableServers();

List<Server> getAllServers();
    }
```




在负载均衡的时候，首先第一步则进行选择负载均衡服务器，即chooseServer(Object):server，该方法的基本实现有

```bash
BaseLoadBalancer(com.netflix.loadbalancer) 基本负载均衡实现
NoOpLoadBalancer(com.netflix.loadbalancer)
ZoneAwareLoadBalancer(com.netflix.loadbalancer)
```



```java
public Server chooseServer(Object key) {
        if (this.counter == null) {
            this.counter = this.createCounter();
        }

        this.counter.increment();
        if (this.rule == null) {
            return null;
        } else {
            try {
                return this.rule.choose(key);
            } catch (Exception var3) {
                logger.warn("LoadBalancer [{}]:  Error choosing server for key {}", new Object[]{this.name, key, var3});
                return null;
            }
        }
    }
```

在`if(counter == null){` 该处放一个断点，看到时候进来到这个断点，就将会去实现这个负载均衡。将消费者服务进行断点调试。就会发现它会进来到chooseServer当中进行调用负载均衡。通过debug方式启动portalApplication服务，这样可以通过调试进入断点。

在这个三个chooseServer方法的实现当中都打一个断点，看看到时候会进入哪一个实现方法，经过运行发现断点进入到的是ZoneAwareLoadBalancer该类当中

```java
public Server chooseServer(Object key) {
        if (ENABLED.get() && this.getLoadBalancerStats().getAvailableZones().size() > 1) {
```

第一步得到状态，下一步Step Over（F6），第二步打印了日志

```java
logger.debug("Zone aware logic disabled or there is only one zone");
return super.chooseServer(key);
```

然后此处进行选择服务器 chooseServer(key)，以及该key，是`"default"`，即为一个 default key，接着进入选择即 chooseServer(Object key)；

就到了BaseLoadBalancer当中的chooseServer(Object key)中来了。

```java
public Server chooseServer(Object key) {
        if (this.counter == null) {
            this.counter = this.createCounter();
```

接着往下走执行到代码行

```java
        this.counter.increment();
//计数器增加
```

再接着往下走，则进入到规则的判断，该rule为规则鼠标上移出现的是`ZoneAvoidanceRule@8523`，这个就是规则使用的规则是ZoneAvoidanceRule。即默认的情况下使用的负载均衡是通过ZoneAvoidanceRule类来进行实现的算法。（或许其他版本的Spring使用的负载均衡算法是轮询也就是RoundRobinRule，但是当前G版本的spring使用的负载均衡是通过ZoneAvoidanceRule来进行实现的）

```java
        if (this.rule == null) {

```

接着继续向下执行，由于rule不为空，那么就会走到相应的规则当中去即进入到else判断

```java
if (this.rule == null) {
            return null;
        } else {
            try {
                return this.rule.choose(key);
            } catch (Exception var3) {
                logger.warn("LoadBalancer [{}]:  Error choosing server for key {}", new Object[]{this.name, key, var3});
                return null;
            }
        }
```

中的代码行

```java
                return this.rule.choose(key);

```

继续接着往下执行就走到了ZoneAwareLoadBalancer当中的代码行

```java
            return super.chooseServer(key);

```

再接着往下走就到了选择服务器了。即ZoneAwareLoadBalancer中的代码行 返回服务器

```java
protected Server getServer(ILoadBalancer loadBalancer, Object hint) {
        return loadBalancer == null ? null : loadBalancer.chooseServer(hint != null ? hint : "default");
    } 
```



由此可以知道ribbon的默认负载均衡实现是使用ZoneAvoidanceRule去进行实现的算法。

![image-20210521151339418](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521151339418.png)

去掉断点，即上图当中的View Breakpointers（Ctrl+Shift+F8）

![image-20210521151446604](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521151446604.png)

在弹出的界面当中点击 【-】图标，然后进行删除断点记录。

![image-20210521151536531](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521151536531.png)

以上就是清除完之后的样子。

接着点击Done按钮即可。

以上就是负载均衡接口，以及它的体系结构。如果在代码当中想要进行切换负载均衡的实现方式的话（如果要切换负载均衡策略），那么即在配置类当中添加一个@Bean，实现IRule。

在消费者项目当中的RestConfig配置类当中，@LoadBalanced底层默认实现负载均衡的算法是ZoneAvoidanceRule实现类。

而目前是要切换负载均衡策略，显然是要换一种负载均衡实现方式即IRule的不同实现类。就需要进行覆盖一下之前的ZoneAvoidanceRule该类的实现负载均衡方式



要使用 ribbon 实现负载均衡，在 Spring 的配置类里面把 对应的负载均衡接口实现类 作为一个 Bean配置一下 就行了。

![image-20210521160228212](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521160228212.png)

负载均衡的入口：ILoadBalancer该接口，该接口下有一些实现。

```java
@Bean
public IRule iRule(){
    return new RoundRobinRule();
}
```

| 负载均衡实现   | 策略 |
| -------------- | ---- |
| RandomRule     | 随机 |
| RoundRobinRule | 轮询 |
|                |      |



问题：消费者在调用的时候是指定application的名字还是status那一栏的值

消费者在调用提供者服务的时候是指定的application下的名字。

即34-SPRINGCLOUD-SERVICE-GOODS这样子的取值，即spring.application.name的取值。而不是服务实例id，即34-springcloud-service-goods-9100、34-springcloud-service-goods-9200这个，如果使用服务实例id则无法调用通。

只有通过spring.application.name的取值可以调通，该取值可以大写也可以小写。这个表示服务名称，通过服务名称来进行调用的。而Status栏下的为服务实例id，不是服务名称。

ribbon的@LoadBalanced负载均衡策略默认是ZoneAvoidanceRule来进行实现的负载均衡算法。

![image-20210521161410769](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210521161410769.png)

该操作即将断点全部置为不可用

Mute Breakpoints让所有断点静音（

这个地方好像不太对，mute breakpoints，我是进行的重启服务运行的）

##### Spring Cloud Ribbon客户端负载均衡策略分析02

负载均衡算法规则

| 负载均衡实现                                                 | 策略                                                         |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| RandomRule                                                   | 随机                                                         |
| RoundRobinRule                                               | 轮询（每个服务执行一遍）                                     |
| AvailabilityFilteringRule                                    | 先过滤掉由于多次访问故障的服务， 以及并发连接数超过阈值的服务，然后对剩下的服务按照轮询策略进行访问； |
| WeightedResponseTimeRule                                     | 根据平均响应时间计算所有服务的权重，响应时间越快服务权重就越大被选中的概率即越高，如果服务刚启动时统计信息不足，则使用RoundRobinRule策略，待统计信息足够会切换到该WeightedResponseTimeRule策略； |
| RetryRule                                                    | 先按照 RoundRobinRule 策略分发，如果分发的服务不能访问，则在指定时间内进行重试，然后分发其他可用的服务。 |
| BestAvailableRule                                            | 先过滤掉由于多次访问故障的服务，然后选择一个并发量最小的服务。 |
| ZoneAvoidanceRule（spring cloud G版本使用的默认负载均衡策略） | 综合判断服务节点所在区域的性能和服务节点的可用性，来决定选择哪个服务；（根据宕机的比例高不高，性能好不好来决定） |
|                                                              |                                                              |
|                                                              |                                                              |

如果开发人员没有指定负载均衡策略，ribbon默认的负载均衡是ZoneAvoidanceRule；

ResponseTimeWeightedRule过时了的策略以及AbstractLoadBalancerRule抽象的就不再关注。ClientConfigEnabledRoundRobinRule也不需要关注，ClientConfigEnabledRoundRobinRule就是PredicateBasedRule和BestAvailableRule。PredicateBasedRule也是抽象的。

总共就七个负载均衡策略

* WeightedResponseTimeRule
* RoundRobinRule
* AvailabilityFilteringRule
* ZoneAvoidanceRule
* BestAvailableRule
* RandomRule
* RetryRule

AbstractLoadBalancerRule、ClientConfigEnabledRoundRobinRule、PredicateBasedRule这三个是抽象的类。

如果要切换ribbon的默认负载均衡策略则在RestConfig配置类当中，让spring容器当中去配置定义一个Bean，即IRule 构造方法中返回任意一个上述七个当中任意一个负载均衡实现方法类的实例对象即可。 

（spring cloud新版本即G版本使用的默认的负载均衡策略为ZoneAvoidanceRule）

ribbon即一个jar包。