package com.huanyu.mybatis.scripting.xmltags;

/**
 * ClassName: SqlNode
 * Package: com.huanyu.mybatis.scripting.defaults.xmltags
 * Description: SQL 节点
 * 在mybatis中，当需要写动态的SQL语句时，<if></if>  <where></where> 这些就是sqlNode
 * @Author: 寰宇
 * @Create: 2024/6/17 21:48
 * @Version: 1.0
 */
public interface SqlNode {

    /**
     * 完成该节点自身的解析
     * @param context 上下文环境，节点自身的解析结果将合并到该上下文环境中
     * @return 解析是否成功
     */
    boolean apply(DynamicContext context);

}
