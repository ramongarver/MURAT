package es.ugr.murat.model;

public class RoadStretchModel {

    private Integer crossroadOriginId;
    private Integer crossroadDestinationId;
    private String direction;
    private String name;
    private Double length;
    private Integer lanes;
    private Double occupancyPercentage;
    private Integer capacity;
    private Double input;
    private Double output;
    private String type;

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
