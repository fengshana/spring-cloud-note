

## 搭建Spring Cloud服务提供者服务

下面看Spring Cloud如何进行开发。

开发之前，先看下Spring Cloud 的整体开发结构，它和Dubbo非常相似，所以有的时候经常会将Spring Cloud与Dubbo进行比较。

在原来讲解Dubbo的时候，会有一个注册中心，有一个服务提供者，以及一个服务消费者。

那么Spring Cloud大概也是这样一个模式。有一个服务提供方，一个服务消费方，以及一个注册中心。

服务提供方启动服务之后会在注册中心进行注册，而服务消费方在启动服务之后也会在注册中心进行注册，因为在微服务当中，Service Consumer或者是Service Provider都既可以是服务提供方也可以是服务消费方。所以这两个服务Service Consumer/Service Provider都会往注册中心Eureka Server进行注册，即Service Consumer可以进行调用Service Provider，而Service Provider也可以进行调用Service Consumer。（微服务之间是可以进行相互间的调用的。然后子微服务都会向注册中心注册自己的服务。）那么其整体结构即可以分为这三个部分，Service Consumer、Service Provider即服务，Eureka Server即注册中心，和Dubbo非常相似。

### 2、Spring Cloud的整体架构

![image-20210511234948296](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210511234948296.png)

Service Provider：暴露服务的服务提供方。

Service Consumer：调用远程服务的服务消费方。

Eureka Server：服务注册中心和服务发现中心。

### 3、服务消费方直接调用服务提供方

我们知道，SpringCloud构建微服务是基于SpringBoot开发的。

搭建环境：有一个服务提供方和服务消费方，但是没有注册中心，而是服务消费方直接调用服务提供方，类似Dubbo，也可以没有注册中心，直接进行调用。

将环境搭建好之后，接下来将注册中心搭建起来。通过向注册中心注册了服务，服务消费方再来进行调用服务提供方。

1. 打开IDEA，建立服务的一个模块为Maven模块，该Maven模块为一个**父项目**（到时候其他子项目将会继承该父项目，这里是用到了maven的继承和聚合结构）。

![image-20210512001042089](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512001042089.png)

2. 在弹出的界面【New Module】当中找到【Maven】并点击。

![image-20210512001835616](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512001835616.png)

3. 选择好Module <u>S</u>DK：`1.8(java version "1.8.0_201")`之后进行点击 <u>N</u>ext 按钮。

   ![image-20210512002218042](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512002218042.png)

4. 点击 <u>N</u>ext 按钮之后，将会出现另外一个弹窗。并在弹窗中输入GroupId、ArtifactId以及Version（这是一个父工程），填写完成之后再点击 <u>N</u>ext 按钮下一步。

   ```bash
   GroupId：com.bjpowernode.springcloud
   
   ArtifactId：34-springcloud-service-parent
   
   Version：1.0.0
   ```

   ![image-20210512002836707](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512002836707.png)

5. 在弹出的新弹窗中，检查`Content root:`标签该行中的内容取值是否正确。如若不正确则进行修改。这里看到显然是不正确的，需要将取值`_34springcloudserviceparent`修改为`34-springcloud-service-parent`。在进行修改`Content root:`标签该行中的取值时，`Module name:`和`Module file location:`也会跟着进行更改。

   ![image-20210512003131413](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512003131413.png)

   ![image-20210512003309291](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512003309291.png)

6. 最后点击 <u>F</u>inish 按钮，完成。

   ![image-20210512003522279](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512003522279.png)

7. 点击导入`import Changes`

   ![image-20210512004200305](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512004200305.png)

8. 在该父项目34-springcloud-service-parent 当中主要是配置些基础的内容。

   首先在该父项目中配置一个Spring Boot的相关内容。（Spring Boot 的父级依赖）该父项目工程需要集成springboot父项目。

```xml
<!-- 继承springboot父项目 -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.2.1.RELEASE</version>
    <relativePath/><!-- looup parent from repository -->
</parent>
```

​	即在34-springcloud-service-parent父项目的`pom.xm`文件当中添加内容如上，以下是添加好的，接着点击`import Changes`导入相关依赖。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承springboot父项目-->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.2.1.RELEASE</version>
        <relativePath/><!-- lookup parent from repository -->
    </parent>

    <groupId>com.bjpowernode.springcloud</groupId>
    <artifactId>34-springcloud-service-parent</artifactId>
    <version>1.0.0</version>


</project>

```

![image-20210512005248279](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512005248279.png)

9. 当前的SpringCloud的G版本需要使用Spring Boot的2.1.x系列，所以`spring-boot-starter-parent`的版本version需要进行修改为**2.1.9.RELEASE**或者其他（2.1.9是目前最新的版本），不能够使用Spring Boot的2.2.x系列。

![image-20210512005733587](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512005733587.png)

修改的pom.xml如下，当中仅仅修改了版本号为`2.1.9.RELEASE`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- 继承springboot父项目-->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.1.9.RELEASE</version>
        <relativePath/><!-- lookup parent from repository -->
    </parent>

    <groupId>com.bjpowernode.springcloud</groupId>
    <artifactId>34-springcloud-service-parent</artifactId>
    <version>1.0.0</version>


</project>
```

