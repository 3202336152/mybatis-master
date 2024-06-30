# Small MyBatis

Small MyBatis 是基于MyBatis源码实现了一个简化版的 MyBatis 框架，深入理解和实现了 MyBatis 核心功能及其原理。
## 目录

1. [映射器和 XML 的注册与使用](#映射器和-xml-的注册与使用)
2. [数据源的使用与池化](#数据源的使用与池化)
3. [SQL 执行器的定义和实现](#sql-执行器的定义和实现)
4. [反射器的定义与实现](#反射器的定义与实现)
5. [细化 XML 语句构建器](#细化-xml-语句构建器)
6. [基于策略模式解析参数](#基于策略模式解析参数)
7. [封装结果集处理器](#封装结果集处理器)
8. [增删改查操作](#增删改查操作)
9. [注解配置执行 SQL 语句](#注解配置执行-sql-语句)
10. [解析 ResultMap 映射参数配置](#解析-resultmap-映射参数配置)
11. [解析动态 SQL 语句](#解析动态-sql-语句)
12. [Plugin 插件功能实现](#plugin-插件功能实现)
13. [一级缓存实现](#一级缓存实现)
14. [二级缓存实现](#二级缓存实现)


## 映射器和 XML 的注册与使用

MyBatis 中映射器和 XML 配置的详细使用方法。您可以通过阅读此部分了解如何在 MyBatis 中定义和注册映射器，如何编写 XML 配置文件以映射数据库表，以及如何使用这些映射器进行数据库操作。

详细内容请参考：[映射器和 XML 的注册与使用](http://www.huanyujoker.top/articles/169)

## 数据源的使用与池化

数据源的使用和连接池的配置是保证数据库高效访问的重要环节。此部分介绍了如何在 MyBatis 项目中配置和使用数据源，如何设置数据库连接池以提高性能，以及一些常见的连接池实现，如 Druid 和 HikariCP。

详细内容请参考：[数据源的使用与池化](http://www.huanyujoker.top/articles/170)

## SQL 执行器的定义和实现

MyBatis 中 SQL 执行过程的实现原理。此部分深入探讨了 SQL 执行器的定义和实现，包括如何将 SQL 语句解析为数据库命令，以及如何处理执行结果。

详细内容请参考：[SQL 执行器的定义和实现](http://www.huanyujoker.top/articles/171)

## 反射器的定义与实现

反射机制在 MyBatis 中的应用与实现。反射器在 MyBatis 中用于动态获取和设置对象的属性，此部分详细介绍了反射器的定义、实现以及在 MyBatis 中的具体应用场景。

详细内容请参考：[反射器的定义与实现](http://www.huanyujoker.top/articles/173)

## 细化 XML 语句构建器

MyBatis 中 XML 语句构建器的高级用法。此部分介绍了如何使用 XML 构建复杂的 SQL 语句，如何在 XML 中使用动态 SQL，以及如何通过配置优化 SQL 构建过程。

详细内容请参考：[细化 XML 语句构建器](http://www.huanyujoker.top/articles/174)

## 基于策略模式解析参数

使用策略模式处理 MyBatis 参数的方法。策略模式在参数解析中的应用可以使参数处理更加灵活和可扩展，此部分详细介绍了策略模式的定义、实现及其在 MyBatis 中的具体应用。

详细内容请参考：[基于策略模式解析参数](http://www.huanyujoker.top/articles/175)

## 封装结果集处理器

自定义结果集处理器的实现方法。结果集处理器用于处理数据库查询返回的结果集，此部分介绍了如何自定义结果集处理器以满足特殊需求，并提供了具体的实现示例。

详细内容请参考：[封装结果集处理器](http://www.huanyujoker.top/articles/176)

## 增删改查操作

MyBatis 中常见的增删改查操作实现。此部分提供了详细的增删改查操作示例，包括如何在映射器中编写 SQL 语句，如何执行这些语句以及如何处理操作结果。

详细内容请参考：[增删改查操作](http://www.huanyujoker.top/articles/177)

## 注解配置执行 SQL 语句

使用注解配置和执行 SQL 语句的方法。MyBatis 支持通过注解方式配置 SQL 语句，此部分介绍了如何使用注解定义 SQL 语句，以及如何执行这些注解定义的语句。

详细内容请参考：[注解配置执行 SQL 语句](http://www.huanyujoker.top/articles/178)

## 解析 ResultMap 映射参数配置

使用 ResultMap 驼峰映射解析的方法。ResultMap 是 MyBatis 中用于映射查询结果到对象的配置，此部分详细介绍了如何定义和使用 ResultMap 进行驼峰映射解析。

详细内容请参考：[解析 ResultMap 映射参数配置](http://www.huanyujoker.top/articles/179)

## 解析动态 SQL 语句

脚本构建器中扩展动态 SQL 语句解析的方法。动态 SQL 是 MyBatis 的一个强大特性，允许根据条件动态生成 SQL 语句，此部分详细介绍了动态 SQL 的实现原理及其应用场景。

详细内容请参考：[解析动态 SQL 语句](http://www.huanyujoker.top/articles/180)

## Plugin 插件功能实现

插件配置实现性能监控和日志查询的方法。MyBatis 支持通过插件扩展其功能，此部分介绍了如何编写和配置插件以实现性能监控、日志查询等功能。

详细内容请参考：[Plugin 插件功能实现](http://www.huanyujoker.top/articles/181)

## 一级缓存实现

面向 SqlSession 的默认缓存机制的实现。一级缓存是 MyBatis 默认启用的缓存机制，此部分详细介绍了一级缓存的工作原理及其在实际应用中的效果。

详细内容请参考：[一级缓存实现](http://www.huanyujoker.top/articles/182)

## 二级缓存实现

面向 Mapper 的缓存机制的实现。二级缓存是 MyBatis 中另一个重要的缓存机制，此部分介绍了二级缓存的定义、配置和使用方法，以及它与一级缓存的区别。

详细内容请参考：[二级缓存实现](http://www.huanyujoker.top/articles/183)

## 贡献

如果您发现任何问题或有改进建议，欢迎提交 [issues](https://github.com/your-repo/issues) 或 [pull requests](https://github.com/your-repo/pulls)。
