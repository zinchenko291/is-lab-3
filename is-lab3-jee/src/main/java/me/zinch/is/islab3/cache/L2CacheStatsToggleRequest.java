package me.zinch.is.islab3.cache;

public class L2CacheStatsToggleRequest {
    private boolean enabled;

    public L2CacheStatsToggleRequest() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
