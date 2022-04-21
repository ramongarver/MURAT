package es.ugr.murat.model;

import java.time.Duration;
import java.time.LocalTime;

public class ConfigurationModel {

    Integer id;
    Double vehicleLength;
    Double inputRatio;
    Double inputInnerRatio;
    Double outputInnerRatio;
    LocalTime initialTime;
    LocalTime finalTime;
    Duration sampleTime;
    String mode;

    public ConfigurationModel(Integer id, Double vehicleLength,
                              Double inputRatio, Double inputInnerRatio, Double outputInnerRatio,
                              LocalTime initialTime, LocalTime finalTime, Duration sampleTime, String mode) {
        this.id = id;
        this.vehicleLength = vehicleLength;
        this.inputRatio = inputRatio;
        this.inputInnerRatio = inputInnerRatio;
        this.outputInnerRatio = outputInnerRatio;
        this.initialTime = initialTime;
        this.finalTime = finalTime;
        this.sampleTime = sampleTime;
        this.mode = mode;
    }

}
