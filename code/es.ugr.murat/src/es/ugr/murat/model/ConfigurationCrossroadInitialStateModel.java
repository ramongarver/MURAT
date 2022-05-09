package es.ugr.murat.model;

/**
 * Clase representando el modelo del estado inicial de un cruce.
 *
 * @author Ramón García Verjaga
 */
public class ConfigurationCrossroadInitialStateModel {

    private final Integer crossroadId; // Id del cruce
    private final Integer stateId; // Id del estado

    public ConfigurationCrossroadInitialStateModel(Integer id, Integer stateId) {
        this.crossroadId = id;
        this.stateId = stateId;
    }

    public Integer getCrossroadId() {
        return crossroadId;
    }

    public Integer getStateId() {
        return stateId;
    }

}
