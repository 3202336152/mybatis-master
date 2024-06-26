package com.huanyu.mybatis.builder;

import com.huanyu.mybatis.mapping.ResultMap;
import com.huanyu.mybatis.mapping.ResultMapping;

import java.util.List;

/**
 * ClassName: ResultMapResolver
 * Package: com.huanyu.mybatis.builder
 * Description: 结果映射解析器
 *
 * @Author: 寰宇
 * @Create: 2024/6/25 12:37
 * @Version: 1.0
 */
public class ResultMapResolver {

    private final MapperBuilderAssistant assistant;
    private String id;
    private Class<?> type;
    private List<ResultMapping> resultMappings;

    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, List<ResultMapping> resultMappings) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
    }

    public ResultMap resolve() {
        return assistant.addResultMap(this.id, this.type, this.resultMappings);
    }

}
