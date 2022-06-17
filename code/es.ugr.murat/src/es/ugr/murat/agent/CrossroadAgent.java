package es.ugr.murat.agent;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CityConfigurationConstant;
import es.ugr.murat.constant.CommonConstant;
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
import jade.lang.acl.MessageTemplate;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

/**
 * Clase representando al agente cruce (CrossroadAgent).
 *
 * @author Ramón García Verjaga
 */
public class CrossroadAgent extends MURATBaseAgent {

    private String cityName; // Nombre de la ciudad
    private String cityInputMode; // Modo de entrada de vehículos en la simulación (LINEAR, SINGLE PEAK, DOUBLE PEAK)
    private Integer crossroadId; // Identificador del cruce
    private CrossroadModel crossroadModel; // Información del cruce
    private Map<Integer, TrafficLightModel> trafficLights; // Semáforos del cruce | (trafficLightId -> trafficLightModel)
    private Map<Integer, StateModel> initialStates; // Estados del cruce iniciales | (stateId -> stateModel)
    private Map<Integer, StateModel> states; // Estados del cruce | (stateId -> stateModel)
    private Map<Integer, StateModel> futureStates; // Estados del cruce para el siguiente ciclo, se obtienen con la heurística de optimización de tiempo | (stateId -> stateModel)
    private Integer initialState; // Estado inicial del cruce
    private Integer currentState; // Estado actual del cruce
    private Boolean updateStateTimes; // Indicador de actualización de los tiempos de los cruces en el siguiente ciclo
    private Map<Integer, Map<Integer, String>> trafficLightsColorsPerState; // Colores de semáforos para cada estado | (stateId -> trafficLightId -> color)
    private Map<String, RoadStretchModel> roadStretchesIn; // Tramos de calle que entran al cruce | (roadStretchInName -> roadStretchModel)
    private Map<String, RoadStretchModel> roadStretchesInFromOutOfSystem; // Tramos de calle que entran al cruce desde fuera del sistema | (roadStretchInName -> roadStretchModel)
    private Map<String, RoadStretchModel> roadStretchesInFromAnotherCrossroad; // Tramos de calle que entran al cruce desde otro cruce | (roadStretchInName -> roadStretchModel)
    private Map<String, RoadStretchModel> roadStretchesOut; // Tramos de calle que salen del cruce | (roadStretchOutName -> roadStretchModel)
    private Map<String, RoadStretchModel> roadStretchesOutToOutOfSystem; // Tramos de calle que salen del cruce hacia fuera del sistema | (roadStretchOutName -> roadStretchModel)
    private Map<String, RoadStretchModel> roadStretchesOutToAnotherCrossroad; // Tramos de calle que salen del cruce hacia otro cruce | (roadStretchOutName -> roadStretchModel)
    private List<Integer> crossroadsIn; // Cruces que tienen tramos de calle de salida que entran a este cruce
    private List<Integer> crossroadsOut; // Cruces que tienen tramos de calle de entrada que salen de este cruce
    private Map<String, CrossroadStretchModel> crossroadsStretches; // Tramos de cruce | (name -> crossroadStretchModel)
    private Map<Integer, Map<String, Set<String>>> statesCrossroadStretches; // Tramos de cruce abiertos en cada estado | (stateId -> crossroadStretchOrigin -> crossroadStretchDestination)
    private Map<Integer, Map<Integer, Set<String>>> statesTrafficLightsCrossroadStretches; // Tramos de cruce abiertos en cada estado por cada semáforo | (stateId -> trafficLightId -> crossroadStretchNames)

    // Simulación
    private Boolean optimizeStateTimesPolicy; // Indicador de la política de optimización de tiempos (si es true está activa)
    private Integer sampleTime; // Tiempo de muestreo
    private LocalTime initialTime; // Hora de inicio de la simulación
    private Integer currentTicks; // Número de ticks realizados en la simulación
    private Integer stateTicks; // Número de ticks realizados en el estado actual
    private Integer totalTicks; // Número total de ticks a realizar para completar la simulación

    private Integer totalTicksToExit; // Suma de los ticks que ha tardado cada uno de los vehículos en salir del cruce
    private Integer totalTicksToExitOutOfSystem; // Suma de los ticks que ha tardado cada uno de los vehículos en salir del cruce hacia fuera del sistema
    private Integer totalTicksToExitToAnotherCrossroad; // Suma de los ticks que ha tardado cada uno de los vehículos en salir del cruce hacia otro cruce

    private Map<Integer, Map<String, Double>> tickRoadStretchOccupation; //  (tick -> roadStretchName -> occupation)

    // Contadores de vehículos
    private Map<String, Queue<Integer>> currentVehicles; // Número total de vehículos que hay en cada tramo de calle (cada vehículo está representado en la cola con el valor del tick en el que ha sido añadido) | (roadStretchName -> Cola)
    private Integer totalVehiclesIn; // Número total de vehículos que han entrado en el cruce
    private Integer totalVehiclesInFromOutOfSystem; // Número total de vehículos que han entrado en el cruce desde fuera del sistema
    private Integer totalVehiclesInFromAnotherCrossroad; //  Número total de vehículos que han entrado en el cruce desde otro cruce
    private Integer totalVehiclesOut; // Número total de vehículos que han salido del cruce
    private Integer totalVehiclesOutOfSystem; // Número total de vehículos que han salido del sistema por el cruce
    private Integer totalVehiclesOutToAnotherCrossroad; // Número total de vehículos que han salido del cruce hacia otro cruce

    private Map<Integer, Integer> totalVehiclesOutPerTick; // Número total de vehículos que han salido del cruce por tick | (tick -> vehicle)
    private Map<Integer, Integer> totalVehiclesOutOfSystemPerTick; // Número total de vehículos que han salido del sistema por el cruce por tick | (tick -> vehicles)
    private Map<Integer, Integer> totalVehiclesOutToAnotherCrossroadPerTick; // Número total de vehículos que han salido del cruce hacia otro cruce por tick | (tick -> vehicles)

    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        status = CrossroadConstant.LOAD_DATA;
        cityName = null;
        cityInputMode = null;
        crossroadId = Integer.parseInt(this.getLocalName().split(CrossroadConstant.AGENT_NAME)[1]);
        crossroadModel = null;
        trafficLights = null;
        states = null;
        initialState = null;
        currentState = null;
        updateStateTimes = false;
        trafficLightsColorsPerState = null;
        roadStretchesIn = null;
        roadStretchesInFromOutOfSystem = null;
        roadStretchesInFromAnotherCrossroad = null;
        roadStretchesOut = null;
        roadStretchesOutToOutOfSystem = null;
        roadStretchesOutToAnotherCrossroad = null;
        crossroadsIn = new ArrayList<>();
        crossroadsOut = new ArrayList<>();
        crossroadsStretches = null;
        statesCrossroadStretches = null;
        statesTrafficLightsCrossroadStretches = null;
        optimizeStateTimesPolicy = null;
        sampleTime = null;
        initialTime = null;
        currentTicks = 0;
        stateTicks = 0;
        totalTicks = 0;
        totalTicksToExit = 0;
        totalTicksToExitOutOfSystem = 0;
        totalTicksToExitToAnotherCrossroad = 0;
        tickRoadStretchOccupation = new HashMap<>();
        currentVehicles = null;

