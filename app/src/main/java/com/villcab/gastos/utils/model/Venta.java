package com.villcab.gastos.utils.model;

/**
 * Created by villcab on 22-06-15.
 */
public class Venta extends Entity {

    private String nombre;
    private Long fecha;

    public Venta() {

    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Long getFecha() {
        return fecha;
    }

    public void setFecha(Long fecha) {
        this.fecha = fecha;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return "Venta{" +
                "nombre='" + nombre + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
