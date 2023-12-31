package com.ecspresso.mau.reservation.time;

import java.time.LocalTime;

public enum Time {
    T0815("08:15", 1),
    T1015("10:15", 2),
    T1315("13:15", 3),
    T1515("15:15", 4),
    T1715("17:15", 5);

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
            case T0815, T1015 -> {return T0815;}
            case T1315 -> {return T1015;}
            case T1515 -> {return T1315;}
            case T1715 -> {return T1515;}
            default -> {return null;}
        }
    }

    public static Time convertFromInt(int time) {
        switch(time) {
            case 1 -> {return T0815;}
            case 2 -> {return T1015;}
            case 3 -> {return T1315;}
            case 4 -> {return T1515;}
            case 5 -> {return T1715;}
            default -> {throw new IllegalArgumentException(time + " is not a valid time number (1-5).");}
        }
    }

    public boolean isAfter(LocalTime time) {
        return this.getTime().equals(time) || this.getTime().isAfter(time);
    }

    public boolean isBefore(LocalTime time) {
        return this.getTime().equals(time) || this.getTime().isBefore(time);
    }

    public int getTimeslot() {
        return timeslot;
    }
}
