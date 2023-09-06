package com.ecspresso.mau.reservation;

import com.ecspresso.mau.reservation.room.Room;
import com.ecspresso.mau.reservation.room.RoomFinder;
import com.ecspresso.mau.reservation.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class Schedule {
    private final Logger logger = LoggerFactory.getLogger(Schedule.class);
    private final HashMap<Room, Reservation> map = new HashMap<>();

    public Schedule() {
        logger.info("Skapar en lista med reservationer.");
        for(Room room : RoomFinder.getRooms()) {
            map.put(room, new Reservation());
        }
    }

    public void setBooked(Room room, Time time) {
        logger.info("Ändrar {} till bokad kl {}.", room, time);
        map.get(room).setBooked(time);
    }

    public void setBooked(Room room, Time time, String text) {
        logger.info("Ändrar {} till bokad kl {} med texten {}.", room, time, text);
        map.get(room).setBooked(time, text);
    }

    public void setVacant(Room room, Time time) {
        map.get(room).setVacant(time);
    }

    public void setUnknown(Room room, Time time) {
        logger.info("Ändrar {} till okänd kl {}.", room, time);
        map.get(room).setUnknown(time);
    }

    public int isBooked(Room room, Time time) {
        return map.get(room).getBooked(time);
    }

    public String getText(Room room, Time time) {
        return map.get(room).getText(time);
    }
}
