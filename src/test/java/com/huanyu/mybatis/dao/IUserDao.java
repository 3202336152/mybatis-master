package com.huanyu.mybatis.dao;

import com.huanyu.mybatis.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);

    User queryUserInfo(User req);

}
