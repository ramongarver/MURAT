package es.ugr.murat.model;

import java.time.Duration;

public class StateModel {

    Integer id;
    String name;
    Duration durationTime;

    public StateModel(Integer id, String name, Duration durationTime) {
        this.id = id;
        this.name = name;
        this.durationTime = durationTime;
    }

}
