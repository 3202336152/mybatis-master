package com.huanyu.mybatis.dao;

import com.huanyu.mybatis.po.User;

import java.util.List;

public interface IUserDao {

    User queryUserInfoById(Long uId);

    User queryUserInfo(User req);

    List<User> queryUserInfoList();

    int updateUserInfo(User req);

    void insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);

}
