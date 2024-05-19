package com.ecspresso.mau.reservation.time;

import java.time.LocalTime;

public enum Time {
    T0815_1000("08:15", "10:00"),
    T1015_1300("10:15", "13:00"),
    T1315_1500("13:15", "15:00"),
    T1515_1700("15:15", "17:00"),
    T1715_2000("17:15", "20:00");

    private final String name;
    private final LocalTime start;
    private final LocalTime end;

    Time(String start, String end) {
        this.name = start;
        this.start = LocalTime.parse(start);
        this.end = LocalTime.parse(end);
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isInSlot(LocalTime otherStart, LocalTime otherEnd) {
//            if((otherStart.isBefore(start) || otherStart.equals(start)) && (otherEnd.equals(end) || otherEnd.isAfter(end))) return true;
            if((otherStart.isBefore(start) || otherStart.equals(start)) && otherEnd.isAfter(start)) return true;
            else return otherStart.isBefore(end) && (otherEnd.isAfter(end) || otherEnd.equals(end));
        }
}
