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
    private Duration durationTime; // Duración de tiempo del estado

    public StateModel(Integer stateId, String name, Duration durationTime) {
        this.stateId = stateId;
        this.name = name;
        this.durationTime = durationTime;
    }

}
