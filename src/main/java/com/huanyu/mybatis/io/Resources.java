package com.huanyu.mybatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * ClassName: Resources
 * Package: com.huanyu.mybatis.io
 * Description: 通过类加载器获得resource的辅助类
 *
 * @Author: 寰宇
 * @Create: 2024/6/12 14:50
 * @Version: 1.0
 */
public class Resources {

    /**
     * 获取指定资源的 Reader
     *
     * @param resource - 资源的路径
     * @return 用于读取资源的 Reader 对象
     * @throws IOException 如果资源未找到或读取失败
     */
    public static Reader getResourceAsReader(String resource) throws IOException {
        return new InputStreamReader(getResourceAsStream(resource));
    }

    /**
     * 获取指定资源的输入流
     *
     * @param resource - 资源的路径
     * @return 用于读取资源的 InputStream 对象
     * @throws IOException 如果资源未找到或读取失败
     */
    private static InputStream getResourceAsStream(String resource) throws IOException {
        ClassLoader[] classLoaders = getClassLoaders();
        for (ClassLoader classLoader : classLoaders) {
            InputStream inputStream = classLoader.getResourceAsStream(resource);
            if (null != inputStream) {
                return inputStream;
            }
        }
        throw new IOException("Could not find resource " + resource);
    }

    /**
     * 获取多个类加载器
     *
     * @return 包含系统类加载器和当前线程上下文类加载器的数组
     */
    private static ClassLoader[] getClassLoaders() {
        return new ClassLoader[]{
                ClassLoader.getSystemClassLoader(),
                Thread.currentThread().getContextClassLoader()};
    }

    /**
     * 加载一个类
     *
     * @param className - 类的名称
     * @return 加载的类对象
     * @throws ClassNotFoundException 如果找不到该类
     */
    public static Class<?> classForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
