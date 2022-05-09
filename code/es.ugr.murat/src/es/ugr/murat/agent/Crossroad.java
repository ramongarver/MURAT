package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.constant.TrafficLightConstant;
import es.ugr.murat.model.CrossroadModel;
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
 * Clase representando al agente cruce (Crossroad).
 *
 * @author Ramón García Verjaga
 */
public class Crossroad extends MURATBaseAgent {

    private Integer id;
    private Integer status;
    private CrossroadModel crossroadModel;
    private Map<Integer, TrafficLightModel> trafficLights;
    private Map<Integer, StateModel> states;
    private Integer initialState;
    private Integer currentState;
    private Map<Integer, Map<Integer, String>> trafficLightsColorsPerState;

    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        id = Integer.parseInt(this.getLocalName().split(CrossroadConstant.AGENT_NAME)[1]);
        status = CrossroadConstant.LOAD_DATA;
        crossroadModel = null;
        trafficLights = null;
        states = null;
        initialState = null;
        currentState = null;
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
        crossroadModel = Simulation.simulation.getCrossroadModel(id);
        // Obtenemos datos de los semáforos del cruce
        trafficLights = Simulation.simulation.getCrossroadTrafficLights(id);
        // Obtenemos datos de los estados del cruce
        states = Simulation.simulation.getCrossroadStates(id);
        // Obtenemos el estado inicial del cruce
        initialState = Simulation.simulation.getCrossroadInitialState(id);
        // Obtenemos los colores de semáforos para cada estado del cruce
        trafficLightsColorsPerState = Simulation.simulation.getCrossroadTrafficLightsColorsPerCrossroadState(id);
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
        status = CrossroadConstant.FINALIZE_TRAFFIC_LIGHTS;
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
    //******************************************************//

    // Utilidades y otros
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

}