10. 再将如下内容粘贴到34-springcloud-service-parent工程的pom.xml中

    ```xml
    <!--依赖管理 -->
    <dependencyManagement>
        <dependencies>
            <!--现在spring cloud使用的是G版本即Greenwich SR3，所以需要将此处的Finchley.RELEASE H版本换成G版本Greenwich SR3版本的依赖，此处可以去maven仓库粘贴过来-->
          <!--<dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
               <version>Finchley.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>-->
            
            <!--
    	步骤：
    	1. 浏览器打开网址：https://mvnrepository.com/
    	2. 在最上方的文本框中输入spring-cloud-dependencies
    	3. 点击Search按钮进行检索
    	4. 在查询出来的列表中，点击最上方一条Spring Cloud Dependencies超链接
    	5. 在跳转到的页面中进行查找Greenwich.SR3该版本链接并点击
    	6. 进行复制spring-cloud-dependencies依赖
    	<!- - https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies - ->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>Greenwich.SR3</version>
        <type>pom</type>
        <scope>runtime</scope>
    </dependency>
    
    	7. 将内容复制进来之后再将<scope>runtime</scope>标签中的内容修改为import，即<scope>runtime</scope>
    	即
    <!- - https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies - ->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>Greenwich.SR3</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
    	8. 最后将F版(Finchley.RELEASE)的spring-cloud-dependencies依赖进行删除（此处做出注释处理）
    
    Greenwich.SR3为Spring Cloud的一个整体的英文版本，
    那么spring-cloud-dependencies该依赖下依赖了一系列的组件，
    且该依赖的一系列组件的版本都各不相同，比如说：
    spring-cloud-dependencies依赖下的组件版本如下所示，将这些不同版本的组件依赖都进行了归纳，归纳成为spring-cloud-dependencies的一个英文版本Greenwich.SR3
    所以此处只需要依赖一个spring-cloud-dependencies的一个英文版本即可，该spring-cloud-dependencies下依赖的各个组件不同版本都已经被归纳好了，不需要去关注底下那些组件的小版本，因为spring-cloud-dependencies依赖了一系列的组件了已经。
    
    <properties>
            <spring-cloud-aws.version>2.1.3.RELEASE</spring-cloud-aws.version>
            <spring-cloud-bus.version>2.1.3.RELEASE</spring-cloud-bus.version>
            <spring-cloud-cloudfoundry.version>2.1.3.RELEASE</spring-cloud-cloudfoundry.version>
            <spring-cloud-commons.version>2.1.3.RELEASE</spring-cloud-commons.version>
            <spring-cloud-config.version>2.1.4.RELEASE</spring-cloud-config.version>
            <spring-cloud-consul.version>2.1.3.RELEASE</spring-cloud-consul.version>
            <spring-cloud-consul.version>2.1.3.RELEASE</spring-cloud-consul.version>
            <spring-cloud-contract.version>2.1.3.RELEASE</spring-cloud-contract.version>
            <spring-cloud-function.version>2.0.2.RELEASE</spring-cloud-function.version>
            <spring-cloud-gateway.version>2.1.3.RELEASE</spring-cloud-gateway.version>
            <spring-cloud-gcp.version>1.1.3.RELEASE</spring-cloud-gcp.version>
            <spring-cloud-kubernetes.version>1.0.3.RELEASE</spring-cloud-kubernetes.version>
            <spring-cloud-netflix.version>2.1.3.RELEASE</spring-cloud-netflix.version>
            <spring-cloud-openfeign.version>2.1.3.RELEASE</spring-cloud-openfeign.version>
            <spring-cloud-security.version>2.1.4.RELEASE</spring-cloud-security.version>
            <spring-cloud-sleuth.version>2.1.4.RELEASE</spring-cloud-sleuth.version>
            <spring-cloud-stream.version>FIshtown.SR4</spring-cloud-stream.version>
            <spring-cloud-task.version>2.1.3.RELEASE</spring-cloud-task.version>
            <spring-cloud-vault.version>2.1.3.RELEASE</spring-cloud-vault.version>
            <spring-cloud-zookeeper.version>2.1.3.RELEASE</spring-cloud-zookeeper.version>
            </properties>
    -->
            
            
            
            <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies 
    spring-cloud-dependencies
    -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>Greenwich.SR3</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
    
        </dependencies>
    </dependencyManagement>
    ```

