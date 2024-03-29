package es.ugr.murat.model;

import es.ugr.murat.constant.CommonConstant;

/**
 * Clase representando el modelo de un tramo de calle.
 * Puede ser:
 *  - tramo de calle de entrada al sistema si no tiene origen y sí tiene destino. (ROOT)
 *  - tramo de calle de salida del sistema si tiene origen y no tiene destino. (LEAF)
 *  - tramo de calle intermedio entre dos cruces si se tiene tanto origen como destino. (INNER)
 *
 * @author Ramón García Verjaga
 */
public class  RoadStretchModel {

    private Integer crossroadOriginId; // Id del cruce de origen
    private Integer crossroadDestinationId; // Id del cruce de destino
    private String direction; // Dirección del flujo de tráfico
    private String name; // Nombre del tramo de calle
    private Double length; // Longitud del tramo de calle
    private Integer lanes; // Número de vías del tramo de calle
    private Integer vehicles; // Número de vehículos
    private Integer maxVehicles; // Capacidad máxima de vehículos
    private Double occupancyPercentage; // Porcentaje de ocupación del tramo de calle
    private Double input; // Cantidad de vehículos que entran al tramo de calle por segundo
    private Double output; // Cantidad de vehículos que salen del tramo de calle por segundo
    private String type; // Tipo de tramo de calle: raíz (entrada al sistema), hoja (salida del sistema) e intermedio

    public RoadStretchModel(Integer crossroadOriginId, Integer crossroadDestinationId, String direction, String name, Double length, Integer lanes, Integer vehicles, Double vehicleLength, Double inputRatio, Double inputInnerRatio, Double outputInnerRatio) {
        this.crossroadOriginId = crossroadOriginId;
        this.crossroadDestinationId = crossroadDestinationId;
        this.direction = direction;
        this.name = name;
        this.length = length;
        this.lanes = lanes;
        this.vehicles = vehicles;
        this.maxVehicles = (int) (this.length * this.lanes / vehicleLength);
        this.occupancyPercentage = (double) this.vehicles / (double) this.maxVehicles * 100.0;
        this.type = this.calculateAndSetType();
        this.input = CommonConstant.ROOT.equals(this.type) ? this.lanes * inputRatio : this.lanes * inputInnerRatio;
        this.output = this.lanes * outputInnerRatio;
    }

    public Integer getCrossroadOriginId() {
        return crossroadOriginId;
    }

    public Integer getCrossroadDestinationId() {
        return crossroadDestinationId;
    }

    public String getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public Double getLength() {
        return length;
    }

    public Integer getLanes() {
        return lanes;
    }

    public Double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public Integer getMaxVehicles() {
        return maxVehicles;
    }

    public Double getInput() {
        return input;
    }

    public Integer getVehicles() {
        return vehicles;
    }

    public Double getOutput() {
        return output;
    }

    public String getType() {
        return type;
    }

    public void setCrossroadOriginId(Integer crossroadOriginId) {
        this.crossroadOriginId = crossroadOriginId;
        this.type = this.calculateAndSetType();
    }

    public void setCrossroadDestinationId(Integer crossroadDestinationId) {
        this.crossroadDestinationId = crossroadDestinationId;
        this.type = this.calculateAndSetType();
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public void setLanes(Integer lanes) {
        this.lanes = lanes;
    }

    public void setVehicles(Integer vehicles) {
        this.vehicles = vehicles;
        this.occupancyPercentage = (double) this.vehicles / (double) this.maxVehicles * 100.0;
    }

    public void setMaxVehicles(Integer maxVehicles) {
        this.maxVehicles = maxVehicles;
    }

    public void setInput(Double input) {
        this.input = input;
    }

    public void setOutput(Double output) {
        this.output = output;
    }

    private String calculateAndSetType() {
        return  this.crossroadOriginId == null && this.crossroadDestinationId != null ? CommonConstant.ROOT : // Si no tiene cruce origen y tiene cruce destino (calle de entrada al sistema)
                this.crossroadOriginId != null && this.crossroadDestinationId == null ? CommonConstant.LEAF : // Si tiene cruce origen y nodo tiene cruce destino (calle de salida del sistema)
                this.crossroadOriginId != null && this.crossroadDestinationId != null ? CommonConstant.INNER : // Si tiene cruce origen y cruce destino (calle interna)
                null;   // Nada de lo anterior
    }

}
