package com.huanyu.mybatis.scripting.xmltags;

import com.huanyu.mybatis.io.Resources;
import ognl.ClassResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: OgnlClassResolver
 * Package: com.huanyu.mybatis.scripting.xmltags
 * Description:
 *
 * @Author: 寰宇
 * @Create: 2024/6/26 16:02
 * @Version: 1.0
 */
public class OgnlClassResolver implements ClassResolver {

    private Map<String, Class<?>> classes = new HashMap<String, Class<?>>(101);

    @Override
    public Class classForName(String className, Map context) throws ClassNotFoundException {
        Class<?> result = null;
        if ((result = classes.get(className)) == null) {
            try {
                result = Resources.classForName(className);
            } catch (ClassNotFoundException e1) {
                if (className.indexOf('.') == -1) {
                    result = Resources.classForName("java.lang." + className);
                    classes.put("java.lang." + className, result);
                }
            }
            classes.put(className, result);
        }
        return result;
    }
}
