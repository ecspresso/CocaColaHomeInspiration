package com.ecspresso.mau.reservation;

public class Booking {
    private int booked = 0;
    private String text = "";

    public void setBooked() {
        booked = 1;
    }

    public void setBooked(String text) {
        booked = 1;
        this.text = text;
    }

    public void setVacant() {
        booked = 0;
    }

    public void setUnknown() {
        booked = -1;
    }

    public int getBooked() {
        return booked;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
