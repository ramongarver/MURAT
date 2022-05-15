package es.ugr.murat.simulation;

import es.ugr.murat.agent.CityAgent;
import es.ugr.murat.agent.CrossroadAgent;
import es.ugr.murat.agent.TrafficLightAgent;
import es.ugr.murat.appboot.JADEBoot;
import es.ugr.murat.model.CityConfigurationModel;
import es.ugr.murat.model.CityModel;
import es.ugr.murat.model.ConfigurationCrossroadInitialStateModel;
import es.ugr.murat.model.CrossroadModel;
import es.ugr.murat.model.CrossroadStretchModel;
import es.ugr.murat.model.RoadStretchModel;
import es.ugr.murat.model.StateModel;
import es.ugr.murat.model.TrafficLightModel;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clase representando a una simulación, encargada de obtener todos los datos y lanzar los agentes.
 *
 * @author Ramón García Verjaga
 */
public class Simulation {

    public static Simulation simulation = null;

    private final Integer cityId = 1;
    private final Integer cityConfigurationId = 1;

    private final CityModel cityModel; // Información de la ciudad (nombre y descripción)
    private final Map<Integer, CityConfigurationModel> cityConfiguration; // Configuración de la ciudad | (cityConfigurationId -> cityConfigurationModel)
    private final Map<Integer, CrossroadModel> crossroads; // Cruces de la ciudad | (crossroadId -> crossroadModel)
    private final Map<Integer, Map<Integer, TrafficLightModel>> crossroadTrafficLights; // Semáforos de la ciudad | (crossroadId -> trafficLightId -> trafficLightModel)
    private final Map<Integer, Map<Integer, StateModel>> crossroadStates; // Estados por cruce de la ciudad | (crossroadId -> stateId -> stateModel)
    private final Map<Integer, Map<Integer, Map<Integer, String>>> trafficLightsColorsPerCrossroadsStates; // Colores de semáforos para cada estado de un cruce | (crossroadId -> stateId -> trafficLightId -> color)
    private final Map<String, RoadStretchModel> roadStretches; // Tramos de calle de la ciudad | (name -> roadStretchModel)
    private final Map<Integer, Map<String, CrossroadStretchModel>> crossroadsStretches; // Tramos de cruce por cruce de la ciudad | (crossroadId -> name -> crossroadStretchModel)
    private final Map<Integer, Map<Integer, Map<Integer, Set<String>>>> crossroadsStatesTrafficLightsCrossroadStretchesNames; // Tramos de cruce abiertos por cada semáforo y en cada estado por cruce de la ciudad | (crossroadId -> stateId -> trafficLightId -> crossroadStretchNames)

    public static void start() {
        if (simulation == null) {
            simulation = new Simulation();
            simulation.initAgents();
        }
    }

