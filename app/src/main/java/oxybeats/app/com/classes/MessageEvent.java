package oxybeats.app.com.classes;

public class MessageEvent {
    private MeasureData measureData;

    public MessageEvent(MeasureData newMeasure){
        this.measureData = newMeasure;
    }

    public MeasureData getMeasureData() {
        return this.measureData;
    }
}
