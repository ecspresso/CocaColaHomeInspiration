package com.ecspresso.mau.reservation.time;

import java.time.LocalTime;

public enum Time {
    T0815_1000("08:15", 1),
    T1015_1300("10:15", 2),
    T1315_1500("13:15", 3),
    T1515_1700("15:15", 4),
    T1715_2000("17:15", 5);

    private final String name;
    private final LocalTime time;
    private final int timeslot;

    Time(String name, int timeslot) {
        this.name = name;
        time = LocalTime.parse(name);
        this.timeslot = timeslot;
    }

    @Override
    public String toString() {
        return name;
    }

    public LocalTime getTime() {
        return time;
    }

    public Time getPreviousSlot() {
        switch(this) {
            case T0815_1000, T1015_1300-> {return T0815_1000;}
            case T1315_1500 -> {return T1015_1300;}
            case T1515_1700 -> {return T1315_1500;}
            case T1715_2000 -> {return T1515_1700;}
            default -> {return null;}
        }
    }

    public static Time convertFromInt(int time) {
        switch(time) {
            case 1 -> {return T0815_1000;}
            case 2 -> {return T1015_1300;}
            case 3 -> {return T1315_1500;}
            case 4 -> {return T1515_1700;}
            case 5 -> {return T1715_2000;}
            default -> {throw new IllegalArgumentException(time + " is not a valid time number (1-5).");}
        }
    }

    public int getTimeslot() {
        return timeslot;
    }
}
