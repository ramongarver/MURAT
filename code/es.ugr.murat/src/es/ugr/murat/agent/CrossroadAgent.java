package es.ugr.murat.agent;

import com.eclipsesource.json.JsonObject;
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
    private Integer totalTicksToExit; // Número total de ticks que han tardado los vehículos en salir
    private Map<Integer, Map<String, Double>> tickRoadStretchOccupation; //  (tick -> roadStretchName -> occupation)

    // Contadores de vehículos
    private Map<String, Queue<Integer>> currentVehicles; // Número total de vehículos que hay en el cruce (cada vehículo está representado en la cola con el valor del tick en el que ha sido añadido) | (roadStretchName -> Cola)
    private Integer totalVehiclesIn; // Número total de vehículos que han entrado en el cruce
    private Integer totalVehiclesOut; // Número total de vehículos que han salido del cruce
    private Integer totalVehiclesOutOfSystem; // Número total de vehículos que han salido del sistema por el cruce
    private Map<Integer, Integer> totalVehiclesOutOfSystemPerTick; // Número total de vehículos que han salido del sistema por el cruce por tick | (tick -> vehicles)


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
        tickRoadStretchOccupation = new HashMap<>();
        currentVehicles = null;
        totalVehiclesIn = 0;
        totalVehiclesOut = 0;
        totalVehiclesOutOfSystem = 0;
        totalVehiclesOutOfSystemPerTick = new HashMap<>();
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
        totalVehiclesOutOfSystemPerTick.put(0, 0); // TODO: initialize
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
        this.reportCurrentState(false);

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
        this.receiveACLMessage();
        switch (incomingMessage.getPerformative()) {
            case ACLMessage.INFORM -> { // Si la performativa es INFORM

            }
            case ACLMessage.REQUEST -> { // Si la performativa es REQUEST
                // Finalizamos el agente
                if (MessageConstant.FINALIZE.equals(incomingMessage.getContent())) {
                    status = CrossroadConstant.FINALIZE_TRAFFIC_LIGHTS;
                } else { // Manejamos mensajes no conocidos
                    Logger.info(ActionConstant.MESSAGE_UNKNOWN_RECEIVED, this.getClass().getSimpleName(), this.getLocalName());
                }
            }
        }
    }

    // Hacemos que el tráfico avance
    private void moveTrafficForward() {
        // Obtenemos los ids de los semáforos en verde
        Set<Integer> greenTrafficLightsIds = this.getGreenTrafficLightsIds();

        // Obtenemos los cruces abiertos (calle origen y calle destino) y el porcentaje de vehículos interesados en ir por cada uno
        Map<String, Map<String, Double>> crossroadStretches = this.getCrossroadStretches(greenTrafficLightsIds);

        // Obtenemos los vehículos que van a cruzar y hacia qué calle va cada vehículo
        Map<String, Map<String, Integer>> crossroadStretchesVehicles = this.getCrossroadStretchesVehicles(crossroadStretches); // (origen -> destino -> número de vehículos)

        // Movemos los vehículos fuera del sistema de tráfico
        roadStretchesOut.forEach((roadStretchOutName, roadStretchesOutModel) -> {
            if (roadStretchesOutModel.getCrossroadDestinationId() == null) { // Si es salida del sistema de tráfico
                Integer roadStretchOutgoingVehicles = roadStretchesOutModel.getOutput().intValue();
                Integer roadStretchVehicles = roadStretchesOutModel.getVehicles();
                Integer vehiclesToOutSystem = roadStretchVehicles < roadStretchOutgoingVehicles ? roadStretchVehicles : roadStretchOutgoingVehicles;
                roadStretchesOutModel.setVehicles(roadStretchVehicles - vehiclesToOutSystem);
                totalVehiclesOut += vehiclesToOutSystem;
                totalVehiclesOutOfSystem += vehiclesToOutSystem;
                for(int i = 0; i < vehiclesToOutSystem; i++) {
                    Integer inputTick = currentVehicles.get(roadStretchOutName).remove();
                    Integer ticksToExit = currentTicks - inputTick;
                    totalTicksToExit += ticksToExit;
                }
            } else { // Si no es salida del sistema de tráfico

            }
        });

        // Movemos los vehículos
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
                    if (roadStretchesOut.get(roadStretchDestinationName).getCrossroadDestinationId() != null) { // Si no es salida del sistema de tráfico
                        totalVehiclesOut += vehiclesToDestination;
                    }
                }
            });
        });

        // TODO: Quitar si no se llega a esta fase
