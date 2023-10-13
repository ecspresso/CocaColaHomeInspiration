package com.ecspresso.mau;


import com.ecspresso.mau.reservation.Schedule;
import com.ecspresso.mau.reservation.room.Room;
import com.ecspresso.mau.reservation.room.RoomFinder;
import com.ecspresso.mau.reservation.time.Time;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlNoBreak;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class MAU {
    private final Logger logger = LoggerFactory.getLogger(MAU.class);
    private final String template;
    private final String outputLocation;
    private final String htmlFileName;

    public MAU(String template, String htmlFileName, @NotNull String outputLocation) {
        this.template = template;
        this.outputLocation = outputLocation;
        this.htmlFileName = htmlFileName;
    }

    public void run() {
        logger.info("Letar efter bokade rum.");
        Schedule schedule = new Schedule();
        findPrebookedRooms(schedule);

        logger.info("Skapar tidstämplar.");
        LocalDateTime now = LocalDateTime.now();
        StringBuilder timestamp = new StringBuilder(18);
        timestamp.append(now.toLocalDate().toString()).append("T");
        int hourValue = now.getHour();
        int minuteValue = now.getMinute();
        int secondValue = now.getSecond();
        timestamp.append(hourValue < 10 ? "0" : "").append(hourValue);
        timestamp.append(minuteValue < 10 ? ":0" : ":").append(minuteValue);
        timestamp.append(secondValue < 10 ? ":0" : ":").append(secondValue);

        logger.info("Skapar HTML filer.");
        createHtmlFiles(schedule, outputLocation+htmlFileName, timestamp.toString());
    }

    private void findPrebookedRooms(Schedule map) {
        for(Room room : RoomFinder.getRooms()) {
            String url = String.format("https://schema.mau.se/setup/jsp/Schema.jsp?startDatum=idag&intervallTyp=d&intervallAntal=1&resurser=l.%s", room);
            updateMap(map, room, url);
        }
    }

    private void updateMap(Schedule schedule, Room room, String url) {
        try(final WebClient webClient = new WebClient(BrowserVersion.FIREFOX)) {
            webClient.getOptions().setJavaScriptEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setTimeout(3000);

            HtmlPage page;
            try {
                page = getPage(webClient, url, 1, 10, room);
            } catch(IOException e) {
                setUnknown(schedule, room);
                logger.error("Kunde inte hämta data för rum {}.", room, e);
                return;
            }


            List<HtmlNoBreak> takenTimes = page.getByXPath("//nobr");

            for (HtmlNoBreak timeTxt : takenTimes) {
                LocalTime start = LocalTime.parse(timeTxt.getTextContent().substring(0, 5));
                LocalTime end = LocalTime.parse(timeTxt.getTextContent().substring(6, 11));
                logger.info("Start: {} - End: {}", start, end);

                if (start.isBefore(Time.T1015.getTime())) {
                    setBooked(schedule, room, Time.T0815, String.format("%s - %s", start, end));

                    Time[] times = {Time.T1015, Time.T1315, Time.T1515};

                    for (Time time : times) {
                        if (end.isAfter(time.getTime())) {
                            setBooked(schedule, room, time, String.format("%s - %s", start, end));
                        }
                    }
                }

                if (Time.T1015.isBefore(start) && start.isBefore(Time.T1315.getTime())) {
                    setBooked(schedule, room, Time.T1015, String.format("%s - %s", start, end));

                    Time[] times = {Time.T1315, Time.T1515};

                    for (Time time : times) {
                        if (end.isAfter(time.getTime())) {
                            setBooked(schedule, room, time, String.format("%s - %s", start, end));
                        }
                    }
                }

                if (Time.T1315.isBefore(start) && start.isBefore(Time.T1515.getTime())) {
                    setBooked(schedule, room, Time.T1315, String.format("%s - %s", start, end));

                    if (end.isAfter(Time.T1515.getTime())) {
                        setBooked(schedule, room, Time.T1515, String.format("%s - %s", start, end));
                    }
                }

                if (Time.T1515.isBefore(start) && start.isBefore(Time.T1715.getTime())) {
                    setBooked(schedule, room, Time.T1515, String.format("%s - %s", start, end));
                }

                if (end.isAfter(Time.T1715.getTime())) {
                    setBooked(schedule, room, Time.T1715, String.format("%s - %s", start, end));
                }
            }
        }
    }


    private HtmlPage getPage(@NotNull WebClient webClient, String url, int thisTry, int maxTries, @NotNull Room room) throws IOException {
        try {
            logger.info("Hämtar data för {} ({} av {}).", room.getName(), thisTry, maxTries);
            return webClient.getPage(url);
        } catch (IOException e) {
            if(thisTry == maxTries) {
                throw e;
            }

            thisTry++;
            return getPage(webClient, url, thisTry, maxTries, room);
        }
    }

    private void setUnknown(Schedule schedule, Room room) {
        for(Time time : Time.values()) {
            logger.warn("Sätter rum {} kl {} till okänd.", room, time);
            schedule.setUnknown(room, time);
        }
    }

    private void setBooked(@NotNull Schedule schedule, @NotNull Room room, Time time, String text) {
        // logger.info("{} är reserverad kl {} {}.", room, text, date);
        schedule.setBooked(room, time, text);
    }

    private void createHtmlFiles(Schedule schedule, String outFileName, String timestamp) {
        logger.info("Skapar {}.", outFileName);

        StringBuilder buttons = new StringBuilder();
        StringBuilder filterKeys = new StringBuilder();

        for(String building: RoomFinder.getBuildings()) {
            buttons.append(String.format("            <button onclick=\"toggleFilter(this, '%s')\">%s</button>", building, building)).append("\n");
            filterKeys.append("\"").append(building).append("\",");
        }

        if(buttons.lastIndexOf("\n") != -1) buttons.deleteCharAt(buttons.lastIndexOf("\n"));
        if(filterKeys.lastIndexOf(",") != -1) filterKeys.deleteCharAt(filterKeys.lastIndexOf(","));

        StringBuilder tr = new StringBuilder();

        for(Room room : RoomFinder.getRooms()) {
            tr.append(String.format("                <tr%s>%s%n", room.getDataTags(), room.getHtmlString("idag")));
            for(Time time : Time.values()) {
                tr.append("                    ");
                if(schedule.isBooked(room, time) == -1) {
                    tr.append("<td class=\"unknown tooltip\"><span class=\"tooltiptext\">Unknown</span></td>");
                } else if(schedule.isBooked(room, time) == 1) {
                    tr.append(String.format("<td class=\"booked tooltip\"><span class=\"tooltiptext\">Booked</span>%s</td>", schedule.getText(room, time)));
                } else {
                    tr.append("<td class=\"free tooltip\"><span class=\"tooltiptext\">Free</span></td>");
                }
                tr.append("\n");
            }
            tr.append("                </tr>\n");
        }
        tr.deleteCharAt(tr.lastIndexOf("\n"));



        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName))) {
            byte[] template = Files.readAllBytes(Path.of(this.template));
            String index = new String(template);

            index = index.replace("$filterKeys", filterKeys.toString());
            index = index.replace("$filterButton", buttons.toString());
            index = index.replace("$datum", timestamp);
            index = index.replace("$tableRows", tr.toString());
            writer.write(index);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
}
