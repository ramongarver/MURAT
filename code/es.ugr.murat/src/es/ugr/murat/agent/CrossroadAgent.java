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

import java.util.HashMap;
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

    // Simulación
    private Integer currentTicks;
    private Integer stateTicks;
    private Integer totalTicks;


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
        currentTicks = 0;
        stateTicks = 0;
        totalTicks = 580;
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
        // Mostramos el estado actual del cruce
        Logger.info(this.writeCurrentState());

        // Comprobamos si ha finalizado la simulación
        if (this.isSimulationFinished()) {
            status = CrossroadConstant.FINALIZE_TRAFFIC_LIGHTS;
        }

        // Comprobamos si hay que cambiar de estado
        Integer stateDuration = states.get(currentState).getDurationTime();
        if (stateTicks.equals(stateDuration)) {
            this.changeToTheNextState();
        }

        // Movemos el tráfico a otras calles (hacemos que los vehículos pasen por el cruce)
        this.moveTrafficForward();

        // Añadimos tráfico a la simulación
        this.addTraffic();
        // this.moveVehicles();
        // this.inform();

        currentTicks++;
        stateTicks++;
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

    // Hacemos que el tráfico avance
    private void moveTrafficForward() {
        // Obtenemos los ids de los semáforos en verde
        Set<Integer> greenTrafficLightsIds = this.getGreenTrafficLightsIds();

        Map<String, Map<String, Double>> crossroadStretches = new HashMap<>(); // (roadStretchOriginName -> roadStretchDestinationName -> carsPercentageFromOriginToDestination)
        Map<String, Double> destinationRoadStretch = new HashMap<>(); // (roadStretchDestinationName -> carsPercentageFromOriginToDestination)
        greenTrafficLightsIds.forEach((trafficLightId) -> { // Para cada semáforo en verde
            String originRoadStretchName = trafficLights.get(trafficLightId).getRoadStretchIn(); // Obtenemos la calle que regula, calle origen del cruce asociada al semáforo
            statesTrafficLightsCrossroadStretches.get(currentState).get(trafficLightId).forEach((crossroadStretchName) -> { // Obtenemos el conjunto de cruces que habilita estando en verde
                String destinationRoadStretchName = getCrossroadStretchDestination(crossroadStretchName); // Obtenemos la calle de destino
                Double carsPercentageFromOriginToDestination = crossroadsStretches.get(crossroadStretchName).getCarsPercentageFromOriginToDestination(); // Obtenemos el porcentaje de coches interesados a ir a ese destino
                destinationRoadStretch.put(destinationRoadStretchName, carsPercentageFromOriginToDestination); // Añadimos el destino con el porcentaje asociado
            });
            crossroadStretches.put(originRoadStretchName, destinationRoadStretch); // Añadimos el origen con todos los destinos asociados para ese estado y semáforo
        });
    }

    // Obtenemos los ids de los semáforos en verde
    private Set<Integer> getGreenTrafficLightsIds() {
        Set<Integer> greenTrafficLightsIds = new HashSet<>();
        trafficLightsColorsPerState.get(currentState).forEach((trafficLightId, color) -> {
            if (TrafficLightConstant.GREEN.equals(color)) {
                greenTrafficLightsIds.add(trafficLightId);
            }
        });
        return greenTrafficLightsIds;
    }

    private String getCrossroadStretchOrigin(String crossroadStretchName) {
        return crossroadStretchName.split("-")[0];
    }

    private String getCrossroadStretchDestination(String crossroadStretchName) {
        return crossroadStretchName.split("-")[1];
    }

    // Añadimos tráfico a la simulación
    private void addTraffic() {
        roadStretchesIn.forEach((roadStretchInName, roadStretchInModel) -> {
            Integer roadStretchVehicles = roadStretchInModel.getVehicles();
            if (this.isRoadStretchFull(roadStretchInModel)) {
                roadStretchInModel.setVehicles(roadStretchVehicles + 1);
            }
        });
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
            // Recibimos mensajes de información de cambio de color de los semáforos que cambian
            for (int i = 0; i < changingTrafficLights.size(); i++) {
                this.listenMessages();
            }

            Logger.info(ActionConstant.STATE_CHANGED, this.getClass().getSimpleName(), this.getLocalName(), "||Previous::" + currentState + "||New::" + newState);
            currentState = newState;
            stateTicks = 0;
        }
    }

    private Boolean isSimulationFinished() {
        return currentTicks > totalTicks;
    }

    private Boolean isRoadStretchFull (RoadStretchModel roadStretchModel) {
        Integer roadStretchVehicles = roadStretchModel.getVehicles();
        Integer roadStretchMaxVehicles = roadStretchModel.getMaxVehicles();
        return !(roadStretchVehicles < roadStretchMaxVehicles);
    }

    private String writeCurrentState() {
        String state = "" +
                "||" + "currentTicks:" + currentTicks +
                "||" + "currentStateTicks:" + stateTicks + "/" + states.get(currentState).getDurationTime() +
                "||" + "state:" + currentState;

        var ref1 = new Object() {
            String str = "||";
        };
        trafficLightsColorsPerState.get(currentState).forEach((trafficLightId, color) ->
                ref1.str = ref1.str + "TL" + trafficLightId + ":" + color + " ");
        String str = ref1.str;
        if (str.endsWith(" ")) {
            str = str.substring(0, str.length()-1);
        }

        if ((!"||".equals(str))) {
            state += str;
        }

        var ref2 = new Object() {
            String str = "||";
        };
        roadStretchesIn.forEach((roadStretchInName, roadStretchInModel) ->
                ref2.str = ref2.str + roadStretchInName + ":" + roadStretchInModel.getVehicles() + "/" + roadStretchInModel.getMaxVehicles() + "(" + String.format("%.2f", roadStretchInModel.getOccupancyPercentage()) + "%)" + " ");
        str = ref2.str;
        if (str.endsWith(" ")) {
            str = str.substring(0, str.length()-1);
        }

        if ((!"||".equals(str))) {
            state += str;
        }

        state += "||";

        return state;
    }
    //**************************************************//
}
