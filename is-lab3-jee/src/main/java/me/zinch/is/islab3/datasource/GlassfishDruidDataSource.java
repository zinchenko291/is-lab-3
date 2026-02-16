package me.zinch.is.islab3.datasource;

import com.alibaba.druid.pool.DruidDataSource;

public class GlassfishDruidDataSource extends DruidDataSource {
    public void setUser(String user) {
        setUsername(user);
    }

    public String getUser() {
        return getUsername();
    }
}
