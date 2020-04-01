package oxybeats.app.com.classes;

public class ItemData {
    String measure;
    String type;

    public ItemData(String measure, String type) {
        this.measure = measure;
        this.type = type;
    }

    public String getMeasure() {
        return measure;
    }

    public void setMeasure(String measure) {
        this.measure = measure;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
