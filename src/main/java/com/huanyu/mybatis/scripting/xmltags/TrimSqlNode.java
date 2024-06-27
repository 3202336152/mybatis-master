package com.huanyu.mybatis.scripting.xmltags;

import com.huanyu.mybatis.session.Configuration;

import java.util.*;

/**
 * ClassName: TrimSqlNode
 * Package: com.huanyu.mybatis.scripting.xmltags
 * Description: trim Sql Node 节点解析
 *
 * @Author: 寰宇
 * @Create: 2024/6/26 16:07
 * @Version: 1.0
 */
public class TrimSqlNode implements SqlNode {
    private SqlNode contents; // 包含的SQL节点
    private String prefix; // SQL前缀
    private String suffix; // SQL后缀
    private List<String> prefixesToOverride; // 要覆盖的前缀列表
    private List<String> suffixesToOverride; // 要覆盖的后缀列表
    private Configuration configuration;

    public TrimSqlNode(Configuration configuration, SqlNode contents, String prefix, String prefixesToOverride, String suffix, String suffixesToOverride) {
        this(configuration, contents, prefix, parseOverrides(prefixesToOverride), suffix, parseOverrides(suffixesToOverride));
    }

    protected TrimSqlNode(Configuration configuration, SqlNode contents, String prefix, List<String> prefixesToOverride, String suffix, List<String> suffixesToOverride) {
        this.contents = contents;
        this.prefix = prefix;
        this.prefixesToOverride = prefixesToOverride;
        this.suffix = suffix;
        this.suffixesToOverride = suffixesToOverride;
        this.configuration = configuration;
    }

    @Override
    public boolean apply(DynamicContext context) {
        FilteredDynamicContext filteredDynamicContext = new FilteredDynamicContext(context); // 创建过滤上下文
        boolean result = contents.apply(filteredDynamicContext); // 应用内容节点
        filteredDynamicContext.applyAll(); // 应用所有修改
        return result;
    }

    // 解析覆盖前缀和后缀的字符串为列表
    private static List<String> parseOverrides(String overrides) {
        if (overrides != null) { // 如果输入字符串不为null
            // 使用StringTokenizer按'|'分割字符串
            final StringTokenizer parser = new StringTokenizer(overrides, "|", false);
            // 初始化一个ArrayList，容量为分割后的令牌数量
            final List<String> list = new ArrayList<String>(parser.countTokens());
            // 遍历所有令牌
            while (parser.hasMoreTokens()) {
                // 将每个令牌转换为大写并添加到列表中
                list.add(parser.nextToken().toUpperCase(Locale.ENGLISH));
            }
            return list; // 返回解析后的列表
        }
        return Collections.emptyList(); // 如果输入字符串为null，返回空列表
    }

    private class FilteredDynamicContext extends DynamicContext {
        private DynamicContext delegate; // 委托的上下文
        private boolean prefixApplied; // 前缀是否已应用
        private boolean suffixApplied; // 后缀是否已应用
        private StringBuilder sqlBuffer; // SQL缓冲区

        public FilteredDynamicContext(DynamicContext delegate) {
            super(configuration, null);
            this.delegate = delegate;
            this.prefixApplied = false;
            this.suffixApplied = false;
            this.sqlBuffer = new StringBuilder();
        }

        // 应用所有修改
        public void applyAll() {
            sqlBuffer = new StringBuilder(sqlBuffer.toString().trim()); // 去除首尾空格
            String trimmedUppercaseSql = sqlBuffer.toString().toUpperCase(Locale.ENGLISH);
            if (trimmedUppercaseSql.length() > 0) {
                applyPrefix(sqlBuffer, trimmedUppercaseSql); // 应用前缀
                applySuffix(sqlBuffer, trimmedUppercaseSql); // 应用后缀
            }
            delegate.appendSql(sqlBuffer.toString()); // 将结果追加到委托上下文中
        }

        @Override
        public Map<String, Object> getBindings() {
            return delegate.getBindings();
        }

        @Override
        public void bind(String name, Object value) {
            delegate.bind(name, value);
        }

        @Override
        public int getUniqueNumber() {
            return delegate.getUniqueNumber();
        }

        @Override
        public void appendSql(String sql) {
            sqlBuffer.append(sql); // 将SQL追加到缓冲区
        }

        @Override
        public String getSql() {
            return delegate.getSql();
        }

        // 应用前缀
        private void applyPrefix(StringBuilder sql, String trimmedUppercaseSql) {
            if (!prefixApplied) {
                prefixApplied = true;
                if (prefixesToOverride != null) {
                    for (String toRemove : prefixesToOverride) {
                        if (trimmedUppercaseSql.startsWith(toRemove)) {
                            sql.delete(0, toRemove.trim().length());
                            break;
                        }
                    }
                }
                if (prefix != null) {
                    sql.insert(0, " ");
                    sql.insert(0, prefix); // 在开头插入前缀
                }
            }
        }

        // 应用后缀
        private void applySuffix(StringBuilder sql, String trimmedUppercaseSql) {
            if (!suffixApplied) { // 如果后缀尚未应用
                suffixApplied = true; // 标记后缀已应用
                if (suffixesToOverride != null) { // 如果有要覆盖的后缀列表
                    for (String toRemove : suffixesToOverride) { // 遍历后缀列表
                        // 如果当前SQL以指定的后缀或其修剪版结尾
                        if (trimmedUppercaseSql.endsWith(toRemove) || trimmedUppercaseSql.endsWith(toRemove.trim())) {
                            int start = sql.length() - toRemove.trim().length(); // 计算要删除的起始位置
                            int end = sql.length(); // 计算要删除的结束位置
                            sql.delete(start, end); // 删除指定的后缀
                            break; // 一旦找到匹配的后缀并删除，退出循环
                        }
                    }
                }
                if (suffix != null) { // 如果指定了后缀
                    sql.append(" "); // 在SQL末尾添加空格
                    sql.append(suffix); // 在结尾追加后缀
                }
            }
        }

    }
}