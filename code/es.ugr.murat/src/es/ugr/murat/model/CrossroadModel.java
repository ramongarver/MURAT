package es.ugr.murat.model;

import java.time.Duration;

/**
 * Clase representando el modelo de un cruce.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class CrossroadModel {

    Integer id; // Id del cruce
    String name; // Nombre del cruce
    Duration minimumStateTime; // Mínima duración de tiempo que puede permanecer el cruce en el mismo estado
    Duration cycleTime; // Duración de tiempo que tarda el cruce en pasar por todos sus estados de forma completa

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
