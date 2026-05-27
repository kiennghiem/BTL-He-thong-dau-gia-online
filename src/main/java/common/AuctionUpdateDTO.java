package common;

import common.NetworkMessage;

/**
 * DTO sent from Server to all Clients to broadcast changes in an auction.
 * This facilitates "Realtime Update" and "Anti-sniping" visibility.
 */
public class AuctionUpdateDTO implements NetworkMessage {
    // Encapsulation: Private fields for the updated auction state
    private final String auctionId;
    private final double currentHighestBid;
    private final String leadingBidderName;
    private final long endTimeMillis; // Updated end time (crucial for Anti-sniping)
    private final String status;      // e.g., RUNNING, FINISHED

    /**
     * Constructor for the broadcast message.
     *
     * @param auctionId The ID of the auction being updated.
     * @param currentHighestBid The new price to be displayed[cite: 1].
     * @param leadingBidderName The name of the current winner[cite: 1].
     * @param endTimeMillis The current end time (including any extensions)[cite: 1].
     * @param status The current state of the auction[cite: 1].
     */
    public AuctionUpdateDTO(String auctionId, double currentHighestBid,
                            String leadingBidderName, long endTimeMillis, String status) {
        this.auctionId = auctionId;
        this.currentHighestBid = currentHighestBid;
        this.leadingBidderName = leadingBidderName;
        this.endTimeMillis = endTimeMillis;
        this.status = status;
    }

    // --- Getters for JavaFX UI Binding[cite: 1] ---

    public String getAuctionId() {
        return auctionId;
    }

    public double getCurrentHighestBid() {
        return currentHighestBid;
    }

    public String getLeadingBidderName() {
        return leadingBidderName;
    }

    public long getEndTimeMillis() {
        return endTimeMillis;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("Update[ID: %s, Price: %.2f, Leader: %s, Ends: %d]",
                auctionId, currentHighestBid, leadingBidderName, endTimeMillis);
    }
}