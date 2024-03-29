package es.ugr.murat.constant;

/**
 * Clase representando las constantes del agente cruce (Crossroad).
 *
 * @author Ramón García Verjaga
 */
public class CrossroadConstant {

    public static final String AGENT_NAME = "C"; // Parte principal del nombre de los agentes

    public static final int EXIT = 0;
    public static final int LOAD_DATA = 1;
    public static final int INITIALIZE_TRAFFIC_LIGHTS = 2;
    public static final int CONTROL_TRAFFIC = 3;
    public static final int FINALIZE_TRAFFIC_LIGHTS = 4;

    public static final Double SATURATION_THRESHOLD = 60.0;
    public static final Double VEHICLE_WEIGHT = 0.32;
    public static final Double OCCUPATION_WEIGHT = 0.68;

    public static final int STATE_TIME_VARIATION = 10;

}
