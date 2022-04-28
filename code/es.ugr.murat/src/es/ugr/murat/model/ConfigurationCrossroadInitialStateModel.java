package es.ugr.murat.model;

import java.util.Map;

/**
 * Clase representando el modelo de los estados iniciales de los cruces para cada configuración.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class ConfigurationCrossroadInitialStateModel {
    
    private final Map<Integer, Integer> crossroadInitialState; // Estado inicial para cada cruce (crossroadId --> initialStateId)

    public ConfigurationCrossroadInitialStateModel(Map<Integer, Integer> crossroadInitialState) {
        this.crossroadInitialState = crossroadInitialState;
    }

}
