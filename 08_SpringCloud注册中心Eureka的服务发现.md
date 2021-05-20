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





















