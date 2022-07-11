package es.ugr.murat.model;

/**
 * Clase representando el modelo de un semáforo.
 *
 * @author Ramón García Verjaga
 */
public class TrafficLightModel {

    private final Integer trafficLightId; // Id del semáforo
    private final String name; // Nombre del semáforo
    private final String roadStretchIn; // Calle que entra al cruce que regula el semáforo

    public TrafficLightModel(Integer trafficLightId, String name, String roadStretchIn) {
        this.trafficLightId = trafficLightId;
        this.name = name;
        this.roadStretchIn = roadStretchIn;
    }

    public Integer getTrafficLightId() {
        return trafficLightId;
    }

    public String getName() {
        return name;
    }

    public String getRoadStretchIn() {
        return roadStretchIn;
    }
}
