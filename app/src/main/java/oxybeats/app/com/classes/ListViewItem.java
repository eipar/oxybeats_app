package oxybeats.app.com.classes;

public class ListViewItem {
    private String day;
    private String media;
    private String max;
    private String min;

    public ListViewItem(String day, String media, String max, String min) {
        this.day = day;
        this.media = media;
        this.max = max;
        this.min = min;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getMax() {
        return max;
    }

    public void setMax(String max) {
        this.max = max;
    }

    public String getMin() {
        return min;
    }

    public void setMin(String min) {
        this.min = min;
    }
}
