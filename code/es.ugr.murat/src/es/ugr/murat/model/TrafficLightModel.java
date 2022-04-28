package es.ugr.murat.model;

/**
 * Clase representando el modelo de un semáforo.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class TrafficLightModel {

    private final Integer id; // Id del semáforo
    private final String name; // Nombre del semáforo

    public TrafficLightModel(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
