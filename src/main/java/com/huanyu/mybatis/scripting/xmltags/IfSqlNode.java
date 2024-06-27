package com.huanyu.mybatis.scripting.xmltags;

/**
 * ClassName: IfSqlNode
 * Package: com.huanyu.mybatis.scripting.xmltags
 * Description: IF SQL 节点
 * <if>节点
 * @Author: 寰宇
 * @Create: 2024/6/26 15:53
 * @Version: 1.0
 */
public class IfSqlNode implements SqlNode{

    // 表达式评估器
    private ExpressionEvaluator evaluator;
    // if判断时的测试条件
    private String test;
    // if成立时，要被拼接的SQL片段信息
    private SqlNode contents;

    public IfSqlNode(SqlNode contents, String test) {
        this.test = test;
        this.contents = contents;
        this.evaluator = new ExpressionEvaluator();
    }

    /**
     * 完成该节点自身的解析
     * @param context 上下文环境，节点自身的解析结果将合并到该上下文环境中
     * @return 解析是否成功
     */
    @Override
    public boolean apply(DynamicContext context) {
        // 如果满足条件，则apply，并返回true
        // 判断if条件是否成立
        if (evaluator.evaluateBoolean(test, context.getBindings())) {
            // 将contents拼接到context
            contents.apply(context);
            return true;
        }
        return false;
    }
}
