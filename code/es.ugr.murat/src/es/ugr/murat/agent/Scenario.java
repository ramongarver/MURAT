package es.ugr.murat.agent;

import es.ugr.murat.model.*;
import jade.core.Agent;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class Scenario extends Agent {

    private final String name;
    private final ConfigurationModel configurationModel;
    private final ConfigurationCrossroadInitialStateModel configurationCrossroadInitialStateModel;
    private final Map<Integer, CrossroadModel> crossroads;
    private final Map<Integer, Map<Integer, TrafficLightModel>> crossroadTrafficLights;
    private final Map<Integer, Map<Integer, StateModel>> crossroadStates;

    public Scenario() {
        name = "Simple cross scenario";
        configurationModel = new ConfigurationModel(1, 4.0, 0.05, 2.0, 2.0,
                LocalTime.of(06, 00), LocalTime.of(21, 00), Duration.ofSeconds(15), "linear");

        Map<Integer, Integer> crossroadInitialState = new HashMap<>();
        crossroadInitialState.put(1, 1);
        configurationCrossroadInitialStateModel = new ConfigurationCrossroadInitialStateModel(crossroadInitialState);

        crossroads = new HashMap<>();
        crossroads.put(1, new CrossroadModel(1, "C1", Duration.ofSeconds(20), Duration.ofSeconds(540)));

        crossroadTrafficLights = new HashMap<>();
        Map<Integer, TrafficLightModel> trafficLights = new HashMap<>();
        trafficLights.put(1, new TrafficLightModel(1, "S1"));
        trafficLights.put(2, new TrafficLightModel(2, "S2"));
        trafficLights.put(3, new TrafficLightModel(3, "S3"));
        trafficLights.put(4, new TrafficLightModel(4, "S4"));
        crossroadTrafficLights.put(1, trafficLights);

        crossroadStates = new HashMap<>();
        Map<Integer, StateModel> states = new HashMap<>();
        states.put(1, new StateModel(1, "E1", Duration.ofSeconds(120)));
        states.put(2, new StateModel(2, "E2", Duration.ofSeconds(120)));
        states.put(3, new StateModel(3, "E3", Duration.ofSeconds(120)));
        states.put(4, new StateModel(4, "E4", Duration.ofSeconds(60)));
        states.put(5, new StateModel(5, "E5", Duration.ofSeconds(60)));
        states.put(6, new StateModel(6, "E5", Duration.ofSeconds(60)));
        crossroadStates.put(1, states);
    }
}
