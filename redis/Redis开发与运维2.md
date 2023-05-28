#### 2.4  列表

**列表**（`list`）类型 是用来 存储多个 **有序的 字符串**，

如图 2-18 所示，a、b、c、d、e 五个元素 从左到右 组成了一个 **有序的列表**，列表中的 **每个字符串** 称为 **元素**（**element**），

**一个列表** 最多可以存储 **((2的32次方)-1)** 个元素。

在 Redis 中，可以对 **列表** **两端** **插入**（**push**）和 **弹出**（**pop**），还可以获取 **指定范围的元素列表**、获取 **指定索引下标的元素** 等（如图 2-18 和 图 2-19 所示）。

**列表** 是一种 比较灵活的 数据结构，它可以充当 **栈** 和 **队列** 的角色，在实际开发上 有很多应用场景。

![image-20210517205903000](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210517205903000.png)

**列表类型** 有 **两个特点**：

**第一**、**列表** 中的 **元素 是 有序的**，这就意味着可以通过 **索引下标 获取某个元素** 或者 **某个范围内的 元素列表**，例如要获取 图 2-19的 第5个元素，可以执行 `lindex user:1:message 4` （**索引从 0 算起**）就可以得到元素e。

**第二**、**列表** 中的 **元素 可以是 重复的**，例如图 2-20所示 列表中 包含了 两个字符串 a。

![image-20210517210257862](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210517210257862.png)

这两个特点在后面介绍 **集合** 和 **有序集合** 后，会显得更加突出，因此在考虑是否使用该数据结构前，首先需要弄清楚 列表 数据结构 的特点。

##### 2.4.1  命令

下面将按照对 列表的 5种操作类型 对命令 进行介绍，命令如表2-4所示。

![image-20210517210707928](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210517210707928.png)

| 操作类型 | 操作                 |
| -------- | -------------------- |
| 添加     | rpush lpush linsert  |
| 查       | lrange lindex llen   |
| 删除     | lpop rpop lrem ltrim |
| 修改     | lset                 |
| 阻塞操作 | blpop brpop          |

1. **添加操作**

   （1）**从右边插入元素**

   ```bash
   rpush key value [value ...]
   ```

   下面代码 从右向左 插入元素 c、b、a：

   ```bash
   127.0.0.1:6379> rpush listkey c b a
   (integer) 3
   ```

   `lrange 0 -1` 命令可以获取 **从左到右 获取列表的所有元素**：

   ```bash
   127.0.0.1:6379> lrange listkey 0 -1
   1) "c"
   2) "b"
   3) "a"
   ```

   （2）**从左边插入元素**

   ```bash
   lpush key value [value ...]
   ```

   使用方法和 `rpush` 相同，只不过**从左侧插入**，这里不再赘述。

   （3）**向 某个元素 前 或者 后 插入元素**

   ```bash
   linsert key before|after pivot value
   ```

   `linsert` 命令会从 列表 中 **找到等于** `pivot` 的**元素**，在其 **前**（**before**） 或者 **后**（**after**） **插入一个新的元素** **value**，例如下面操作会在列表的元素 b 前 插入 java：

   ```bash
   127.0.0.1:6379> linsert listkey before b java
   (integer) 4
   ```

   返回结果为 4，代表 **当前列表的长度**，当前列表变为：

   ```bash
   127.0.0.1:6379> lrange listkey 0 -1
   1) "c"
   2) "java"
   3) "b"
   4) "a"
   ```

2. 查找

   （1） **获取 指定范围内的元素列表**

   ```bash
   lrange key start end
   ```

   `lrange` 操作会 **获取列表** **指定索引范围** **所有的元素**。

   **索引下标** 有 **两个特点**：

   **第一**，**索引下标** **从左到右** 分别是 **0 到 N-1**，但是 **从右到左** 分别是 **-1 到 -N**。

   **第二**，`lrange` 中的 **end 选项包含了自身**，这个和很多编程语言不包含 end 不太相同，例如想获取 列表的第2到第4个元素，可以执行如下操作：

   ```bash
   127.0.0.1:6379> lrange listkey 1 3
   1) "java"
   2) "b"
   3) "a"
   ```

   （2）**获取 列表 指定索引下标 的元素**

   ```bash
   lindex key index
   ```

   例如 当前列表 最后一个元素为a：

   ```bash
   127.0.0.1:6379> lindex listkey -1
   "a"
   ```

   （3） **获取列表长度**

   ```bash
   llen key
   ```

   例如，下面示例 当前列表长度为 4：

   ```bash
   127.0.0.1:6379> llen listkey
   (integer) 4
   ```

3. **删除**

   （1）**从列表左侧 弹出 元素**

   ```bash
   lpop key
   ```

   如下操作将 列表最左侧的元素 c 会被弹出，弹出后列表变为 java、b、a：

   ```bash
   127.0.0.1:6379> lpop listkey
   "c"
   127.0.0.1:6379> lrange listkey 0 -1
   1) "java"
   2) "b"
   3) "a"
   ```

   （2）**从列表右侧弹出**

   ```bash
   rpop key
   ```

   它的使用方法和 `lpop` 是一样的，只不过从 列表 **右侧弹出**，这里不再赘述。

   （3）**删除指定元素**

   ```bash
   lrem key count value
   ```

   `lrem` 命令会从 **列表中找到等于 value 的元素进行删除**，根据 **count 的不同** 分为 **三种情况**：

   * **count > 0** ，**从左到右**，**删除最多 count 个元素**。
   * **count < 0**，**从右到左**，**删除最多 count 绝对值个元素**。
   * **count = 0**，**删除所有**。

   例如向 列表 从左向右 插入 5个a，那么当前列表变为 "a a a a a java b a"，下面操作将从 列表左边开始删除4个为a 的元素：

   ```bash
   127.0.0.1:6379> lrem listkey 4 a
   (integer) 4
   127.0.0.1:6379> lrange listkey 0 -1
   1) "a"
   2) "java"
   3) "b"
   4) "a"
   ```

   （4）**按照索引范围 修剪 列表**

   ```bash
   ltrim key start end
   ```

   例如，下面操作会 只保留 列表 listkey 第2个到第4个元素：

   ```bash
   127.0.0.1:6379> ltrim listkey 1 3
   OK
   127.0.0.1:6379> lrange listkey 0 -1
   1) "java"
   2) "b"
   3) "a"
   ```

