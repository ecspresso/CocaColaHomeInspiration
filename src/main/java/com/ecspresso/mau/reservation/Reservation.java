package com.ecspresso.mau.reservation;

import com.ecspresso.mau.reservation.time.Time;

import java.util.HashMap;

public class Reservation {
    private final HashMap<Time, Booking> map = new HashMap<>();

    public Reservation() {
        for(Time time: Time.values()) {
            map.put(time, new Booking());
        }
    }

    public void setBooked(Time time) {
        map.get(time).setBooked();
    }

    public void setBooked(Time time, String text) {
        map.get(time).setBooked(text);
    }

    public void setVacant(Time time) {
        map.get(time).setUnknown();
    }

    public void setUnknown(Time time) {
        map.get(time).setUnknown();
    }

    public int getBooked(Time time) {
        return map.get(time).getBooked();
    }

    public String getText(Time time) {
        return map.get(time).getText();
    }
}
