package com.auction.models.dto;
import java.io.Serializable;
/**
 * The base interface for all data objects sent between the Client and Server.
 */
public interface NetworkMessage extends Serializable {
    long serialVersionUID = 1L;
    
    /**
     * Identifies the type of packet for efficient routing.
     */
    PacketType getType();
}