4. **修改**

   **修改指定索引下标的元素**：

   ```bash
   lset key index newValue
   ```

   下面操作会将 列表 listkey 中的 第3个 元素设置为 python：

   ```bash
   127.0.0.1:6379> lset listkey 2 python
   OK
   127.0.0.1:6379> lrange listkey 0 -1
   1) "java"
   2) "b"
   3) "python"
   ```

5. **阻塞操作**

   **阻塞式弹出** 如下：

   ```bash
   blpop key [key ...] timeout
   brpop key [key ...] timeout
   ```

   `blpop` 和 `brpop` 是 `lpop` 和 `rpop` 的 **阻塞版本**，它们除了 **弹出方向不同**，**使用方法基本相同**，所以下面以 `brpop` 命令进行说明，`brpop` 命令包含 两个参数：

   * **key [key ...]**：**多个列表的键**。
   * **timeout**：**阻塞时间**（单位：**秒**）

   1）**列表为空**：

   如果 `timeout = 3`，那么 **客户端 要等到 3 秒后返回**，

   如果 `timeout = 0`，那么 **客户端 一直阻塞等下去**：

   ```bash
   127.0.0.1:6379> brpop list:test 3
   (nil)
   (3.10s)
   127.0.0.1:6379> brpop list:test 0
   ...阻塞...
   ```

   如果此期间添加了数据 element1，客户端立即返回：

   ```bash
   127.0.0.1:6379> brpop list:test 3
   1) "list:test"
   2) "element1"
   (2.06s)
   ```

   2）**列表不为空**：**客户端会立即返回**。

   ```bash
   127.0.0.1:6379> brpop list:test 0
   1) "list:test"
   2) "element1"
   ```

   在使用 `brpop` 时，有 **两点** 需要注意。

   **第一点**，如果是 **多个键**，那么 `brpop` 会 **从左至右 遍历键**，**一旦有一个键 能弹出元素，客户端立即返回**：

   ```bash
   127.0.0.1:6379> brpop list:1 list:2 list:3
   ...阻塞...
   ```

   此时 另一个客户端 分别向 list:2 和 list:3 插入元素：

   ```bash
   client-lpush> lpush list:2 element2
   (integer) 1
   client-lpush> lpush list:3 element3
   (integer) 1
   ```

   客户端会 立即返回 list:2 中的element2 ，因为 list:2 最先有可以弹出的元素：

   ```bash
   127.0.0.1:6379> brpop list:1 list:2 list:3
   1) "list:2"
   2) "element2"
   ```

   **第二点**，如果 **多个客户端 对 同一个键执行** `brpop`，那么 **最先执行** `brpop`  **命令的客户端** **可以获取到 弹出的值**。

   客户端1：

   ```bash
   client-1> brpop list:test 0
   ...阻塞...
   ```

   客户端2：

   ```bash
   client-2> brpop list:test 0
   ...阻塞...
   ```

   客户端3：

   ```bash
   client-3> brpop list:test 0
   ...阻塞...
   ```

   此时另一个客户端 lpush 一个元素到 list:test 列表中：

   ```bash
   client-lpush> lpush list:test element
   (integer) 1
   ```

   那么 客户端1 最先会获取到元素，因为 客户端1 最先执行 brpop，而客户端2 和 客户端3继续阻塞：

   ```bash
   client> brpop list:test 0
   1) "list:test"
   2) "element"
   ```

   有关 列表 的 基础命令 已经介绍完了，表 2-5 是这些命令的时间复杂度，开发人员可以参考此表 选择合适的命令。

   ![image-20210517214858071](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210517214858071.png)

   | 操作类型 | 命令                                  | 时间复杂度                                          |
   | -------- | ------------------------------------- | --------------------------------------------------- |
   | 添加     | rpush key value [value ...]           | O(k)，k 是元素个数                                  |
   | 添加     | lpush key value [value ...]           | O(k)，k 是元素个数                                  |
   | 添加     | linsert key before\|after pivot value | O(n)，n 是pivot 距离列表头或尾的举例                |
   | 查找     | lrange key start end                  | O(s+n)，s 是 start 偏移量，n 是 start 到 end 的范围 |
   | 查找     | lindex key index                      | O(n)，n 是索引的偏移量                              |
   | 查找     | llen key                              | O(1)                                                |
   | 删除     | lpop key                              | O(1)                                                |
   | 删除     | rpop key                              | O(1)                                                |
   | 删除     | lrem count key                        | O(n)，n 是列表长度                                  |
   | 删除     | ltrim key start end                   | O(n)，n 是要裁剪的元素总数                          |
   | 修改     | lset key index value                  | O(n)，n 是索引的偏移量                              |
   | 阻塞操作 | blpop brpop                           | O(1)                                                |
   |          |                                       |                                                     |
   |          |                                       |                                                     |
   |          |                                       |                                                     |

   

##### 2.4.2   内部编码

**列表类型** 的 **内部编码** 有**两种**。

* **ziplist**（**压缩列表**）

  * 当 **列表的 元素个数 小于** `list-max-ziplist-entries` 配置（**默认 512 个**），同时 **列表中 每个元素的值 都小于** `list-max-ziplist-value` 配置时（**默认 64 字节**），Redis 会选用 `ziplist`  来作为 **列表的内部实现** 来 **减少内存的使用**。

* **linkedlist**（**链表**）

  * 当 **列表类型 无法满足** `ziplist` 的**条件**时，Redis 会使用 `linkedlist` 作为 **列表的内部实现**。

  下面的示例演示了 列表类型的内部编码，以及相应的变化。

  1）当 **元素个数 较少 且 没有大元素** 时，**内部编码**为 `ziplist`：

  ```bash
  127.0.0.1:6379> rpush listkey e1 e2 e3
  (integer) 3
  127.0.0.1:6379> object encoding listkey
  "ziplist"
  ```

  2.1）当 **元素个数超过 512个**，**内部编码**变为 `linkedlist`：

  ```bash
  127.0.0.1:6379> rpush listkey e4 e5 ...忽略... e512 e 513
  (integer) 513
  127.0.0.1:6379> object encoding listkey
  "linkedlist"
  ```

  2.2）或者当 **某个元素超过 64 字节**，**内部编码**也会变为 `linkedlist`：

  ```bash
  127.0.0.1:6379> rpush listkey "one string is bigger than 64 byte ......................................."
  (integer) 4
  127.0.0.1:6379> object encoding listkey
  "linkedlist"
  ```

  > 开发提示
  >
  > **Redis 3.2** 版本提供了 `quicklist` 内部编码，
  >
  > 简单地说它是 **以一个ziplist 为节点的 linkedlist**，它结合了 **ziplist** 和 **linkedlist** 两者的优势，为 **列表类型** 提供了一种更为优秀的**内部编码实现**，它的设计原理可以参考Redis的另一个作者`Matt Stancliff` 的博客：http://matt.sh/redis-quicklist。

  有关 **列表类型的优化技巧** 将在 8.3节 详细介绍。