11. 以下为`34-springcloud-service-parent`父项目的`pom.xml`

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
    
        <!-- 继承springboot父项目-->
        <parent>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>2.1.9.RELEASE</version>
            <relativePath/><!-- lookup parent from repository -->
        </parent>
    
        <groupId>com.bjpowernode.springcloud</groupId>
        <artifactId>34-springcloud-service-parent</artifactId>
        <version>1.0.0</version>
    
        <!--依赖管理 -->
        <dependencyManagement>
            <dependencies>
                <!--现在spring cloud使用的是G版本即Greenwich SR3，所以需要将此处的Finchley.RELEASE H版本换成G版本Greenwich SR3版本的依赖，此处可以去maven仓库粘贴过来-->
                <!--<dependency>
                      <groupId>org.springframework.cloud</groupId>
                      <artifactId>spring-cloud-dependencies</artifactId>
                     <version>Finchley.RELEASE</version>
                      <type>pom</type>
                      <scope>import</scope>
                  </dependency>-->
    
                <!--
            步骤：
            1. 浏览器打开网址：https://mvnrepository.com/
            2. 在最上方的文本框中输入spring-cloud-dependencies
            3. 点击Search按钮进行检索
            4. 在查询出来的列表中，点击最上方一条Spring Cloud Dependencies超链接
            5. 在跳转到的页面中进行查找Greenwich.SR3该版本链接并点击
            6. 进行复制spring-cloud-dependencies依赖
            <!- - https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies - ->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Greenwich.SR3</version>
            <type>pom</type>
            <scope>runtime</scope>
        </dependency>
    
            7. 将内容复制进来之后再将<scope>runtime</scope>标签中的内容修改为import，即<scope>runtime</scope>
            即
        <!- - https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies - ->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Greenwich.SR3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
            8. 最后将F版的spring-cloud-dependencies依赖进行删除
        -->
                <!-- https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-dependencies -->
                <dependency>
                    <groupId>org.springframework.cloud</groupId>
                    <artifactId>spring-cloud-dependencies</artifactId>
                    <version>Greenwich.SR3</version>
                    <type>pom</type>
                    <scope>import</scope>
                </dependency>
    
            </dependencies>
        </dependencyManagement>
    </project>
    ```

12. 接下来进行创建服务提供者以及服务消费者，以及服务提供者以及服务消费者需要继承父项目`34-spring-cloud-service-parent`

    现在建立一个服务提供者的子服务（产品服务goods）。

    打开IDEA，找到顶部的`File`选项卡并点击，找到`New`后，再找到`Module...`

    ![image-20210512094716694](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512094716694.png)

    由于知道Spring Cloud开发底下采用的依然是Spring Boot，所以这里选择Spring Boot来进行开发Spring Cloud。

    在弹出的弹窗当中找到`Spring Initializr`该选项卡并点击，将`Module SDK：`该标签行的内容修改为`1.8（java version "1.8.0_201"）`，然后点击`Next`按钮，下一步

    ![image-20210512095048200](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512095048200.png)

    ![image-20210512095147802](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512095147802.png)

    

    点击Next按钮之后将出现弹窗如下：

    ![image-20210512095327158](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512095327158.png)

    ![image-20210512095404768](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512095404768.png)

    

    此处改为通过maven创建Spring Boot项目。

    同样打开IDEA-->`File`-->`New`-->`Module...`-->`Maven`-->将`Module SDK:` 该标签行的内容修改为`1.8（java version "1.8.0_201"）`-->点击`Next`按钮

    ![image-20210512095710345](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512095710345.png)

    ![image-20210512095742926](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512095742926.png)

    ![image-20210512100003284](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512100003284.png)

    点击`Next`按钮之后将弹出新窗口，在对应的标签行中填入相应的值之后（`34-spring-cloud-service-goods`即产品服务），再次点击`Next`按钮

    ```bash
    GroupId：com.bjpowernode.springcloud
    
    ArtifactId：34-springcloud-service-goods
    
    Version：1.0.0
    ```

    ![image-20210512100358726](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512100358726.png)

    

    ![image-20210512100557032](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512100557032.png)

    点击完`Next`按钮之后，在弹出的新窗口页面当中进行修改`Content root:` 标签行内的取值`_34springcloudserivcegoods`修改为`34-springcloud-service-goods`，`Module name:`标签行中的内容以及`Module file location:` 标签行内的内容也会跟着修改，

    以及还需要将`Content root:` 该标签行中的内容`\34-springcloud-service-parent`去掉，否则的话工程将建立在`34-springcloud-service-parent`该项目/文件夹中，同时`Module file location:` 该标签行中的内容也会跟着改变。

    修改完成之后点击`Finish`按钮，最后点击`import Changes`

    ![image-20210512100925653](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512100925653.png)

    

    ![image-20210512101008084](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512101008084.png)

    ![image-20210512101531582](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512101531582.png)

    ![image-20210512101728318](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512101728318.png)

    ![image-20210512102015090](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512102015090.png)

    可以看到左侧工程列中出现了有`34-springcloud-service-goods`，那么此时就将该产品服务建立起来了。产品服务建立起来之后，就通过maven的方式进行继承父项目，也就是`34-springcloud-service-parent`

    子项目产品服务`34-springcloud-service-goods`继承父项目则需要将父项目`34-springcloud-service-parent`中`pom.xml`中的`gav`坐标，也就是`groupId`、`artifactId`、`version`这三个坐标在子项目中，也就是产品服务`34-springcloud-service-goods`的`pom.xml`中进行定义一下，即子项目产品服务继承父项目

    ![image-20210512103113491](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512103113491.png)

    即将如下内容放到子项目产品服务`34-springcloud-service-goods`中的`pom.xml`中（需要加上\<parent>\</parent>标签）

    ```xml
    <parent>
        <groupId>com.bjpowernode.springcloud</groupId>
        <artifactId>34-springcloud-service-parent</artifactId>
        <version>1.0.0</version>
    </parent>
    ```

    即此时子项目产品服务`34-springcloud-service-goods`中的`pom.xml`为

    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
    
        <!-- 统一继承的父项目 -->
        <parent>
            <groupId>com.bjpowernode.springcloud</groupId>
            <artifactId>34-springcloud-service-parent</artifactId>
            <version>1.0.0</version>
        </parent>
    
        <groupId>com.bjpowernode.springcloud</groupId>
        <artifactId>34-springcloud-service-goods</artifactId>
        <version>1.0.0</version>
    </project>
    ```

    点击`import Changes`

    ![image-20210512103330752](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512103330752.png)

    当子项目继承好父项目之后，由于父项目当中已经定义好了Spring Boot（`spring-boot-starter-parent`）的版本为`2.1.9.RELEASE`，所以后续在使用Spring Boot进行开发的时候都不再需要填写Spring Boot的版本号了，因为在父项目当中已经继承了`spring-boot-starter-parent`依赖了。

    由于子项目继承了父项目，父项目继承了`spring-boot-starter-parent`，所以相当于子项目也继承了`spring-boot-starter-parent` Spring Boot项目。

    在子项目产品服务34-springcloud-service-goods当中看到pom.xml中其

    ```xml
    <groupId>com.bjpowernode.springcloud</groupId>
    ```

    groupId标签背景为淡淡的橙黄色背景，说明该标签groupId是可以省略的，为什么可以进行省略呢，由于子项目中继承了父项目，且子项目与父项目中pom.xml中的groupId如果一致，则子项目中的groupId可以进行省略。此处由于父项目pom.xml中的groupId的取值和子项目pom.xml中的groupId的取值一样，都为com.bjpowernode.springcloud，所以此处子项目中的groupId可以进行省略，也可以不进行省略，放在这里也可以。除非子项目pom.xml中的groupId和父项目pom.xml中的groupId不一样，就不能进行省略。

    这是maven的继承关系。

    ![image-20210512103910064](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512103910064.png)



​	开始进行写服务提供者即产品服务。

​	该服务提供者项目的开发和原来SpringBoot项目的开发其实是有什么大的区别，即按照SpringBoot开发即可。

该Maven工程需要变成SpringBoot项目的步骤如下

1. 子项目产品服务34-springcloud-service-goods需要继承父级依赖父项目即34-springcloud-service-parent，即需要在子项目产品服务34-springcloud-service-goods的pom.xml中配置如下

   ```xml
   <!-- 继承统一的父项目 -->
   <parent>
       <groupId>com.bjpowernode.springcloud</groupId>
       <artifactId>34-springcloud-service-parent</artifactId>
       <version>1.0.0</version>
   </parent>
   ```

