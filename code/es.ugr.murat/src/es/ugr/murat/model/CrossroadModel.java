package es.ugr.murat.model;

import java.time.Duration;

public class CrossroadModel {

    Integer id;
    String name;
    Duration minimumStateTime;
    Duration cycleTime;

    public CrossroadModel(Integer id, String name, Duration minimumStateTime, Duration cycleTime) {
        this.id = id;
        this.name = name;
        this.minimumStateTime = minimumStateTime;
        this.cycleTime = cycleTime;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Duration getMinimumStateTime() {
        return minimumStateTime;
    }

    public Duration getCycleTime() {
        return cycleTime;
    }

}
