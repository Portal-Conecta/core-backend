package com.portal.conecta.hub.module.classes.domain.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "classes")
public class ClassEntity {

    @Id
    private UUID id;

    @Column(name = "active", nullable = false)
    private boolean active;

    protected ClassEntity() {
    }

    public ClassEntity(UUID id, boolean active) {
        this.id = id;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }
}
