package es.ugr.murat.simulation;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.ugr.murat.agent.CityAgent;
import es.ugr.murat.agent.CrossroadAgent;
import es.ugr.murat.agent.TrafficLightAgent;
import es.ugr.murat.appboot.JADEBoot;
import es.ugr.murat.constant.CityConstant;
import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.SimulationConstant;
import es.ugr.murat.model.CityConfigurationModel;
import es.ugr.murat.model.CityModel;
import es.ugr.murat.model.ConfigurationCrossroadInitialStateModel;
import es.ugr.murat.model.CrossroadModel;
import es.ugr.murat.model.CrossroadStretchModel;
import es.ugr.murat.model.RoadStretchModel;
import es.ugr.murat.model.StateModel;
import es.ugr.murat.model.TrafficLightModel;
import es.ugr.murat.util.Logger;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Clase representando a una simulación, encargada de obtener todos los datos y lanzar los agentes.
 *
 * @author Ramón García Verjaga
 */
public class Simulation {

    public static Simulation simulation = null;

    // Atributos de elección de la ciudad, configuración y política de optimización de tiempos
    private Integer cityId; // Identificador de la ciudad
    private Integer cityConfigurationId; // Identificador de la configuración de la ciudad
    private Boolean optimizeStateTimesPolicy; // Indicador de política de tiempos variables o fijos

    // Atributos de información sobre la ciudad a simular
    private final CityModel cityModel; // Información de la ciudad (nombre y descripción)
    private final Map<Integer, CityConfigurationModel> cityConfiguration; // Configuración de la ciudad | (cityConfigurationId -> cityConfigurationModel)
    private final Map<Integer, CrossroadModel> crossroads; // Cruces de la ciudad | (crossroadId -> crossroadModel)
    private final Map<Integer, Map<Integer, TrafficLightModel>> crossroadTrafficLights; // Semáforos de la ciudad | (crossroadId -> trafficLightId -> trafficLightModel)
    private final Map<Integer, Map<Integer, StateModel>> crossroadStates; // Estados por cruce de la ciudad | (crossroadId -> stateId -> stateModel)
    private final Map<Integer, Map<Integer, Map<Integer, String>>> trafficLightsColorsPerCrossroadsStates; // Colores de semáforos para cada estado de un cruce | (crossroadId -> stateId -> trafficLightId -> color)
    private final Map<String, RoadStretchModel> roadStretches; // Tramos de calle de la ciudad | (name -> roadStretchModel)
    private final Map<Integer, Map<String, CrossroadStretchModel>> crossroadsStretches; // Tramos de cruce por cruce de la ciudad | (crossroadId -> crossroadStretchName -> crossroadStretchModel)
    private final Map<Integer, Map<Integer, Map<String, Set<String>>>> crossroadsStatesCrossroadStretchesNames; // Tramos de cruce abiertos en cada estado por cruce de la ciudad | (crossroadId -> stateId -> crossroadStretchOrigin -> crossroadStretchDestination)
    private final Map<Integer, Map<Integer, Map<Integer, Set<String>>>> crossroadsStatesTrafficLightsCrossroadStretchesNames; // Tramos de cruce abiertos por cada semáforo y en cada estado por cruce de la ciudad | (crossroadId -> stateId -> trafficLightId -> crossroadStretchNames)

    public static void start() {
        if (simulation == null) {
            simulation = new Simulation();
            simulation.initAgents();
        }
    }

