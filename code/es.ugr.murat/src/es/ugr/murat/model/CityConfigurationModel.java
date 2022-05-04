package es.ugr.murat.model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Map;

/**
 * Clase representando el modelo de la configuración de la ciudad/escenario/simulación.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class CityConfigurationModel {

    private final Integer id; // Id de la configuración
    private final Double vehicleLength; // Longitud de un vehículo (en metros)
    private final Double inputRatio; // Relación de entrada de tráfico al sistema: Cantidad de vehículos que se entran a la simulación por vía y segundo
    private final Double inputInnerRatio; // Relación de entrada interna de tráfico: Cantidad de vehículos que entran a calles interiores (RoadStretch) por vía y segundo
    private final Double outputInnerRatio; // Relación de salida interna de tráfico: Cantidad de vehículos que salen, bien de calles interiores, bien de la simulación
    private final LocalTime initialTime; // Hora de inicio de la simulación
    private final LocalTime finalTime; // Hora de fin de la simulación
    private final Duration sampleTime; // Tiempo de muestreo de la simulación
    private final String mode; // Modo de entrada de vehículos en la simulación (linear, single peak, double peak)
    private final Map<Integer, ConfigurationCrossroadInitialStateModel> crossroadsInitialState; // Estados iniciales de los cruces (crossroadId -> configurationCrossroadInitialStateModel)

    public CityConfigurationModel(Integer id, Double vehicleLength,
                                  Double inputRatio, Double inputInnerRatio, Double outputInnerRatio,
                                  LocalTime initialTime, LocalTime finalTime, Duration sampleTime,
                                  String mode, Map<Integer, ConfigurationCrossroadInitialStateModel> crossroadsInitialState) {
        this.id = id;
        this.vehicleLength = vehicleLength;
        this.inputRatio = inputRatio;
        this.inputInnerRatio = inputInnerRatio;
        this.outputInnerRatio = outputInnerRatio;
        this.initialTime = initialTime;
        this.finalTime = finalTime;
        this.sampleTime = sampleTime;
        this.mode = mode;
        this.crossroadsInitialState = crossroadsInitialState;
    }

}