##### 2.4.3   使用场景

1. **消息队列**

   如图 2-21所示，Redis的 lpush+brpop 命令组合即可实现 阻塞队列，生产者客户端 使用 lpush 从列表左侧插入元素，多个消费者客户端使用 brpop 命令阻塞式的  “抢” 列表尾部的元素，多个客户端 保证了 消费的负载均衡 和 高可用性。

   ![image-20210517222037844](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210517222037844.png)

2. **文章列表**

   每个用户有属于自己的 文章列表，现需要 分页展示 文章列表。

   此时可以考虑使用 列表，因为 列表 不但是 有序的，同时 支持按照 索引范围 获取元素。

   1）每篇文章使用 哈希结构 存储，例如每篇文章有 3个属性 title、timestamp、content：

   ```bash
   hmset article:1 title xx timestamp 1476536196 content xxxx
   ...
   hmset article:k title yy timestamp 1476512536 content yyyy
   ....
   ```

   2）向 用户文章列表 添加文章，`user : {id} : articles` 作为 用户文章列表 的键：

   ```bash
   lpush user:1:articles article:1 article:3
   ...
   lpush user:k:articles article:5
   ...
   ```

   3）分页 获取 用户文章列表，例如下面伪代码获取用户 id=1 的前 10 篇文章：

   ```java
   articles = lrange user:1:articles 0 9
   for article in {articles}
   	hgetall {article}
   ```

   使用 **列表类型保存** 和 **获取文章列表** 会存在 **两个问题**。

   **第一**，如果 **每次分页获取的文章个数较多**，需要执行多次 `hgetall` 操作，

   ​	此时可以考虑使用 `Pipeline`（第3章会介绍）**批量获取**，

   ​	或者考虑 **将文章数据序列化为字符串**，使用 `mget` **批量获取**。

   

   **第二**，分页获取文章列表时，`lrange` 命令在列表两端性能较好，但是如果 **列表较大**，**获取列表中间范围的元素 性能会变差**，

   ​	此时可以考虑将 **列表做二级拆分**，

   ​	或者使用 **Redis 3.2** 的 `quicklist` **内部编码**实现，它结合 `ziplist` 和 `linkedlist` 的特点，**获取 列表中间范围的元素** 时也可以**高效**完成。

   > 开发提示
   >
   > 实际上 列表 的 使用场景 很多，在选择时可以参考一下口诀：
   >
   > * lpush + lpop = Stack (栈)
   > * lpush + rpop = Queue (队列)
   > * lpush + ltrim = Capped Collection (有限集合)
   > * lpush + brpop = Message Queue (消息队列)



#### 2.5  集合

​	**集合**（**set**）**类型** 也是用来 **保存多个的字符串元素**，

但和 **列表类型** 不一样的是，

**集合** 中 **不允许** 有 **重复元素**，

并且 **集合中的元素** 是 **无序的**，

**不能通过 索引下标 获取元素**。

如图 2-22所示，集合 `user:1:follow` 包含着 “it”、“music”、“his”、“sports”是个元素，

一个 **集合** 最多可以 **存储** **((2的32次方)-1)** 个**元素**。

Redis除了支持 **集合内的增删改查**，同时还支持 **多个集合**取**交集**、**并集**、**差集**，合理地使用好 **集合类型**，能在实际开发中解决很多实际问题。

##### 2.5.1   命令

下面将按照 **集合内** 和 **集合间** 两个维度 对集合的常用命令进行介绍。

1. **集合内操作**

   （1）**添加元素**

   ```bash
   sadd key element [element ...]
   ```

   返回结果 为 **添加成功的元素个数**，例如：

   ```bash
   127.0.0.1:6379> exists myset
   (integer) 0
   127.0.0.1:6379> sadd myset a b c
   (integer) 3
   127.0.0.1:6379> sadd myset a b
   (integer) 0
   ```

   （2）**删除元素**

   ```bash
   srem key element [element ...]
   ```

   返回结果 为 **成功删除元素个数**，例如：

   ```bash
   127.0.0.1:6379> srem myset a b
   (integer) 2
   127.0.0.1:6379> srem myset hello
   (integer) 0
   ```

   （3）**计算元素个数**

   ```bash
   scard key
   ```

   `scard` 的 **时间复杂度** 为 **O(1)**，它 **不会遍历集合所有元素**，而是 **直接用 Redis 内部的变量**，例如：

   ```bash
   127.0.0.1:6379> scard myset
   (integer) 1
   ```

   （4）**判断 元素 是否在集合中**

   ```bash
   sismember key element
   ```

   如果 给定元素 element **在集合内返回1**，**反之返回0**，例如：

   ```bash
   127.0.0.1:6379> sismember myset c
   (integer) 1
   ```

   （5）**随机 从集合返回 指定个数 元素**

   ```bash
   srandmember key [count]
   ```

   `[ count ]` 是 **可选参数**，如果不写 **默认是1**，例如：

   ```bash
   127.0.0.1:6379> srandmember myset 2
   1) "a"
   2) "c"
   127.0.0.1:6379> srandmember myset
   "d"
   ```

   （6）**从集合 随机 弹出元素**

   ```bash
   spop key
   ```

   `spop` 操作可以从 **集合中 随机弹出 一个元素**，

   例如下面代码是一次 spop 后，集合元素变为“d b a”：

   ```bash
   127.0.0.1:6379> spop myset
   "c"
   127.0.0.1:6379> smembers myset
   1) "d"
   2) "b"
   3) "a"
   ```

   需要注意的是 **Redis** 从 **3.2** 版本开始，`spop` 也支持 `[ count ]` 参数。

   `srandmember` 和 `spop` 都是 **随机从集合选出元素**，两者不同的是 `spop` **命令执行后**，**元素会从集合中删除**，而 `srandmember` 不会。

   （7）**获取所有元素**

   ```bash
   smembers key
   ```

   下面代码 获取集合 myset 所有元素，并且返回结果 是 **无序的**：

   ```bash
   127.0.0.1:6379> smembers myset
   1) "d"
   2) "b"
   3) "a"
   ```

   `smembers` 和 `lrange`、`hgetall` 都属于 **比较重的命令**，如果 **元素过多 存在阻塞 Redis 的可能性**，这时候可以使用 `sscan` 来完成，有关 `sscan` 命令 2.7节会介绍。