2. 然后将一些配置文件(application.properties)、main方法(Application)等类文件拷贝过来即可（创建文件夹com.bjpowernode.springcloud将Application该main方法放入该文件夹中即可）。

   这样就可以变成SpringBoot项目了，因为刚刚是使用Maven进行创建的子项目产品服务34-springcloud-service-goods。

   由于后续到时候将可能有多个Application main方法，所以此处做一下区分，将该Application修改名称为GoodsApplication。

   ```java
   package com.bjpowernode.springcloud;
   
   import org.springframework.boot.SpringApplication;
   import org.springframework.boot.autoconfigure.SpringBootApplication;
   
   @SpringBootApplication
   public class GoodsApplication{
       public static void main(String[] args){
           SpringApplication.run(GoodsApplication.class,args);
       }
   }
   ```

   此处可以发现GoodsApplication该类中的@SpringBootApplication等注解还有一些类报错，找不到。

   这个时候就需要在子项目产品服务34-springcloud-service-goods的pom.xml中添加SpringBoot的相关依赖。因为这里是使用Spring Boot进行开发，这时候将Spring Boot的相关依赖添加进来。

   将如下内容放入到子项目产品服务34-springcloud-service-goods 的`pom.xml`中，并点击`import Changes`

   ```xml
   <name>34-springcloud-service-goods</name>
   <description>34-springcloud-service-goods project for Spring Boot</description>
   
   <properties>
       <java.version>1.8</java.version>
   </properties>
   
   <dependencies>
       <!-- spring web 起步依赖 -->
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-web</artifactId>
           <!--此处使用内嵌的Tomcat-->
           <!--
   <!--排除内嵌的Tomcat- ->
           <exclusions>
               <exclusion>
                   <groupId>
                   	org.springframework.boot
                   </groupId>
                   <artifactId>
                   spring-boot-starter-tomcat
                   </artifactId>
               </exclusion>
           </exclusions>
   		-->
       </dependency>
   </dependencies>
   
   ```

   在这当中web的起步依赖已经有了，

   后续将开发的是一个web程序，为什么子项目产品服务将会是一个web程序呢？

   由于该子项目产品服务相当于是一个服务提供者，即当客户端请求后端查询产品服务的相关信息时，由于SpringCloud当中服务与服务之间的服务调用是通过RESTful API风格，也就是Controller调用Controller。所以该项目并不是一个Java项目，而是一个Web项目，需要提供Controller以供别人去进行调用。

   所以在子项目产品服务的pom.xml中添加有spring-boot-starter-web web的起步依赖。

   ![image-20210512110517600](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512110517600.png)

3. 在依赖完spring-boot-starter-web web起步依赖之后，再来看GoodsApplication该类文件时，就已经没有那么多报错内容了。

   ![image-20210512111141283](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210512111141283.png)

   然后在完成上述操作之后，该服务该怎么进行开发就怎么进行开发。

   比如说需要进行查询产品信息，那么就进行开发查询数据库那一块的内容，因为该项目依然是Spring Boot，Spring Cloud当中项目依然是Spring Boot。

   

   创建mapper、model、service、service impl去进行查询数据的相关文件夹在com.bjpowernode.springcloud文件夹下。

   需要进行添加mybatis的启动器，起步依赖，即自动配置起步依赖。

   mybatis以及jdbc驱动

   maven：https://mvnrepository.com/search?q=mybatis-spring-boot-starater

   ![image-20210513145917448](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210513145917448.png)

   ```xml
   <!--mybatis-spring-boot-starter-->
   <dependency>
       <groupId>org.mybatis.spring.boot</groupId>
       <artifactId>mybatis-spring-boot-starter</artifactId>
       <version>2.1.1</version>
   </dependency>
   
   <!-- MySQL的jdbc驱动包 -->
   <dependency>
       <groupId>mysql</groupId>
       <artifactId>mysql-connector-java</artifactId>
   </dependency>
   ```

   GoodsMapper.java

   ```java
   package com.bjpowernode.springcloud.mapper;
   
   import com.bjpowernode.springcloud.model.Goods;
   import org.apache.ibatis.annotations.Mapper;
   import org.apache.ibatis.annotations.Param;
   
   import java.util.List;
   
   @Mapper
   public interface GoodsMapper{
       
       int deleteByPrimaryKey(Integer id);
       
       int insert(Goods record);
       
       int insertSelective(Goods record);
       
       Goods selectByPrimaryKey(Integer id);
       
       int updateByPrimaryKeySelective(Goods record);
       
       int updateByPrimaryKey(Goods record);
       
       List<Goods> selectAllGoods();
       
       int updateByStore(@Param("goodsId") Integer goodsId, @Param("buyNum") Integer buyNum);
   }
   ```

   GoodsMapper.xml

   ```xml
   <?xml version="1.0" encoding="UTF-8" ?>
   <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
   <mapper namespace="com.bjpowernode.springcloud.mapper.GoodsMapper">
       <resultMap id="BaseResultMap" type="com.bjpowernode.springcloud.model.Goods">
           <id column="id" jdbcType="INTEGER" property="id" />
           <result column="name" jdbcType="VARCHAR" property="name" />
           <result column="price" jdbcType="DECIMAL" property="price" />
           <result column="store" jdbcType="INTEGER" property="store" />
       </resultMap>
       
       <sql id="Base_Column_List">
           id,name,price,store
       </sql>
       
       <select id="selectAllGoods" resultMap="BaseResultMap">
           select 
           <include refid="Base_Column_list" />
           from goods 
       </select>
       
       <select id="selectByPrimaryKey" parameterType="java.lang.Integer" resultMap="BaseResultMap">
           select 
           <include refid="Base_Column_List" />
           from goods
           where id = #{id, jdbcType=INTEGER}
       </select>
       
       <delete id="deleteByPrimaryKey" parameterType="java.lang.Integer">
           delete 
           from goods
           where id = #{id, jdbcType=INTEGER}
       </delete>
       
       <insert id="insert" parameterType="com.bjpowernode.springlcoud.model.Goods">
           insert into goods(id, name, price, store)
           values(#{id, jdbcType=INTEGER}, #{name, jdbcType=VARCHAR}, #{price, jdbcType=DECIMAL}, #{store, jdbcType=INTEGER})
       </insert>
       
       
       <insert id="insertSelective" parameterType="com.bjpowernode.springcloud.model.Goods">
           insert into goods
           <trim prefix="(" suffix=")" suffixOverrides=",">
               <if test="id != null">
                   id,
               </if>
               <if test="name != null">
                   name,
               </if>
               <if test="price != null">
                   price,
               </if>
               <if test="store != null">
                   store,
               </if>
           </trim>
           
           <trim prefix="values (" suffix=")" suffixOverrides=",">
               <if test="id != null">
                   #{id, jdbcType=INTEGER},
               </if>
               <if test="name != null">
                   #{name, jdbcType=VARCHAR},
               </if>
               <if test="price != null">
                   #{price, jdbcType=DECIMAL},
               </if>
               <if test="store != null">
                   #{store, jdbcType=INTEGER},
               </if>
           </trim>
       </insert>
       
       
       <update id="updateByPrimaryKeySelective" parameterType="com.bjpowernode.springcloud.model.Goods">
       update goods 
           <set>
               <if test="name != null">
                   name = #{name, jdbcType=VARCHAR},
               </if>
               <if test="price != null">
                   price = #{price, jdbcType=DECIMAL},
               </if>
               <if test="store != null">
                   store = #{store, jdbcType=INTEGER},
               </if>
           </set>
           where id = #{id, jdbcType=INTEGER}
       </update>
       
       <update id="updateByPrimaryKey" parameterType="com.bjpowernode.springcloud.model.Goods">
           update goods 
           set name = #{name, jdbcType=VARCHAR},
           price = #{price, jdbcType=DECIMAL},
           store = #{store, jdbcType=INTEGER} 
           where id = #{id, jdbcType=INTEGER}
       </update>
       
       <update id="updateByStore">
           update goods
           set store = store - #{buyNum, jdbcType=INTEGER} 
           where id = #{id, jdbcType=INTEGER}
       </update>
   </mapper>
   ```

   ```java
   package com.bjpowernode.springcloud.service;
   
   import com.bjpowernode.springcloud.model.Goods; 
   
   import java.util.List;
   
   public interface GoodsService{
       
       public List<Goods> getAllGoods();
       
       public Goods getGoodsById(Integer goodsId);
   }
   ```

   ```java
   package com.bjpowernode.springcloud.service.impl;
   
   import com.bjpowernode.springcloud.mapper.GoodsMapper;
   import com.bjpowernode.springcloud.model.Goods;
   import com.bjpowernode.springcloud.service.GoodsService;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.stereotype.Service;
   
   import java.util.List;
   
   @Service
   public class GoodsServiceImpl implements GoodsService {
   
       @Autowired
       private GoodsMapper goodsMapper;
   
       public List<Goods> getAllGoods(){
           return goodsMapper.selectAllGoods();
       }
   
       public Goods getGoodsById(Integer goodsId){
           return goodsMapper.selectByPrimaryKey(goodsId);
       }
   }
   ```

   目录结构

   ![image-20210513152125857](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210513152125857.png)

   以上接口以及实现都有了。

   像model类当中，例如Users、ResultObject等等有可能在消费者服务当中也会被使用到，所以会将model类在项目当中单独提出来使用，和dubbo开发很相似，一些model类、公共的方法、公共的工具类等等提出来，提出来即这时候新建一个项目，这个项目是一个maven项目，该项目主要提供一些model类、常用工具类、常量类等等，是一个maven项目，不是SpringBoot，就是一个maven项目。

   新建maven项目步骤：

   1. ![image-20210519120848970](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519120848970.png)

   2. <img src="C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519121018640.png" alt="image-20210519121018640" style="zoom:25%;" />

   3. ```bash
      GroupId:com.bjpowernode.springcloud
      ArtifactId:34-springcloud-service-commons
      Version:1.0.0
      # 该项目当中放一些通用的内容
      ```

      ![image-20210519122259600](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519122259600.png)

   4. ```bash
      Module name:34-springcloud-service-commons
      Content root:F:\Project\UserProject\fsn\34-springcloud-service-commons
      Module file location:F:\Project\UserProject\fsn\34-springcloud-service-commons
      ```

      ![image-20210519122420421](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519122420421.png)

   5. import Changes

      ![image-20210519122542767](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519122542767.png)

   6. 那么此时通用项目 commons就准备好了。准备好了之后，在该项目当中建立包package，包名为：`com.bjpowernode.springcloud`，包名新建好之后，将34-springcloud-service-goods服务当中的com.bjpowernode.springcloud包下的model包复制到34-springcloud-service-commons服务中来。![image-20210519122749131](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519122749131.png)

   7. ![image-20210519123131177](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519123131177.png)



