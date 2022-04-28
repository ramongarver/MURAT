package es.ugr.murat.model;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Clase representando el modelo de la configuración de la ciudad/escenario/simulación.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class CityConfigurationModel {

    Integer id; // Id de la configuración
    Double vehicleLength; // Longitud de un vehículo (en metros)
    Double inputRatio; // Relación de entrada de tráfico al sistema: Cantidad de vehículos que se entran a la simulación por vía y segundo
    Double inputInnerRatio; // Relación de entrada interna de tráfico: Cantidad de vehículos que entran a calles interiores (RoadStretch) por vía y segundo
    Double outputInnerRatio; // Relación de salida interna de tráfico: Cantidad de vehículos que salen, bien de calles interiores, bien de la simulación
    LocalTime initialTime; // Hora de inicio de la simulación
    LocalTime finalTime; // Hora de fin de la simulación
    Duration sampleTime; // Tiempo de muestreo de la simulación
    String mode; // Modo de entrada de vehículos en la simulación (linear, single peak, double peak)

    public CityConfigurationModel(Integer id, Double vehicleLength,
                                  Double inputRatio, Double inputInnerRatio, Double outputInnerRatio,
                                  LocalTime initialTime, LocalTime finalTime, Duration sampleTime, String mode) {
        this.id = id;
        this.vehicleLength = vehicleLength;
        this.inputRatio = inputRatio;
        this.inputInnerRatio = inputInnerRatio;
        this.outputInnerRatio = outputInnerRatio;
        this.initialTime = initialTime;
        this.finalTime = finalTime;
        this.sampleTime = sampleTime;
        this.mode = mode;
    }

}
