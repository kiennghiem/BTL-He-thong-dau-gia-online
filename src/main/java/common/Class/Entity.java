package main.java.common.Class;

import java.io.Serializable;
import java.util.UUID;  

public abstract class Entity implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String id;
    
    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}