package me.zinch.is.islab2.server.context;

import jakarta.enterprise.context.SessionScoped;
import me.zinch.is.islab2.models.entities.User;

import java.io.Serializable;

@SessionScoped
public class CurrentUser implements Serializable {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
