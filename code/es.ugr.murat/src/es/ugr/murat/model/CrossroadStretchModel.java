package es.ugr.murat.model;

public class CrossroadStretchModel {

    private Integer crossroadId;
    private String roadStretchOrigin;
    private String roadStretchDestination;
    private String name;
    private Double carsPercentageFromOriginToDestination;

    public CrossroadStretchModel(Integer crossroadId, String roadStretchOrigin, String roadStretchDestination, String name, Double carsPercentageFromOriginToDestination) {
        this.crossroadId = crossroadId;
        this.roadStretchOrigin = roadStretchOrigin;
        this.roadStretchDestination = roadStretchDestination;
        this.name = name;
        this.carsPercentageFromOriginToDestination = carsPercentageFromOriginToDestination;
    }

}
