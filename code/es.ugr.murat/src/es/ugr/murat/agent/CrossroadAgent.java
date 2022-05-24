package es.ugr.murat.agent;

import com.eclipsesource.json.JsonObject;
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
import java.util.Random;
import java.util.Set;

/**
 * Clase representando al agente cruce (CrossroadAgent).
 *
 * @author Ramón García Verjaga
 */
public class CrossroadAgent extends MURATBaseAgent {

    private String cityName; // Nombre de la ciudad
    private Integer crossroadId; // Identificador del cruce
    private CrossroadModel crossroadModel; // Información del cruce
    private Map<Integer, TrafficLightModel> trafficLights; // Semáforos del cruce | (trafficLightId -> trafficLightModel)
    private Map<Integer, StateModel> states; // Estados del cruce | (stateId -> stateModel)
    private Integer initialState; // Estado inicial del cruce
    private Integer currentState; // Estado actual del cruce
    private Map<Integer, Map<Integer, String>> trafficLightsColorsPerState; // Colores de semáforos para cada estado | (stateId -> trafficLightId -> color)
    private Map<String, RoadStretchModel> roadStretchesIn; // Tramos de calle que entran al cruce | (roadStretchInName -> roadStretchModel)
    private Map<String, RoadStretchModel> roadStretchesOut; // Tramos de calle que salen del cruce | (roadStretchOutName -> roadStretchModel)
    private Map<String, CrossroadStretchModel> crossroadsStretches; // Tramos de cruce | (name -> crossroadStretchModel)
    private Map<Integer, Map<Integer, Set<String>>> statesTrafficLightsCrossroadStretches; // Tramos de cruce abiertos en cada estado por cada semáforo | (stateId -> trafficLightId -> crossroadStretchNames)

    // Simulación
    private Integer currentTicks; // Número de ticks realizados en la simulación
    private Integer stateTicks; // Número de ticks realizados en el estado actual
    private Integer totalTicks; // Número total de ticks a realizar para completar la simulación
    private Map<Integer, Map<String, Double>> tickRoadStretchOccupation; //  (tick -> roadStretchName -> occupation)

    // Contadores de vehículos;
    private Integer totalVehiclesIn; // Número total de vehículos que han entrado en el cruce
    private Integer totalVehiclesOut; // Número total de vehículos que han salido del cruce


    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        status = CrossroadConstant.LOAD_DATA;
        cityName = null;
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
        totalTicks = 0;
        tickRoadStretchOccupation = new HashMap<>();
        totalVehiclesIn = 0;
        totalVehiclesOut = 0;
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
            // Obtenemos datos el nombre de la ciudad;
        cityName = Simulation.simulation.getCityName();
            // Obtenemos datos generales del cruce
        crossroadModel = Simulation.simulation.getCrossroadModel(crossroadId);
            // Obtenemos los semáforos del cruce
        trafficLights = Simulation.simulation.getCrossroadTrafficLights(crossroadId);
            // Obtenemos los estados del cruce
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
            // Obtenemos los ticks totales de la simulación
        totalTicks = 3600; // Simulation.simulation.getSimulationSeconds();
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

        // Informamos del estado actual del cruce
        this.reportCurrentState(false);

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

        // Obtenemos los cruces abiertos (calle origen y calle destino) y el porcentaje de coches interesados en ir por cada uno
        Map<String, Map<String, Double>> crossroadStretches = this.getCrossroadStretches(greenTrafficLightsIds);

        // Obtenemos los coches que van a cruzar y hacia qué calle va cada coche
        Map<String, Map<String, Integer>> crossroadStretchesVehicles = this.getCrossroadStretchesVehicles(crossroadStretches); // (origen -> destino -> número de vehículos)

