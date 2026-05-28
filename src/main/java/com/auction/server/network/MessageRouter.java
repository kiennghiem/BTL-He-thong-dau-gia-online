package com.auction.server.network;

import com.auction.models.dto.*;
import com.auction.util.LocalDateTimeAdapter;
import com.google.gson.*;

import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Centralized Router for NetworkMessages.
 * Maps PacketType to specific handler logic, avoiding sequential instanceof checks.
 */
public class MessageRouter {
    private final UserHandler userHandler;
    private final AuctionHandler auctionHandler;
    private final Map<PacketType, Consumer<NetworkMessage>> routes;
    private final Gson gson;

    public MessageRouter(PrintWriter out) {
        // We use PrintWriter for JSON strings instead of ObjectOutputStream
        this.userHandler = new UserHandler(out);
        this.auctionHandler = new AuctionHandler(out);
        this.routes = new HashMap<>();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        registerRoutes();
    }

    private void registerRoutes() {
        // User Routes
        routes.put(PacketType.LOGIN, userHandler::handleLogin);
        routes.put(PacketType.LOGOUT, userHandler::handleLogout);
        routes.put(PacketType.REGISTER, userHandler::handleRegister);

        // Auction Routes
        routes.put(PacketType.BID, auctionHandler::handleBid);
        routes.put(PacketType.CREATE_AUCTION, auctionHandler::handleCreateAuction);
        routes.put(PacketType.GET_ACTIVE_AUCTIONS, auctionHandler::handleGetActiveAuctions);
        routes.put(PacketType.GET_BID_HISTORY, auctionHandler::handleGetBidHistory);
        routes.put(PacketType.GET_SELLER_ITEMS, auctionHandler::handleGetSellerItems);
        routes.put(PacketType.PAYMENT, auctionHandler::handlePayment);
        routes.put(PacketType.DELETE_ITEM, auctionHandler::handleDeleteAuction);
    }

    /**
     * Entry point for JSON strings from the network.
     */
    public void handleJson(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if (!jsonObject.has("type")) return;

            PacketType type = PacketType.valueOf(jsonObject.get("type").getAsString());
            NetworkMessage msg = (NetworkMessage) gson.fromJson(json, getDtoClass(type));

            if (msg != null) {
                handle(msg);
            }
        } catch (Exception e) {
            System.err.println("[Router] Error parsing JSON: " + e.getMessage());
        }
    }

    private Class<?> getDtoClass(PacketType type) {
        return switch (type) {
            case LOGIN -> LoginRequest.class;
            case LOGOUT -> LogoutRequest.class;
            case REGISTER -> RegisterRequest.class;
            case BID -> BidRequest.class;
            case CREATE_AUCTION -> CreateAuctionRequest.class;
            case GET_ACTIVE_AUCTIONS -> GetActiveAuctionsRequest.class;
            case GET_BID_HISTORY -> GetBidHistoryRequest.class;
            case GET_SELLER_ITEMS -> GetSellerItemsRequest.class;
            case PAYMENT -> PaymentRequest.class;
            case DELETE_ITEM -> DeleteItemRequest.class;
            default -> null;
        };
    }

    public void handle(NetworkMessage msg) {
        PacketType type = msg.getType();
        Consumer<NetworkMessage> handler = routes.get(type);
        
        if (handler != null) {
            handler.accept(msg);
        } else {
            System.err.println("[Router] No route defined for PacketType: " + type);
        }
    }

    public void cleanUp() {
        userHandler.cleanUp();
        auctionHandler.cleanUp();
    }
}