不记录这么详细了，就自己实操一边先





#### 3、服务消费方 直接调用 服务提供方

我们知道，SpringCloud 构建微服务是基于 SpringBoot开发的。

也就是 刚刚的案例当中，服务消费方直接调用服务提供方，通过服务提供方提供的一个地址去进行调用即可。



1. 创建一个SpringBoot工程，并且添加SpringBoot的相关依赖；

2. 创建服务提供者的访问方法，也就是后续 消费者如何访问提供者；

   Spring Cloud 是基于 rest 的访问，所以我们添加一个 Controller，在该 Controller 中提供一个 访问入口：

   ```java
   @RestController
   public class GoodsController{
       
       @Autowired
       private GoodsService goodsService;
       
       /*
       * 查询所有商品
       * @param model
       * @return 
       */
       @GetMapping("/service/goods")
       public ResultObject goods(Model model){
           List<Goods> goodsList = goodsService.getAllGoods();
           return new ResultObject(Constant.ZERO, "查询成功", goodsList);
       }
   }
   ```

3. 启动运行该 SpringBoot 程序，访问该 controller。



服务消费者 也是一个 SpringBoot项目，服务消费者 主要用来 消费服务提供者 提供的服务；

1. 创建一个 SpringBoot 工程，并且添加 SpringBoot的相关依赖；

2. 开发一个消费者方法，去消费服务提供者提供的服务，这个消费者方法也是一个Controller

   ```java
   @RequestMapping("/cloud/goods")
   public @ResponseBody Object goods(Model model){
       ResponseEntity<ResultObject> responseEntity = restTemplate.getForEntity(GOODS_URL_01, ResultObject.class);
       int statusCodeValue = responseEntity.getStatusCodeValue();
       HttpStatus httpStatus = responseEntity.getStatusCode();
      HttpHeaders httpHeaders = responseEntity.getHeaders();
       ResultObject body = responseEntity.getBody();//最终的数据
       
       System.out.println(statusCodeValue);
       System.out.println(httpStatus);
       System.out.println(httpHeaders);
       System.out.println(body);
       
       model.addAttribute("goodsList", body.getData());
      return body;
   }
   ```

3. 启动该 SpringBoot程序，测试服务消费者调用服务提供者；

上述中消费方直接调用服务提供方就完成了。 

在上述步骤中还并没有使用到 spring cloud。

