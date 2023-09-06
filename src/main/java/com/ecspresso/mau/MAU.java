package com.ecspresso.mau;


import com.ecspresso.mau.reservation.Schedule;
import com.ecspresso.mau.reservation.time.Time;
import com.ecspresso.mau.reservation.room.Room;
import com.ecspresso.mau.reservation.room.RoomFinder;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MAU {
    private final Logger logger = LoggerFactory.getLogger(MAU.class);
    private final Schedule today = new Schedule();
    private final Schedule tomorrow = new Schedule();
    private final Schedule inTwoDays = new Schedule();
    private final String template;
    private final String outputLocation;

    public MAU(String template, @NotNull String outputLocation) {
        this.template = template;
        this.outputLocation = outputLocation;
    }

    public void run() {
        logger.info("Letar efter bokade rum.");
        LocalDate date = LocalDate.now();
        findPrebookedRooms(today, "idag");
        findPrebookedRooms(tomorrow, dateToString(date.plusDays(1)));
        findPrebookedRooms(inTwoDays, dateToString(date.plusDays(2)));

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
        String span = "<span class=\"active\">€</span>";
        createHtmlFiles(today, outputLocation+"indexToday.html", timestamp.toString(), "idag", "€1.+€", span.replace("€", "Idag"));
        createHtmlFiles(tomorrow, outputLocation+"indexTomorrow.html", timestamp.toString(), date.plusDays(1), "€2.+€", span.replace("€", "Imorgon"));
        createHtmlFiles(inTwoDays, outputLocation+"indexInTwoDays.html", timestamp.toString(), date.plusDays(2), "€3.+€", span.replace("€", "Om två dagar"));
    }

    private void findPrebookedRooms(Schedule map, String date) {
        for(Room room : RoomFinder.getRooms()) {
            logger.info("Hämtar data för {} ({})", room, date);
            String url = String.format("https://schema.mau.se/setup/jsp/Schema.jsp?startDatum=%s&intervallTyp=d&intervallAntal=1&resurser=l.%s", date, room);
            updateMap(map, room, url, date);
        }
    }

    private void updateMap(Schedule schedule, Room room, String url, String date) {
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

                for(Time time: Time.values()) {
                    boolean s = time.getTime().isAfter(start) || time.getTime().equals(start);
                    boolean e = time.getTime().isBefore(end) || time.getTime().equals(end);
                    if(s && e) {
                        setBooked(schedule, room, time, date, String.format("%s - %s", start, end));
                    }
                }
            }
        }
    }

    private HtmlPage getPage(WebClient webClient, String url, int thisTry, int maxTries, Room room) throws IOException {
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

    private void setBooked(Schedule schedule, @NotNull Room room, Time time, String date) {
        logger.info("{} är reserverad kl {} {}.", room, time, date);
        schedule.setBooked(room, time);
    }

    private void setBooked(Schedule schedule, @NotNull Room room, Time time, String date, String text) {
        // logger.info("{} är reserverad kl {} {}.", room, text, date);
        schedule.setBooked(room, time, text);
    }

    private void createHtmlFiles(Schedule schedule, String outFileName, String timestamp, LocalDate date, String pattern, String replacement) {
        createHtmlFiles(schedule, outFileName, timestamp, dateToString(date), pattern, replacement);
    }

    private void createHtmlFiles(Schedule schedule, String outFileName, String timestamp, String date, String pattern, String replacement) {
        logger.info("Skapar {}.", outFileName);

        StringBuilder tr = new StringBuilder();

        for(Room room : RoomFinder.getRooms()) {
            tr.append(String.format("            <tr%s>%s%n", room.getDataTags(), room.getHtmlString(date)));
            for(Time time : Time.values()) {
                tr.append("                ");
                if(schedule.isBooked(room, time) == -1) {
                    tr.append("<td class=\"unknown tooltip\"><span class=\"tooltiptext\">Unknown</span></td>");
                } else if(schedule.isBooked(room, time) == 1) {
                    tr.append(String.format("<td class=\"booked tooltip\"><span class=\"tooltiptext\">Booked</span>%s</td>", schedule.getText(room, time)));
                } else {
                    tr.append("<td class=\"free tooltip\"><span class=\"tooltiptext\">Free</span></td>");
                }
                tr.append("\n");
            }
            tr.append("            </tr>\n");
        }

        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName))) {
            byte[] template = Files.readAllBytes(Path.of(this.template));
            String index = new String(template);

            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(index);
            index = matcher.replaceAll(replacement);
            index = index.replace("€1", "");
            index = index.replace("€2", "");
            index = index.replace("€3", "");
            index = index.replace("€", "");
            index = index.replace("$datum", timestamp);
            index = index.replace("$tableRows", tr.toString());
            writer.write(index);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String dateToString(@NotNull LocalDate date) {
        int y = date.getYear();
        int m = date.getMonthValue();
        int d = date.getDayOfMonth();
        return String.format("%s-%s-%s", y, m, d);
    }
}
