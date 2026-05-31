
package com.auction.server;

import com.auction.server.network.AuctionServer;
import com.auction.util.DatabaseSetup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApp {
    private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);

    public static void main( String[] args){
        try {
            DatabaseSetup dbSetup = new DatabaseSetup();
            dbSetup.initDatabase();
        } catch (Exception e) {
            logger.error("Error during database initialization", e);
        }
        AuctionServer.main(args);
    }
}