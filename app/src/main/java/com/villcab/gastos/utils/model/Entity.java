package com.villcab.gastos.utils.model;

import java.io.Serializable;

import android.util.Log;

import com.villcab.gastos.utils.App;
import com.villcab.gastos.utils.model.annotations.Ignore;
import com.villcab.gastos.utils.model.annotations.Key;

public class Entity implements Serializable, Cloneable {

    @Key
    protected Long id;

    @Ignore
    private Action action = Action.NONE;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action value) {
        this.action = value;
    }

    public <T extends Entity> T getClone() {

        Entity obj = null;
        try {
            obj = (Entity) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(App.TAG, e.getMessage());
        }
        return (T) obj;
    }

    public <T extends Entity> T getMe() {
        return (T) this;
    }

}
