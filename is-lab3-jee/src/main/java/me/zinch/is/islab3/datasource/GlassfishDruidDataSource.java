package me.zinch.is.islab3.datasource;

import com.alibaba.druid.pool.DruidDataSource;

public class GlassfishDruidDataSource extends DruidDataSource {
    public void setUser(String user) {
        setUsername(user);
    }

    public String getUser() {
        return getUsername();
    }

    public void setTimeBetweenConnectErrorMillis(String millis) {
        super.setTimeBetweenConnectErrorMillis(Long.parseLong(millis));
    }

    public void setConnectionErrorRetryAttempts(String attempts) {
        super.setConnectionErrorRetryAttempts(Integer.parseInt(attempts));
    }

    public void setBreakAfterAcquireFailure(String value) {
        super.setBreakAfterAcquireFailure(Boolean.parseBoolean(value));
    }
}
