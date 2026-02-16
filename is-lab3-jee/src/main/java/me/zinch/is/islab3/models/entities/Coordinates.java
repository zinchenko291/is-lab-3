package me.zinch.is.islab3.models.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import org.eclipse.persistence.annotations.CacheInterceptor;
import me.zinch.is.islab3.server.cache.InfinispanEclipseLinkCacheInterceptor;

@Entity
@CacheInterceptor(InfinispanEclipseLinkCacheInterceptor.class)
@Table(name = "coordinates", uniqueConstraints = { @UniqueConstraint(columnNames = { "x", "y" } ) })
public class Coordinates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private double x;

    @Column(nullable = false)
    @NotNull
    @Max(910)
    private Double y;

    @ManyToOne
    @JoinColumn(name = "ownerId", nullable = false)
    @NotNull
    private User owner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public @NotNull Double getY() {
        return y;
    }

    public void setY(@NotNull Double y) {
        this.y = y;
    }

    public @NotNull User getOwner() {
        return owner;
    }

    public void setOwner(@NotNull User owner) {
        this.owner = owner;
    }
}