        totalVehiclesIn = 0;
        totalVehiclesInFromOutOfSystem = 0;
        totalVehiclesInFromAnotherCrossroad = 0;

        totalVehiclesOut = 0;
        totalVehiclesOutOfSystem = 0;
        totalVehiclesOutToAnotherCrossroad = 0;

        totalVehiclesOutPerTick = new HashMap<>();
        totalVehiclesOutOfSystemPerTick = new HashMap<>();
        totalVehiclesOutToAnotherCrossroadPerTick = new HashMap<>();

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

    // Cargamos datos
    protected void loadData() {
        Logger.info(ActionConstant.LOADING_DATA, this.getClass().getSimpleName(), this.getLocalName());
        // Cargamos datos del cruce
            // Obtenemos el nombre de la ciudad;
        cityName = Simulation.simulation.getCityName();
            // Obtenemos el modo de entrada de vehículos a la simulación
        cityInputMode = Simulation.simulation.getCityConfigurationMode();
            // Obtenemos datos generales del cruce
        crossroadModel = Simulation.simulation.getCrossroadModel(crossroadId);
            // Obtenemos los semáforos del cruce
        trafficLights = Simulation.simulation.getCrossroadTrafficLights(crossroadId);
            // Obtenemos los estados del cruce
        states = Simulation.simulation.getCrossroadStates(crossroadId);
        initialStates = this.initializeInitialStates();
            // Obtenemos el estado inicial del cruce
        initialState = Simulation.simulation.getCrossroadInitialState(crossroadId);
            // Obtenemos los colores de semáforos para cada estado del cruce
        trafficLightsColorsPerState = Simulation.simulation.getCrossroadTrafficLightsColorsPerCrossroadState(crossroadId);
            // Obtenemos los tramos de calle de entrada al cruce
                // Todos los tramos de entrada al cruce
        roadStretchesIn = Simulation.simulation.getCrossroadRoadStretchesIn(crossroadId);
                // Tramos de calle de entrada al cruce desde fuera del sistema
        roadStretchesInFromOutOfSystem = Simulation.simulation.getCrossroadRoadStretchesInFromOutOfSystem(crossroadId);
                // Tramos de calle de entrada al cruce desde otro cruce
        roadStretchesInFromAnotherCrossroad = Simulation.simulation.getCrossroadRoadStretchesInFromAnotherCrossroad(crossroadId);
            // Obtenemos los tramos de calle de salida del cruce
                // Todos los tramos de calle de salida del cruce
        roadStretchesOut = Simulation.simulation.getCrossroadRoadStretchesOut(crossroadId);
                // Tramos de calle de salida desde el cruce hacia fuera del sistema
        roadStretchesOutToOutOfSystem = Simulation.simulation.getCrossroadRoadStretchesOutToOutOfSystem(crossroadId);
                // Tramos de calle de salida desde el cruce hacia otro cruce
        roadStretchesOutToAnotherCrossroad = Simulation.simulation.getCrossroadRoadStretchesOutToAnotherCrossroad(crossroadId);
            // Obtenemos los cruces que tienen tramos de calle de salida que entran a este cruce
        crossroadsIn = Simulation.simulation.getCrossroadCrossroadsIn(crossroadId);
            // Obtenemos los cruces que tienen tramos de calle de entrada que salen de este cruce
        crossroadsOut = Simulation.simulation.getCrossroadCrossroadsOut(crossroadId);
            // Obtenemos los tramos de cruce del cruce
        crossroadsStretches = Simulation.simulation.getCrossroadCrossroadsStretches(crossroadId);
            // Obtenemos los tramos de cruce abiertos en cada estado
        statesCrossroadStretches = Simulation.simulation.getCrossroadStatesCrossroadStretches(crossroadId);
            // Obtenemos los tramos de cruce abiertos por cada semáforo en verde en cada estado
        statesTrafficLightsCrossroadStretches = Simulation.simulation.getCrossroadStatesTrafficLightsCrossroadStretches(crossroadId);
            // Obtenemos si la política de optimización de tiempos de estados está activa o no
        optimizeStateTimesPolicy = Simulation.simulation.getOptimizeStateTimesPolicy();
            // Obtenemos datos del tiempo de muestreo
        sampleTime = Simulation.simulation.getCityConfigurationSampleTime();
            // Obtenemos la hora de inicio de la simulación
        initialTime = Simulation.simulation.getCityConfigurationInitialTime();
            // Obtenemos los ticks totales de la simulación
        totalTicks = Simulation.simulation.getSimulationSeconds();
            // Inicializamos los vehículos con la ocupación de los tramos de calle de entrada
        currentVehicles = this.initializeCurrentVehicles();
        totalVehiclesOutPerTick.put(0, 0); // TODO: initialize
        totalVehiclesOutOfSystemPerTick.put(0, 0); // TODO: initialize
        totalVehiclesOutToAnotherCrossroadPerTick.put(0, 0); // TODO: initialize
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = CrossroadConstant.INITIALIZE_TRAFFIC_LIGHTS;
    }

