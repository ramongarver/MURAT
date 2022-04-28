package es.ugr.murat.model;

import java.time.Duration;

/**
 * Clase representando el modelo de un estado.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class StateModel {

    Integer id; // Id del estado
    String name; // Nombre del estado
    Duration durationTime; // Duración de tiempo del estado

    public StateModel(Integer id, String name, Duration durationTime) {
        this.id = id;
        this.name = name;
        this.durationTime = durationTime;
    }

}
