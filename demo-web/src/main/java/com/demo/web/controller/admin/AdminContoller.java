package com.demo.web.controller.admin;

import com.demo.business.admin.AdminBusiness;
import com.demo.data.entity.AdminInfo;
import com.demo.web.controller.vo.ResultInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhoujian on 2017/10/20.
 */
@Controller
@RequestMapping(value = "/admin")
public class AdminContoller {
    @Autowired
    private AdminBusiness adminBusiness;

    @RequestMapping(value = "getAdmin", method = RequestMethod.GET)
    @ResponseBody
    public ResultInfo<AdminInfo> getAdmin(Long id, Model model, HttpServletRequest request)
    {
        ResultInfo<AdminInfo> result=new ResultInfo<>();
        AdminInfo adminInfo=adminBusiness.getAdmin(id);
        if (adminInfo==null)
            result.setExceptionData("获取失败");

        result.setSuccessData(adminInfo);

        return result;
    }
}
