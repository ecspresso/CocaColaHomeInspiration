package com.ecspresso.mau.reservation.room;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class Room implements Comparable<Room>{
    private final String name;
    private final String type;
    private final String seating;
    private final String building;

    public Room(String name, String type, String seating, String building) {
        this.name = name;
        this.type = type;
        this.seating = seating;
        this.building = building;
    }

    public String getHtmlString(String sDate) {

        return String.format("<td><a href=\"https://schema.mau.se/setup/jsp/Schema.jsp?startDatum=%s&intervallTyp=d&intervallAntal=1&resurser=l.%s\" target=\"_blank\"><b>%s</b></a><br><small>%s (%s)</small></td>",
                sDate, name, name, seating, type);
    }

    public String getDataTags() {
        return String.format(" data-type=\"%s\" data-platser=\"%s\" data-namn=\"%s\" data-byggnad=\"%s\"",
                type, seating, name, building);
    }

    public String getName() { return name; }

    public String getType() { return type; }

    public String getSeating() { return seating; }

    public String getBuilding() { return building; }

    @Override
    public String toString() {
        return name;
    }

    public static Comparator<Room> getMultiFieldComparator() {
        return Comparator.comparing(Room::getBuilding)
                .thenComparing(Room::getType)
                .thenComparing(Room::getName);
    }

    @Override
    public int compareTo(@NotNull Room room) {
        return getMultiFieldComparator().compare(room, this);
    }
}