package com.huanyu.mybatis.mapping;

import com.huanyu.mybatis.session.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * ClassName: ResultMap
 * Package: com.huanyu.mybatis.mapping
 * Description: 结果映射
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:28
 * @Version: 1.0
 */
public class ResultMap {

    // resultMap的编号
    private String id;
    // 最终输出结果对应的Java类
    private Class<?> type;
    // XML中的<result>的列表，即ResultMapping列表
    private List<ResultMapping> resultMappings;
    // 所有参与映射的数据库中字段的集合
    private Set<String> mappedColumns;

    // 私有构造函数：只能通过内部类 Builder 构建实例
    private ResultMap() {
    }

    // 用于构建 ResultMap 实例
    public static class Builder {
        private ResultMap resultMap = new ResultMap(); // 创建一个新的 ResultMap 实例

        // 构造函数：初始化 ResultMap 实例的 id、type 和 resultMappings
        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
        }

        // build 方法：设置 mappedColumns 并返回构建好的 ResultMap 实例
        public ResultMap build() {
            // 初始化 mappedColumns 集合
            resultMap.mappedColumns = new HashSet<>();
            // 遍历 resultMappings 列表
            for (ResultMapping resultMapping : resultMap.resultMappings) {
                // 获取列名
                final String column = resultMapping.getColumn();
                // 检查列名是否不为空
                if (column != null) {
                    // 将列名转换为大写并添加到 mappedColumns 集合中
                    resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                }
            }
            // 返回构建好的 resultMap
            return resultMap;
        }
    }

    public String getId() {
        return id;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    public List<ResultMapping> getPropertyResultMappings() {
        return resultMappings;
    }

}