我们仅仅使用了 RestTemplate 类来进行调用。那么该 RestTemplate是 spring自带的。它本身存在于spring中，所属包 `package org.springframework.web.client;`

所以controller与controller之间的调用即使不使用springcloud也可以使用restTemplate去进行调用。

那么接下来就开始引入spring cloud 的组件。

#### 4、 服务注册与发现

那么第一个组件就是 注册中心 组件。上述是直接通过Spring中的RestTemplate调用，没有使用SpringCloud 的注册中心。

前面的例子，我们看到了，是通过 手动指定 每个服务 来实现调用的，这是相当**低效**的，当服务接口**增多**，这种手动指定接口地址的方式 变得 **非常难以维护**，

即

```java
//产品服务的接口地址
private static final String GOODS_SERVICE_URL = "http://localhost:9100/service/goods";
//...
//那么这个时候就需要有注册中心将这些服务的接口地址放到注册中心上
```

Spring Cloud 提供了 多种服务注册与发现的实现方式，例如：Eureka、Consul、Zookeeper。

Spring Cloud 支持得最好的是 Eureka，其次是 Consul，再次是 Zookeeper。（最早一出来就是Eureka，Eureka目前停止更新维护了，Eureka是Netflix公司的一个产品，除了Eureka之外还有其他的方案，如Consul、Zookeeper等，包括还有国内还有一些产品 阿波罗，百度的.....等，大部分公司开发的时候注服务的注册发现还是使用Eureka，虽然它停止更新了，目前的现状这是，可能也有公司会慢慢转向其他的一些产品）



什么是服务注册，原来学过dubbo的话就比较清楚了，将服务在注册中心登记一下，原来dubbo就是这样处理的，将API端口、服务名称放上去。

##### 什么是服务注册？

服务注册：将服务所在 主机、端口号、版本号、通信协议等信息登记到注册中心上。

##### 什么是服务发现？

服务发现：服务消费者向 注册中心 请求已经登记的服务列表，然后得到某个服务的 主机、端口、版本号、通信协议等信息，从而实现对具体服务的调用；

服务发现可以说是 服务订阅，订阅这个服务。

可以认为服务发现是到注册中心上将服务提供者注册到注册中心的接口地址拿到，拿到之后就可以进行调用服务提供方提供的服务了。

和dubbo比较类似，在原理上是差不多的。



##### Eureka是什么？

Eureka注册中心。

Netflix即国外专门做版权视频的一个公司，同时它也是做云服务的一个公司。该公司下有着很多的开源产品，比如说阿里、百度等旗下都有一些开源产品。那么Eureka就是Netflix公司在GitHub上开源的一个产品，作为注册中心来使用的。

Eureka 是 Netflix 的子模块之一，也是一个核心的模块，Eureka 采用了 C-S（客户端/服务端）的设计架构，也就是 Eureka 由两个组件组成：Eureka服务端和Eureka客户端。

Eureka Server（一个独立的项目）用于注册服务 以及 实现服务的 负载均衡 和 故障转移，它是 服务的 注册中心，

Eureka Client（我们的微服务）它是用于与 Eureka Server交互(我们的服务即Eureka Client客户端与Eureka Server服务端进行交互)，获取其上注册的服务(不需要去记住接口地址)，使得交互变得非常简单，只需要通过 **服务标识** 即可拿到服务(其实就是从Eureka Server上获取拿到接口地址，也不需要自己去记住接口地址，即代码层当中的GOODS_SERVICE_URL就不再需要了)。

（我们的服务要注册到Eureka Server上面去，那么我们的服务即Eureka Client，即称为客户端，那么其服务端就是一个单独的项目，需要去单独部署运行；）



##### 与 spring-cloud 的关系：

Eureka 是 Netflix 公司开发的（一家做版权视频和云服务的公司），Spring Cloud封装了 Netflix 公司开发的 Eureka 模块来 实现服务的注册和发现，也就是说 Spring Cloud对 Netflix Eureka做了二次封装。

Spring Cloud将Netflix Eureka 拿进来整合了一下，方便Spring Cloud采用Netflix  Eureka来做服务注册中心，方便使用。

所以也就是Spring家族很多的东西不是其自身发明的轮子，而是使用别人的轮子，自己再做一个整合，即整合这个组件，整合那个组件，即整合mybatis、整合hibernate、整合rabbitmq、整合mongodb等等，什么东西都可以进行整合，在它基础上再做了一层包装，让开发者可以结合使用。

Eureka是Netflix公司开发并开源的。

角色关系图：

![image-20210519162912996](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210519162912996.png)

Eureka Server ：注册中心

Service Consumer：消费者

Service Provider: 提供者

消费者、提供者都可以向注册中心注册并订阅服务，拿到接口信息。



**搭建与配置 Eureka 服务注册中心**

Spring Cloud 要使用 Eureka 注册中心非常简单和方便，

Spring Cloud中的Eureka 服务注册中心 实际上也是一个 Spring Boot 工程，

我们只需要通过 引入 相关 **依赖**和**注解配置** 就能让 Spring Boot 构建的微服务应用轻松地与 Eureka进行整合。

接下来进行搭建Eureka注册中心，将其搭起来，然后服务就可以在注册中心Eureka上面使用了。就不再是通过restTemplate去调用远程接口了。

再建立一个项目

Eureka Server依然是一个 Spring Boot项目，

Spring Cloud 由于将Eureka做了一个包装，所以开发的时候每个项目都是Spring Boot。

具体步骤如下：

1. 创建一个 SpringBoot项目，并且添加 SpringBoot的相关依赖。

   03-springcloud-eureka-server

2. 添加 Eureka的依赖

   ```xml
   <!--Spring Cloud 的 eureka-server 起步依赖-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
   </dependency>
   ```

3. 在 SpringBoot 的入口类上 添加一个 @EnableEurekaServer注解，用于开启 Eureka 注册中心服务端

