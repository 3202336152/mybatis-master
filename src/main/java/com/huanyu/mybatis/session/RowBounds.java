package com.huanyu.mybatis.session;

/**
 * ClassName: RowBounds
 * Package: com.huanyu.mybatis.session
 * Description: 分页记录限制
 *
 * @Author: 寰宇
 * @Create: 2024/6/20 16:43
 * @Version: 1.0
 */
public class RowBounds {

    public static final int NO_ROW_OFFSET = 0;
    public static final int NO_ROW_LIMIT = Integer.MAX_VALUE;
    public static final RowBounds DEFAULT = new RowBounds();

    // offset,limit就等于一般分页的start,limit,
    private int offset;
    private int limit;

    // 默认是一页Integer.MAX_VALUE条
    public RowBounds() {
        this.offset = NO_ROW_OFFSET;
        this.limit = NO_ROW_LIMIT;
    }

    public RowBounds(int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

}
