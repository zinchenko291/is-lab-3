package me.zinch.is.islab3.server.cache;

public class L2CacheStatsToggleRequestDto {
    private boolean enabled;

    public L2CacheStatsToggleRequestDto() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
