package es.ugr.murat.simulation;

import es.ugr.murat.agent.City;
import es.ugr.murat.agent.Crossroad;
import es.ugr.murat.agent.TrafficLight;
import es.ugr.murat.appboot.JADEBoot;
import es.ugr.murat.model.CityModel;
import es.ugr.murat.model.ConfigurationCrossroadInitialStateModel;
import es.ugr.murat.model.ConfigurationModel;
import es.ugr.murat.model.CrossroadModel;
import es.ugr.murat.model.CrossroadStretchModel;
import es.ugr.murat.model.RoadStretchModel;
import es.ugr.murat.model.StateModel;
import es.ugr.murat.model.TrafficLightModel;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Simulation {

    private final CityModel cityModel;
    private final ConfigurationModel configurationModel;
    private final ConfigurationCrossroadInitialStateModel configurationCrossroadInitialStateModel;
    private final Map<Integer, CrossroadModel> crossroads;
    private final Map<Integer, Map<Integer, TrafficLightModel>> crossroadTrafficLights;
    private final Map<Integer, Map<Integer, StateModel>> crossroadStates; // crossroadId -> stateId -> stateModel
    private final Map<Integer, Map<Integer, Map<Integer, String>>> trafficLightsColorsPerCrossroadsStates; // crossroadId -> stateId -> trafficLightId -> Color
    private final Map<String, RoadStretchModel> roadStretches;
    private final Map<Integer, Map<String, CrossroadStretchModel>> crossroadsStretches;

    public static void start() {
        new Simulation();
    }

    private Simulation() {
        cityModel = new CityModel("CITY1", "Simple cross city");
        configurationModel = new ConfigurationModel(1, 4.0, 0.05, 2.0, 2.0,
                LocalTime.of(06, 00), LocalTime.of(21, 00), Duration.ofSeconds(15), "linear");

        Map<Integer, Integer> crossroadInitialState = new HashMap<>();
        crossroadInitialState.put(1, 1);
        configurationCrossroadInitialStateModel = new ConfigurationCrossroadInitialStateModel(crossroadInitialState);

        crossroads = new HashMap<>();
        crossroads.put(1, new CrossroadModel(1, "C1", Duration.ofSeconds(20), Duration.ofSeconds(540)));

        crossroadTrafficLights = new HashMap<>();
        Map<Integer, TrafficLightModel> trafficLights = new HashMap<>();
        trafficLights.put(1, new TrafficLightModel(1, "TL1"));
        trafficLights.put(2, new TrafficLightModel(2, "TL2"));
        trafficLights.put(3, new TrafficLightModel(3, "TL3"));
        trafficLights.put(4, new TrafficLightModel(4, "TL4"));
        crossroadTrafficLights.put(1, trafficLights);

        crossroadStates = new HashMap<>();
        Map<Integer, StateModel> states = new HashMap<>();
        states.put(1, new StateModel(1, "E1", Duration.ofSeconds(120)));
        states.put(2, new StateModel(2, "E2", Duration.ofSeconds(120)));
        states.put(3, new StateModel(3, "E3", Duration.ofSeconds(120)));
        states.put(4, new StateModel(4, "E4", Duration.ofSeconds(60)));
        states.put(5, new StateModel(5, "E5", Duration.ofSeconds(60)));
        states.put(6, new StateModel(6, "E6", Duration.ofSeconds(60)));
        crossroadStates.put(1, states);

        trafficLightsColorsPerCrossroadsStates = new HashMap<>();
        Map<Integer, Map<Integer, String>> trafficLightsColorsPerStates = new HashMap<>();
        Map<Integer, String> trafficLightsColors = new HashMap<>();

        trafficLightsColors.put(1, "R");
        trafficLightsColors.put(2, "G");
        trafficLightsColors.put(3, "R");
        trafficLightsColors.put(4, "G");
        trafficLightsColorsPerStates.put(1, trafficLightsColors);

        trafficLightsColors = new HashMap<>();
        trafficLightsColors.put(1, "R");
        trafficLightsColors.put(2, "G");
        trafficLightsColors.put(3, "R");
        trafficLightsColors.put(4, "R");
        trafficLightsColorsPerStates.put(2, trafficLightsColors);

        trafficLightsColors = new HashMap<>();
        trafficLightsColors.put(1, "R");
        trafficLightsColors.put(2, "R");
        trafficLightsColors.put(3, "R");
        trafficLightsColors.put(4, "G");
        trafficLightsColorsPerStates.put(3, trafficLightsColors);

        trafficLightsColors = new HashMap<>();
        trafficLightsColors.put(1, "G");
        trafficLightsColors.put(2, "R");
        trafficLightsColors.put(3, "G");
        trafficLightsColors.put(4, "R");
        trafficLightsColorsPerStates.put(4, trafficLightsColors);

        trafficLightsColors = new HashMap<>();
        trafficLightsColors.put(1, "G");
        trafficLightsColors.put(2, "R");
        trafficLightsColors.put(3, "R");
        trafficLightsColors.put(4, "R");
        trafficLightsColorsPerStates.put(5, trafficLightsColors);


        trafficLightsColors = new HashMap<>();
        trafficLightsColors.put(1, "R");
        trafficLightsColors.put(2, "R");
        trafficLightsColors.put(3, "G");
        trafficLightsColors.put(4, "R");
        trafficLightsColorsPerStates.put(6, trafficLightsColors);
        trafficLightsColorsPerCrossroadsStates.put(1, trafficLightsColorsPerStates);

        roadStretches = new HashMap<>();
        roadStretches.put("RS1", new RoadStretchModel(null, 1, "W", "RS1",
                600.0, 2, 0.0, 300, 0.1, 4.0, "root"));
        roadStretches.put("RS2", new RoadStretchModel(1, null, "N", "RS2",
                200.0, 2, 0.0, 100, 4.0, 4.0, "left"));
        roadStretches.put("RS3", new RoadStretchModel(null, 1, "S", "RS3",
                600.0, 2, 0.0, 300, 0.1, 4.0, "root"));
        roadStretches.put("RS4", new RoadStretchModel(1, null, "W", "RS4",
                200.0, 2, 0.0, 100, 4.0, 4.0, "left"));
        roadStretches.put("RS5", new RoadStretchModel(null, 1, "E", "RS5",
                600.0, 2, 0.0, 300, 0.1, 4.0, "root"));
        roadStretches.put("RS6", new RoadStretchModel(1, null, "S", "RS6",
                200.0, 2, 0.0, 100, 4.0, 4.0, "left"));
        roadStretches.put("RS7", new RoadStretchModel(null, 1, "N", "RS7",
                600.0, 2, 0.0, 300, 0.1, 4.0, "root"));
        roadStretches.put("RS8", new RoadStretchModel(1, null, "E", "RS8",
                200.0, 2, 0.0, 100, 4.0, 4.0, "left"));

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

        initAgents();
    }

    private void initAgents() {
        System.out.println("¡Hello MURAT!");
        JADEBoot connection = new JADEBoot();
        connection.launchAgent(cityModel.getName(), City.class);
        crossroads.forEach((crossroadId, crossroadModel) -> connection.launchAgent(crossroadModel.getName(), Crossroad.class));
        crossroadTrafficLights.forEach((crossroadId, trafficLights) -> trafficLights.
                forEach((trafficLightId, trafficLightModel) -> connection.launchAgent(trafficLightModel.getName(), TrafficLight.class)));
    }

}
