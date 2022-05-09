package es.ugr.murat.model;

import java.time.Duration;

/**
 * Clase representando el modelo de un cruce.
 *
 * @author Ramón García Verjaga
 */
public class CrossroadModel {

    private Integer id; // Id del cruce
    private String name; // Nombre del cruce
    private Duration minimumStateTime; // Mínima duración de tiempo que puede permanecer el cruce en el mismo estado
    private Duration cycleTime; // Duración de tiempo que tarda el cruce en pasar por todos sus estados de forma completa

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

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinimumStateTime(Duration minimumStateTime) {
        this.minimumStateTime = minimumStateTime;
    }

    public void setCycleTime(Duration cycleTime) {
        this.cycleTime = cycleTime;
    }

}