4. 在 application.properties 文件配置 Eureka 服务注册中心信息：

   ```properties
   #在application.properties 该核心文件当中指定一下内嵌tomcat端口是多少
   
   #内嵌定死tomcat 的端口
   server.port=8761
   
   # 服务的hostname，本地的 localhost
   #设置该服务注册中心的 hostname
   eureka.instance.hostname=localhost
   
   # register-with-eureka 注册使用Eureka
   # 因为 eureka-server 该项目本身也是一个微服务，spingboot开发的项目web应用，微服务；那么这个微服务的话会默认自己向自己进行注册
   # 修改为false的原因在于，自己只是作为注册中心，自己并不代表其他服务，不代表服务提供者或者服务消费者，所以不要将自己本身 注册中心 往注册中心也就是自己 进行注册
   # 应该是可以理解为 自己该服务并不提供业务服务出去也不会进行消费业务服务，
   # 由于我们目前创建的应用是一个 服务注册中心，而不是普通的应用，默认情况下，这个应用会向注册中心（也是它自己）注册它自己
   # 设置为false，表示禁止这种 自己向自己注册的默认行为
   eureka.client.register-with-eureka=false
   
   # 修改为false 即 不要去检查其他服务，由于自己本身就是注册中心，不用去检索其他服务
   # fetch 获取
   # 表示不去从服务端 检索 其他服务信息，因为自己就是服务端，服务注册中心本身的职责就是维护服务实例，它不需要去检索其他服务
   # 即不需要去订阅其他的服务，去发现其他的服务
   eureka.client.fetch-registry=false
   
   # 也就是注册中心的路径，可以理解为对外提供的注册接口的地址
   # 即http://eureka.instance.hostname + : + eureka-server.server.port + /eureka
   # 表示注册中心到时候提供的服务是这个接口地址，其他子服务 通过这个接口地址向注册中心注册服务即可，它就可以接应到
   # eureka-server本身也提供了对外的一个接口，然后其他子服务 往这个接口去进行注册即可，对外暴露该接口，然后其余子服务向该接口注册服务即可
   # 指定服务注册中心的位置
   eureka.client.service-url.defaultZone=http://localhost:8761/eureka
   
   
   # 一般情况下配置如上
   # eureka.  后面会有很多的配置信息，有一些配置存在有默认值，
   # springboot当中不管集成什么，都会有一大堆这样类似的配置；不需要每一个配置都去看一遍，使用一些常用的即可
   # 上述配置完成之后，进行启动与测试 Eureka 服务注册中心
   # 1. 完成上面的项目搭建后，我们就可以启动springboot程序， main方法运行
   # 2. 启动成功之后，通过在浏览器地址栏访问我们的注册中心；
   
   
   
   ```

   

   ##### 启动与测试 Eureka 服务注册中心

1. 完成上面的项目搭建后，我们就可以启动 springboot 程序，main 方法运行；
2. 启动成功之后，通过在浏览器地址栏访问我们的注册中心；



##### 3-7. 向 Eureka 服务注册中心注册服务

我们前面搭建了 服务提供者项目（goods），接下来我们就可以将该 服务提供者注册到 Eureka注册中心，步骤如下：

1. 在该服务提供者中添加 Eureka的依赖，因为服务提供者向注册中心注册服务，需要连接 eureka，所以需要 eureka客户端的支持；

   ```xml
   <!--spring-cloud-starter-netflix-eureka-client-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
   </dependency>
   ```

2. 激活 Eureka中的 EnableEurekaClient 功能：在 SpringBoot 的入口函数处，通过添加 @EnableEurekaClient 注解来表明自己是一个 eureka客户端，让我的服务提供者 可以连接 eureka 注册中心；

