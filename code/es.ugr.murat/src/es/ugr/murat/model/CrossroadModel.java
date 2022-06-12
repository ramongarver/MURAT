package es.ugr.murat.model;

import java.time.Duration;

/**
 * Clase representando el modelo de un cruce.
 *
 * @author Ramón García Verjaga
 */
public class CrossroadModel {

    private Integer crossroadId; // Id del cruce
    private String name; // Nombre del cruce
    private Integer minimumStateTime; // Mínima duración de tiempo que puede permanecer el cruce en el mismo estado
    private Duration cycleTime; // Duración de tiempo que tarda el cruce en pasar por todos sus estados de forma completa

    public CrossroadModel(Integer crossroadId, String name, Integer minimumStateTime, Duration cycleTime) {
        this.crossroadId = crossroadId;
        this.name = name;
        this.minimumStateTime = minimumStateTime;
        this.cycleTime = cycleTime;
    }

    public Integer getCrossroadId() {
        return crossroadId;
    }

    public String getName() {
        return name;
    }

    public Integer getMinimumStateTime() {
        return minimumStateTime;
    }

    public Duration getCycleTime() {
        return cycleTime;
    }

    public void setCrossroadId(Integer crossroadId) {
        this.crossroadId = crossroadId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMinimumStateTime(Integer minimumStateTime) {
        this.minimumStateTime = minimumStateTime;
    }

    public void setCycleTime(Duration cycleTime) {
        this.cycleTime = cycleTime;
    }

}