2. **集合间操作**

   现在有两个集合，它们分别是 `user:1:follow` 和 `user:2:follow`：

   ```bash
   127.0.0.1:6379> sadd user:1:follow it music his sports
   (integer) 4
   127.0.0.1:6379> sadd user:2:follow it news ent sports
   (integer) 4
   ```

   （1）**求 多个集合的 交集**

   ```bash
   sinter key [key ...]
   ```

   例如下面代码是求 `user:1:follow` 和 `user:2:follow` 两个集合的 **交集**，返回结果是 sports、it：

   ```bash
   127.0.0.1:6379> sinter user:1:follow user:2:follow
   1) "sprots"
   2) "it"
   ```

   （2）**求 多个集合的 并集**

   ```bash
   sunion key [key ...]
   ```

   例如下面代码是求 `user:1:follow` 和 `user:2:follow` 两个集合的 **并集**，返回结果是 sports、it、his、news、music、ent：

   ```bash
   127.0.0.1:6379> sunion user:1:follow user:2:follow
   1) "sports"
   2) "it"
   3) "his"
   4) "news"
   5) "music"
   6) "ent"
   ```

   （3）**求 多个集合的差集**

   ```bash
   sdiff key [key ...]
   ```

   例如下面代码是求 `user:1:follow` 和 `user:2:follow` 两个集合的 **差集**，返回结果是 music 和 his：

   ```bash
   127.0.0.1:6379> sdiff user:1:follow user:2:follow
   1) "music"
   2) "his"
   ```

   前面三个命令如图 2-23 所示。

   ![image-20210518142052098](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210518142052098.png)

   （4）**将 交集、并集、差集的结果保存**

   ```bash
   sinterstore destination key [key ...]
   sunionstore destination key [key ...]
   sdiffstore destination key [key ...]
   ```

   **集合间的运算**  在**元素较多**的情况下 会**比较耗时**，所以 Redis 提供了上面三个命令（`原命令+store`） 将 **集合间 交集、并集、差集 的结果 保存**在 `destination key` 中，

   例如下面操作将 `user:1:follow` 和 `user:2:follow` 两个集合的交集保存在 `user:1_2:inter` 中，`user:1_2:inter` 本身也是 **集合类型**：

   ```bash
   127.0.0.1:6379> sinterstore user:1_2:inter user:1:follow user:2:follow
   (integer) 2
   127.0.0.1:6379> type user:1_2:inter
   set
   127.0.0.1:6379> smembers user:1_2:inter
   1) "it"
   2) "sports"
   ```

   至此有关集合的命令基本已经介绍完了，表2-6给出集合常用命令的 时间复杂度，开发人员可以根据自身需求进行选择。

   ![image-20210518143258721](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210518143258721.png)

   | 命令                                  | 时间复杂度                                       |
   | ------------------------------------- | ------------------------------------------------ |
   | sadd key element [element ...]        | O(k)，k 是元素个数                               |
   | srem key element [element ...]        | O(k)，k 是元素个数                               |
   | scard key                             | O(1)                                             |
   | sismember key element                 | O(1)                                             |
   | srandmember key [count]               | O(count)                                         |
   | spop key                              | O(1)                                             |
   | smembers key                          | O(n)，n 是元素总数                               |
   | sinter key [key ...] 或者 sinterstore | O(m*k)，k 是多个集合中元素最少的个数，m 是键个数 |
   | sunion key [key ...] 或者 sunionstore | O(k)，k 是多个集合元素个数和                     |
   | sdiff key [key ...] 或者 sdiffstore   | O(k)，k 是多个集合元素个数和                     |
   |                                       |                                                  |
   |                                       |                                                  |
   |                                       |                                                  |



##### 2.5.2   内部编码

​	**集合类型** 的 **内部编码** 有 两种：

* **intset**（**整数集合**）
  * 当 **集合中的元素** 都是 **整数** 且 **元素个数** **小于** `set-max-intset-entries` 配置（默认 **512**个）时，Redis 会选用 `intset` 来作为 **集合的内部实现**，从而 **减少内存的使用**。
* **hashtable**（**哈希表**）
  * 当 **集合类型** **无法满足** `intset` **的条件**时，Redis 会使用 `hashtable` 作为 **集合的内部实现**。

下面用示例来说明：

1）当 **元素个数较少 且 都为 整数**时，**内部编码** 为 `intset`：

```bash
127.0.0.1:6379> sadd setkey 1 2 3 4
(integer) 4
127.0.0.1:6379> object encoding setkey
"intset"
```

2.1）当 **元素个数 超过 512个**，**内部编码** 变为 `hashtable`：

```bash
127.0.0.1:6379> sadd setkey 1 2 3 4 5 6 ... 512 513
(integer) 509
127.0.0.1:6379> scard setkey
(integer) 513
127.0.0.1:6379> object encoding setkey
"hashtable"
```

2.2） 当 **某个元素 不为整数** 时，**内部编码** 也会变为 `hashtable`：

```bash
127.0.0.1:6379> sadd setkey a
(integer) 1
127.0.0.1:6379> object encoding setkey
"hashtable"
```

有关 集合类型的内存优化技巧 将在8.3节中详细介绍。

##### 2.5.3  使用场景

**集合类型** 比较典型的 **使用场景** 就是 **标签**（**tag**）。

例如一个用户可能对 娱乐、体育 比较干兴趣，另一个用户可能对 历史、新闻 比较感兴趣，这些 **兴趣点** 就是 **标签**。

有了这些数据就可以得到 喜欢同一个标签的人，以及 用户的共同喜好 的标签，这些数据对于 **用户体验** 以及 **增强用户粘度** 比较重要。

例如 一个电子商务的网站 会对 不同标签的用户 做不同类型的推荐，比如对 数码产品比较感兴趣的人，在各个页面 或者通过 邮件的形式 给他们推荐最新的数码产品，通常会为网站带来更多的利益。

下面使用 **集合类型** **实现** **标签功能** 的 若干功能。

（1）给用户添加标签