    // Inicializamos los agentes semáforo
    protected void initializeTrafficLights() {
        // Enviamos un mensaje a cada semáforo para inicializarlo con en color que corresponda en función del estado inicial del cruce
        trafficLightsColorsPerState.get(initialState).forEach((trafficLightId, color) ->
                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
                        new AID(TrafficLightConstant.AGENT_NAME + trafficLightId, AID.ISLOCALNAME),
                        TrafficLightConstant.RED.equals(color) ? MessageConstant.SET_LIGHT_TO_RED : MessageConstant.SET_LIGHT_TO_GREEN));
        // Recibimos mensajes de información de los semáforos establecidos
        MessageTemplate messageTemplate1 = MessageTemplate.MatchContent(MessageConstant.CHANGE_LIGHT);
        MessageTemplate messageTemplate2 = MessageTemplate.MatchContent(MessageConstant.SET_LIGHT_TO_RED);
        MessageTemplate messageTemplate3 = MessageTemplate.MatchContent(MessageConstant.SET_LIGHT_TO_GREEN);
        MessageTemplate messageTemplateOr = MessageTemplate.or(messageTemplate1, messageTemplate2);
        MessageTemplate messageTemplate = MessageTemplate.or(messageTemplateOr, messageTemplate3);
        for (int i = 0; i < trafficLights.size(); i++) {
            this.listenMessages(messageTemplate);
        }
        currentState = initialState;
        status = CrossroadConstant.CONTROL_TRAFFIC;
    }

    // Controlamos el tráfico
    protected void controlTraffic() {
        // Mostramos el estado actual del cruce
        Logger.info(this.writeCurrentState());

        // Almacenamos el estado actual del cruce
        this.storeCurrentState();

        // Informamos del estado actual del cruce
        this.reportCurrentState();

        // Comprobamos si ha finalizado la simulación
        if (this.hasSimulationFinished()) {
            status = CrossroadConstant.FINALIZE_TRAFFIC_LIGHTS;
        }

        // Comprobamos si hay que cambiar de estado
        Integer stateDuration = states.get(currentState).getDurationTime();
        if (stateTicks.equals(stateDuration)) {
            this.changeToTheNextState();
            // Comprobamos si hay que actualizar los tiempos de los estados y estamos en un nuevo ciclo
            if (updateStateTimes /*&& this.isNewCycle()*/) {
                states = futureStates;
            }
        }

        // Movemos el tráfico a otras calles (hacemos que los vehículos pasen por el cruce)
        this.moveTrafficForward();

        // Añadimos tráfico a la simulación
        this.addTraffic();

        // Optimizamos los tiempos de los estados en función del tráfico
        this.optimizeStateTimes(optimizeStateTimesPolicy);

        currentTicks++;
        stateTicks++;
    }

    // Finalizamos los agentes semáforo
    protected void finalizeTrafficLights() {
        // Enviamos un mensaje a cada semáforo para pedirle que finalice
        trafficLights.forEach((trafficLightId, trafficLightModel) ->
                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
                        new AID(TrafficLightConstant.AGENT_NAME + trafficLightId, AID.ISLOCALNAME),
                        MessageConstant.FINALIZE));
        status = CrossroadConstant.EXIT;
    }

    // Salimos
    protected void exit() {
        exit = true;
    }
    //**************************************************//

    //*************** Utilidades y otros ***************//
    // Inicializamos los vehículos con la ocupación de los tramos de calle de entrada
    private Map<String, Queue<Integer>> initializeCurrentVehicles() {
        Map<String, Queue<Integer>> currentVehicles = new HashMap();
        for (Map.Entry<String, RoadStretchModel> entry : roadStretchesIn.entrySet()) {
            Queue<Integer> roadStretchQueue = new LinkedList<>();
            RoadStretchModel roadStretchModel = entry.getValue();
            Integer vehicles = roadStretchModel.getVehicles();
            for (int i = 0; i < vehicles; i++) {
                roadStretchQueue.add(currentTicks);
            }
            currentVehicles.put(roadStretchModel.getName(), roadStretchQueue);
        }
        for (Map.Entry<String, RoadStretchModel> entry : roadStretchesOutToOutOfSystem.entrySet()) {
            Queue<Integer> roadStretchQueue = new LinkedList<>();
            RoadStretchModel roadStretchModel = entry.getValue();
            Integer vehicles = roadStretchModel.getVehicles();
            for (int i = 0; i < vehicles; i++) {
                roadStretchQueue.add(currentTicks);
            }
            currentVehicles.put(roadStretchModel.getName(), roadStretchQueue);
        }
        for (Map.Entry<String, RoadStretchModel> entry : roadStretchesOutToAnotherCrossroad.entrySet()) {
            Queue<Integer> roadStretchQueue = new LinkedList<>();
            RoadStretchModel roadStretchModel = entry.getValue();
            currentVehicles.put(roadStretchModel.getName(), roadStretchQueue);
        }
        return currentVehicles;
    }

    // Escuchamos mensajes
    private void listenMessages() {
        this.listenMessages(null);
    }

    private void listenMessages(MessageTemplate messageTemplate) {
        if (messageTemplate != null) {
            this.receiveACLMessage(messageTemplate);
        } else {
            this.receiveACLMessage();
        }
        switch (incomingMessage.getPerformative()) {
            case ACLMessage.INFORM -> { // Si la performativa es INFORM
                // Comprobamos si están enviando vehículos al agente
                if (incomingMessage.getContent().startsWith(MessageConstant.RECEIVED_ALL_TICK_REPORTS)) {
                    System.out.println(this.getLocalName() + "::RECEIVED_REPORTS");// TODO: Logger
                }
            }
            case ACLMessage.REQUEST -> { // Si la performativa es REQUEST
                // Finalizamos el agente
                if (MessageConstant.FINALIZE.equals(incomingMessage.getContent())) {
                    status = CrossroadConstant.FINALIZE_TRAFFIC_LIGHTS;
                } else if (incomingMessage.getContent().startsWith(MessageConstant.TRANSFERRED_VEHICLES)) {
                    String transferredVehiclesMessage = incomingMessage.getContent().replace(MessageConstant.TRANSFERRED_VEHICLES + " ", "");
                    String roadStretchName = transferredVehiclesMessage.split(" ")[0];
                    String transferredVehicles = transferredVehiclesMessage.split(" ")[1];
                    this.processVehiclesReceivedFromAnotherCrossroad(roadStretchName, transferredVehicles);
                    this.sendACLMessage(ACLMessage.INFORM, this.getAID(), incomingMessage.getSender(), incomingMessage.getContent());
                } else { // Manejamos mensajes no conocidos
                    Logger.info(ActionConstant.MESSAGE_UNKNOWN_RECEIVED, this.getClass().getSimpleName(), this.getLocalName());
                }
            }
        }
    }

    private Integer getVehiclesToOut(RoadStretchModel roadStretchesOutModel) {
        String roadStretchName = roadStretchesOutModel.getName();
        Integer roadStretchOutgoingVehicles = roadStretchesOutModel.getOutput().intValue();
        Integer roadStretchVehicles = currentVehicles.get(roadStretchName).size();
        return roadStretchVehicles < roadStretchOutgoingVehicles ? roadStretchVehicles : roadStretchOutgoingVehicles;
    }

    // Hacemos que el tráfico avance
    private void moveTrafficForward() {
        // Obtenemos los ids de los semáforos en verde
        Set<Integer> greenTrafficLightsIds = this.getGreenTrafficLightsIds();

        // Obtenemos los cruces abiertos (calle origen y calle destino) y el porcentaje de vehículos interesados en ir por cada uno
        Map<String, Map<String, Double>> crossroadStretches = this.getCrossroadStretches(greenTrafficLightsIds);

        // Obtenemos los vehículos que van a cruzar y hacia qué calle va cada vehículo
        Map<String, Map<String, Integer>> crossroadStretchesVehicles = this.getCrossroadStretchesVehicles(crossroadStretches); // (origen -> destino -> número de vehículos)

        // Movemos los vehículos fuera del sistema de tráfico o a otros cruces
        for (Map.Entry<String, RoadStretchModel> roadStretchOut : roadStretchesOut.entrySet()) {
            String roadStretchOutName = roadStretchOut.getKey();
            RoadStretchModel roadStretchOutModel = roadStretchOut.getValue();
            Integer roadStretchVehicles = currentVehicles.get(roadStretchOutName).size(); // TODO: Explicar porqué
            Integer vehiclesOut = this.getVehiclesToOut(roadStretchOutModel);
            roadStretchOutModel.setVehicles(roadStretchVehicles - vehiclesOut);
            totalVehiclesOut += vehiclesOut;
            if (roadStretchOutModel.getCrossroadDestinationId() == null) { // Si es salida del sistema de tráfico
                totalVehiclesOutOfSystem += vehiclesOut;
                for (int i = 0; i < vehiclesOut; i++) {
                    Integer inputTick = currentVehicles.get(roadStretchOutName).remove();
                    Integer ticksToExit = currentTicks - inputTick;
                    totalTicksToExit += ticksToExit;
                    totalTicksToExitOutOfSystem += ticksToExit;
                }
            } else { // Si no es salida del sistema de tráfico | Enviamos vehículos a otros cruces
                totalVehiclesOutToAnotherCrossroad += vehiclesOut;
                JsonObject vehiclesTicksJsonObject = new JsonObject();
                JsonArray jsonArray = new JsonArray();
                for (int i = 0; i < vehiclesOut; i++) {
                    Integer inputTick = currentVehicles.get(roadStretchOutName).remove();
                    jsonArray.add(inputTick);
                    Integer ticksToExit = currentTicks - inputTick;
                    totalTicksToExit += ticksToExit;
                    totalTicksToExitToAnotherCrossroad += ticksToExit;
                }
                vehiclesTicksJsonObject.add("vehicles", jsonArray);
                String vehiclesTicks = vehiclesTicksJsonObject.toString();

                String transferredVehicles = MessageConstant.TRANSFERRED_VEHICLES + " " + roadStretchOutName + " " + vehiclesTicks;
                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(), new AID(CrossroadConstant.AGENT_NAME + roadStretchOutModel.getCrossroadDestinationId(), AID.ISLOCALNAME), transferredVehicles);
                MessageTemplate messageTemplate = MessageTemplate.MatchContent(transferredVehicles);
                this.listenMessages(messageTemplate);
                // Preguntamos a otros cruces cuantos vehículos podemos enviar a los tramos de calle
                // Contestamos a otros cruces cuantos vehículos pueden enviar a los tramos de calle
            }
        }

        // Recibimos vehículos de otros cruces
        for (int i = 0; i < roadStretchesInFromAnotherCrossroad.entrySet().size(); i++) {
            this.listenMessages();
        }

        // Movemos los vehículos haciendo que crucen por un tramo de cruce
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
                    for (int i = 0; i < vehiclesToDestination; i++) {
                        currentVehicles.get(roadStretchDestinationName).add(currentVehicles.get(roadStretchOriginName).remove());
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

    // Obtenemos los cruces abiertos (calle origen y calle destino) y el porcentaje de vehículos interesados en ir por cada uno para un conjunto de semáforos | (roadStretchOriginName -> roadStretchDestinationName -> carsPercentageFromOriginToDestination)
    private Map<String, Map<String, Double>> getCrossroadStretches(Set<Integer> greenTrafficLightsIds) {
        Map<String, Map<String, Double>> crossroadStretches = new HashMap<>(); // (roadStretchOriginName -> roadStretchDestinationName -> carsPercentageFromOriginToDestination)
        greenTrafficLightsIds.forEach((trafficLightId) -> { // Para cada semáforo en verde
            Map<String, Double> destinationRoadStretch = new HashMap<>(); // (roadStretchDestinationName -> carsPercentageFromOriginToDestination)
            String originRoadStretchName = trafficLights.get(trafficLightId).getRoadStretchIn(); // Obtenemos la calle que regula, calle origen del cruce asociada al semáforo
            statesTrafficLightsCrossroadStretches.get(currentState).get(trafficLightId).forEach((crossroadStretchName) -> { // Obtenemos el conjunto de cruces que habilita estando en verde
                String destinationRoadStretchName = getCrossroadStretchDestination(crossroadStretchName); // Obtenemos la calle de destino
                Double carsPercentageFromOriginToDestination = crossroadsStretches.get(crossroadStretchName).getCarsPercentageFromOriginToDestination(); // Obtenemos el porcentaje de vehículos interesados a ir a ese destino
                destinationRoadStretch.put(destinationRoadStretchName, carsPercentageFromOriginToDestination); // Añadimos el destino con el porcentaje asociado
            });
            crossroadStretches.put(originRoadStretchName, destinationRoadStretch); // Añadimos el origen con todos los destinos asociados para ese estado y semáforo
        });
        return crossroadStretches;
    }

    // Obtenemos los vehículos que van a cruzar y hacia qué calle va cada vehículo | (originRoadStretchName -> destinationRoadStretchName -> vehiclesToDestination)
    private Map<String, Map<String, Integer>> getCrossroadStretchesVehicles(Map<String, Map<String, Double>> crossroadStretches) {
        Map<String, Map<String, Integer>> crossroadStretchesVehicles = new HashMap<>();
        Random random = new Random();
        crossroadStretches.forEach((originRoadStretchName, destinationRoadStretchNameAndPercentage) -> {
            Integer originRoadStretchOutputVehicles = roadStretchesIn.get(originRoadStretchName).getOutput().intValue();
            Integer originRoadStretchVehicles = roadStretchesIn.get(originRoadStretchName).getVehicles();
            Integer vehicles = originRoadStretchVehicles < originRoadStretchOutputVehicles ? originRoadStretchVehicles : originRoadStretchOutputVehicles;
            Integer percentagesSum = 0;
            Map<String, Integer> destinationVehicles = new HashMap<>();
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
                    Integer currentPercentage = entry.getValue().intValue(); // Porcentaje de vehículo que van hacia esa calle de destino
                    Integer vehiclesToDestination = destinationVehicles.get(destinationRoadStretchName); // Vehículos que por ahora van hacia esa calle de destino
                    lowerIntervalValue += previousPercentage == null ? 1 : previousPercentage;
                    upperIntervalValue += currentPercentage;
                    if (lowerIntervalValue <= randomNum && randomNum <= upperIntervalValue) {
                        destinationVehicles.put(destinationRoadStretchName, vehiclesToDestination + 1); // Añadimos un vehículo para que vaya a la calle de destino
                    }
                    previousPercentage = currentPercentage;
                }
            }
            crossroadStretchesVehicles.put(originRoadStretchName, destinationVehicles);
        });
        return crossroadStretchesVehicles;
    }

    // Obtenemos el origen del cruce | Para la forma RSN-RSM obtenemos RSN // TODO: Revisar código repetido, también está en la clase Simulation
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
            if (roadStretchInModel.getCrossroadOriginId() == null) { // Si es un tramo de calle de entrada al sistema (no tiene tramo de calle de origen)
                Double vehiclesPerSecond = roadStretchInModel.getInput(); // Vehículos por segundo que pueden entrar al tramo de calle
                Double secondsPerVehicle = 1 / roadStretchInModel.getInput(); // Segundos que tarda un vehículo en poder entrar al tramo de calle

                // Comprobamos si la simulación está en alguno de los modos pico y si está en alguna de las horas pico
                if (this.isOnPeakModeAndInPeakHour()) { // Modificamos la cantidad de vehículos por hora pico
                    vehiclesPerSecond *= CityConfigurationConstant.PEAK_FACTOR;
                    secondsPerVehicle /= CityConfigurationConstant.PEAK_FACTOR;
                }

                // Comprobamos si se añaden uno o más vehículos por segundo al tramo de calle
                Boolean oneOrMoreVehiclesPerSecond = secondsPerVehicle <= 1.0;
                // Calculamos la cantidad de vehículos que quieren entrar al tramo de calle:
                    // --> Si el número de vehículos por segundo es mayor o igual a uno se quiere añadir esa cantidad. Se añade en cada tick.
                    // --> Si el número de vehículos por segundo es menor que uno se quiere añadir uno cada N número de segundos. Se añade cada determinados ticks.
                Integer roadStretchInputVehicles = oneOrMoreVehiclesPerSecond ? vehiclesPerSecond.intValue() : 1; // Cantidad de vehículos que quieren entrar al tramo de calle
                // Comprobamos si en este tick hay que añadir vehículos
                if (this.isTimeToAddTraffic(secondsPerVehicle.intValue())) {
                    this.addVehiclesIfPossible(roadStretchInputVehicles, roadStretchInModel);
                }
            }
        });
    }

    // Comprobamos si estamos en alguno de los modos peak y si estamos en alguna de las horas peak
    private Boolean isOnPeakModeAndInPeakHour() {
        return  CityConfigurationConstant.SINGLE_PEAK.equals(cityInputMode) && isPeakHour(8, 9) ||
                CityConfigurationConstant.DOUBLE_PEAK.equals(cityInputMode) && isPeakHour(8, 9) ||
                CityConfigurationConstant.DOUBLE_PEAK.equals(cityInputMode) && isPeakHour(14, 15);
    }

    // Comprobamos si el instante actual pertenece a la hora pico en intervalo cerrado
    private Boolean isPeakHour(Integer startHour, Integer endHour) {
        LocalTime currentHour = initialTime.plusSeconds(currentTicks);
        LocalTime startPeakHour = LocalTime.of(startHour, 0, 0);
        LocalTime endPeakHour = LocalTime.of(endHour, 0, 0);
        return (currentHour.isAfter(startPeakHour) && currentHour.isBefore(endPeakHour)) || currentHour.equals(startPeakHour) || currentHour.equals(endPeakHour);
    }

    // Comprobamos si en el tick actual hay que añadir tráfico a la simulación
    private Boolean isTimeToAddTraffic(Integer secondsPerVehicle) {
        return secondsPerVehicle <= 1 || currentTicks % secondsPerVehicle == 0;
    }

    // Añadimos una cantidad de vehículos al tramo de calle si es posible en función de su ocupación y capacidad máxima
    private void addVehiclesIfPossible(Integer roadStretchInputVehicles, RoadStretchModel roadStretchInModel) {
        Integer roadStretchVehicles = roadStretchInModel.getVehicles(); // Cantidad de vehículos que hay en el tramo de calle
        Integer roadStretchMaxVehicles = roadStretchInModel.getMaxVehicles(); // Cantidad máxima de vehículos que puede haber en el tramo de calle
        Integer roadStretchFreeSpaces = roadStretchMaxVehicles - roadStretchVehicles; // Cantidad de espacios libres para vehículos que hay en el tramo de calle
        Integer roadStretchIncomingVehicles = roadStretchFreeSpaces < roadStretchInputVehicles ? roadStretchFreeSpaces : roadStretchInputVehicles; // Cantidad de vehículos que se puede añadir al tramo de calle
        // Si el tramo de calle no está lleno añadimos los vehículos
        if (!this.isRoadStretchFull(roadStretchInModel)) {
            roadStretchInModel.setVehicles(roadStretchVehicles + roadStretchIncomingVehicles);
            totalVehiclesIn += roadStretchIncomingVehicles;
            totalVehiclesInFromOutOfSystem += roadStretchIncomingVehicles;
            for (int i = 0; i < roadStretchIncomingVehicles; i++) {
                currentVehicles.get(roadStretchInModel.getName()).add(currentTicks);
            }
        }
    }

    // Comprobamos si una calle está llena de vehículos
    private Boolean isRoadStretchFull(RoadStretchModel roadStretchModel) {
        Integer roadStretchVehicles = roadStretchModel.getVehicles(); // Cantidad de vehículos que hay en el tramo de calle
        Integer roadStretchMaxVehicles = roadStretchModel.getMaxVehicles(); // Cantidad máxima de vehículos que puede haber en el tramo de calle
        return !(roadStretchVehicles < roadStretchMaxVehicles);
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

    private Boolean isNewCycle() {
        return currentState.equals(1);
    }

    // Evaluamos si la simulación ha llegado a su fin
    private Boolean hasSimulationFinished() {
        return currentTicks > totalTicks;
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
            str = str.substring(0, str.length() - 1);
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
            str = str.substring(0, str.length() - 1);
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
            str = str.substring(0, str.length() - 1);
        }

        if ((!"||".equals(str))) {
            state += str;
        }

        state += "" +
                "||Vin:" + totalVehiclesIn +
                " Vout:" + totalVehiclesOut;

        state += "||";

        return state;
    }

    // Almacenamos el estado actual de ocupación de los tramos de calle del cruce
    private void storeCurrentState() {
        Map<String, Double> roadStretchOccupation = new HashMap<>();
        roadStretchesIn.forEach((roadStretchName, roadStretchModel) -> roadStretchOccupation.put(roadStretchName, roadStretchModel.getOccupancyPercentage()));
        roadStretchesOut.forEach((roadStretchName, roadStretchModel) -> roadStretchOccupation.put(roadStretchName, roadStretchModel.getOccupancyPercentage()));
        tickRoadStretchOccupation.put(currentTicks, roadStretchOccupation);
        totalVehiclesOutPerTick.put(currentTicks, totalVehiclesOutOfSystem + totalVehiclesOutToAnotherCrossroad);
        totalVehiclesOutOfSystemPerTick.put(currentTicks, totalVehiclesOutOfSystem);
        totalVehiclesOutToAnotherCrossroadPerTick.put(currentTicks, totalVehiclesOutToAnotherCrossroad);
    }

    // Informamos al agente ciudad del estado actual del cruce
    private void reportCurrentState() {
        // Construimos el objeto JSON con los datos de la simulación
        JsonObject reportJsonObject = new JsonObject();
        JsonObject tickDataJsonObject = new JsonObject();
        // Añadimos información del estado del cruce
        tickDataJsonObject.add(this.getColumnName(CommonConstant.STATE), String.format("%s", currentState));
        // Añadimos información sobre la ocupación de cada uno de los tramos de calle
        tickRoadStretchOccupation.get(currentTicks).forEach((roadStretchName, occupation) -> tickDataJsonObject.add(roadStretchName, String.format("%.2f", occupation)));
        // Añadimos información del total de vehículos en el cruce
        tickDataJsonObject.add(this.getColumnName(CommonConstant.VEHICLES_TOTAL), String.format("%s", this.calculateTotalVehicles()));
        // Añadimos información de los vehículos que han entrado en el cruce
        tickDataJsonObject.add(this.getColumnName(CommonConstant.VEHICLES_IN), String.format("%s", totalVehiclesIn));
        tickDataJsonObject.add(this.getColumnName(CommonConstant.VEHICLES_IN_FROM_OUT_OF_SYSTEM), String.format("%s", totalVehiclesInFromOutOfSystem));
        tickDataJsonObject.add(this.getColumnName(CommonConstant.VEHICLES_IN_FROM_ANOTHER_CROSSROAD), String.format("%s", totalVehiclesInFromAnotherCrossroad));
        // Añadimos información de los vehículos que han salido del cruce
        tickDataJsonObject.add(this.getColumnName(CommonConstant.VEHICLES_OUT), String.format("%s", totalVehiclesOut));
        tickDataJsonObject.add(this.getColumnName(CommonConstant.VEHICLES_OUT_OF_SYSTEM), String.format("%s", totalVehiclesOutOfSystem));
        tickDataJsonObject.add(this.getColumnName(CommonConstant.VEHICLES_OUT_TO_ANOTHER_CROSSROAD), String.format("%s", totalVehiclesOutToAnotherCrossroad));
        // Añadimos información sobre el tiempo que los vehículos han tardado en salir del cruce
            // Media de ticks de los vehículos que salen del cruce durante la última muestra
        tickDataJsonObject.add(this.getColumnName(CommonConstant.TICKS_AVERAGE_PER_SAMPLE_OUT), String.format("%s", this.getTicksAveragePerSample(currentTicks, totalVehiclesOutPerTick)));
            // Media de ticks acumulada de los vehículos que salen del cruce
        tickDataJsonObject.add(this.getColumnName(CommonConstant.TICKS_AVERAGE_CUMULATIVE_OUT), String.format("%s", totalVehiclesOut == 0 ? 0 : totalTicksToExit / totalVehiclesOut));
            // Media de ticks de los vehículos que salen del sistema por el cruce durante la última muestra
        tickDataJsonObject.add(this.getColumnName(CommonConstant.TICKS_AVERAGE_PER_SAMPLE_OUT_OF_SYSTEM), String.format("%s", this.getTicksAveragePerSample(currentTicks, totalVehiclesOutOfSystemPerTick)));
            // Media de ticks acumulada de los vehículos que salen del sistema por el cruce
        tickDataJsonObject.add(this.getColumnName(CommonConstant.TICKS_AVERAGE_CUMULATIVE_OUT_OF_SYSTEM), String.format("%s", totalVehiclesOutOfSystem == 0 ? 0 : totalTicksToExitOutOfSystem / totalVehiclesOutOfSystem));
            // Media de ticks de los vehículos que salen desde el cruce hacia otro cruce durante la última muestra
        tickDataJsonObject.add(this.getColumnName(CommonConstant.TICKS_AVERAGE_PER_SAMPLE_OUT_TO_ANOTHER_CROSSROAD), String.format("%s", this.getTicksAveragePerSample(currentTicks, totalVehiclesOutToAnotherCrossroadPerTick)));
            // Media de ticks acumulada de los vehículos que salen desde el cruce hacia otro cruce
        tickDataJsonObject.add(this.getColumnName(CommonConstant.TICKS_AVERAGE_CUMULATIVE_OUT_TO_ANOTHER_CROSSROAD), String.format("%s", totalVehiclesOutToAnotherCrossroad == 0 ? 0 : totalTicksToExitToAnotherCrossroad / totalVehiclesOutToAnotherCrossroad));
        reportJsonObject.add(currentTicks.toString(), tickDataJsonObject);

        // Enviamos el mensaje al agente ciudad
        String report = MessageConstant.REPORT + " " + reportJsonObject;
        this.sendACLMessage(ACLMessage.INFORM, this.getAID(), new AID(cityName, AID.ISLOCALNAME), report);

        // Esperamos el mensaje del agente ciudad (sincronización entre cruces)
        MessageTemplate messageTemplate = MessageTemplate.MatchContent(MessageConstant.RECEIVED_ALL_TICK_REPORTS);
        this.blockingReceive(messageTemplate);
    }

    private Double getTicksAveragePerSample(Integer currentTick, Map<Integer, Integer> totalVehiclesPerTick) {
        Integer previousSampleTick = Math.max((currentTick - sampleTime), 0);
        Double previousVehicles = Double.valueOf(totalVehiclesPerTick.get(previousSampleTick));
        Double currentVehicles = Double.valueOf(totalVehiclesPerTick.get(currentTick));
        return sampleTime != 0 ? (currentVehicles - previousVehicles) / sampleTime : 0;
    }

    // Mejoramos los tiempos de los estados
    private void optimizeStateTimes(Boolean active) {
        if (active) {
            // Obtenemos los tramos de calle de entrada congestionados y sus puntuaciones de congestión asociadas
            Map<String, Double> congestedRoadStretchesIn = this.getCongestedRoadStretchesIn();
            // Evaluamos si existe algún tramo de calle de entrada con congestión
            Boolean isCongested = congestedRoadStretchesIn.size() > 0;
            // Si hay congestión en el cruce, optimizamos, si es posible, los tiempos de los estados para resolverla
            if (isCongested) {
                // Analizamos si existe algún estado del cruce que beneficie la salida de tráfico de los tramos de calle,
                // es decir, algún estado del cruce que habilite tramos de cruce cuyos tramos de calle de origen sean el mayor número de entre los congestionados

                // Obtenemos para cada estado los tramos de calle de entrada congestionados que habilita y sus puntuaciones de congestión asociadas
                Map<Integer, Map<String, Double>> statesRoadStretchesCongestionScores = this.getStatesRoadStretchesCongestionScores(congestedRoadStretchesIn);

                List<Integer> bestStateCandidates = new ArrayList<>(); // Candidatos a ser el peor estado
                List<Integer> worstStateCandidates = new ArrayList<>(); // Candidatos a ser el mejor estado
                Integer roadStretchesMaxNumber = 0; // Máximo número de tramos de calle origen saturados cuyos tramos de cruce asociados son habilitados por un estado concreto
                Integer roadStretchesMinNumber = congestedRoadStretchesIn.size(); // Mínimo número de tramos de calle origen saturados cuyos tramos de cruce asociados son habilitados por un estado concreto
                for (Map.Entry<Integer, Map<String, Double>> stateRoadStretchesCongestionScores : statesRoadStretchesCongestionScores.entrySet()) {
                    Integer stateId = stateRoadStretchesCongestionScores.getKey(); // (stateId)
                    Integer roadStretchesNumber = stateRoadStretchesCongestionScores.getValue().size(); // size(roadStretchOrigin -> congestionScore)
                    // Obtenemos los candidatos a ser el mejor estado, en función de los tramos de calle origen saturados cuyos tramos de cruce asociados son habilitados
                    if (roadStretchesMaxNumber < roadStretchesNumber) { // Si el estado actual es mejor que los candidatos anteriores
                        bestStateCandidates.clear(); // Eliminamos los mejores candidatos hasta el momento
                        bestStateCandidates.add(stateId); // Añadimos el candidato actual
                        roadStretchesMaxNumber = roadStretchesNumber;
                    } else if (roadStretchesMaxNumber.equals(roadStretchesNumber)) { // Si el estado actual es igual a los candidatos actuales
                        bestStateCandidates.add(stateId);
                    }
                    // Obtenemos los candidatos a ser el peor estado, en función de los tramos de calle origen saturados cuyos tramos de cruce asociados son habilitados
                    if (roadStretchesMinNumber > roadStretchesNumber) { // Si el estado actual es peor que los candidatos anteriores
                        worstStateCandidates.clear();
                        worstStateCandidates.add(stateId);
                        roadStretchesMinNumber = roadStretchesNumber;
                    }
                    else if (roadStretchesMinNumber.equals(roadStretchesNumber)) { // Si el estado actual es igual a los candidatos actuales
                        worstStateCandidates.add(stateId);
                    }
                }

                Integer bestState = -1;
                Integer worstState = -1;
                // Hay que elegir el mejor estado entre los candidatos (el estado que habilite tramos de cruce tales que la suma de las puntuaciones de congestión de sus tramos de calle de origen sea la mayor)
                Double bestStateScore = Double.NEGATIVE_INFINITY;
                for (Integer bestStateCandidate : bestStateCandidates) {
                    // Comprobamos si es posible el incremento de tiempo del estado candidato
                    // --> Si el tiempo actual del estado candidato más lo que se va a aumentar es menor o igual que el máximo, pasar a valorar las puntuaciones
                    // --> Si el tiempo actual del estado candidato más lo que se va a aumentar es mayor que el máximo, pasar al siguiente candidato a mejor estado
                    if (this.isPossibleToIncreaseStateTime(bestStateCandidate)) {
                        Double currentScore = 0.0; // TODO: Refactorizar si es posible, extraer código a una función (ref 1.0)
                        for (Map.Entry<String, Double> stateRoadStretchesCongestionScores : statesRoadStretchesCongestionScores.get(bestStateCandidate).entrySet()) {
                            Double congestionScore = stateRoadStretchesCongestionScores.getValue();
                            currentScore += congestionScore;
                        }
                        if (bestStateScore < currentScore) {
                            bestState = bestStateCandidate;
                            bestStateScore = currentScore;
                        }
                    }
                }

                // Hay que elegir el peor estado entre los candidatos (el estado que habilite menos tramos de cruce o habilitándolos tales que la suma de las puntuaciones de congestión de sus tramos de calle de origen sea la menor)
                Double worstStateScore = Double.POSITIVE_INFINITY;
                for (Integer worstStateCandidate : worstStateCandidates) {
                    // Comprobar si es posible la disminución de tiempo del estado candidato
                    // --> Si el tiempo actual del estado candidato menos lo que se va a disminuir es mayor o igual que el mínimo, pasar a valorar las puntuaciones
                    // --> Si el tiempo actual del estado candidato menos lo que se va a disminuir es menor que el mínimo, pasar al siguiente candidato a peor estado
                    if(this.isPossibleToReduceStateTime(worstStateCandidate)) {
                        Double currentScore = 0.0;
                        for (Map.Entry<String, Double> stateRoadStretchesCongestionScores : statesRoadStretchesCongestionScores.get(worstStateCandidate).entrySet()) {
                            Double congestionScore = stateRoadStretchesCongestionScores.getValue();
                            currentScore += congestionScore;
                        }
                        if (worstStateScore > currentScore) {
                            worstState = worstStateCandidate;
                            worstStateScore = currentScore;
                        }
                    }
                }

                // Ajustamos los tiempos
                updateStateTimes = this.adjustStateTimes(bestState, worstState);
            }
        }
    }

    // Obtenemos los tramos de calle de entrada congestionados y sus puntuaciones de congestión asociadas
    private Map<String, Double> getCongestedRoadStretchesIn() {
        Map<String, Double> congestedRoadStretchesIn = new HashMap<>();
        roadStretchesIn.forEach((roadStretchInName, roadStretchInModel) -> {
            if (roadStretchInModel.getOccupancyPercentage() > CrossroadConstant.SATURATION_THRESHOLD) {
                Integer vehicles = roadStretchInModel.getVehicles();
                Double occupancyPercentage = roadStretchInModel.getOccupancyPercentage();
                Double congestionScore = CrossroadConstant.VEHICLE_WEIGHT * vehicles + CrossroadConstant.OCCUPATION_WEIGHT * occupancyPercentage;
                congestedRoadStretchesIn.put(roadStretchInName, congestionScore);
            }
        });
        return congestedRoadStretchesIn;
    }

    private Map<Integer, Map<String, Double>> getStatesRoadStretchesCongestionScores(Map<String, Double> congestedRoadStretchesIn) {
        Map<Integer, Map<String, Double>> statesRoadStretchesCongestionScores = new HashMap<>(); // | (stateId -> roadStretchOrigin -> congestionScore)
        statesCrossroadStretches.forEach((stateId, roadStretches) -> {
            Map<String, Double> roadStretchCongestionScore = new HashMap<>();
            for (Map.Entry<String, Set<String>> roadStretch : roadStretches.entrySet()) {
                String roadStretchOrigin = roadStretch.getKey();
                if (congestedRoadStretchesIn.containsKey(roadStretchOrigin)) {
                    roadStretchCongestionScore.put(roadStretchOrigin, congestedRoadStretchesIn.get(roadStretchOrigin));
                }
            }
            statesRoadStretchesCongestionScores.put(stateId, roadStretchCongestionScore);
        });
        return statesRoadStretchesCongestionScores;
    }

    private Boolean adjustStateTimes(Integer bestState, Integer worstState) {
        Boolean adjustedStateTimes = false;
        if (bestState != -1 && worstState != -1) {
            futureStates = new HashMap<>();
            states.forEach((stateId, stateModel) -> {
                Integer futureDuration;
                if (bestState.equals(stateId)) {
                    futureDuration = stateModel.getDurationTime() + CrossroadConstant.STATE_TIME_VARIATION;
                } else if (worstState.equals(stateId)) {
                    futureDuration = stateModel.getDurationTime() - CrossroadConstant.STATE_TIME_VARIATION;
                } else {
                    futureDuration = stateModel.getDurationTime();
                }
                StateModel futureStateModel = new StateModel(stateId, stateModel.getName(), futureDuration);
                futureStates.put(stateId, futureStateModel);
            });
            adjustedStateTimes = true;
        }
        return adjustedStateTimes;
    }

    private Boolean isPossibleToIncreaseStateTime(Integer stateId) { // TODO: Pensar si quitar o añadir limitación
        Integer currentStateTime = states.get(stateId).getDurationTime();
        Integer futureStateTime = currentStateTime + CrossroadConstant.STATE_TIME_VARIATION;
        Integer maxStateTime;
        return true;
    }

    private Boolean isPossibleToReduceStateTime(Integer stateId) {
        Integer currentStateTime = states.get(stateId).getDurationTime();
        Integer futureStateTime = currentStateTime - CrossroadConstant.STATE_TIME_VARIATION;
        Integer minStateTime = crossroadModel.getMinimumStateTime();
        return futureStateTime >= minStateTime;
    }

    private Integer calculateTotalVehicles() {
        Integer totalVehicles = 0;
        for (Map.Entry<String, Queue<Integer>> entry : currentVehicles.entrySet()) {
            Integer vehicles = entry.getValue().size();
            totalVehicles += vehicles;
        }
        return totalVehicles;
    }

    private Map<Integer, StateModel> copyStates(Map<Integer, StateModel> states) {
        Map<Integer, StateModel> copyStates = new HashMap<>();
        states.forEach((stateId, stateModel) -> {
            StateModel copyStateModel = new StateModel(stateModel.getStateId(), stateModel.getName(), stateModel.getDurationTime());
            copyStates.put(stateId, copyStateModel);
        });
        return copyStates;
    }

    private Map<Integer, StateModel> initializeInitialStates() {
        return this.copyStates(states);
    }

    private void processVehiclesReceivedFromAnotherCrossroad(String roadStretchName, String transferredVehicles) {
        JsonObject transferredVehiclesJsonObject = Json.parse(transferredVehicles).asObject();
        JsonArray transferredVehiclesJsonArray = transferredVehiclesJsonObject.get("vehicles").asArray();
        Integer numberOfTransferredVehicles = 0;
        for(JsonValue transferredVehicleJsonValue : transferredVehiclesJsonArray) {
            Integer vehicleTick = transferredVehicleJsonValue.asInt();
            currentVehicles.get(roadStretchName).add(vehicleTick);
            numberOfTransferredVehicles++;
        }
        RoadStretchModel roadStretchModel = roadStretchesIn.get(roadStretchName);
        Integer previousNumberOfVehicles = roadStretchModel.getVehicles();
        Integer numberOfVehicles = previousNumberOfVehicles + numberOfTransferredVehicles;
        roadStretchModel.setVehicles(numberOfVehicles);
        totalVehiclesIn += numberOfTransferredVehicles;
        totalVehiclesInFromAnotherCrossroad += numberOfTransferredVehicles;
    }

    private String getColumnName(String name) {
        return this.getLocalName() + name;
    }

    // TODO: Refactorizar o eliminar (ref 1.0)
    private Integer getBestState(Integer bestStateCandidates, Double bestStateScore) {
        return 0;
    }
    //**************************************************//
}
