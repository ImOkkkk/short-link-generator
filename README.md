# 短链接生成

## 预生成短URL

预先生成一批不会重复的短URL字符串，当外部请求输入长URL获取短URL时，直接从预先生成好的短URL字符串池中获取一个即可。

预生成短URL的算法采用随机数来实现，6个字符，每个字符使用随机数生成(0-63，再取对应的Base64编码字符)。避免冲突，预生成时使用布隆过滤器检查是否存在。

## 短URL预加载

服务启动预先从数据库加载1000个短URL到LinkedBlockingQueue，每使用一个短URL出队一个。单独的线程监控队列长度，当队列长度不足200时，从数据库中加载1000个短URL到队列尾部。

## TODO

- [ ] ~~预生成URL布隆过滤器内容持久化~~
- [x] 引入Redis布隆过滤器
- [x] 引入Kafka，预生成URL异步多线程入库
- [ ] ~~生成游标记录本次已使用到的短URL主键Id~~
- [ ] 增加内存缓存caffeine，缓存热点数据
- [ ] 支持用户自定义短URL
- [x] Github Action + Docker自动构建部署
- [x] 分库分表 (笔记：https://imokkkk.github.io/fenkufenbiaon/)
- [x] WebFlux整合Resilience4j实现并发控制(resilience4j分支)
- [x] SpringCloud Config实现动态更新Resilience4j配置(resilience4j分支)
- [x] Redis Spring API监控指标注册到MeterRegistry

  

> 参考：https://time.geekbang.org/column/article/488496



部署运行服务，访问 http://127.0.0.1:6324/index.html

**长链接转换短链接**

![长链接转换短链接](img/image-20220506162739059.png)**预生成短链接**

![预生成短链接](img/image-20220506162822750.png)

