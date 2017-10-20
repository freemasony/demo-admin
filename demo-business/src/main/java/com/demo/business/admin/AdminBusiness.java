package com.demo.business.admin;

import com.demo.data.entity.AdminInfo;
import com.demo.data.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by zhoujian on 2017/10/20.
 */
@Component
public class AdminBusiness {
    @Autowired
    private AdminService adminService;

    public AdminInfo getAdmin(Long id){
        return adminService.getAdmin(id);
    }
}
