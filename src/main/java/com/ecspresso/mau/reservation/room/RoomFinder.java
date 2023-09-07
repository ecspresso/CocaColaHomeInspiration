package com.ecspresso.mau.reservation.room;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class RoomFinder {
    private final Logger logger = LoggerFactory.getLogger(RoomFinder.class);
    private static RoomFinder instance = null;
    private final ArrayList<Room> rooms = new ArrayList<>();
    private final HashSet<String> buildings = new HashSet<>(8);

    private RoomFinder() {
    }

    private static void init() {
        if(instance == null) {
            instance = new RoomFinder();
            instance.findRooms();
            instance.logger.info("Alla bokade rum: {}.", instance.rooms);
        }
    }

    public static synchronized ArrayList<Room> getRooms() {
        init();
        return instance.rooms;
    }

    public static List<String> getBuildings() {
        init();
        ArrayList<String> list = new ArrayList<>(instance.buildings);
        Collections.sort(list);
        return list;
    }

    private void findRooms() {
        try(final WebClient webClient = new WebClient(BrowserVersion.FIREFOX)) {
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setTimeout(3000);

            HtmlPage page;
            try {
                page = getPage(webClient, "https://schema.mau.se/ajax/ajax_resurser.jsp?op=hamtaResursDialog&resurstyp=RESURSER_LOKALER", 1, 10);
            } catch(IOException e) {
                logger.error("Kunde inte hämta listan med alla rum.", e);
                return;
            }
            List<HtmlTableRow> listOfRooms = page.getByXPath("//html/body/table/tbody/tr[td[contains(text(), 'Grupprum')] or td[contains(text(), 'Lärosal')]]");

            for(HtmlTableRow room : listOfRooms) {
                String name = room.getCell(1).getTextContent().trim();
                String type = room.getCell(2).getTextContent().trim();
                String seating = room.getCell(4).getTextContent().trim();
                String building = room.getCell(5).getTextContent().trim();
                rooms.add(new Room(name, type, seating, building));
                buildings.add(building);
                logger.info("Lägger till rum {}.", name);
            }
        }
    }

    private HtmlPage getPage(@NotNull WebClient webClient, String url, int thisTry, int maxTries) throws IOException {
        try {
            logger.info("Hämtar alla rum ({} av {}).", thisTry, maxTries);
            return webClient.getPage(url);
        } catch (IOException e) {
            if(thisTry == maxTries) {
                throw e;
            }

            thisTry++;
            return getPage(webClient, url, thisTry, maxTries);
        }
    }
}
