package com.auction.util;

import com.auction.models.*;
import com.auction.models.dto.*;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * Shared utility for JSON serialization and deserialization using GSON.
 * Handles custom types, polymorphic objects (User, Item), and collections.
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final Gson gson;
    private static final Gson baseGson; // Used inside adapters to avoid recursion
    private static final Map<String, Class<?>> typeMap = new HashMap<>();

    static {
        // Base GSON for simple types and internal adapter use
        baseGson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) 
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) 
                    (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();

        // Main GSON with polymorphic support
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) 
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) 
                    (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .registerTypeAdapter(User.class, new UserAdapter())
                .registerTypeAdapter(Bidder.class, new UserAdapter())
                .registerTypeAdapter(Seller.class, new UserAdapter())
                .registerTypeAdapter(Admin.class, new UserAdapter())
                .registerTypeAdapter(Item.class, new ItemAdapter())
                .registerTypeAdapter(Art.class, new ItemAdapter())
                .registerTypeAdapter(Electronics.class, new ItemAdapter())
                .registerTypeAdapter(Vehicle.class, new ItemAdapter())
                .create();

        initializeTypeMap();
    }

    private static void initializeTypeMap() {
        // Requests
        typeMap.put("LOGIN", LoginRequest.class);
        typeMap.put("LOGOUT", LogoutRequest.class);
        typeMap.put("REGISTER", RegisterRequest.class);
        typeMap.put("BID", BidRequest.class);
        typeMap.put("PAY", PayRequest.class);
        typeMap.put("DEPOSIT", DepositRequest.class);
        typeMap.put("CREATE_AUCTION", CreateAuctionRequest.class);
        typeMap.put("SUBSCRIBE", SubscribeRequest.class);
        typeMap.put("GET_ACTIVE", GetActiveAuctionsRequest.class);
        typeMap.put("CANCEL", CancelAuctionRequest.class);
        typeMap.put("APPROVE", ApproveAuctionRequest.class);
        typeMap.put("END_EARLY", EndAuctionEarlyRequest.class);

        // Responses / Notifications
        typeMap.put("AUTH_RESPONSE", AuthResponse.class);
        typeMap.put("GENERIC_RESPONSE", GenericResponse.class);
        typeMap.put("NOTIFICATION", Notification.class);
        typeMap.put("AUCTION_LIST_RESPONSE", AuctionListResponse.class);
        typeMap.put("AUCTION_UPDATE", AuctionUpdateDTO.class);
    }

    public static String toJson(Object obj) {
        if (obj == null) return null;
        
        JsonElement tree = gson.toJsonTree(obj);
        if (tree.isJsonObject()) {
            JsonObject jsonObject = tree.getAsJsonObject();
            String typeName = getTypeName(obj);
            if (typeName != null) {
                jsonObject.addProperty("type", typeName);
            }
            return gson.toJson(jsonObject);
        }
        return gson.toJson(tree);
    }

    private static String getTypeName(Object obj) {
        for (Map.Entry<String, Class<?>> entry : typeMap.entrySet()) {
            if (entry.getValue().isInstance(obj)) {
                return entry.getKey();
            }
        }
        if (obj instanceof Notification) return "NOTIFICATION";
        return null;
    }

    public static Object fromJson(String json) {
        try {
            JsonElement element = JsonParser.parseString(json);
            
            // 1. Handle JSON Arrays (Lists of Auctions)
            if (element.isJsonArray()) {
                Type listType = new com.google.gson.reflect.TypeToken<ArrayList<Auction>>(){}.getType();
                return gson.fromJson(element, listType);
            }

            // 2. Handle JSON Objects (NetworkMessages)
            if (element.isJsonObject()) {
                JsonObject root = element.getAsJsonObject();
                if (!root.has("type")) {
                    return null;
                }

                String type = root.get("type").getAsString().toUpperCase();
                Class<?> targetClass = typeMap.get(type);

                if (targetClass == null) {
                    return null;
                }

                return gson.fromJson(json, targetClass);
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error deserializing JSON: {}", e.getMessage());
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return gson.fromJson(json, clazz);
    }

    /**
     * Custom adapter for User polymorphism in JSON.
     */
    private static class UserAdapter implements JsonSerializer<User>, JsonDeserializer<User> {
        @Override
        public JsonElement serialize(User src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = baseGson.toJsonTree(src).getAsJsonObject();
            result.addProperty("userClass", src.getClass().getSimpleName());
            return result;
        }

        @Override
        public User deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement userClassElem = jsonObject.get("userClass");
            String userClass = (userClassElem != null) ? userClassElem.getAsString() : null;
            
            if (userClass == null && jsonObject.has("role")) {
                userClass = jsonObject.get("role").getAsString();
            }

            if (userClass == null) return null;

            if ("Bidder".equalsIgnoreCase(userClass)) {
                return baseGson.fromJson(json, Bidder.class);
            } else if ("Seller".equalsIgnoreCase(userClass)) {
                return baseGson.fromJson(json, Seller.class);
            } else if ("Admin".equalsIgnoreCase(userClass)) {
                return baseGson.fromJson(json, Admin.class);
            }
            return null;
        }
    }

    /**
     * Custom adapter for Item polymorphism in JSON.
     */
    private static class ItemAdapter implements JsonSerializer<Item>, JsonDeserializer<Item> {
        @Override
        public JsonElement serialize(Item src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = baseGson.toJsonTree(src).getAsJsonObject();
            result.addProperty("itemClass", src.getClass().getSimpleName());
            return result;
        }

        @Override
        public Item deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement itemClassElem = jsonObject.get("itemClass");
            if (itemClassElem == null) return null;
            
            String itemClass = itemClassElem.getAsString();
            if ("Art".equalsIgnoreCase(itemClass)) {
                return baseGson.fromJson(json, Art.class);
            } else if ("Electronics".equalsIgnoreCase(itemClass)) {
                return baseGson.fromJson(json, Electronics.class);
            } else if ("Vehicle".equalsIgnoreCase(itemClass)) {
                return baseGson.fromJson(json, Vehicle.class);
            }
            return null;
        }
    }
}
