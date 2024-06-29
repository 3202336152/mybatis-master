package com.huanyu.mybatis.cache;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: CacheKey
 * Package: com.huanyu.mybatis.cache
 * Description: 缓存 Key，一般缓存框架的数据结构基本上都是 Key->Value 方式存储
 * MyBatis 对于其 Key 的生成采取规则为：[mappedStatementId + offset + limit + SQL + queryParams + environment]生成一个哈希码
 * @Author: 寰宇
 * @Create: 2024/6/28 16:55
 * @Version: 1.0
 */
public class CacheKey implements Cloneable, Serializable {

    private static final long serialVersionUID = 1146682552656046210L;

    public static final CacheKey NULL_CACHE_KEY = new NullCacheKey();

    private static final int DEFAULT_MULTIPLYER = 37;
    private static final int DEFAULT_HASHCODE = 17;

    // 乘数，用来计算hashcode时使用
    private int multiplier;
    // 哈希值，整个CacheKey的哈希值。如果两个CacheKey该值不同，则两个CacheKey一定不同
    private int hashcode;
    // 求和校验值，整个CacheKey的求和校验值。如果两个CacheKey该值不同，则两个CacheKey一定不同
    private long checksum;
    // 更新次数，整个CacheKey的更新次数
    private int count;
    // 更新历史
    private List<Object> updateList;

    public CacheKey() {
        this.hashcode = DEFAULT_HASHCODE;
        this.multiplier = DEFAULT_MULTIPLYER;
        this.count = 0;
        this.updateList = new ArrayList<>();
    }

    public CacheKey(Object[] objects) {
        this();
        updateAll(objects);
    }

    public int getUpdateCount() {
        return updateList.size();
    }

    /**
     * 更新CacheKey
     * @param object 此次更新的参数
     */
    public void update(Object object) {
        // 检查object是否不为null并且是一个数组
        if (object != null && object.getClass().isArray()) {
            // 获取数组的长度
            int length = Array.getLength(object);
            // 遍历数组的每一个元素
            for (int i = 0; i < length; i++) {
                Object element = Array.get(object, i);
                // 调用doUpdate方法更新每个元素
                doUpdate(element);
            }
        } else {
            // 如果object不是数组，直接调用doUpdate方法更新
            doUpdate(object);
        }
    }

    private void doUpdate(Object object) {
        // 计算Hash值，校验码
        int baseHashCode = object == null ? 1 : object.hashCode();
        // 更新计数器
        count++;
        // 更新校验和
        checksum += baseHashCode;
        // 乘以计数器更新基本哈希码
        baseHashCode *= count;
        // 更新哈希码
        hashcode = multiplier * hashcode + baseHashCode;
        // 将更新的对象添加到更新列表中
        updateList.add(object);
    }


    public void updateAll(Object[] objects) {
        for (Object o : objects) {
            update(o);
        }
    }

    /**
     * 比较当前对象和入参对象（通常也是CacheKey对象）是否相等
     * @param object 入参对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object object) {
        // 如果地址一样，是一个对象，肯定相等
        if (this == object) {
            return true;
        }
        // 如果入参不是CacheKey对象，肯定不相等
        if (!(object instanceof CacheKey)) {
            return false;
        }

        final CacheKey cacheKey = (CacheKey) object;
        // 依次通过hashcode、checksum、count判断。必须完全一致才相等
        if (hashcode != cacheKey.hashcode) {
            return false;
        }
        if (checksum != cacheKey.checksum) {
            return false;
        }
        if (count != cacheKey.count) {
            return false;
        }
        // 详细比较变更历史中的每次变更
        for (int i = 0; i < updateList.size(); i++) {
            Object thisObject = updateList.get(i);
            Object thatObject = cacheKey.updateList.get(i);
            if (thisObject == null) {
                if (thatObject != null) {
                    return false;
                }
            } else {
                if (!thisObject.equals(thatObject)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder().append(hashcode).append(':').append(checksum);
        for (Object obj : updateList) {
            returnValue.append(':').append(obj);
        }

        return returnValue.toString();
    }

    @Override
    public CacheKey clone() throws CloneNotSupportedException {
        CacheKey clonedCacheKey = (CacheKey) super.clone();
        clonedCacheKey.updateList = new ArrayList<>(updateList);
        return clonedCacheKey;
    }
}
