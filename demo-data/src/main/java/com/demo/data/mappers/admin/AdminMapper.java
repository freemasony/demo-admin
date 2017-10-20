package com.demo.data.mappers.admin;

import com.demo.data.entity.AdminInfo;

/**
 * Created by zhoujian on 2017/10/20.
 */
public interface AdminMapper {

    AdminInfo selectAdminInfoById(Long id);
}