    // Cargamos los datos de la simulación
    private Simulation() {
            // Mostramos el banner de "HELLO MURAT"
            Logger.info(SimulationConstant.BANNER);

            // Obtenemos las ciudades disponibles y seleccionamos una de ellas para la simulación
            List<String> cities = this.getCitiesFromFolder(); // Ciudades disponibles en la simulación
            List<String> configurations = new ArrayList<>(); // Configuraciones disponibles para la ciudad seleccionada
            String[] citiesArray = cities.toArray(new String[0]);
            String selectedCity = (String)
                    JOptionPane.showInputDialog(null, SimulationConstant.SELECT_CITY, SimulationConstant.CITY,
                            JOptionPane.QUESTION_MESSAGE, null, citiesArray, citiesArray[0]);
            cityId = Integer.parseInt(selectedCity.split(CityConstant.AGENT_NAME)[1]);

            // Leemos el JSON de la ciudad seleccionada
            JsonObject simulationJsonObject = null;
            try {
                FileReader fileReader = new FileReader(SimulationConstant.DATA_PATH + selectedCity + ".json");
                simulationJsonObject = Json.parse(fileReader).asObject();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Información de la ciudad
            JsonObject cityJsonObject = simulationJsonObject.get("city").asObject();
            String cityName = cityJsonObject.get("name").asString();
            String description = cityJsonObject.get("description").asString();
            cityModel = new CityModel(cityName, description);

            // Configuración de la ciudad
            cityConfiguration = new HashMap<>();
                // Obtenemos cada una de las configuraciones
            JsonArray cityConfigurationsJsonArray = simulationJsonObject.get("cityConfiguration").asArray();
            for (JsonValue cityConfigurationJsonValue : cityConfigurationsJsonArray) {
                JsonObject cityConfigurationJsonObject = cityConfigurationJsonValue.asObject();
                Integer configurationId = cityConfigurationJsonObject.get("id").asInt();
                Double vehicleLength = cityConfigurationJsonObject.get("vehicleLength").asDouble();
                Double inputRatio = Double.parseDouble(cityConfigurationJsonObject.get("inputRatio").asString());
                Double inputInnerRatio = cityConfigurationJsonObject.get("inputInnerRatio").asDouble();
                Double outputInnerRatio =  cityConfigurationJsonObject.get("outputInnerRatio").asDouble();
                LocalTime initialTime = LocalTime.of(cityConfigurationJsonObject.get("initialTime").asInt(), 0, 0);
                LocalTime finalTime = LocalTime.of(cityConfigurationJsonObject.get("finalTime").asInt(), 0, 0);
                Duration sampleTime = Duration.ofSeconds(cityConfigurationJsonObject.get("sampleTime").asInt());
                String mode = cityConfigurationJsonObject.get("mode").asString();
                Map<Integer, ConfigurationCrossroadInitialStateModel> crossroadsInitialState = new HashMap<>();
                JsonArray crossroadInitialStatesJsonArray = cityConfigurationJsonObject.get("crossroadInitialState").asArray();
                for (JsonValue crossroadInitialStateJsonValue : crossroadInitialStatesJsonArray) {
                    JsonObject crossroadInitialStateJsonObject = crossroadInitialStateJsonValue.asObject();
                    Integer crossroadId = crossroadInitialStateJsonObject.get("crossroadId").asInt();
                    Integer initialStateId = crossroadInitialStateJsonObject.get("initialStateId").asInt();
                    ConfigurationCrossroadInitialStateModel configurationCrossroadInitialStateModel =
                            new ConfigurationCrossroadInitialStateModel(crossroadId, initialStateId);
                    crossroadsInitialState.put(crossroadId, configurationCrossroadInitialStateModel);
                }
                CityConfigurationModel cityConfigurationModel =
                        new CityConfigurationModel(configurationId, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio,
                                initialTime, finalTime, sampleTime, mode, crossroadsInitialState);
                cityConfiguration.put(configurationId, cityConfigurationModel);
                configurations.add(configurationId.toString()); // Configuraciones disponibles para la ciudad seleccionada
            }

            // Obtenemos las configuraciones disponibles de la ciudad seleccionada y seleccionamos una de ellas para la simulación
            String[] configurationsArray = configurations.toArray(new String[0]);
            String selectedConfiguration = (String)
                    JOptionPane.showInputDialog(null, SimulationConstant.SELECT_CONFIGURATION, SimulationConstant.CONFIGURATION,
                            JOptionPane.QUESTION_MESSAGE, null, configurationsArray, configurationsArray[0]);
            cityConfigurationId = Integer.parseInt(selectedConfiguration);

            // Obtenemos los valores de configuración necesarios para la creación de roadStretches
            Double vehicleLength = cityConfiguration.get(cityConfigurationId).getVehicleLength();
            Double inputRatio = cityConfiguration.get(cityConfigurationId).getInputRatio();
            Double inputInnerRatio = cityConfiguration.get(cityConfigurationId).getInputInnerRatio();
            Double outputInnerRatio = cityConfiguration.get(cityConfigurationId).getOutputInnerRatio();

            // Seleccionamos una política de tiempos para la simulación
            List<String> policies = new ArrayList<>();
            policies.add(SimulationConstant.FIXED_TIME_POLICY);
            policies.add(SimulationConstant.VARIABLE_TIME_POLICY);
            String[] policiesArray = policies.toArray(new String[0]);
            String selectedPolicy = (String)
                    JOptionPane.showInputDialog(null, SimulationConstant.SELECT_POLICY, SimulationConstant.CONFIGURATION,
                            JOptionPane.QUESTION_MESSAGE, null, policiesArray, configurationsArray[0]);
            optimizeStateTimesPolicy = SimulationConstant.VARIABLE_TIME_POLICY.equals(selectedPolicy);

            // Cruces de la ciudad
            crossroads = new HashMap<>(); // Cruces de la ciudad
            crossroadTrafficLights = new HashMap<>(); // Semáforos por cruce de la ciudad
            crossroadStates = new HashMap<>(); // Estados por cruce de la ciudad
            trafficLightsColorsPerCrossroadsStates = new HashMap<>(); // Colores de semáforos por estado por cruce de la ciudad
            crossroadsStretches = new HashMap<>(); // Tramos de cruce por cruce de la ciudad
            crossroadsStatesCrossroadStretchesNames = new HashMap<>(); // Tramos de cruce abiertos en cada estado por cruce de la ciudad
            crossroadsStatesTrafficLightsCrossroadStretchesNames = new HashMap<>(); // Tramos de cruce abiertos por cada semáforo y en cada estado por cruce de la ciudad
            // Para cada cruce
            JsonArray crossroadsJsonArray = simulationJsonObject.get("crossroad").asArray();
            for (JsonValue crossroadJsonValue : crossroadsJsonArray) {
                // Información del cruce
                JsonObject crossroadJsonObject = crossroadJsonValue.asObject();
                Integer crossroadId = crossroadJsonObject.get("crossroadId").asInt();
                String crossroadName = crossroadJsonObject.get("name").asString();
                Integer minimumStateTime = crossroadJsonObject.get("minimumStateTime").asInt();
                Integer cycleTime = crossroadJsonObject.get("cycleTime").asInt();
                CrossroadModel crossroadModel =
                        new CrossroadModel(crossroadId, crossroadName, minimumStateTime, Duration.ofSeconds(cycleTime));
                crossroads.put(crossroadId, crossroadModel);

                // Semáforos asociados al cruce
                Map<Integer, TrafficLightModel> trafficLights = new HashMap<>();
                JsonArray crossroadTrafficLightsJsonArray = crossroadJsonObject.get("trafficLight").asArray();
                for (JsonValue crossroadTrafficLightJsonValue : crossroadTrafficLightsJsonArray) {
                    JsonObject crossroadTrafficLightJsonObject = crossroadTrafficLightJsonValue.asObject();
                    Integer trafficLightId = crossroadTrafficLightJsonObject.get("trafficLightId").asInt();
                    String trafficLightName = crossroadTrafficLightJsonObject.get("name").asString();
                    String roadStretchInName = crossroadTrafficLightJsonObject.get("roadStretchInName").asString();
                    TrafficLightModel trafficLightModel = new TrafficLightModel(trafficLightId, trafficLightName, roadStretchInName);
                    trafficLights.put(trafficLightId, trafficLightModel);
                }
                crossroadTrafficLights.put(crossroadId, trafficLights);

                // Estados asociados al cruce
                Map<Integer, StateModel> states = new HashMap<>();
                Map<Integer, Map<Integer, String>> trafficLightsColorsPerStates = new HashMap<>();
                Map<Integer, Map<String, Set<String>>> statesCrossroadStretchesNames = new HashMap<>();
                Map<Integer, Map<Integer, Set<String>>> statesTrafficLightsCrossroadStretchesNames = new HashMap<>();
                JsonArray crossroadStatesJsonArray = crossroadJsonObject.get("state").asArray();
                for (JsonValue crossroadStateJsonValue : crossroadStatesJsonArray) {
                    JsonObject crossroadStateJsonObject = crossroadStateJsonValue.asObject();
                    Integer stateId = crossroadStateJsonObject.get("stateId").asInt();
                    String stateName = crossroadStateJsonObject.get("name").asString();
                    Integer duration = crossroadStateJsonObject.get("duration").asInt();
                    StateModel stateModel = new StateModel(stateId, stateName, duration);
                    states.put(stateId, stateModel);

                    // Colores de semáforos asociados a cada estado de un cruce
                    Map<String, Set<String>> crossroadStretchesOriginDestinations = new HashMap<>();
                    Map<Integer, String> trafficLightsColors = new HashMap<>();
                    Map<Integer, Set<String>> trafficLightsCrossroadStretchesNames = new HashMap<>();
                    JsonArray trafficLightsColorsJsonArray = crossroadStateJsonObject.get("trafficLightState").asArray();
                    for (JsonValue trafficLightColorJsonValue : trafficLightsColorsJsonArray) {
                        JsonObject trafficLightColorJsonObject = trafficLightColorJsonValue.asObject();
                        Integer trafficLightId = trafficLightColorJsonObject.get("trafficLightId").asInt();
                        String light = trafficLightColorJsonObject.get("light").asString();
                        trafficLightsColors.put(trafficLightId, light);

                        // Tramo de cruce que habilita el semáforo en el estado
                        Set<String> crossroadStretchesNames = new HashSet<>();
                        JsonArray crossroadStretchRoutesJsonArray = trafficLightColorJsonObject.get("crossroadStretchRoutes").asArray();
                        for (JsonValue crossroadStretchRouteJsonValue : crossroadStretchRoutesJsonArray) {
                            JsonObject crossroadStretchRouteJsonObject = crossroadStretchRouteJsonValue.asObject();
                            String crossroadStretchName = crossroadStretchRouteJsonObject.get("crossroadStretchName").asString();
                            crossroadStretchesNames.add(crossroadStretchName);
                            String crossroadStretchOrigin = this.getCrossroadStretchOrigin(crossroadStretchName);
                            String crossroadStretchDestination = this.getCrossroadStretchDestination(crossroadStretchName);
                            if (crossroadStretchesOriginDestinations.containsKey(crossroadStretchOrigin)) {
                                crossroadStretchesOriginDestinations.get(crossroadStretchOrigin).add(crossroadStretchDestination);
                            } else {
                                Set<String> crossroadStretchesDestination = new HashSet<>();
                                crossroadStretchesDestination.add(crossroadStretchDestination);
                                crossroadStretchesOriginDestinations.put(crossroadStretchOrigin, crossroadStretchesDestination);
                            }
                        }
                        if (crossroadStretchesNames.size() > 0) {
                            trafficLightsCrossroadStretchesNames.put(trafficLightId, crossroadStretchesNames);
                        }
                    }
                    trafficLightsColorsPerStates.put(stateId, trafficLightsColors);
                    statesCrossroadStretchesNames.put(stateId, crossroadStretchesOriginDestinations);
                    statesTrafficLightsCrossroadStretchesNames.put(stateId, trafficLightsCrossroadStretchesNames);
                }
                crossroadStates.put(crossroadId, states);
                trafficLightsColorsPerCrossroadsStates.put(crossroadId, trafficLightsColorsPerStates);
                crossroadsStatesCrossroadStretchesNames.put(crossroadId, statesCrossroadStretchesNames);
                crossroadsStatesTrafficLightsCrossroadStretchesNames.put(crossroadId, statesTrafficLightsCrossroadStretchesNames);

                // Tramos de cruce asociados al cruce
                Map<String, CrossroadStretchModel> crossroadStretches = new HashMap<>();
                JsonArray crossroadStretchesJsonArray = crossroadJsonObject.get("crossroadStretch").asArray();
                for (JsonValue crossroadStretchJsonValue : crossroadStretchesJsonArray) {
                    JsonObject crossroadStretchJsonObject = crossroadStretchJsonValue.asObject();
                    String originRoadStretchName = crossroadStretchJsonObject.get("originRoadStretchName").asString();
                    String destinationRoadStretchName = crossroadStretchJsonObject.get("destinationRoadStretchName").asString();
                    String crossroadStretchName = crossroadStretchJsonObject.get("name").asString();
                    Double carsPercentageFromOriginToDestination = crossroadStretchJsonObject.get("carsPercentageFromOriginToDestination").asDouble();
                    CrossroadStretchModel crossroadStretchModel =
                            new CrossroadStretchModel(crossroadId, originRoadStretchName, destinationRoadStretchName,
                                    crossroadStretchName, carsPercentageFromOriginToDestination);
                    crossroadStretches.put(crossroadStretchName, crossroadStretchModel);
                }
                crossroadsStretches.put(crossroadId, crossroadStretches);
            }

            // Tramos de calle de la ciudad
            roadStretches = new HashMap<>();
            JsonArray roadStretchesJsonArray = simulationJsonObject.get("roadStretch").asArray();
            for (JsonValue roadStretchJsonValue : roadStretchesJsonArray) {
                JsonObject roadStretchJsonObject = roadStretchJsonValue.asObject();
                JsonValue crossroadOriginIdJsonValue = roadStretchJsonObject.get("crossroadOriginId");
                Integer crossroadOriginId = crossroadOriginIdJsonValue.isNull() ? null : crossroadOriginIdJsonValue.asInt();
                JsonValue crossroadDestinationIdJsonValue = roadStretchJsonObject.get("crossroadDestinationId");
                Integer crossroadDestinationId = crossroadDestinationIdJsonValue.isNull() ? null : crossroadDestinationIdJsonValue.asInt();
                String direction = roadStretchJsonObject.get("direction").asString();
                String roadStretchName = roadStretchJsonObject.get("name").asString();
                Double length = roadStretchJsonObject.get("length").asDouble();
                Integer lanes = roadStretchJsonObject.get("lanes").asInt();
                Integer vehicles = roadStretchJsonObject.get("vehicles").asInt();
                RoadStretchModel roadStretchModel =
                        new RoadStretchModel(crossroadOriginId, crossroadDestinationId, direction, roadStretchName, length, lanes,
                                vehicles, vehicleLength, inputRatio, inputInnerRatio, outputInnerRatio);
                roadStretches.put(roadStretchName, roadStretchModel);
            }
    }

    // Iniciamos los agentes
    private void initAgents() {
        // Creamos la instancia JADEBoot, objeto utilizado para crear a los agentes
        JADEBoot connection = new JADEBoot();
        // Lanzamos agentes
            // Agentes semáforo (TrafficLightAgent)
        crossroadTrafficLights.forEach((crossroadId, trafficLights) -> trafficLights.
                forEach((trafficLightId, trafficLightModel) -> connection.launchAgent(trafficLightModel.getName(), TrafficLightAgent.class)));
            // Agente ciudad (CityAgent)
        connection.launchAgent(cityModel.getName(), CityAgent.class);
            // Agentes cruce (CrossroadAgent)
        crossroads.forEach((crossroadId, crossroadModel) -> connection.launchAgent(crossroadModel.getName(), CrossroadAgent.class));
    }

    //******************* Utilidades *******************//
    // Obtenemos la lista de archivos de ciudades
    private List<String> getCitiesFromFolder() {
        List<String> cities = new ArrayList<>();
        File folder = new File(SimulationConstant.DATA_PATH);
        for (File file : folder.listFiles()) {
            if (!file.isDirectory()) {
                cities.add(file.getName().split(".json")[0]);
            }
        }
        return cities;
    }
    // Obtenemos el origen del cruce | Para la forma RSN-RSM obtenemos RSN
    private String getCrossroadStretchOrigin(String crossroadStretchName) {
        return crossroadStretchName.split("-")[0];
    }
    // Obtenemos el destino del cruce | Para la forma RSN-RSM obtenemos RSM
    private String getCrossroadStretchDestination(String crossroadStretchName) {
        return crossroadStretchName.split("-")[1];
    }
    //**************************************************//

    //*************** API de información inicial de la simulación ***************//
        // Crossroad
    // Obtenemos el modelo del cruce identificado por crossroadId | (crossroadModel)
    public CrossroadModel getCrossroadModel(Integer crossroadId) {
        return crossroads.get(crossroadId);
    }
    // Obtenemos los semáforos del cruce identificado por crossroadId | (trafficLightId -> trafficLightModel)
    public Map<Integer, TrafficLightModel> getCrossroadTrafficLights(Integer crossroadId) {
        return crossroadTrafficLights.get(crossroadId);
    }
    // Obtenemos los estados del cruce identificado por crossroadId | (stateId -> stateModel)
    public Map<Integer, StateModel> getCrossroadStates(Integer crossroadId) {
        return crossroadStates.get(crossroadId);
    }
    // Obtenemos el estado inicial del cruce identificado por crossroadId | (initialStateId)
    public Integer getCrossroadInitialState(Integer crossroadId) {
        return cityConfiguration.get(cityConfigurationId).getCrossroadsInitialState().get(crossroadId).getStateId();
    }
    // Obtenemos los colores de los semáforos por cada estado del cruce identificado por crossroadId | (stateId -> trafficLightId -> color)
    public Map<Integer, Map<Integer, String>> getCrossroadTrafficLightsColorsPerCrossroadState(Integer crossroadId) {
        return trafficLightsColorsPerCrossroadsStates.get(crossroadId);
    }
    // Obtenemos los tramos de calle que entran al cruce identificado por crossroadId | (roadStretchNameIn -> roadStretchModelIn)
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
    // Obtenemos los tramos de calle que entran al cruce identificado por crossroadId desde fuera del sistema | (roadStretchNameIn -> roadStretchModelIn)
    public Map<String, RoadStretchModel> getCrossroadRoadStretchesInFromOutOfSystem(Integer crossroadId) {
        Map<String, RoadStretchModel> roadStretchesInFromOutOfSystem = new HashMap<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadOriginId = roadStretchModel.getCrossroadOriginId();
            Integer crossroadDestinationId = roadStretchModel.getCrossroadDestinationId();
            if (crossroadOriginId == null &&
                    crossroadDestinationId != null && crossroadDestinationId.equals(crossroadId)) {
                roadStretchesInFromOutOfSystem.put(roadStretchName, roadStretchModel);
            }
        });
        return roadStretchesInFromOutOfSystem;
    }
    // Obtenemos los tramos de calle que entran al cruce identificado por crossroadId desde otro cruce | (roadStretchNameIn -> roadStretchModelIn)
    public Map<String, RoadStretchModel> getCrossroadRoadStretchesInFromAnotherCrossroad(Integer crossroadId) {
        Map<String, RoadStretchModel> roadStretchesInFromAnotherCrossroad = new HashMap<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadOriginId = roadStretchModel.getCrossroadOriginId();
            Integer crossroadDestinationId = roadStretchModel.getCrossroadDestinationId();
            if (crossroadOriginId != null && !crossroadOriginId.equals(crossroadId) &&
                    crossroadDestinationId != null && crossroadDestinationId.equals(crossroadId)) {
                roadStretchesInFromAnotherCrossroad.put(roadStretchName, roadStretchModel);
            }
        });
        return roadStretchesInFromAnotherCrossroad;
    }
    // Obtenemos los tramos de calle que salen del cruce identificado por crossroadId | (roadStretchNameOut -> roadStretchModelOut)
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
    // Obtenemos los tramos de calle que salen del cruce identificado por crossroadId hacia fuera del sistema | (roadStretchNameOut -> roadStretchModelOut)
    public Map<String, RoadStretchModel> getCrossroadRoadStretchesOutToOutOfSystem(Integer crossroadId) {
        Map<String, RoadStretchModel> roadStretchesOutToOutOfSystem = new HashMap<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadOriginId = roadStretchModel.getCrossroadOriginId();
            Integer crossroadDestinationId = roadStretchModel.getCrossroadDestinationId();
            if (crossroadOriginId != null && crossroadOriginId.equals(crossroadId) &&
                    crossroadDestinationId == null) {
                roadStretchesOutToOutOfSystem.put(roadStretchName, roadStretchModel);
            }
        });
        return roadStretchesOutToOutOfSystem;
    }
    // Obtenemos los tramos de calle que salen del cruce identificado por crossroadId hacia otro cruce (roadStretchNameOut -> roadStretchModelOut)
    public Map<String, RoadStretchModel> getCrossroadRoadStretchesOutToAnotherCrossroad(Integer crossroadId) {
        Map<String, RoadStretchModel> roadStretchesOutToAnotherCrossroad = new HashMap<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadOriginId = roadStretchModel.getCrossroadOriginId();
            Integer crossroadDestinationId = roadStretchModel.getCrossroadDestinationId();
            if (crossroadOriginId != null && crossroadOriginId.equals(crossroadId) &&
                    crossroadDestinationId != null && !crossroadDestinationId.equals(crossroadId)) {
                roadStretchesOutToAnotherCrossroad.put(roadStretchName, roadStretchModel);
            }
        });
        return roadStretchesOutToAnotherCrossroad;
    }
    // Obtenemos los cruces que tienen tramos de calle de salida que entran a este cruce
    public List<Integer> getCrossroadCrossroadsIn(Integer crossroadId) {
        List<Integer> crossroadsIn = new ArrayList<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadOrigin = roadStretchModel.getCrossroadOriginId();
            Integer crossroadDestination = roadStretchModel.getCrossroadDestinationId();
            if (crossroadId.equals(crossroadDestination) && crossroadOrigin != null) {
                crossroadsIn.add(crossroadOrigin);
            }
        });
        return crossroadsIn;
    }
    // Obtenemos los cruces que tienen tramos de calle de entrada que salen de este cruce
    public List<Integer> getCrossroadCrossroadsOut(Integer crossroadId) {
        List<Integer> crossroadsOut = new ArrayList<>();
        roadStretches.forEach((roadStretchName, roadStretchModel) -> {
            Integer crossroadOrigin = roadStretchModel.getCrossroadOriginId();
            Integer crossroadDestination = roadStretchModel.getCrossroadDestinationId();
            if (crossroadId.equals(crossroadOrigin) && crossroadDestination != null) {
                crossroadsOut.add(crossroadDestination);
            }
        });
        return crossroadsOut;
    }
    // Obtenemos los tramos de cruce del cruce identificado por crossroadId | (crossroadStretchName -> crossroadStretchModel)
    public Map<String, CrossroadStretchModel> getCrossroadCrossroadsStretches(Integer crossroadId) {
        return crossroadsStretches.get(crossroadId);
    }
    // Obtenemos los tramos de cruce habilitados por cada estado del cruce identificado por crossroadId | (crossroadId -> stateId -> crossroadStretchOrigin -> crossroadStretchDestination)
    public Map<Integer, Map<String, Set<String>>> getCrossroadStatesCrossroadStretches(Integer crossroadId) {
        return crossroadsStatesCrossroadStretchesNames.get(crossroadId);
    }
    // Obtenemos los tramos de cruce habilitados por cada semáforo en verde por cada estado del cruce identificado por crossroadId | (stateId -> trafficLightId -> crossroadStretchNames)
    public Map<Integer, Map<Integer, Set<String>>> getCrossroadStatesTrafficLightsCrossroadStretches(Integer crossroadId) {
        return crossroadsStatesTrafficLightsCrossroadStretchesNames.get(crossroadId);
    }
        // TrafficLight
    // Obtenemos el identificador del cruce al que pertenece el semáforo identificado por trafficLightId | (trafficLightId)
    public Integer getTrafficLightCrossroadId(Integer trafficLightId) {
        // Obtenemos los semáforos que hay en cada cruce y comprobamos si el semáforo se encuentra ahí
        for (var crossroadTrafficLightsEntry : crossroadTrafficLights.entrySet()) {
            if (crossroadTrafficLightsEntry.getValue().containsKey(trafficLightId)) {
                return crossroadTrafficLightsEntry.getKey();
            }
        }
        return -1;
    }
    // Obtenemos el tramo de calle de entrada al cruce identificado por crossroadId que regula el semáforo identificado por trafficLightId | (roadStretchName)
    public String getTrafficLightRoadStretchInName(Integer trafficLightId) {
        Integer crossroadId = this.getTrafficLightCrossroadId(trafficLightId);
        return crossroadTrafficLights.get(crossroadId).get(trafficLightId).getRoadStretchIn();
    }
        // City
    // Obtenemos el nombre de la ciudad | (cityName)
    public String getCityName() {
        return cityModel.getName();
    }
    // Obtenemos los cruces de la ciudad | (crossroadId -> crossroadModel)
    public Map<Integer, CrossroadModel> getCityCrossroads() {
        return crossroads;
    }
    // Obtenemos los nombres de los cruces de la ciudad | (crossroadName)
    public List<String> getCityCrossroadsNames() {
        List<String> crossroadNames = new ArrayList<>();
        crossroads.forEach((crossroadId, crossroadModel) -> crossroadNames.add(CrossroadConstant.AGENT_NAME + crossroadId));
        Collections.sort(crossroadNames);
        return crossroadNames;
    }
    // Obtenemos los nombres de los tramos de calle de la ciudad | (roadStretchName)
    public List<String> getCityRoadStretchesNames() {
        List<String> roadStretchesNames = new ArrayList<>();
        roadStretches.forEach((roadStretchId, roadStretchModel) -> roadStretchesNames.add(roadStretchModel.getName()));
        Collections.sort(roadStretchesNames, new Comparator<String>() { // TODO: Extraer comparator
            public int compare(String o1, String o2) {
                if (extractString(o1).equals(extractString(o2)))
                    return extractInt(o1) - extractInt(o2);
                else
                    return o1.compareTo(o2);
            }

            String extractString(String s) {
                String prefix = s.replaceAll("\\d", "");
                return prefix;
            }

            int extractInt(String s) {
                String num = s.replaceAll("\\D", "");
                // return 0 if no digits found
                return num.isEmpty() ? 0 : Integer.parseInt(num);
            }
        });
        return roadStretchesNames;
    }
        // CityConfiguration (Simulación)
    // Obtenemos la hora de inicio de la simulación | (initialTime)
    public LocalTime getCityConfigurationInitialTime() {
        return cityConfiguration.get(cityConfigurationId).getInitialTime();
    }
    // Obtenemos la hora de fin de la simulación | (finalTime)
    public LocalTime getCityConfigurationFinalTime() {
        return cityConfiguration.get(cityConfigurationId).getFinalTime();
    }
    // Obtenemos el tiempo de muestreo de la simulación | (sampleTimeSeconds)
    public Integer getCityConfigurationSampleTime() {
        return (Integer) (int) cityConfiguration.get(cityConfigurationId).getSampleTime().getSeconds();
    }
    // Obtenemos el modo de entrada de vehículo en la simulación | (mode)
    public String getCityConfigurationMode() {
        return cityConfiguration.get(cityConfigurationId).getMode();
    }
    // Obtenemos el total de segundos de la simulación | (totalSimulationSeconds)
        // Simulation
    public Integer getSimulationSeconds() {
        LocalTime initialTime = cityConfiguration.get(cityConfigurationId).getInitialTime();
        LocalTime finalTime = cityConfiguration.get(cityConfigurationId).getFinalTime();
        Duration duration = Duration.between(initialTime, finalTime);
        return (Integer) (int) duration.getSeconds();
    }
    // Obtenemos si la política de optimización de tiempos está activa | (totalSimulationSeconds)
    public Boolean getOptimizeStateTimesPolicy() {
        return optimizeStateTimesPolicy;
    }
    //**************************************************//
}
