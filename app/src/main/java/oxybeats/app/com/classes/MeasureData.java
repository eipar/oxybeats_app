package oxybeats.app.com.classes;

import java.util.Calendar;
import java.util.Date;

public class MeasureData {
    private String usr;
    private String hr;
    private String spo;
    private String sleep;
    private String year;
    private String month;
    private String day;
    private String timestamp;

    public MeasureData(String usr, String hr, String spo, String sleep, String year, String month, String day, String timestamp) {
        this.usr = usr;
        this.hr = hr;
        this.spo = spo;
        this.sleep = sleep;
        this.year = year;
        this.month = month;
        this.day = day;
        this.timestamp = timestamp;
    }

    public MeasureData() {
    }

    public String getUsr() {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getHr() {
        return hr;
    }

    public void setHr(String hr) {
        this.hr = hr;
    }

    public String getSpo() {
        return spo;
    }

    public void setSpo(String spo) {
        this.spo = spo;
    }

    public String getSleep() {
        return sleep;
    }

    public void setSleep(String sleep) {
        this.sleep = sleep;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    //Hacer método que divida el timestamp
    public Date divideTimestamp(String time){
        //time -> hh:mm:ss
        //arr  -> 0  1  2
        String [] arrOfStr = time.split(":");
        Date date = new Date();

        date.setHours(Integer.parseInt(arrOfStr[0]));
        date.setMinutes(Integer.parseInt(arrOfStr[1]));
        date.setSeconds(Integer.parseInt(arrOfStr[2]));

        return date;
    }

}