```bash
sadd user:1:tags tag1 tag2 tag3
sadd user:2:tags tag2 tag3 tag5
...
sadd user:k:tags tag1 tag2 tag4
...
```

（2）给标签添加用户

```bash
sadd tag1:users user:1 user:3
sadd tag2:users user:1 user:2 user:3
...
sadd tagk:users user:1 user:2
...
```

> 开发提示
>
> **用户** 和 **标签** 的 **关系维护** 应该在 **一个事务** 内执行，**防止 部分命令失败 造成的数据 不一致**，有关如何将两个命令放在一个事务，第3章会介绍 **事务** 以及 **Lua的使用方法**。

（3）删除用户下的标签

```bash
srem user:1:tags tag1 tag5
...
```

（4）删除标签下的用户

```bash
srem tag1:users user:1
srem tag2:users user:1
...
```

（3）和（4）也是尽量放在 **一个事务** 执行。

（5）计算用户 共同感兴趣的标签。

可以用 sinter 命令，来计算 用户共同感兴趣的标签，如下代码所示：

```bash
sinter user:1:tags user:2:tags
```

> 开发提示
>
> 前面只是给出了使用 Redis 集合类型 实现标签 的基本思路，
>
> 实际上 一个标签系统 远比这个要 复杂 得多，不过 集合类型的应用场景 通常为以下几种：
>
> * sadd = Tagging （标签）
> * spop/srandmember = Random item（生成随机数，比如抽奖）
> * sadd + sinter = Social Graph（社交需求）



#### 2.6  有序集合

​	**有序集合** 相对于 **哈希**、**列表**、**集合** 来说会有一点点陌生，但既然叫 **有序集合**，那么它和 **集合** 必然有着练习，它 **保留了集合 不能有重复成员 的特性**，但不同的是，**有序集合中的元素 可以 排序**。

但是它和 **列表 使用索引下标 作为 排序依据** 不同的是，它给 **每个元素设置一个分数**（**score**） 作为**排序的依据**。

如图2-24所示，该有序集合包含 kris、mike、frank、tim、martin、tom，它们的分数分别是 1、91、200、220、250、251，

**有序集合** 提供了 **获取指定分数和元素范围查询**、**计算成员排名** 等功能，合理的利用有序集合，能帮助我们在实际开发中解决很多问题。

> 开发提示
>
> **有序集合** 中的 **元素 不能重复**，但是 **score 可以重复**，就和一个班里的同学 学号不能重复，但是考试成绩可以相同。

表2-7 给出了 列表、集合、有序集合 三者的异同点。

![image-20210518153459695](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210518153459695.png)

| 数据结构 | 是否允许重复元素 | 是否有序 | 有序实现方式 | 应用场景           |
| -------- | ---------------- | -------- | ------------ | ------------------ |
| 列表     | 是               | 是       | 索引下标     | 时间轴、消息队列等 |
| 集合     | 否               | 否       | 无           | 标签、社交等       |
| 有序集合 | 否               | 是       | 分值         | 排行榜系统、社交等 |

##### 2.6.1  命令

本节依旧按照 集合内 和 集合外 两个维度 对 有序集合 的命令进行介绍。

1. **集合内**

   （1）**添加成员**

   ```bash
   zadd key score member [score member ...]
   ```

   下面操作 向有序集合 `user:ranking` 添加用户 tom 和他的分数 251：

   ```bash
   127.0.0.1:6379> zadd user:ranking 251 tom
   (integer) 1
   ```

   返回结果 代表 **成功添加成员的个数**：

   ```bash
   127.0.0.1:6379> zadd user:ranking 1 kris 91 mike 200 frank 220 tim 250 martin
   (integer) 5
   ```

   有关 `zadd` 命令有 **两点** 需要注意：

   * **Redis 3.2** 为 `zadd` 命令添加了 `nx`、`xx`、`ch`、`incr`四个选项：
     * **nx**：**member 必须不存在，才可以设置成功，用于添加**。
     * **xx**：**member 必须存在，才可以设置成功，用于更新**。
     * **ch**：返回此次操作后，**有序集合元素 和 分数 发生变化的个数**。
     * **incr**：**对 score 做增加**，相当于后面介绍的 `zincrby`。
   * **有序集合** 相比 **集合** 提供了 **排序** 字段，但是也产生了代价，`zadd` 的 **时间复杂度** 为 **O(log(n))**，`sadd` 的 **时间复杂度** 为 **O(1)**。

   （2）**计算成员个数**

   ```bash
   zcard key
   ```

   例如下面操作返回 **有序集合** `user:ranking` 的 **成员数**为5，和 **集合类型** 的 `scard` 命令一样，`zcard` 的 **时间复杂度** 为 **O(1)**。

   ```bash
   127.0.0.1:6379> zcard user:ranking
   (integer) 5
   ```

   （3）**计算某个成员的分数**

   ```bash
   zscore key member
   ```

   tom 的分数为 251，**如果成员不存在则返回nil**：

   ```bash
   127.0.0.1:6379> zscore user:ranking tom
   "251"
   127.0.0.1:6379> zscore user:ranking test
   (nil)
   ```

   （4）**计算成员的排名**

   ```bash
   zrank key member
   zrevrank key member
   ```

   `zrank` 是**分数 从低到高 返回排名**，`zrevrank` 反之。

   例如下面操作中，tom 在 zrank 和 zrevrank 分别排名第5 和 第0 （**排名从0开始计算**）。

   ```bash
   127.0.0.1:6379> zrank user:ranking tom
   (integer) 5
   127.0.0.1:6379> zrevrank user:ranking tom
   (integer) 0
   ```

   （5）**删除成员**

   ```bash
   zrem key member [member ...]
   ```

   下面操作将成员 mike 从 有序集合 user:ranking 中 删除。

   ```bash
   127.0.0.1:6379> zrem user:ranking mike
   (integer) 1
   ```

   返回结果为 **成功删除的个数**。

   （6）**增加成员的分数**

   ```bash
   zincrby key increment member
   ```

   下面操作给 tom 增加了9分，分数变为了260分：

   ```bash
   127.0.0.1:6379> zincrby user:ranking 9 tom
   "260"
   ```

   （7）**返回指定排名范围的成员**

   ```bash
   zrange key start end [withscores]
   zrevrange key starat end [withscores]
   ```

   **有序集合** 是按照 **分值** 排名的，`zrange` 是 **从低到高** 返回，`zrevrange` 反之。

   下面代码返回排名最低的三个成员，如果加上 `withscore` 选项，同时会 **返回成员的分数**：

   ```bash
   127.0.0.1:6379> zrange user:ranking 0 2 withscore
   1) "kris"
   2) "1"
   3) "frank"
   4) "200"
   5) "tim"
   6) "220"
   127.0.0.1:6379> zrevrange user:ranking 0 2 withscore
   1) "tom"
   2) "260"
   3) "martin"
   4) "250"
   5) "tim"
   6) "220"
   ```

   （8）**返回指定分数范围的成员**

   ```bash
   zrangebyscore key min max [withscores] [limit offset count]
   zrevrangebyscore key max min [withscores] [limit offset count]
   ```

   其中 `zrangebyscore` 按照 **分数** **从低到高** 返回，`zrevrangebyscore` 反之。

   例如下面操作 从低到高 返回 200到221分 的成员，`withscores` 选项会同时返回 **每个成员的分数**。

   `[limit offset count]` 选项可以 **限制输出的 起始位置 和 个数**：

   ```bash
   127.0.0.1:6379> zrangebyscore user:ranking 200 221 withscores
   1) "frank"
   2) "200"
   3) "tim"
   4) "220"
   127.0.0.1:6379> zrevrangebyscore user:ranking 221 200 withscores
   1) "tim"
   2) "220"
   3) "frank"
   4) "200"
   ```

   同时 `min` 和 `max` 还支持 **开区间（小括号）** 和 **闭区间（中括号）**，`-inf` 和 `+inf` 分别代表 **无限小** 和 **无限大**：

   ```bash
   127.0.0.1:6379> zrangebyscore user:ranking 200 +inf withscores
   1) "tim"
   2) "220"
   3) "martin"
   4) "250"
   5) "tom"
   6) "260"
   ```

   （9）**返回 指定分数范围 成员个数**

   ```bash
   zcount key min max
   ```

   下面操作返回 200 到 221 分的成员的个数：

   ```bash
   127.0.0.1:6379> zcount user:ranking 200 221
   (integer) 2
   ```

   （10）**删除 指定排名内的升序元素**

   ```bash
   zremrangebyrank key start end
   ```

   下面操作删除 第 start 到 end 名的成员：

   ```bash
   127.0.0.1:6379> zremrangebyrank user:ranking 0 2
   (integer) 3
   ```

   （11）**删除指定分数范围的成员**

   ```bash
   zremrangebyscore key min max
   ```

   下面操作将 250 分以上的成员全部删除，返回结果为 成功删除的个数：

   ```bash
   127.0.0.1:6379> zremrangebyscore user:ranking 250 +inf
   (integer) 2
   ```

