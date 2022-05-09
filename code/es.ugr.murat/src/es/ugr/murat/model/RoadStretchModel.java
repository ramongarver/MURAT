package es.ugr.murat.model;

/**
 * Clase representando el modelo de un tramo de calle.
 * Puede ser:
 *  - entrada al sistema si no tiene origen, pero sí se tiene destino.
 *  - salida del sistema si se tiene origen, pero no se tiene destino.
 *  - tramo de calle intermedio entre dos cruces si se tiene tanto origen como destino.
 *
 * @author Ramón García Verjaga
 */
public class RoadStretchModel {

    private Integer crossroadOriginId; // Id del cruce de origen
    private Integer crossroadDestinationId; // Id del cruce de destino
    private String direction; // Dirección del flujo de tráfico
    private String name; // Nombre del tramo de calle
    private Double length; // Longitud del tramo de calle
    private Integer lanes; // Número de vías del tramo de calle
    private Double occupancyPercentage; // Porcentaje de ocupación del tramo de calle
    private Integer capacity; // Capacidad de vehículos
    private Double input; // Cantidad de coches que entran al tramo de calle por segundo
    private Double output; // Cantidad de coches que salen del tramo de calle por segundo
    private String type; // Tipo de tramo de calle: raíz (entrada al sistema), hoja (salida del sistema) e intermedio

    public RoadStretchModel(Integer crossroadOriginId, Integer crossroadDestinationId, String direction, String name, Double length, Integer lanes, Double occupancyPercentage, Integer capacity, Double input, Double output, String type) {
        this.crossroadOriginId = crossroadOriginId;
        this.crossroadDestinationId = crossroadDestinationId;
        this.direction = direction;
        this.name = name;
        this.length = length;
        this.lanes = lanes;
        this.occupancyPercentage = occupancyPercentage;
        // TODO: Son atributos calculados --> Programar el cálculo
        this.capacity = capacity;
        this.input = input;
        this.output = output;
        this.type = type;
    }
}