    private Simulation() {
        if (cityId.equals(1)) {
            // CITY2: Very simple cross city
            // Información de la ciudad
            cityModel = new CityModel("CITY2", "Very simple cross");

            // Configuración de la ciudad
            cityConfiguration = new HashMap<>();
                // Configuración 1
            Map<Integer, ConfigurationCrossroadInitialStateModel> crossroadsInitialState = new HashMap<>();
            crossroadsInitialState.put(1, new ConfigurationCrossroadInitialStateModel(1, 1));
            cityConfiguration.put(1, new CityConfigurationModel(1, 4.0, 0.05, 2.0, 2.0,
                    LocalTime.of(6, 0), LocalTime.of(21, 0), Duration.ofSeconds(15), "linear", crossroadsInitialState));
                // Configuración 2
            crossroadsInitialState = new HashMap<>();
            crossroadsInitialState.put(1, new ConfigurationCrossroadInitialStateModel(1, 3));
            cityConfiguration.put(2, new CityConfigurationModel(2, 4.0, 0.1, 2.0, 1.0,
                    LocalTime.of(4, 0), LocalTime.of(22, 0), Duration.ofSeconds(10), "single_peak", crossroadsInitialState));

            // Cruces de la ciudad
            crossroads = new HashMap<>();
            crossroads.put(1, new CrossroadModel(1, "C1", Duration.ofSeconds(20), Duration.ofSeconds(90)));

            // Semáforos por cruce de la ciudad
            crossroadTrafficLights = new HashMap<>();
            Map<Integer, TrafficLightModel> trafficLights = new HashMap<>();
            trafficLights.put(1, new TrafficLightModel(1, "TL1", "RS2"));
            trafficLights.put(2, new TrafficLightModel(2, "TL2", "RS3"));
            crossroadTrafficLights.put(1, trafficLights);

            // Estados por cruce de la ciudad
            crossroadStates = new HashMap<>();
            Map<Integer, StateModel> states = new HashMap<>();
            states.put(1, new StateModel(1, "S1", 45));
            states.put(2, new StateModel(2, "S2", 45));
            crossroadStates.put(1, states);

            // Colores de semáforos para cada estado de un cruce
            trafficLightsColorsPerCrossroadsStates = new HashMap<>();
            Map<Integer, Map<Integer, String>> trafficLightsColorsPerStates = new HashMap<>();
            Map<Integer, String> trafficLightsColors = new HashMap<>();
                // Estado 1 (S1)
            trafficLightsColors.put(1, "G");
            trafficLightsColors.put(2, "R");
            trafficLightsColorsPerStates.put(1, trafficLightsColors);
                // Estado 2 (S2)
            trafficLightsColors = new HashMap<>();
            trafficLightsColors.put(1, "R");
            trafficLightsColors.put(2, "G");
            trafficLightsColorsPerStates.put(2, trafficLightsColors);
            trafficLightsColorsPerCrossroadsStates.put(1, trafficLightsColorsPerStates);

            // Tramos de calle de la ciudad
            roadStretches = new HashMap<>();
            Double vehicleLength = cityConfiguration.get(cityConfigurationId).getVehicleLength();
            Double inputRatio = cityConfiguration.get(cityConfigurationId).getInputRatio();
            Double inputInnerRatio = cityConfiguration.get(cityConfigurationId).getInputInnerRatio();
            Double outputInnerRatio = cityConfiguration.get(cityConfigurationId).getOutputInnerRatio();
            roadStretches.put("RS1", new RoadStretchModel(1,null, "E", "RS1",
                    100.0, 1, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS2", new RoadStretchModel(null, 1, "S", "RS2",
                    500.0, 1, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS3", new RoadStretchModel(null, 1, "E", "RS3",
                    100.0, 1, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS4", new RoadStretchModel(1, null, "S", "RS4",
                    500.0, 1, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));

            // Tramos de cruce por cruce de la ciudad
            crossroadsStretches = new HashMap<>();
            Map<String, CrossroadStretchModel> crossroadStretches = new HashMap<>();
            crossroadStretches.put("RS2-RS1", new CrossroadStretchModel(1, "RS2", "RS1", "RS2-RS1", 20.0));
            crossroadStretches.put("RS2-RS4", new CrossroadStretchModel(1, "RS2", "RS4", "RS2-RS4", 80.0));
            crossroadStretches.put("RS3-RS1", new CrossroadStretchModel(1, "RS3", "RS1", "RS3-RS1", 35.0));
            crossroadStretches.put("RS3-RS4", new CrossroadStretchModel(1, "RS3", "RS4", "RS3-RS4", 65.0));
            crossroadsStretches.put(1, crossroadStretches);

            // Tramos de cruce abiertos por cada semáforo y en cada estado por cruce de la ciudad
            crossroadsStatesTrafficLightsCrossroadStretchesNames = new HashMap<>();
            Map<Integer, Map<Integer, Set<String>>> statesTrafficLightsCrossroadStretchesNames = new HashMap<>();
            // Estado 1 (S1)
                // Semáforo 1 (TL1)
            Map<Integer, Set<String>> trafficLightsCrossroadStretchesNames = new HashMap<>();
            Set<String> crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS2-RS1");
            crossroadStretchesNames.add("RS2-RS4");
            trafficLightsCrossroadStretchesNames.put(1, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(1, trafficLightsCrossroadStretchesNames);
            // Estado 2 (S2)
                // Semáforo 2 (TL2)
            trafficLightsCrossroadStretchesNames = new HashMap<>();
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS3-RS1");
            crossroadStretchesNames.add("RS3-RS4");
            trafficLightsCrossroadStretchesNames.put(2, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(2, trafficLightsCrossroadStretchesNames);
            crossroadsStatesTrafficLightsCrossroadStretchesNames.put(1, statesTrafficLightsCrossroadStretchesNames);
        } else {
            // CITY1: Simple cross city
            // Información de la ciudad
            cityModel = new CityModel("CITY1", "Simple cross city");

            // Configuración de la ciudad
            cityConfiguration = new HashMap<>();
                // Configuración 1
            Map<Integer, ConfigurationCrossroadInitialStateModel> crossroadsInitialState = new HashMap<>();
            crossroadsInitialState.put(1, new ConfigurationCrossroadInitialStateModel(1, 1));
            cityConfiguration.put(1, new CityConfigurationModel(1, 4.0, 0.05, 2.0, 2.0,
                    LocalTime.of(6, 0), LocalTime.of(21, 0), Duration.ofSeconds(15), "linear", crossroadsInitialState));

            // Cruces de la ciudad
            crossroads = new HashMap<>();
            crossroads.put(1, new CrossroadModel(1, "C1", Duration.ofSeconds(20), Duration.ofSeconds(540)));

            // Semáforos por cruce de la ciudad
            crossroadTrafficLights = new HashMap<>();
            Map<Integer, TrafficLightModel> trafficLights = new HashMap<>();
            trafficLights.put(1, new TrafficLightModel(1, "TL1", "RS1"));
            trafficLights.put(2, new TrafficLightModel(2, "TL2", "RS3"));
            trafficLights.put(3, new TrafficLightModel(3, "TL3", "RS5"));
            trafficLights.put(4, new TrafficLightModel(4, "TL4", "RS7"));
            crossroadTrafficLights.put(1, trafficLights);

            // Estados por cruce de la ciudad
            crossroadStates = new HashMap<>();
            Map<Integer, StateModel> states = new HashMap<>();
            states.put(1, new StateModel(1, "S1", 120));
            states.put(2, new StateModel(2, "S2", 120));
            states.put(3, new StateModel(3, "S3", 120));
            states.put(4, new StateModel(4, "S4", 60));
            states.put(5, new StateModel(5, "S5", 60));
            states.put(6, new StateModel(6, "S6", 60));
            crossroadStates.put(1, states);

            // Colores de semáforos para cada estado de un cruce
            trafficLightsColorsPerCrossroadsStates = new HashMap<>();
            Map<Integer, Map<Integer, String>> trafficLightsColorsPerStates = new HashMap<>();
            Map<Integer, String> trafficLightsColors = new HashMap<>();
                // Estado 1 (S1)
            trafficLightsColors.put(1, "R");
            trafficLightsColors.put(2, "G");
            trafficLightsColors.put(3, "R");
            trafficLightsColors.put(4, "G");
            trafficLightsColorsPerStates.put(1, trafficLightsColors);
                // Estado 2 (S2)
            trafficLightsColors = new HashMap<>();
            trafficLightsColors.put(1, "R");
            trafficLightsColors.put(2, "G");
            trafficLightsColors.put(3, "R");
            trafficLightsColors.put(4, "R");
            trafficLightsColorsPerStates.put(2, trafficLightsColors);
                // Estado 3 (S3)
            trafficLightsColors = new HashMap<>();
            trafficLightsColors.put(1, "R");
            trafficLightsColors.put(2, "R");
            trafficLightsColors.put(3, "R");
            trafficLightsColors.put(4, "G");
            trafficLightsColorsPerStates.put(3, trafficLightsColors);
                // Estado 4 (S4)
            trafficLightsColors = new HashMap<>();
            trafficLightsColors.put(1, "G");
            trafficLightsColors.put(2, "R");
            trafficLightsColors.put(3, "G");
            trafficLightsColors.put(4, "R");
            trafficLightsColorsPerStates.put(4, trafficLightsColors);
                // Estado 5 (S5)
            trafficLightsColors = new HashMap<>();
            trafficLightsColors.put(1, "G");
            trafficLightsColors.put(2, "R");
            trafficLightsColors.put(3, "R");
            trafficLightsColors.put(4, "R");
            trafficLightsColorsPerStates.put(5, trafficLightsColors);
                // Estado 6 (S6)
            trafficLightsColors = new HashMap<>();
            trafficLightsColors.put(1, "R");
            trafficLightsColors.put(2, "R");
            trafficLightsColors.put(3, "G");
            trafficLightsColors.put(4, "R");
            trafficLightsColorsPerStates.put(6, trafficLightsColors);
            trafficLightsColorsPerCrossroadsStates.put(1, trafficLightsColorsPerStates);

            // Tramos de calle de la ciudad
            roadStretches = new HashMap<>();
            Double vehicleLength = cityConfiguration.get(cityConfigurationId).getVehicleLength();
            Double inputRatio = cityConfiguration.get(cityConfigurationId).getInputRatio();
            Double inputInnerRatio = cityConfiguration.get(cityConfigurationId).getInputInnerRatio();
            Double outputInnerRatio = cityConfiguration.get(cityConfigurationId).getOutputInnerRatio();
            roadStretches.put("RS1", new RoadStretchModel(null, 1, "W", "RS1",
                    600.0, 2, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS2", new RoadStretchModel(1, null, "N", "RS2",
                    200.0, 2, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS3", new RoadStretchModel(null, 1, "S", "RS3",
                    600.0, 2, 223, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS4", new RoadStretchModel(1, null, "W", "RS4",
                    200.0, 2, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS5", new RoadStretchModel(null, 1, "E", "RS5",
                    600.0, 2, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS6", new RoadStretchModel(1, null, "S", "RS6",
                    200.0, 2, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS7", new RoadStretchModel(null, 1, "N", "RS7",
                    600.0, 2, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));
            roadStretches.put("RS8", new RoadStretchModel(1, null, "E", "RS8",
                    200.0, 2, 0, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio));

            // Tramos de cruce por cruce de la ciudad
            crossroadsStretches = new HashMap<>();
            Map<String, CrossroadStretchModel> crossroadStretches = new HashMap<>();
            crossroadStretches.put("RS1-RS2", new CrossroadStretchModel(1, "RS1", "RS2", "RS1-RS2", 10.0));
            crossroadStretches.put("RS1-RS4", new CrossroadStretchModel(1, "RS1", "RS4", "RS1-RS4", 80.0));
            crossroadStretches.put("RS1-RS6", new CrossroadStretchModel(1, "RS1", "RS6", "RS1-RS6", 10.0));
            crossroadStretches.put("RS3-RS4", new CrossroadStretchModel(1, "RS3", "RS4", "RS3-RS4", 0.0));
            crossroadStretches.put("RS3-RS6", new CrossroadStretchModel(1, "RS3", "RS6", "RS3-RS6", 100.0));
            crossroadStretches.put("RS3-RS8", new CrossroadStretchModel(1, "RS3", "RS8", "RS3-RS8", 0.0));
            crossroadStretches.put("RS5-RS2", new CrossroadStretchModel(1, "RS5", "RS2", "RS5-RS2", 25.0));
            crossroadStretches.put("RS5-RS6", new CrossroadStretchModel(1, "RS5", "RS6", "RS5-RS6", 25.0));
            crossroadStretches.put("RS5-RS8", new CrossroadStretchModel(1, "RS5", "RS8", "RS5-RS8", 50.0));
            crossroadStretches.put("RS7-RS2", new CrossroadStretchModel(1, "RS7", "RS2", "RS7-RS2", 80.0));
            crossroadStretches.put("RS7-RS4", new CrossroadStretchModel(1, "RS7", "RS4", "RS7-RS4", 10.0));
            crossroadStretches.put("RS7-RS8", new CrossroadStretchModel(1, "RS7", "RS8", "RS7-RS8", 10.0));
            crossroadsStretches.put(1, crossroadStretches);

            // Tramos de cruce abiertos por cada semáforo y en cada estado por cruce de la ciudad
            crossroadsStatesTrafficLightsCrossroadStretchesNames = new HashMap<>();
            Map<Integer, Map<Integer, Set<String>>> statesTrafficLightsCrossroadStretchesNames = new HashMap<>();
            // Estado 1 (S1)
                // Semáforo 2 (TL2)
            Map<Integer, Set<String>> trafficLightsCrossroadStretchesNames = new HashMap<>();
            Set<String> crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS3-RS4");
            crossroadStretchesNames.add("RS3-RS6");
            trafficLightsCrossroadStretchesNames.put(2, crossroadStretchesNames);
                // Semáforo 4 (TL4)
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS7-RS2");
            crossroadStretchesNames.add("RS7-RS8");
            trafficLightsCrossroadStretchesNames.put(4, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(1, trafficLightsCrossroadStretchesNames);
            // Estado 2 (S2)
                // Semáforo 2 (TL2)
            trafficLightsCrossroadStretchesNames = new HashMap<>();
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS3-RS4");
            crossroadStretchesNames.add("RS3-RS6");
            crossroadStretchesNames.add("RS3-RS8");
            trafficLightsCrossroadStretchesNames.put(2, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(2, trafficLightsCrossroadStretchesNames);
            // Estado 3 (S3)
                // Semáforo 4 (TL4)
            trafficLightsCrossroadStretchesNames = new HashMap<>();
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS7-RS2");
            crossroadStretchesNames.add("RS7-RS4");
            crossroadStretchesNames.add("RS7-RS8");
            trafficLightsCrossroadStretchesNames.put(4, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(3, trafficLightsCrossroadStretchesNames);
            // Estado 4 (S4)
                // Semáforo 1 (TL1)
            trafficLightsCrossroadStretchesNames = new HashMap<>();
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS1-RS2");
            crossroadStretchesNames.add("RS1-RS4");
            trafficLightsCrossroadStretchesNames.put(1, crossroadStretchesNames);
                // Semáforo 3 (TL3)
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS5-RS6");
            crossroadStretchesNames.add("RS5-RS8");
            trafficLightsCrossroadStretchesNames.put(3, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(4, trafficLightsCrossroadStretchesNames);
            // Estado 5 (S5)
                // Semáforo 1 (TL1)
            trafficLightsCrossroadStretchesNames = new HashMap<>();
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS1-RS2");
            crossroadStretchesNames.add("RS1-RS4");
            crossroadStretchesNames.add("RS1-RS6");
            trafficLightsCrossroadStretchesNames.put(1, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(5, trafficLightsCrossroadStretchesNames);
            // Estado 6 (S6)
                // Semáforo 3 (TL3)
            trafficLightsCrossroadStretchesNames = new HashMap<>();
            crossroadStretchesNames = new HashSet<>();
            crossroadStretchesNames.add("RS5-RS2");
            crossroadStretchesNames.add("RS5-RS6");
            crossroadStretchesNames.add("RS5-RS8");
            trafficLightsCrossroadStretchesNames.put(3, crossroadStretchesNames);
            statesTrafficLightsCrossroadStretchesNames.put(6, trafficLightsCrossroadStretchesNames);
            crossroadsStatesTrafficLightsCrossroadStretchesNames.put(1, statesTrafficLightsCrossroadStretchesNames);
        }
    }

    private void initAgents() {
        JADEBoot connection = new JADEBoot();
        System.out.println("¡Hello MURAT!");
        connection.launchAgent(cityModel.getName(), CityAgent.class);
        crossroads.forEach((crossroadId, crossroadModel) -> connection.launchAgent(crossroadModel.getName(), CrossroadAgent.class));
        crossroadTrafficLights.forEach((crossroadId, trafficLights) -> trafficLights.
                forEach((trafficLightId, trafficLightModel) -> connection.launchAgent(trafficLightModel.getName(), TrafficLightAgent.class)));
    }

    public CrossroadModel getCrossroadModel(Integer crossroadId) {
        return crossroads.get(crossroadId);
    }

    public Map<Integer, TrafficLightModel> getCrossroadTrafficLights(Integer crossroadId) {
        return crossroadTrafficLights.get(crossroadId);
    }

    public Map<Integer, StateModel> getCrossroadStates(Integer crossroadId) {
        return crossroadStates.get(crossroadId);
    }

    public Integer getCrossroadInitialState(Integer crossroadId) {
        return cityConfiguration.get(cityConfigurationId).getCrossroadsInitialState().get(crossroadId).getStateId();
    }

    public Map<Integer, Map<Integer, String>> getCrossroadTrafficLightsColorsPerCrossroadState(Integer crossroadId) {
        return trafficLightsColorsPerCrossroadsStates.get(crossroadId);
    }

    public Map<String, RoadStretchModel> getCrossroadRoadStretchesIn(Integer crossroadId) {
        Map<String, RoadStretchModel> roadStretchesIn = new HashMap<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadDestinationId = roadStretchModel.getCrossroadDestinationId();
            if (crossroadDestinationId != null && crossroadDestinationId.equals(crossroadId)) {
                roadStretchesIn.put(roadStretchName, roadStretchModel);
            }
        });
        return roadStretchesIn;
    }

    public Map<String, RoadStretchModel> getCrossroadRoadStretchesOut(Integer crossroadId) {
        Map<String, RoadStretchModel> roadStretchesOut = new HashMap<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadOriginId = roadStretchModel.getCrossroadOriginId();
            if (crossroadOriginId != null && crossroadOriginId.equals(crossroadId)) {
                roadStretchesOut.put(roadStretchName, roadStretchModel);
            }
        });
        return roadStretchesOut;
    }

    public Map<String, CrossroadStretchModel> getCrossroadCrossroadsStretches(Integer crossroadId) {
        return crossroadsStretches.get(crossroadId);
    }

    public Map<Integer, Map<Integer, Set<String>>> getCrossroadStatesTrafficLightsCrossroadStretches(Integer crossroadId) {
        return crossroadsStatesTrafficLightsCrossroadStretchesNames.get(crossroadId);
    }

    public Integer getTrafficLightCrossroadId(Integer trafficLightId) {
        // Obtenemos los semáforos que hay en cada cruce y comprobamos si el semáforo se encuentra ahí
        for (var crossroadTrafficLightsEntry : crossroadTrafficLights.entrySet()) {
            if (crossroadTrafficLightsEntry.getValue().containsKey(trafficLightId)) {
                return crossroadTrafficLightsEntry.getKey();
            }
        }
        return -1;
    }

    public String getTrafficLightRoadStretchInName(Integer crossroadId, Integer trafficLightId) {
        return crossroadTrafficLights.get(crossroadId).get(trafficLightId).getRoadStretchIn();
    }

    public Map<Integer, CrossroadModel> getCityCrossroads() {
        return crossroads;
    }
}