3. 配置服务名称和注册中心地址

   ```properties
   
   # 在添加完 eureka client 依赖之后，接着进行配置 eureka client相关内容
   # 配置服务名称和注册中心地址
   # 每间隔 2s，向服务端发送一次心跳，证明自己依然 “存活”
   #lease 最近的 renewal 续约的 interval 间隔 seconds 秒
   # goods服务提供者同时也是 eureka client客户端，eureka client客户端是注册到服务端 eureka server，注册到注册中心
   # 下面该表示 eureka client 每隔2s 会给 eureka server服务端发送一次心跳，告诉eureka server服务端该服务提供者 该eureka client 客户端没有宕机
   # 让服务端 eureka server知道
   eureka.instance.lease-renewal-interval-in-seconds=2
   
   # 告诉服务端，如果我 10s 之内没有给你发心跳，就代表我故障了，将我踢出掉
   # 即在10s内，如果eureka client客户端没有给 eureka server服务端发送心跳，就让服务端 eureka server认为该 eureka client客户端已经宕机了，让其将该eureka client进行剔除掉
   # 如果有别的服务需要调用该goods服务的话，那么由于10s内该eureka client没有给eureka server发送心跳被剔除之后就没有办法再次调用该，本服务了
   # 就调用不到了，要不就调用别的另外一个服务，即可能本服务goods该服务进行部署了多份，比如说该服务进行部署了三份，当前该份服务宕机了之后，其余的两份服务并没有宕机
   # 将该份宕机的服务剔除掉了之后，那么有别的服务需要调用的时候，就让它去调用另外那两份没有宕机的服务
   eureka.instance.lease-expiration-duration-in-seconds=10
   
   # 这个心跳原理是什么？一直不太理解，类似于ping 吗？
   # 心跳原理即 eureka client 每隔一段时间就发送一个信息给eureka server服务端，不同的注册中心服务端与注册中心客户端发的内容有所不同
   # 比如说redis的redis-client会向redis-server发送一个ping,这个时候redis-server就会返回redis-client一个pong
   # 比如说mysql的话，心跳则是做一次select查询，select 1、select user等作为查询，那么它这个里面，它进行发心跳就可能是发送一个字符串之类的信息
   # 这个需要具体看一下它的源码实现，看eureka的源码，具体是发送什么信息需要看源码才能知道
   # 即eureka client发送个信息过去然后 eureka server 接收到了，代表着eureka client客户端是活着的，存活着的心跳
   # 不同的应用都存在有这样的机制，很多都有，redis、mysql 一些的服务都有它们的心跳机制，即发送个信息过去
   
   
   #告诉服务端，服务实例以 IP 为链接，而不是取 机器名
   # 等运行之后再看效果，在后台可以看到，链接地址默认是取的机器名称；这里修改为true表示默认取ip；这个配不配置都没有太大关系
   eureka.instance.prefer-ip-address=true
   
   # 告诉服务端，服务实例的名字
   # 即当前goods 服务提供者 eureka client向 服务注册中心注册服务了，给出服务提供者的名称，
   # 即给出 eureka server服务端，该eureka client客户端的名称；相当于对自己的服务做了一个标记，标记一下自己
   eureka.instance.instance-id=34-springcloud-service-goods
   
   # eureka 注册中心的连接地址
   eureka.client.service-url.defaultZone=http://localhost:8761/eureka
   
   # 就像dubbo当中连接zookeeper一样，在dubbo程序当中连接zookeeper，肯定要在dubbo程序当中配置zookeeper的ip、端口；那么此处也是一样
   # 要进行配置 eureka-server服务端的ip 端口 eureka client要向 eureka server注册中心进行注册，那么首先需要知道 eureka server注册中心的接口地址是什么，才能往这个接口地址当中进行注册
   # http://localhost:8761/eureka 这个就是eureka server 服务端所提供的接口地址，当时指定了服务注册中心的路径 eureka.client.service-url.defaultZone=http://localhost:8761/eureka
   # eureka server 对外提供服务的接口地址路径即 http://localhost:8761/eureka
   # 所以在eureka client当中填写 注册中心的连接地址时就需要填写上面这个地址，就相当于要将这个goods服务注册到这个地址上去
   
   # 以上配置完成之后，停止GoodsApplication之后重新启动该GoodsApplication
   # 重新启动成功之后就将注册到 eureka server服务注册中心上去了
   # 看到http://127.0.0.1:8761 首页进行刷新
   # 首页HOME中的 DS Replicas 下的 Instances currently registered with Eureka 下的表格栏 Application     AMIs        Availability Zones      Status下有一行记录
   # 记录值为： UNKNOWN      n/a(1)      (1)       UP(-1)-34-springcloud-service-goods
   
   #以及此时再去看http://127.0.0.1:8761 的菜单 LAST 1000 SINCE STARTUP中的 DS Replicas 下的 LAST 1000 newly  registered leases 下的表格栏 Timestamp       Lease下的记录值
   #记录值为   2021-5-19 23:14:06          UNKNOWN(34-springcloud-service-goods)
   # 表明该服务注册了
   
   # 这两处当中都有一个问题，即UNKNOWN
   # 该记录值Application 所对应的取值为UNKNOWN，有点不正常，需要进行调整，服务注册是ok的
   # spring应用名称 添加完成之后将服务提供者goods重启；需要到main方法当中去进行添加@EnableEurekaClient注解
   # 然后再去看http://127.0.0.1:8761 进行刷新，发现UNKNOWN就变为了下面的spring应用名称
   # 此时在首页HOME的表格Application...中有两条记录，但是UNKNOWN 这一条由于经过重启变为了34-springcloud-service-goods，则该条记录将在一段时间后消失掉；
   # 点击Status 字段下的服务提供者名称超链接，超链接的地址为http://192.168.0.104:9100/actuator/info  actuator 监控功能 所以此处点击它即打开了项目的服务监控功能，即项目需要配置一下项目监控功能，这样的话到时候点击超链接进去就可以看到，没有配置的话否则就会点进去报错；没有配置该actuator的功能
   # Application代表spring 服务提供者应用名称  Status当中取值为UP 代表是开着的是正常的
   spring.application.name=34-springcloud-service-goods
   
   # 提示有 EMERGENCY！ EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT .
   # RENEWALS ARE LESSER THAN  THRESHOLD  HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFF.
   #即：EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.
   # 这个是一个安全模式，后续解释
   
   # 在消费者项目当中也是如此步骤 1.添加依赖 2. application.properties 配置eureka client以及eureka server url 3.写代码
   ```

4. 启动服务提供者SpringBoot程序的main方法运行；

5. 启动运行之后，通过在浏览器地址访问我们之前搭建好的eureka注册中心，就可以看到有一个服务已经注册成功了。



##### 3-8. 从 Eureka 服务注册中心 发现与消费 服务

在服务注册已经ok的情况下，继续下面的步骤。然后就是需要从Eureka服务注册中心进行 发现与消费服务， 消费服务，即调用该服务。之前是通过spring中的restTemplate进行直接调用的，那么现在是需要通过注册中心的方式获取得到这个服务以后，然后再去调用。

我们已经搭建一个服务注册中心，同时也向这个服务注册中心注册了服务，接下来我们就可以 发现和消费服务了，这其中 服务的发现 由eureka客户端实现，而服务的消费 由 Ribbon 实现（这个组件后续还会进行介绍，底层使用到了Ribbon），也就是说服务的调用需要 eureka客户端和 Ribbon，两者配合起来才能实现；（发现和调用，首先要找到这个服务才能够进行调用这个服务，两者结合起来）

Eureka客户端 是一个 Java客户端，用来连接 Eureka服务端，与服务端进行交互、负载均衡，服务的故障切换等；

Ribbon 是一个基于 HTTP 和 TCP 的客户端负载均衡器，当使用 Ribbon对服务进行访问的时候，它会扩展 Eureka客户端的服务发现功能，实现从 Eureka注册中心中 获取服务端列表，并通过 Eureka客户端来确定服务端是否已经启动。（在这当中使用到了Ribbon，可以看jar包依赖，它其中本身就有依赖到Ribbon，看消费者portal项目或者goods项目都可以，只要有spring-cloud-starter-netflix-eureka-client依赖都可以看到）

- org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:2.1.3.RELEASE  
  - org.springframework.cloud:spring-cloud-starter:2.1.3.RELEASE  
  - org.springframework.cloud:spring-cloud-netflix-hystrix:2.1.3.RELEASE  
  - org.springframework.cloud:spring-cloud-netflix-eureka-client:2.1.3.RELEASE  
  - com.netflix.eureka:eureka-client:1.9.13  
  - com.netflix.eureka:eureka-core:1.9.13  
  - org.springframework.cloud:spring-cloud-starter-netflix-archaius:2.1.3.RELEASE  
  - org.springframework.cloud:spring-cloud-starter-netflix-ribbon:2.1.3.RELEASE  
  - com.netflix.ribbon:ribbon-eureka:2.3.0  
  - com.thoughtworks.xstream:xstream:1.4.11.1

在该spring-cloud-starter-netflix-eureka-client依赖当中，该依赖又依赖了有关于ribbon的依赖，即`com.netflix.ribbon:ribbon-eureka:2.3.0`

  