//        // Preguntamos a otros cruces cuantos vehículos podemos enviar a los tramos de calle
//
//        // Contestamos a otros cruces cuantos vehículos pueden enviar a los tramos de calle
//        // Enviamos vehículos a otros cruces
//
//        roadStretchesOutToAnotherCrossroad.entrySet().size()
//        roadStretchesOutToOutOfSystem.entrySet().size()
//
//        // Recibimos vehículos de otros cruces
//        for (int i = 0; i < roadStretchesInFromAnotherCrossroad.entrySet().size(); i++) {
//            this.listenMessages();
//        }
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
        totalVehiclesOutOfSystemPerTick.put(currentTicks, totalVehiclesOutOfSystem);
    }

    // Informamos del estado actual del cruce
    // TODO: Refactor, quitar bulk
    private void reportCurrentState(Boolean isBulk) {
        if (!isBulk || hasSimulationFinished()) {
            // Obtenemos el estado actual de ocupación de los tramos de calle del cruce
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
                tickInformation.add(CommonConstant.VEHICLES_TOTAL + this.getLocalName(), String.format("%s", this.calculateTotalVehicles()));
                tickInformation.add(CommonConstant.VEHICLES_IN + this.getLocalName(), String.format("%s", totalVehiclesIn));
                tickInformation.add(CommonConstant.VEHICLES_OUT + this.getLocalName(), String.format("%s", totalVehiclesOut));
                tickInformation.add(CommonConstant.VEHICLES_OUT_OF_SYSTEM + this.getLocalName(), String.format("%s", totalVehiclesOutOfSystem));
                tickInformation.add(CommonConstant.AVERAGE_TICKS_TO_LEAVE + this.getLocalName(), String.format("%s", totalVehiclesOutOfSystem == 0 ? 0 : totalTicksToExit / totalVehiclesOutOfSystem));
                tickInformation.add(CommonConstant.AVERAGE_CURRENT_TICKS_TO_LEAVE + this.getLocalName(), String.format("%s", this.calculateCurrentTicksToLeave(tick)));
                tickInformation.add(CommonConstant.STATE + this.getLocalName(), String.format("%s", currentState));
                jsonReport.add(tick.toString(), tickInformation);
            });

            // Enviamos el mensaje al agente ciudad
            String report = MessageConstant.REPORT + " " + jsonReport;
            this.sendACLMessage(ACLMessage.INFORM, this.getAID(), new AID(cityName, AID.ISLOCALNAME), report);
        }
    }

    // Mejoramos los tiempos de los estados
    private void optimizeStateTimes(Boolean active) {
        if (active) {
            // Evaluamos si hay algún tramo de calle de entrada con congestión
            Map<String, Double> congestedRoadStretchesIn = this.getCongestedRoadStretchesIn();
            Boolean isCongested = congestedRoadStretchesIn.size() > 0;

            // Si hay congestión en el cruce intentamos optimizar los tiempos de los estados para resolverla
            if (isCongested) {
                // Analizamos si existe algún estado del cruce que beneficie la salida de tráfico de los tramos de calle
                    // --> Estado del cruce que habilite tramos de cruce cuyos tramos de calle de origen sean el mayor número de entre los congestionados

                Map<Integer, Map<String, Double>> stateRoadStretchCongestionScore = new HashMap<>(); // | (stateId -> roadStretchOrigin -> congestionScore)
                statesCrossroadStretches.forEach((stateId, roadStretches) -> {
                    Map<String, Double> roadStretchCongestionScore = new HashMap<>();
                    for (Map.Entry<String, Set<String>> entry : roadStretches.entrySet()) {
                        String roadStretchOrigin = entry.getKey();
                        if (congestedRoadStretchesIn.containsKey(roadStretchOrigin)) {
                            roadStretchCongestionScore.put(roadStretchOrigin, congestedRoadStretchesIn.get(roadStretchOrigin));
                        }
                    }
                    stateRoadStretchCongestionScore.put(stateId, roadStretchCongestionScore);
                });

                List<Integer> bestStateCandidates = new ArrayList<>();
                List<Integer> worstStateCandidates = new ArrayList<>();
                Integer roadStretchesMaxNumber = 0;
                Integer roadStretchesMinNumber = congestedRoadStretchesIn.size() + 1;
                for (Map.Entry<Integer, Map<String, Double>> entry : stateRoadStretchCongestionScore.entrySet()) {
                    Integer stateId = entry.getKey(); // (stateId)
                    Map<String, Double> roadStretchCongestionScore = entry.getValue(); // (roadStretchOrigin -> congestionScore)
                    Integer roadStretchesNumber = roadStretchCongestionScore.size();
                    // Obtenemos los candidatos a ser el mejor estado
                    if (roadStretchesMaxNumber < roadStretchesNumber) {
                        bestStateCandidates.clear();
                        bestStateCandidates.add(stateId);
                        roadStretchesMaxNumber = roadStretchesNumber;
                    } else if (roadStretchesMaxNumber.equals(roadStretchesNumber)) {
                        bestStateCandidates.add(stateId);
                    }
                    // Obtenemos los candidatos a ser el peor estado
                    if (roadStretchesMinNumber > roadStretchesNumber) {
                        worstStateCandidates.clear();
                        worstStateCandidates.add(stateId);
                        roadStretchesMinNumber = roadStretchesNumber;
                    }
                    else if (roadStretchesMinNumber.equals(roadStretchesNumber)) {
                        worstStateCandidates.add(stateId);
                    }
                }

                Integer bestState = -1;
                Integer worstState = -1;
                if (bestStateCandidates.size() == 1) { // Si solo hay un candidato, ese es el mejor estado
                    bestState = bestStateCandidates.get(0);
                } else { // Si hay varios candidatos, hay que elegir el mejor (el estado que habilite tramos de cruce tales que la suma de las puntuaciones de congestión de sus tramos de calle de origen sea la mayor)
                    Double bestStateScore = Double.NEGATIVE_INFINITY;
                    for (Integer bestStateCandidate : bestStateCandidates) {
                        // Comprobar si es posible el incremento de tiempo del estado candidato
                        // --> Si el tiempo actual del estado candidato más lo que se va a aumentar es menor o igual que el máximo, pasar a valorar las puntuaciones
                        // --> Si el tiempo actual del estado candidato más lo que se va a aumentar es mayor que el máximo, pasar al siguiente candidato a mejor estado
                        if (this.isPossibleToIncreaseStateTime(bestStateCandidate)) {
                            Double currentScore = 0.0;
                            for (Map.Entry<String, Double> entry : stateRoadStretchCongestionScore.get(bestStateCandidate).entrySet()) {
                                Double congestionScore = entry.getValue();
                                currentScore += congestionScore;
                            }
                            if (bestStateScore < currentScore) {
                                bestState = bestStateCandidate;
                                bestStateScore = currentScore;
                            }
                        }
                    }
                }

                if (worstStateCandidates.size() == 1) { // Si solo hay un candidato, ese es el peor estado
                    worstState = worstStateCandidates.get(0);
                } else { // Si hay varios candidatos, hay que elegir el peor (el estado que habilite menos tramos de cruce o habilitándolos tales que la suma de las puntuaciones de congestión de sus tramos de calle de origen sea la menor)
                    Double worstStateScore = Double.POSITIVE_INFINITY;
                    for (Integer worstStateCandidate : worstStateCandidates) {
                        // Comprobar si es posible la disminución de tiempo del estado candidato
                            // --> Si el tiempo actual del estado candidato menos lo que se va a disminuir es mayor o igual que el mínimo, pasar a valorar las puntuaciones
                            // --> Si el tiempo actual del estado candidato menos lo que se va a disminuir es menor que el mínimo, pasar al siguiente candidato a peor estado
                        if(this.isPossibleToReduceStateTime(worstStateCandidate)) {
                            Double currentScore = 0.0;
                            for (Map.Entry<String, Double> entry : stateRoadStretchCongestionScore.get(worstStateCandidate).entrySet()) {
                                Double congestionScore = entry.getValue();
                                currentScore += congestionScore;
                            }
                            if (worstStateScore > currentScore) {
                                worstState = worstStateCandidate;
                                worstStateScore = currentScore;
                            }
                        }
                    }
                }

                // Ajustamos los tiempos
                updateStateTimes = this.adjustStateTimes(bestState, worstState);
            }
        }
    }

    private Map<String, Double> getCongestedRoadStretchesIn() {
        Map<String, Double> congestedRoadStretchesIn = new HashMap<>();
        final Double SATURATION_POINT = 60.0;
        final Double VEHICLE_WEIGHT = 0.32;
        final Double OCCUPATION_WEIGHT = 0.68;
        roadStretchesIn.forEach((roadStretchInName, roadStretchInModel) -> {
            if (roadStretchInModel.getOccupancyPercentage() > SATURATION_POINT) {
                Integer vehicles = roadStretchInModel.getVehicles();
                Double occupancyPercentage = roadStretchInModel.getOccupancyPercentage();
                Double congestionScore = VEHICLE_WEIGHT * vehicles + OCCUPATION_WEIGHT * occupancyPercentage;
                congestedRoadStretchesIn.put(roadStretchInName, congestionScore);
            }
        });
        return congestedRoadStretchesIn;
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

    private Double calculateCurrentTicksToLeave(Integer currentTick) {
        Double currentTicksToLeave = 0.0;
        Integer previousSampleTick = Math.max((currentTick - sampleTime), 0);
        Double previousVehiclesOutOfSystem = Double.valueOf(totalVehiclesOutOfSystemPerTick.get(previousSampleTick));
        Double currentVehiclesOutOfSystem = Double.valueOf(totalVehiclesOutOfSystemPerTick.get(currentTick));
        currentTicksToLeave = Double.valueOf((currentVehiclesOutOfSystem - previousVehiclesOutOfSystem) / sampleTime);
        return currentTicksToLeave;
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
    //**************************************************//
}