2. **集合间的操作**

   将图 2-25 的 两个 有序集合 导入到 Redis中。

   ![image-20210518161234397](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210518161234397.png)

   ```bash
   127.0.0.1:6379> zadd user:ranking:1 1 kris 91 mike 200 frank 220 tim 250 martin 251 tom
   (integer) 6
   127.0.0.1:6379> zadd user:ranking 8 james 77 mike 625 martin 888 tom
   (integer) 4
   ```

   （1）**交集**

   ```bash
   zinterstore destination numkeys key [key ...] [weights weight [weight ...]] [aggregate sum|min|max]
   ```

   这个命令参数较多，下面分别进行说明：

   * **destination**：交集计算结果保存到这个键。
   * **numkeys**：需要做 交集计算 键的个数。
   * **key [key ...]**：需要做 交集计算 的键。
   * **weights weight  [weight ...]** ：每个键的权重，在做**交集计算**时，每个键中的 **每个member 会将自己分数乘以这个 权重**，**每个键的权重 默认是1**。
   * **aggregate sum|min|max**：计算成员交集后，分值可以按照 sum（和）、min（最小值）、max（最大值）做汇总，**默认值是 sum**。

   下面操作对 `user:ranking:1` 和 `user:ranking:2` 做交集，`weights` 和 `aggregate` 使用了默认配置，可以看到 目标键 `user:ranking:1_inter_2` 对分值做了 `sum` 操作：

   ```bash
   127.0.0.1:6379> zinterstore user:ranking:1_inter_2 user:ranking:1 user:ranking:2
   (integer) 3
   127.0.0.1:6379> zrange user:ranking:1_inter_2 0 -1 withscores
   1) "mike"
   2) "168"
   3) "martin"
   4) "875"
   5) "tom"
   6) "1139"
   ```

   如果想让 `user:ranking:2` 的权重变为 0.5，并且具和效果使用 `max`，可以执行如下操作：

   ```bash
   127.0.0.1:6379> zinterstore user:ranking:1_inter_2 2 user:ranking:1 user:ranking:2 weights 1 0.5 aggregate max
   (integer) 3
   127.0.0.1:6379> zrange user:ranking:1_inter_2 0 -1 withscores
   1) "mike"
   2) "91"
   3) "martin"
   4) "312.5"
   5) "tom"
   6) "444"
   ```

   （2）**并集**

   ```bash
   zunionstore destination numkeys key [key ...] [weights weight [weight ...]] [aggregate sum|min|max]
   ```

   该命令的所有参数和 `zinterstore` 是一致的，只不过还是做 **并集计算**，例如下面操作是计算 `user:ranking:1` 和 `user:ranking:2` 的**并集**，`weights` 和 `aggregate` 使用了 **默认配置**，可以看到目标键 `user:ranking:1_union_2` 对分值做了 `sum` 操作：

   ```bash
   127.0.0.1:6379> zunionstore user:ranking:1_union_2 2 user:ranking:1 user:ranking:2 
   (integer) 7
   127.0.0.1:6379> zrange user:ranking:1_union_2 0 -1 withscores
   1) "kris"
   2) "1"
   3) "james"
   4) "8"
   5) "mike"
   6) "168"
   7) "frank"
   8) "200"
   9) "tim"
   10) "220"
   11) "martin"
   12) "875"
   13) "tom"
   14) "1139"
   ```

   至此 有序集合的命令 基本介绍完了，表2-8 是这些命令的 **时间复杂度**，开发人员再试用对应的命令进行开发时，不仅要考虑 **功能性**，还要了解 **相应的时间复杂度**，防止由于使用不当造成 **应用效率下降** 以及 **Redis阻塞**。

![image-20210518164515257](C:\Users\ASUS\AppData\Roaming\Typora\typora-user-images\image-20210518164515257.png)

