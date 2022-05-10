package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.constant.TrafficLightConstant;
import es.ugr.murat.model.CrossroadModel;
import es.ugr.murat.model.CrossroadStretchModel;
import es.ugr.murat.model.RoadStretchModel;
import es.ugr.murat.model.StateModel;
import es.ugr.murat.model.TrafficLightModel;
import es.ugr.murat.simulation.Simulation;
import es.ugr.murat.util.Logger;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Clase representando al agente cruce (CrossroadAgent).
 *
 * @author Ramón García Verjaga
 */
public class CrossroadAgent extends MURATBaseAgent {

    private Integer crossroadId;
    private CrossroadModel crossroadModel;
    private Map<Integer, TrafficLightModel> trafficLights;
    private Map<Integer, StateModel> states;
    private Integer initialState;
    private Integer currentState;
    private Map<Integer, Map<Integer, String>> trafficLightsColorsPerState;
    private Map<String, RoadStretchModel> roadStretchesIn;
    private Map<String, RoadStretchModel> roadStretchesOut;
    private Map<String, CrossroadStretchModel> crossroadsStretches;
    private Map<Integer, Map<Integer, Set<String>>> statesTrafficLightsCrossroadStretches;

    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        status = CrossroadConstant.LOAD_DATA;
        crossroadId = Integer.parseInt(this.getLocalName().split(CrossroadConstant.AGENT_NAME)[1]);
        crossroadModel = null;
        trafficLights = null;
        states = null;
        initialState = null;
        currentState = null;
        trafficLightsColorsPerState = null;
        roadStretchesIn = null;
        roadStretchesOut = null;
        crossroadsStretches = null;
        statesTrafficLightsCrossroadStretches = null;
        Logger.info(ActionConstant.LAUNCHED_AGENT, this.getClass().getSimpleName(), this.getLocalName());
    }

    @Override
    protected void execute() {
        switch (status) {
            case CrossroadConstant.LOAD_DATA -> loadData();
            case CrossroadConstant.INITIALIZE_TRAFFIC_LIGHTS -> initializeTrafficLights();
            case CrossroadConstant.CONTROL_TRAFFIC -> controlTraffic();
            case CrossroadConstant.FINALIZE_TRAFFIC_LIGHTS -> finalizeTrafficLights();
            case CrossroadConstant.EXIT -> exit();
        }
    }

    protected void loadData() {
        Logger.info(ActionConstant.LOADING_DATA, this.getClass().getSimpleName(), this.getLocalName());
        // Cargamos datos del cruce
            // Obtenemos datos generales del cruce
        crossroadModel = Simulation.simulation.getCrossroadModel(crossroadId);
            // Obtenemos datos de los semáforos del cruce
        trafficLights = Simulation.simulation.getCrossroadTrafficLights(crossroadId);
            // Obtenemos datos de los estados del cruce
        states = Simulation.simulation.getCrossroadStates(crossroadId);
            // Obtenemos el estado inicial del cruce
        initialState = Simulation.simulation.getCrossroadInitialState(crossroadId);
            // Obtenemos los colores de semáforos para cada estado del cruce
        trafficLightsColorsPerState = Simulation.simulation.getCrossroadTrafficLightsColorsPerCrossroadState(crossroadId);
            // Obtenemos los tramos de calle tanto de entrada al cruce
        roadStretchesIn = Simulation.simulation.getCrossroadRoadStretchesIn(crossroadId);
            // Obtenemos los tramos de calle tanto de salida del cruce
        roadStretchesOut = Simulation.simulation.getCrossroadRoadStretchesOut(crossroadId);
            // Obtenemos los tramos de cruce del cruce
        crossroadsStretches = Simulation.simulation.getCrossroadCrossroadsStretches(crossroadId);
            // Obtenemos los tramos de cruce abiertos por cada semáforo en verde en cada estado
        statesTrafficLightsCrossroadStretches = Simulation.simulation.getCrossroadStatesTrafficLightsCrossroadStretches(crossroadId);
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = CrossroadConstant.INITIALIZE_TRAFFIC_LIGHTS;
    }

    protected void initializeTrafficLights() {
        // Enviamos un mensaje a cada semáforo para inicializarlo con en color que corresponda en función del estado inicial del cruce
        trafficLightsColorsPerState.get(initialState).forEach((trafficLightId, color) ->
                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
                        new AID(TrafficLightConstant.AGENT_NAME + trafficLightId, AID.ISLOCALNAME),
                        TrafficLightConstant.RED.equals(color) ? MessageConstant.SET_LIGHT_TO_RED : MessageConstant.SET_LIGHT_TO_GREEN));
        currentState = initialState;
        status = CrossroadConstant.CONTROL_TRAFFIC;
    }

    protected void controlTraffic() {
        this.changeToTheNextState();
        this.listenMessages();
    }

    protected void finalizeTrafficLights() {
        // Enviamos un mensaje a cada semáforo para pedirle que finalice
        trafficLights.forEach((trafficLightId, trafficLightModel) ->
                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
                        new AID(TrafficLightConstant.AGENT_NAME + trafficLightId, AID.ISLOCALNAME),
                        MessageConstant.FINALIZE));
        status = CrossroadConstant.EXIT;
    }

    protected void exit() {
        exit = true;
    }
    //**************************************************//

    //*************** Utilidades y otros ***************//
    // Escuchamos mensajes
    private void listenMessages() {
        this.receiveACLMessage();
        switch (incomingMessage.getPerformative()) {
            case ACLMessage.INFORM -> { // Si la performativa es INFORM
            }
            case ACLMessage.REQUEST -> { // Si la performativa es REQUEST
                // Finalizamos el agente
                if (MessageConstant.FINALIZE.equals(incomingMessage.getContent())) {
                    status = CrossroadConstant.FINALIZE_TRAFFIC_LIGHTS;
                    // TODO: INFORM
                }
                // Manejamos mensajes no conocidos
                else {
                    Logger.info(ActionConstant.MESSAGE_UNKNOWN_RECEIVED, this.getClass().getSimpleName(), this.getLocalName()); // TODO: pensar si manejar esto de otra forma
                }
            }
        }
    }

    // Cambiamos al siguiente estado del cruce
    private void changeToTheNextState() {
        this.changeToState(currentState == states.size() ? 1 : currentState + 1);
    }

    // Cambiamos a cualquier estado del cruce
    private void changeToState(Integer newState) {
        // Comprobamos si el estado no es el actual y si existe
        if (!currentState.equals(newState) && states.containsKey(newState)) {
            Map<Integer, String> trafficLightsColorsCurrentState = trafficLightsColorsPerState.get(currentState);
            Map<Integer, String> trafficLightsColorsNewState = trafficLightsColorsPerState.get(newState);
            Set<Integer> changingTrafficLights = new HashSet<>();
            // Obtenemos los ids de los semáforos que cambian de color de un estado a otro
            trafficLightsColorsCurrentState.forEach((trafficLightId, color) -> {
                if (!color.equals(trafficLightsColorsNewState.get(trafficLightId))) {
                    changingTrafficLights.add(trafficLightId);
                }
            });
            // Enviamos mensajes de cambio de color a los semáforos que cambian
            changingTrafficLights.forEach((trafficLightId) ->
                    this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
                            new AID(TrafficLightConstant.AGENT_NAME + trafficLightId, AID.ISLOCALNAME),
                            MessageConstant.CHANGE_LIGHT));

            Logger.info(ActionConstant.STATE_CHANGED, this.getClass().getSimpleName(), this.getLocalName(), "||Previous::" + currentState + "||New::" + newState);
            currentState = newState;
        }
    }
    //**************************************************//
}
