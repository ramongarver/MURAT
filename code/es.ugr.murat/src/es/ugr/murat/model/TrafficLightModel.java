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
    private final String roadStretchIn; // Calle que entra al cruce que regula el semáforo

    public TrafficLightModel(Integer id, String name, String roadStretchIn) {
        this.id = id;
        this.name = name;
        this.roadStretchIn = roadStretchIn;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRoadStretchIn() {
        return roadStretchIn;
    }
}