| 命令                                                         | 时间复杂度                                                   |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| zadd key score member [score member ...]                     | O(k × log(n))，k 是添加成员的个数，n 是当前有序集合成员个数  |
| zcard key                                                    | O(1)                                                         |
| zscore key member                                            | O(1)                                                         |
| zrank key member zrevrank key member                         | O(log(n))，n 是当前有序集合成员个数                          |
| zrem key member [member ...]                                 | O(k*log(n))，k 是删除成员的个数，n 是当前有序集合成员个数    |
| zincrby key increment member                                 | O(log(n))，n 是当前有序集合成员个数                          |
| zrange key start end [withscores]          zrevrange key start end [withscores] | O(log(n) + k)，k 是要获取的成员个数，n 是当前有序集合成员个数 |
| zrangebyscore key min max [withscores]            zrevrangebyscore key min max [withscores] | O(log(n) + k)，k 是要获取的成员个数，n 是当前有序集合成员个数 |
| zcount                                                       | O(log(n))，n 是当前有序集合成员个数                          |
| zremrangebyrank key start end                                | O(log(n) + k)，k 是要删除的成员个数， n 是当前有序集合成员个数 |
| zremrangebyscore key min max                                 | O(log(n) + k)，k 是要删除的成员个数，n 是当前有序集合成员个数 |
| zinterstore destination numkeys key [key ...]                | O(n*k) + O(m\*log(m))，n 是成员数最小的有序集合成员个数，k 是有序集合的个数，m 是结果集中的成员个数 |
| zunionstore destination numkeys key [key ...]                | O(n) + O(m*log(m))，n 是所有有序集合成员个数和，m 是结果集中成员个数 |

##### 2.6.2  内部编码

**有序集合** 类型的 **内部编码** 有**两种**：

* **ziplist**（**压缩列表**）：当 **有序集合的元素个数 小于** `zset-max-ziplist-entries` 配置（**默认128个**），同时 **每个元素的值 都小于** `zset-max-ziplist-value` 配置（**默认64字节**）时，Redis 会用 `ziplist` 来作为 有序集合的内部实现，`ziplist` 可以有效 **减少内存的使用**。
* **skiplist**（**跳跃表**）：当 **ziplist 条件不满足时**，有序集合会使用 `skiplist` 作为内部实现，因为此时 `ziplist` 的 **读写效率会下降**。

下面用示例来说明：

1）当 **元素个数较少 且 每个元素较小时**，**内部编码** 为 `ziplist`：

```bash
127.0.0.1:6379> zadd zsetkey 50 e1 60 e2 30 e3
(integer) 3
127.0.0.1:6379> object encoding zsetkey
"ziplist"
```

2.1）当 **元素个数 超过128个**，**内部编码** 变为 `skiplist`：

```bash
127.0.0.1:6379> zadd zsetkey 50 e1 60 e2 30 e3 12 e4 ...忽略... 84 e129
(integer) 129
127.0.0.1:6379> object encoding zsetkey
"skiplist"
```

2.2）当 **某个元素 大于 64个字节时**，**内部编码** 也会变为 `skiplist`：

```bash
127.0.0.1:6379> zadd zsetkey 20 "one string is bigger than 64 byte......................................................"
(integer) 1
127.0.0.1:6379> object encoding zsetkey
"skiplist"
```



##### 2.6.3    使用场景

有序集合 比较典型的使用场景就是 排行榜系统。

例如视频网站需要对用户上传的视频做排行榜，榜单的维度可能是多个方面的：按照时间、按照播放数量、按照获得的赞数。

本节使用在 赞数 这个维度，记录每天用户上传视频的排行榜。

主要需要实现以下4个功能。

（1）添加用户赞数

例如用户 mike 上传了一个视频，并获得了3个赞，可以使用有序集合的 zadd 和 zincrby 功能：

```bash
zadd user:ranking:2016_03_15 mike 3
```

如果之后再获得一个赞，可以使用 `zincrby` ：

```bash
zincrby user:ranking:2016_03_15 mike 1
```

（2）取消用户赞数

由于各种原因（例如用户注销、用户作弊）需要将用户删除，此时需要将用户 从榜单中 删除掉，可以使用 `zrem` 。

例如删除成员 tom：

```bash
zrem user:ranking:2016_03_15 mike
```

（3）展示获取赞数最多的十个用户

此功能使用 `zrevrange` 命令实现：

```bash
zrevrange user:ranking:2016_03_15 0 9
```

（4）展示用户信息以及用户分数

此功能将 用户名 作为键后缀，将用户信息保存在哈希类型中，至于 用户的分数和排名可以使用 zscore 和 zrank 两个功能：

```bash
hgetall user:info:tom
zscore user:ranking:2016_03_15 mike
zrank user:ranking:2016_03_15 mike
```



#### 2.7   键管理

本节将按照 **单个键**、**遍历键**、**数据库管理** 三个维度对一些通用命令进行介绍。

##### 2.7.1   单个键管理

针对 单个键 的命令，前面几节已经介绍过一部分了，例如 type、del、object、exists、expire等，下面将介绍剩余的几个重要命令。

1. **键重命名**

   ```bash
   rename key newkey
   ```

   例如现有一个键值对，键为 python，值为 jedis：

   ```bash
   127.0.0.1:6379> get python
   "jedis"
   ```

   下面操作将 键python 重命名为 java：

   ```bash
   127.0.0.1:6379> set python jedis
   OK
   127.0.0.1:6379> rename python java
   OK
   127.0.0.1:6379> get python
   (nil)
   127.0.0.1:6379> get java
   "jedis"
   ```

   如果在 rename 之前，键 java 已经存在，那么它的值也将被覆盖，如下所示：

   ```bash
   127.0.0.1:6379> set a b
   OK
   127.0.0.1:6379> set c d
   OK
   127.0.0.1:6379> rename a c
   OK
   127.0.0.1:6379> get a
   (nil)
   127.0.0.1:6379> get c
   "b"
   ```

   为了防止被强行 rename，Redis 提供了 renamenx 命令，确保只有 newKey 不存在的时候才被覆盖。

   例如下面操作 renamenx 时，newkey = python 已经存在，返回结果是 0 代表没有完成重命名，所以 键 java 和 python 的值 没变：

   ```bash
   127.0.0.1:6379> set java jedis
   OK
   127.0.0.1:6379> set python redis-py
   OK
   127.0.0.1:6379> renamenx java python
   (integer) 0
   127.0.0.1:6379> get java
   "jedis"
   127.0.0.1:6379> get python
   "redis-py"
   ```

   在使用重命名命令时，有 **两点** 需要注意：

   * 由于 重命名期间 会执行 del 命令删除旧的键，如果 键对应的值比较大，会存在阻塞Redis的可能性，这点不要忽视。

   * 如果 rename 和 renamenx 中的 key 和 newkey 如果是相同的，在 Redis 3.2 和之前版本返回结果略有不同。

     Redis 3.2中会返回 OK：

     ```bash
     127.0.0.1:6379> rename key key
     OK
     ```

     Redis 3.2 之前的版本会提示错误：

     ```bash
     127.0.0.1:6379> rename key key
     (error) ERR source and destination objects are the same
     ```

