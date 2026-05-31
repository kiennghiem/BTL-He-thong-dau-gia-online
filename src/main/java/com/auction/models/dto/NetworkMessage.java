package com.auction.models.dto;
import java.io.Serializable;
/**
 * The base interface for all data objects sent between the Client and Server.
 */
public interface NetworkMessage extends Serializable {
    /**
     * serialVersionUID ensures that both the sender and receiver
     * are using compatible versions of the class during serialization.
     */
    long serialVersionUID = 1L;
}