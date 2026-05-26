package com.auction.models;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public abstract class Entity implements Serializable {

    private static final long serialVersionUID = 1L;
    protected String id;

    // Create an instance of a new entity
    public Entity() {
        this.id = UUID.randomUUID().toString();
    }

    // Create an instance of an existed entity from the database
    public Entity(String id) {
        this.id = id;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    /**
     * Two entities are equal only if they have the same ID.
     * Prevents the situation where two Entity instances are created from the same entity (ex: a Bidder) in the database
     * but calling equals() returns false since by default, Java compares them by their memory location, not ID.
     */
    @Override
    public boolean equals(Object other) {
        // Check if two variables points to the same object.
        if (this == other) return true;
        // Check if two variables points to objects of the same class.
        if (other == null || this.getClass() != other.getClass()) return false;
        Entity entityOther = (Entity) other;
        return this.getId().equals(entityOther.getId());
    }

    // Use id to make the entity's hashCode unique when, for example, the entity is in a HashMap.
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}