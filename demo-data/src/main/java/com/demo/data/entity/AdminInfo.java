package com.demo.data.entity;

import java.sql.Timestamp;

/**
 * Created by zhoujian on 2017/10/20.
 */
public class AdminInfo {

    private Long id;

    private Long userId;

    private String username;

    private Timestamp createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AdminInfo adminInfo = (AdminInfo) o;

        if (id != null ? !id.equals(adminInfo.id) : adminInfo.id != null) return false;
        if (userId != null ? !userId.equals(adminInfo.userId) : adminInfo.userId != null) return false;
        if (username != null ? !username.equals(adminInfo.username) : adminInfo.username != null) return false;
        return createTime != null ? createTime.equals(adminInfo.createTime) : adminInfo.createTime == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        return result;
    }
}
