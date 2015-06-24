package com.villcab.gastos.entitys;

import com.villcab.gastos.utils.model.Entity;

/**
 * Created by villcab on 24-06-15.
 */
public class Producto extends Entity {

    private String nombre;
    private Long createdBy;
    private Long createdAt;

    public Producto() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

}