2. **随机返回一个键**

   ```bash
   randomkey
   ```

   下面示例中，当前数据库有 1000 个键值对，randomkey 命令会随机从中挑选一个键：

   ```bash
   127.0.0.1:6379> dbsize
   1000
   127.0.0.1:6379> randomkey
   "hello"
   127.0.0.1:6379> randomkey
   "jedis"
   ```

3. **键过期**

   2.1节 简单介绍 键过期 功能，它可以 自动将带有过期时间的键删除，在许多应用场景都非常有帮助。

   除了 expire、ttl 命令以外，Redis还提供了 expireat、pexpire、pexpireat、pttl、persist 等一系列命令，下面分别进行说明：

   * expire key seconds：键在 seconds 秒后过期。
   * expireat key timestamp：键在 秒级时间戳 timestamp 后过期。

   下面为 键 hello 设置了 10 秒的过期时间，然后通过 ttl 观察它的 过期剩余时间（单位：秒），随着时间的推移，ttl 主键变小，最终变为 -2：

   ```bash
   127.0.0.1:6379> set hello world
   OK
   127.0.0.1:6379> expire hello 10
   (integer) 1
   
   #还剩7秒
   127.0.0.1:6379> ttl hello
   (integer) 7
   ...
   
   # 还剩0秒
   127.0.0.1:6379> ttl hello
   (integer) 0
   
   #返回结果为-2 ，说明键 hello 已经被删除
   127.0.0.1:6379> ttl hello
   (integer) -2
   ```

   ttl 命令和 pttl 都可以查询 键的剩余过期时间，但是 pttl 精度更高 可以达到毫秒级别，有3中返回值：

   * 大于等于0的整数：键剩余的过期时间（ttl 是秒，pttl 是毫秒）。
   * -1：键没有设置过期时间。
   * -2：键不存在。

   expireat 命令可以设置 间的秒级过期时间戳，例如如果需要将 键 hello 在 2016-08-01 00:00:00 （秒级时间戳为 1469980800）过期，可以执行如下操作：

   ```bash
   127.0.0.1:6379> expireat hello 1469980800
   (integer) 1
   ```

   除此之外，Redis 2.6 版本后提供了 毫秒级的过期方案：

   * pexpire key milliseconds：键在 milliseconds 毫秒后过期。
   * pexpireat key milliseconds-timestamp：键在 毫秒级时间戳 timestamp后过期。

   但无论是使用 过期时间 还是时间戳，秒级还是毫秒级，在 Redis内部 最终使用的都是 pexpireat。

   在使用 Redis 相关过期命令时，需要注意以下几点。

   1）如果 expire key 的键不存在，返回结果为0：

   ```bash
   127.0.0.1:6379> expire not_exist_key 30
   (integer) 0
   ```

   2）如果 过期时间为负值，键会立即被删除，犹如使用 del 命令一样：

   ```bash
   127.0.0.1:6379> set hello world
   OK
   127.0.0.1:6379> expire hello -2
   (integer) 1
   127.0.0.1:6379> get hello
   (nil)
   ```

   3）persist 命令 可以将 键的过期时间 清除：

   ```bash
   127.0.0.1:6379> hset key f1 v1
   (integer) 1
   127.0.0.1:6379> expire key 50
   (integer) 1
   127.0.0.1:6379> ttl key
   (integer) 46
   127.0.0.1:6379> persist key
   (integer) 1
   127.0.0.1:6379> ttl key
   (integer) -1
   ```

   4）对于 字符串类型键，执行 set 命令会去掉 过期时间，这个问题很容易在开发中被忽视。

   如下是 Redis源码中，set 命令的函数 setKey，可以看到最后执行了 removeExpire(db, key) 函数去掉了过期时间：

   ```bash
   void setKey(redisDb *db, robj *key, robj *val){
   	if(lookupKeyWrite(db, key) == NULL){
   		dbAdd(db, key, val);
   	}else{
   		dbOverwrite(db, key, val);
   	}
   	incrRefCount(val);
   	//去掉过期时间
   	removeExpire(db, key);
   	signalModifiedKey(db, key);
   }
   ```

   下面的例子证实了 set 会导致 过期时间失效，因为 ttl 变为 -1：

   ```bash
   127.0.0.1:6379> expire hello 50
   (integer) 1
   127.0.0.1:6379> ttl hello
   (integer) 46
   127.0.0.1:6379> set hello world
   OK
   127.0.0.1:6379> ttl hello 
   (integer) -1
   ```

   5）Redis 不支持 二级数据结构（例如 哈希、列表）内部元素的过期功能，例如 不能对 列表类型的一个元素做过期时间设置。

   6）setex 命令作为 set + expire 的组合，不但是 原子执行，同时 减少了一次网络通讯的时间。

   有关 Redis 键过期的详细原理，8.2节会深入剖析。

4. **迁移键**

   迁移键 功能非常重要，因为有时候我们只想把 部分数据 由一个Redis 迁移到 另一个Redis（例如从生产环境 迁移到 测试环境），Redis 发展历程中 提供了 move、dump+restore、migrate 三组迁移键的方法，它们的实现方式 以及 使用的场景 不太相同，下面分别介绍。

   （1）move

   ```bash
   move key db
   ```

   如图2-26所示， move 命令用于在 Redis 内部做数据迁移，Redis 内部 可以有多个数据库，由于 多个数据库功能 后面会进行介绍，这里只需要知道 Redis内部 可以有多个数据库，彼此在数据上是相互隔离的，more key db 就是把 指定的键 从源数据库 移动到 目标数据库中，但笔者认为 多数据库功能 不建议在 生产环境使用，所以这个命令 读者知道即可。

   （2）dump + restore

   ```bash
   dump key
   restore key ttl value
   ```

   

5. 

6. 

7. 







