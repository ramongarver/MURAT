package es.ugr.murat.agent;

import com.eclipsesource.json.Json;
import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CityConstant;
import es.ugr.murat.constant.CommonConstant;
import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.model.CrossroadModel;
import es.ugr.murat.simulation.Simulation;
import es.ugr.murat.util.Logger;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase representando al agente ciudad (CityAgent).
 *
 * @author Ramón García Verjaga
 */
public class CityAgent extends MURATBaseAgent {

    private Map<Integer, CrossroadModel> crossroads; // Cruces de la ciudad | (crossroadId -> crossroadModel)
    private List<String> crossroadsNames; // Nombres de cruces de la ciudad | (crossroadName)
    private List<String> roadStretchesNames; // Nombres de calles de la ciudad | (roadStretchName)

    private Integer sampleTime; // Tiempo de muestreo
    private LocalTime initialTime; // Hora de inicio de la simulación
    private Integer totalTicks; // Número total de ticks a realizar para completar la simulación

    private Map<String, Integer> crossroadsTicks; // Número de ticks que ha realizado cada cruce | (crossroadName -> ticks)
    private Map<Integer, Map<String, Double>> simulationInfoByTick; // Información de la simulación para cada tick | (tick -> columnName -> value)

    private Map<Integer, Map<String, Boolean>> receivedReports; // Información de la recepción de informes de los cruces en cada tick (tick -> crossroadName -> received);

