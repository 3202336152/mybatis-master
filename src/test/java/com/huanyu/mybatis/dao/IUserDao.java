package com.huanyu.mybatis.dao;

public interface IUserDao {

    String queryUserName(String uId);

    Integer queryUserAge(String uId);

    String queryUserInfoById(String uId);

}
