package es.ugr.murat.model;

import java.time.Duration;

/**
 * Clase representando el modelo de un estado.
 *
 * @author Ramón García Verjaga
 */
public class StateModel {

    private Integer stateId; // Id del estado
    private String name; // Nombre del estado
    private Integer durationTime; // Duración de tiempo del estado

    public StateModel(Integer stateId, String name, Integer durationTime) {
        this.stateId = stateId;
        this.name = name;
        this.durationTime = durationTime;
    }

    public Integer getStateId() {
        return stateId;
    }

    public String getName() {
        return name;
    }

    public Integer getDurationTime() {
        return durationTime;
    }

}
