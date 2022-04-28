package es.ugr.murat.model;

/**
 * Clase representando el modelo de un tramo de cruce.
 * Es un router que indica en qué cruce (Crossroad) se va desde qué calle (RoadStretchOrigin) hasta qué otra calle (RoadStretchDestination).
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class CrossroadStretchModel {

    private Integer crossroadId; // Id del cruce
    private String roadStretchOrigin; // Calle de origen
    private String roadStretchDestination; // Calle de destino
    private String name; // Nombre del tramo del cruce
    private Double carsPercentageFromOriginToDestination; // Porcentaje de vehículos del total de la calle de origen que quieren ir a la calle destino

    public CrossroadStretchModel(Integer crossroadId, String roadStretchOrigin, String roadStretchDestination, String name, Double carsPercentageFromOriginToDestination) {
        this.crossroadId = crossroadId;
        this.roadStretchOrigin = roadStretchOrigin;
        this.roadStretchDestination = roadStretchDestination;
        this.name = name;
        this.carsPercentageFromOriginToDestination = carsPercentageFromOriginToDestination;
    }

}
