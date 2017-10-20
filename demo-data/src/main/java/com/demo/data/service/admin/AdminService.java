package com.demo.data.service.admin;

import com.demo.data.entity.AdminInfo;
import com.demo.data.mappers.admin.AdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhoujian on 2017/10/20.
 */
@Component
public class AdminService {
    @Autowired
    private AdminMapper adminMapper;

    public AdminInfo getAdmin(Long id){
        return adminMapper.selectAdminInfoById(id);
    }
}