    private Double totalVehiclesIn; // Número total de vehículos que han entrado en la simulación
    private Double totalVehiclesOut; // Número total de vehículos que han salido de la simulación


    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        status = CityConstant.LOAD_DATA;
        crossroads = null;
        crossroadsNames = null;
        roadStretchesNames = null;
        sampleTime = null;
        initialTime = null;
        totalTicks = null;
        crossroadsTicks = new HashMap<>();
        simulationInfoByTick = new HashMap<>();
        receivedReports = new HashMap<>();
        totalVehiclesIn = 0.0;
        totalVehiclesOut = 0.0;
        Logger.info(ActionConstant.LAUNCHED_AGENT, this.getClass().getSimpleName(), this.getLocalName());
    }

    @Override
    protected void execute() {
        switch (status) {
            case CityConstant.LOAD_DATA -> loadData();
            case CityConstant.WAIT_CROSSROADS_REPORTS -> waitCrossroadsReports();
            case CityConstant.GENERATE_SIMULATION_REPORT -> generateSimulationReport();
            case CityConstant.FINALIZE_CROSSROADS -> finalizeCrossroads();
            case CityConstant.EXIT -> exit();
        }
    }

    protected void loadData() {
        Logger.info(ActionConstant.LOADING_DATA, this.getClass().getSimpleName(), this.getLocalName());
        // Cargamos datos la ciudad
            // Obtenemos datos de los cruces de la ciudad
        crossroads = Simulation.simulation.getCityCrossroads();
            // Obtenemos datos de los nombres de los cruces de la ciudad
        crossroadsNames = Simulation.simulation.getCityCrossroadsNames();
                // Inicializamos los ticks para cada cruce
        this.initializeCrossroadsTicks();
            // Obtenemos datos de los nombres de las calles de la ciudad
        roadStretchesNames = Simulation.simulation.getCityRoadStretchesNames();
            // Obtenemos datos del tiempo de muestreo
        sampleTime = Simulation.simulation.getCityConfigurationSampleTime();
            // Obtenemos datos del tiempo de inicio de la simulación
        initialTime = Simulation.simulation.getCityConfigurationInitialTime();
            // Obtenemos los ticks totales de la simulación
        totalTicks = Simulation.simulation.getSimulationSeconds();
            // Inicializamos la estructura que va a contener el estado de la simulación en cada tick
        this.initializeTickRoadStretchOccupationTicks();
            // Inicializamos la estructura que va a contener la información sobre la recepción de reports
        this.initializeReceivedReports();
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = CityConstant.WAIT_CROSSROADS_REPORTS;
    }

    protected void waitCrossroadsReports() {
        this.listenMessages();
    }

    protected void generateSimulationReport() {
        try {
            this.createCSVFile();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        status = CityConstant.FINALIZE_CROSSROADS;
    }

    protected void finalizeCrossroads() {
        // Enviamos un mensaje a cada semáforo para pedirle que finalice
        crossroads.forEach((crossroadId, crossroadModel) ->
                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
                        new AID(CrossroadConstant.AGENT_NAME + crossroadId, AID.ISLOCALNAME),
                        MessageConstant.FINALIZE));
        status = CityConstant.EXIT;
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
                // Obtenemos la información del estado de los cruces
                if (incomingMessage.getContent().startsWith(MessageConstant.REPORT)) {
                    String report = incomingMessage.getContent().replace(MessageConstant.REPORT + " ", "");
                    Json.parse(report).asObject().forEach((tick) -> {
                        // Actualizamos el número de ticks del cruce que haya enviado el mensaje
                        String crossroadName = incomingMessage.getSender().getLocalName();
                        Integer currentTick = Integer.parseInt(tick.getName());
                        crossroadsTicks.put(crossroadName, currentTick);

                        // Para el tick actual actualizamos los parámetros del registro con los valores del estado actual
                        tick.getValue().asObject().forEach((columnNameJsonObject) -> {
                            String columnName = columnNameJsonObject.getName();
                            Double value = Double.parseDouble(columnNameJsonObject.getValue().asString().replace(",", "."));
                            simulationInfoByTick.get(currentTick).put(columnName, value);
                            this.updateVehiclesInOut(crossroadName, columnName, currentTick, value);
                        });
                        simulationInfoByTick.get(currentTick).put(CommonConstant.VEHICLES_IN, totalVehiclesIn);
                        simulationInfoByTick.get(currentTick).put(CommonConstant.VEHICLES_OUT, totalVehiclesOut);

                        // Etiquetamos el report como recibido para el tick actual y el cruce remitente del mismo
                        receivedReports.get(currentTick).put(crossroadName, true);

                        // Comprobamos si se han recibido todos los informes y avisamos a los cruces de que pueden continuar
                        if(this.receivedAllTickReports(currentTick)) {
                            this.notifyCrossroads();
                        }

                        // Comprobamos si ha finalizado la simulación
                        if (this.hasSimulationFinished()) {
                            status = CityConstant.GENERATE_SIMULATION_REPORT;
                        }
                    });
                } else { // Manejamos mensajes no conocidos
                    Logger.info(ActionConstant.MESSAGE_UNKNOWN_RECEIVED, this.getClass().getSimpleName(), this.getLocalName());
                }
            }
        }
    }

    // Creamos el archivo CSV y almacenamos la información de la simulación
    private void createCSVFile() throws IOException {
        // Creamos el archivo csv en el que vamos a almacenar la información de la simulación
        FileWriter csv = this.createFileWriter();

        // Construimos la lista de las cabeceras (nombre de cada columna)
        List<String> headers = this.getCSVHeaders();

        // Almacenamos la información de la simulación
        try (CSVPrinter printer = new CSVPrinter(csv,
                CSVFormat.Builder
                    .create(CSVFormat.EXCEL)
                    .setDelimiter(';')
                    .setHeader(headers.toArray(new String[0]))
                    .build())) {
            simulationInfoByTick.forEach((tick, columnNames) -> {
                if (tick % sampleTime == 0) {
                    List<String> record = new ArrayList<>();
                    headers.forEach((columnName) -> {
                        if (columnName.equals(headers.get(0))) {
                            LocalTime time = initialTime.plusSeconds(tick);
                            record.add(time.format(DateTimeFormatter.ISO_LOCAL_TIME));
                        } else {
                            record.add(columnNames.get(columnName).toString().replace(".", ","));
                        }
                    });
                    try {
                        printer.printRecord(record);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    //**************************************************//
    // Inicializamos el número de ticks a 0 para cada cruce
    private void initializeCrossroadsTicks() {
        crossroadsNames.forEach((crossroadName) -> crossroadsTicks.put(crossroadName, 0));
    }

    // Inicializamos la estructura que va a contener el estado de la simulación en cada tick
    private void initializeTickRoadStretchOccupationTicks() {
        // Inicializamos la estructura para cada tick de la simulación
        for (int i = 0; i <= totalTicks + 1; i++) {
            // Creamos el mapa con todos los parámetros que vamos a almacenar
            Map<String, Double> roadStretchOccupation = new HashMap<>();
            roadStretchesNames.forEach((roadStretchName) -> {
                roadStretchOccupation.put(roadStretchName, 0.0);
            });
            roadStretchOccupation.put(CommonConstant.VEHICLES_IN, 0.0);
            roadStretchOccupation.put(CommonConstant.VEHICLES_OUT, 0.0);

            // Ordenamos el mapa por orden alfabético
            Map<String, Double> roadStretchOccupationSorted = roadStretchOccupation.entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByKey(new Comparator<String>() {
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
                    }))
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    Map.Entry::getValue,
                                    (e1, e2) -> e1,
                                    LinkedHashMap::new)
                    );

            simulationInfoByTick.put(i, roadStretchOccupationSorted);
        }
    }

    // Inicializamos la estructura que va a contener la información sobre la recepción de reports
    private void initializeReceivedReports() {
        // Inicializamos la estructura para cada tick de la simulación
        for (int i = 0; i <= totalTicks + 1; i++) {
            // Creamos el mapa con todos los parámetros que vamos a almacenar
            Map<String, Boolean> receivedReport = new HashMap<>();
            crossroadsNames.forEach((crossroadName) -> {
                receivedReport.put(crossroadName, false);
            });
            receivedReports.put(i, receivedReport);
        }
    }

    private FileWriter createFileWriter() throws IOException {
        // Obtenemos la fecha y hora actual y la formateamos para utilizarla en el nombre del archivo
        String currentDateTimeFormatted = this.getCurrentDateTimeFormatted();

        // Creamos el archivo en el que se va a almacenar la información
        final String PATH = "resources/data/result/";
        String fileName = PATH + "simulation-" + currentDateTimeFormatted + ".csv";
        return new FileWriter(fileName);
    }

    private String getCurrentDateTimeFormatted() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        return currentDateTime.format(formatter);
    }

    // Obtenemos la lista de las cabeceras (nombre de cada columna)
    private List<String> getCSVHeaders() {
        List<String> headers = new ArrayList<>();
        headers.add(CommonConstant.TIME);
        headers.addAll(roadStretchesNames);
        headers.add(CommonConstant.VEHICLES_IN);
        headers.add(CommonConstant.VEHICLES_OUT);
        for (Map.Entry<Integer, CrossroadModel> crossroad : crossroads.entrySet()) {
            String crossroadName = crossroad.getValue().getName();
            headers.add(this.getColumnName(crossroadName, CommonConstant.STATE));
            headers.add(this.getColumnName(crossroadName, CommonConstant.VEHICLES_TOTAL));
            headers.add(this.getColumnName(crossroadName, CommonConstant.VEHICLES_IN));
            headers.add(this.getColumnName(crossroadName, CommonConstant.VEHICLES_IN_FROM_OUT_OF_SYSTEM));
            headers.add(this.getColumnName(crossroadName, CommonConstant.VEHICLES_IN_FROM_ANOTHER_CROSSROAD));
            headers.add(this.getColumnName(crossroadName, CommonConstant.VEHICLES_OUT));
            headers.add(this.getColumnName(crossroadName, CommonConstant.VEHICLES_OUT_OF_SYSTEM));
            headers.add(this.getColumnName(crossroadName, CommonConstant.VEHICLES_OUT_TO_ANOTHER_CROSSROAD));
            headers.add(this.getColumnName(crossroadName, CommonConstant.TICKS_AVERAGE_PER_SAMPLE_OUT));
            headers.add(this.getColumnName(crossroadName, CommonConstant.TICKS_AVERAGE_CUMULATIVE_OUT));
            headers.add(this.getColumnName(crossroadName, CommonConstant.TICKS_AVERAGE_PER_SAMPLE_OUT_OF_SYSTEM));
            headers.add(this.getColumnName(crossroadName, CommonConstant.TICKS_AVERAGE_CUMULATIVE_OUT_OF_SYSTEM));
            headers.add(this.getColumnName(crossroadName, CommonConstant.TICKS_AVERAGE_PER_SAMPLE_OUT_TO_ANOTHER_CROSSROAD));
            headers.add(this.getColumnName(crossroadName, CommonConstant.TICKS_AVERAGE_CUMULATIVE_OUT_TO_ANOTHER_CROSSROAD));
        }
        return headers;
    }

    // Evaluamos si la simulación ha finalizado
    private Boolean hasSimulationFinished() {
        for (Map.Entry<String, Integer> entry : crossroadsTicks.entrySet()) {
            Integer ticks = entry.getValue();
            if (!ticks.equals(totalTicks + 1)) {
                return false;
            }
        }
        return true;
    }

    private String getColumnName(String crossroadName, String attributeName) {
        return crossroadName + attributeName;
    }

    private Boolean receivedAllTickReports(Integer tick) {
        for (Map.Entry<String, Boolean> receivedReport : receivedReports.get(tick).entrySet()) {
            Boolean received = receivedReport.getValue();
            if (!received) {
                return false;
            }
        }
        return true;
    }

    private void notifyCrossroads() {
        crossroadsNames.forEach((crossroadName) ->
                this.sendACLMessage(
                        ACLMessage.INFORM,
                        this.getAID(),
                        new AID(crossroadName, AID.ISLOCALNAME),
                        MessageConstant.RECEIVED_ALL_TICK_REPORTS));
    }

    private void updateVehiclesInOut(String crossroadName, String columnName, Integer currentTick, Double value) {
        this.updateVehiclesInFromOutOfSystem(crossroadName, columnName, currentTick, value);
        this.updateVehiclesOutOfSystem(crossroadName, columnName, currentTick, value);
    }

    private void updateVehiclesInFromOutOfSystem(String crossroadName, String columnName, Integer currentTick, Double value) {
        if (columnName.equals(this.getColumnName(crossroadName, CommonConstant.VEHICLES_IN_FROM_OUT_OF_SYSTEM))) {
            Double previousValue = currentTick == 0 ? 0.0 : simulationInfoByTick.get(currentTick - 1).get(this.getColumnName(crossroadName, CommonConstant.VEHICLES_IN_FROM_OUT_OF_SYSTEM));
            totalVehiclesIn += value - previousValue;
        }
    }

    private void updateVehiclesOutOfSystem(String crossroadName, String columnName, Integer currentTick, Double value) {
        if (columnName.equals(this.getColumnName(crossroadName, CommonConstant.VEHICLES_OUT_OF_SYSTEM))) {
            Double previousValue = currentTick == 0 ? 0.0 : simulationInfoByTick.get(currentTick - 1).get(this.getColumnName(crossroadName, CommonConstant.VEHICLES_OUT_OF_SYSTEM));
            totalVehiclesOut += value - previousValue;
        }
    }

}
