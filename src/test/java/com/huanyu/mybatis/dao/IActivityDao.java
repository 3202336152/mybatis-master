package com.huanyu.mybatis.dao;


import com.huanyu.mybatis.po.Activity;

public interface IActivityDao {

    Activity queryActivityById(Long activityId);

}