        //
        roadStretchesOut.forEach((roadStretchOutName, roadStretchesOutModel) -> {
            if (roadStretchesOutModel.getCrossroadDestinationId() == null) { // Si es salida del sistema de tráfico
                Integer roadStretchOutgoingVehicles = roadStretchesOutModel.getOutput().intValue();
                Integer roadStretchVehicles = roadStretchesOutModel.getVehicles();
                Integer vehiclesToOutSystem = roadStretchVehicles < roadStretchOutgoingVehicles ? roadStretchVehicles : roadStretchOutgoingVehicles;
                roadStretchesOutModel.setVehicles(roadStretchVehicles - vehiclesToOutSystem);
                totalVehiclesOut += vehiclesToOutSystem;
            }
        });

        crossroadStretchesVehicles.forEach((roadStretchOriginName, roadStretchesDestination) -> {
            roadStretchesDestination.forEach((roadStretchDestinationName, vehicles) -> {
                if (vehicles > 0) {
                    Integer roadStretchOriginVehicles = roadStretchesIn.get(roadStretchOriginName).getVehicles();
                    Integer roadStretchOriginToDestinationVehicles = roadStretchOriginVehicles < vehicles ? roadStretchOriginVehicles : vehicles;
                    Integer roadStretchDestinationVehicles = roadStretchesOut.get(roadStretchDestinationName).getVehicles();
                    Integer roadStretchDestinationMaxVehicles = roadStretchesOut.get(roadStretchDestinationName).getMaxVehicles();
                    Integer roadStretchDestinationFreeSpaces = roadStretchDestinationMaxVehicles - roadStretchDestinationVehicles;
                    Integer vehiclesToDestination = roadStretchDestinationFreeSpaces < roadStretchOriginToDestinationVehicles ? roadStretchDestinationFreeSpaces : roadStretchOriginToDestinationVehicles;
                    roadStretchesIn.get(roadStretchOriginName).setVehicles(roadStretchOriginVehicles - vehiclesToDestination);
                    roadStretchesOut.get(roadStretchDestinationName).setVehicles(roadStretchDestinationVehicles + vehiclesToDestination);
                    if (roadStretchesOut.get(roadStretchDestinationName).getCrossroadDestinationId() != null) { // Si no es salida del sistema de tráfico
                        totalVehiclesOut += vehiclesToDestination;
                    }
                }
            });
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

    // Obtenemos los cruces abiertos (calle origen y calle destino) y el porcentaje de coches interesados en ir por cada uno para un conjunto de semáforos | ()
    private Map<String, Map<String, Double>> getCrossroadStretches(Set<Integer> greenTrafficLightsIds) {
        Map<String, Map<String, Double>> crossroadStretches = new HashMap<>(); // (roadStretchOriginName -> roadStretchDestinationName -> carsPercentageFromOriginToDestination)
        greenTrafficLightsIds.forEach((trafficLightId) -> { // Para cada semáforo en verde
            Map<String, Double> destinationRoadStretch = new HashMap<>(); // (roadStretchDestinationName -> carsPercentageFromOriginToDestination)
            String originRoadStretchName = trafficLights.get(trafficLightId).getRoadStretchIn(); // Obtenemos la calle que regula, calle origen del cruce asociada al semáforo
            statesTrafficLightsCrossroadStretches.get(currentState).get(trafficLightId).forEach((crossroadStretchName) -> { // Obtenemos el conjunto de cruces que habilita estando en verde
                String destinationRoadStretchName = getCrossroadStretchDestination(crossroadStretchName); // Obtenemos la calle de destino
                Double carsPercentageFromOriginToDestination = crossroadsStretches.get(crossroadStretchName).getCarsPercentageFromOriginToDestination(); // Obtenemos el porcentaje de coches interesados a ir a ese destino
                destinationRoadStretch.put(destinationRoadStretchName, carsPercentageFromOriginToDestination); // Añadimos el destino con el porcentaje asociado
            });
            crossroadStretches.put(originRoadStretchName, destinationRoadStretch); // Añadimos el origen con todos los destinos asociados para ese estado y semáforo
        });
        return crossroadStretches;
    }

    // Obtenemos los coches que van a cruzar y hacia qué calle va cada coche | ()
    private Map<String, Map<String, Integer>> getCrossroadStretchesVehicles(Map<String, Map<String, Double>> crossroadStretches) {
        Map<String, Map<String, Integer>> crossroadStretchesVehicles = new HashMap<>();
        Random random = new Random();
        crossroadStretches.forEach((originRoadStretchName, destinationRoadStretchNameAndPercentage) -> {
            Integer originRoadStretchOutputVehicles = roadStretchesIn.get(originRoadStretchName).getOutput().intValue();
            Integer originRoadStretchVehicles = roadStretchesIn.get(originRoadStretchName).getVehicles();
            Integer vehicles = originRoadStretchVehicles < originRoadStretchOutputVehicles ? originRoadStretchVehicles : originRoadStretchOutputVehicles;
            Integer percentagesSum = 0;
            Map <String, Integer> destinationVehicles = new HashMap<>();
            for (var entry : destinationRoadStretchNameAndPercentage.entrySet()) {
                percentagesSum += entry.getValue().intValue();
                destinationVehicles.put(entry.getKey(), 0);
            }
            // Obtenemos a donde debe ir cada uno de los vehículos
            for (int i = 0; i < vehicles; i++) {
                int randomNum = !percentagesSum.equals(0) ? 1 + random.nextInt(percentagesSum) : 0;
                Integer previousPercentage = null;
                Integer lowerIntervalValue = 0;
                Integer upperIntervalValue = 0;
                for (var entry : destinationRoadStretchNameAndPercentage.entrySet()) { // Para cada calle de destino
                    String destinationRoadStretchName = entry.getKey(); // Calle de destino
                    Integer currentPercentage = entry.getValue().intValue(); // Porcentaje de coches que van hacia esa calle de destino
                    Integer vehiclesToDestination = destinationVehicles.get(destinationRoadStretchName); // Vehículos que por ahora van hacia esa calle de destino
                    lowerIntervalValue += previousPercentage == null ? 1 : previousPercentage;
                    upperIntervalValue += currentPercentage;
                    if (lowerIntervalValue <= randomNum && randomNum <= upperIntervalValue) {
                        destinationVehicles.put(destinationRoadStretchName, vehiclesToDestination + 1); // Añadimos un coche para que vaya a la calle de destino
                    }
                    previousPercentage = currentPercentage;
                }
            }
            crossroadStretchesVehicles.put(originRoadStretchName, destinationVehicles);
        });
        return crossroadStretchesVehicles;
    }

    // Obtenemos el origen del cruce | Para la forma RSN-RSM obtenemos RSN
    private String getCrossroadStretchOrigin(String crossroadStretchName) {
        return crossroadStretchName.split("-")[0];
    }

    // Obtenemos el destino del cruce | Para la forma RSN-RSM obtenemos RSM
    private String getCrossroadStretchDestination(String crossroadStretchName) {
        return crossroadStretchName.split("-")[1];
    }

    // Añadimos tráfico a la simulación
    private void addTraffic() {
        roadStretchesIn.forEach((roadStretchInName, roadStretchInModel) -> {
            if (roadStretchInModel.getCrossroadOriginId() == null) {
                Integer roadStretchInputVehicles = roadStretchInModel.getInput().intValue(); // Vehículos por segundo
                Integer roadStretchVehicles = roadStretchInModel.getVehicles();
                Integer roadStretchMaxVehicles = roadStretchInModel.getMaxVehicles();
                Integer roadStretchFreeSpaces = roadStretchMaxVehicles - roadStretchVehicles;
                Integer roadStretchIncomingVehicles = roadStretchFreeSpaces < roadStretchInputVehicles ? roadStretchFreeSpaces : roadStretchInputVehicles;
                if (!this.isRoadStretchFull(roadStretchInModel)) {
                    roadStretchInModel.setVehicles(roadStretchVehicles + roadStretchIncomingVehicles);
                    totalVehiclesIn += roadStretchIncomingVehicles;
                }
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

    // Evaluamos si la simulación ha llegado a su fin
    private Boolean isSimulationFinished() {
        return currentTicks > totalTicks;
    }

    // Evaluamos si una calle está llena de coches
    private Boolean isRoadStretchFull (RoadStretchModel roadStretchModel) {
        Integer roadStretchVehicles = roadStretchModel.getVehicles();
        Integer roadStretchMaxVehicles = roadStretchModel.getMaxVehicles();
        return !(roadStretchVehicles < roadStretchMaxVehicles);
    }

    // Escribimos el estado actual del cruce
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

        var ref3 = new Object() {
            String str = "||";
        };
        roadStretchesOut.forEach((roadStretchInName, roadStretchInModel) -> {
            if (roadStretchInModel.getCrossroadDestinationId() == null) {
                ref3.str = ref3.str + roadStretchInName + ":" + roadStretchInModel.getVehicles() + "/" + roadStretchInModel.getMaxVehicles() + "(" + String.format("%.2f", roadStretchInModel.getOccupancyPercentage()) + "%)" + " ";
            }
        });
        str = ref3.str;
        if (str.endsWith(" ")) {
            str = str.substring(0, str.length()-1);
        }

        if ((!"||".equals(str))) {
            state += str;
        }

        state += "" +
                "||Vin:" + totalVehiclesIn +
                " Vout:" + totalVehiclesOut;

        state += "||";

        this.storeCurrentState();

        return state;
    }

    private void storeCurrentState() {
        Map<String, Double> roadStretchOccupation = new HashMap<>();
        roadStretchesIn.forEach((roadStretchName, roadStretchModel) -> roadStretchOccupation.put(roadStretchName, roadStretchModel.getOccupancyPercentage()));
        roadStretchesOut.forEach((roadStretchName, roadStretchModel) -> roadStretchOccupation.put(roadStretchName, roadStretchModel.getOccupancyPercentage()));
        tickRoadStretchOccupation.put(currentTicks, roadStretchOccupation);
    }

    // Informamos del estado actual del cruce
    private void reportCurrentState(Boolean isBulk) {
        if (!isBulk || isSimulationFinished()) {
            Map<Integer, Map<String, Double>> currentTickRoadStretchOccupation = new HashMap<>();
            currentTickRoadStretchOccupation.put(currentTicks, new HashMap<>(tickRoadStretchOccupation.get(currentTicks)));
            Map<Integer, Map<String, Double>> tickRoadStretchOccupationSingle = currentTickRoadStretchOccupation; // Información de ocupación de calle en el tick actual
            Map<Integer, Map<String, Double>> tickRoadStretchOccupationBulk = tickRoadStretchOccupation; // Información de ocupación de calles en todos los ticks de la simulación
            Map<Integer, Map<String, Double>> tickRoadStretchOccupationData = isBulk ? tickRoadStretchOccupationBulk : tickRoadStretchOccupationSingle;

            // Construimos el objeto JSON con la información
            JsonObject jsonReport = new JsonObject();
            tickRoadStretchOccupationData.forEach((tick, roadStretchOccupation) -> {
                JsonObject tickInformation = new JsonObject();
                roadStretchOccupation.forEach((roadStretchName, occupation) -> tickInformation.add(roadStretchName, String.format("%.2f", occupation)));
                tickInformation.add("vehiclesIn", String.format("%s", totalVehiclesIn));
                tickInformation.add("vehiclesOut", String.format("%s", totalVehiclesOut));
                jsonReport.add(tick.toString(), tickInformation);
            });

            // Enviamos el mensaje al agente ciudad
            String report = MessageConstant.REPORT + " " + jsonReport;
            this.sendACLMessage(ACLMessage.INFORM, this.getAID(), new AID(cityName, AID.ISLOCALNAME), report);
        }
    }
    //**************************************************//
}
