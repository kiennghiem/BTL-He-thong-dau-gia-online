package com.auction.models.dto;
import java.io.Serializable;
/**
 * The base interface for all data objects sent between the Client and Server.
 */
public interface NetworkMessage extends Serializable {
    long serialVersionUID = 1L;
}