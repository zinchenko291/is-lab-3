package me.zinch.is.islab3.server.datasource;

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

    public void setTestOnBorrow(String value) {
        super.setTestOnBorrow(Boolean.parseBoolean(value));
    }

    public void setTestWhileIdle(String value) {
        super.setTestWhileIdle(Boolean.parseBoolean(value));
    }

    public void setTestOnReturn(String value) {
        super.setTestOnReturn(Boolean.parseBoolean(value));
    }

    public void setKeepAlive(String value) {
        super.setKeepAlive(Boolean.parseBoolean(value));
    }

    public void setValidationQueryTimeout(String seconds) {
        super.setValidationQueryTimeout(Integer.parseInt(seconds));
    }

    public void setKeepAliveBetweenTimeMillis(String millis) {
        super.setKeepAliveBetweenTimeMillis(Long.parseLong(millis));
    }

    public void setTimeBetweenEvictionRunsMillis(String millis) {
        super.setTimeBetweenEvictionRunsMillis(Long.parseLong(millis));
    }

    public void setMinEvictableIdleTimeMillis(String millis) {
        super.setMinEvictableIdleTimeMillis(Long.parseLong(millis));
    }

    public void setMaxWait(String millis) {
        super.setMaxWait(Long.parseLong(millis));
    }

    public void setLoginTimeout(String seconds) {
        super.setLoginTimeout(Integer.parseInt(seconds));
    }
}
