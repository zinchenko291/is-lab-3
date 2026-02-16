package me.zinch.is.islab3.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import me.zinch.is.islab3.server.cache.InfinispanEclipseLinkCacheInterceptor;
import org.eclipse.persistence.annotations.CacheInterceptor;

import java.io.Serializable;

@Entity
@CacheInterceptor(InfinispanEclipseLinkCacheInterceptor.class)
@Table(name = "users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Column(nullable = false)
    @NotBlank
    private String pubkey;

    @Column(nullable = false)
    @NotBlank
    private String email;

    @Column(nullable = false)
    private boolean isAdmin;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public @NotBlank String getName() {
        return name;
    }

    public void setName(@NotBlank String name) {
        this.name = name;
    }

    public @NotBlank String getPubkey() {
        return pubkey;
    }

    public void setPubkey(@NotBlank String pubkey) {
        this.pubkey = pubkey;
    }

    public @NotBlank String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank String email) {
        this.email = email;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
