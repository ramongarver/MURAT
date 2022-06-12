package es.ugr.murat.agent;

import com.eclipsesource.json.Json;
import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CityConstant;
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
    private Map<Integer, Map<String, Double>> tickRoadStretchOccupation; // Información de la simulación para cada tick | (tick -> columnName -> value)

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
        tickRoadStretchOccupation = new HashMap<>();
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
                        tick.getValue().asObject().forEach((columnName) -> {
                            tickRoadStretchOccupation.get(currentTick).put(columnName.getName(), Double.parseDouble(columnName.getValue().asString().replace(",", ".")));
                        });

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
            tickRoadStretchOccupation.forEach((tick, roadStretchNameOccupation) -> {
                if (tick % sampleTime == 0) {
                    List<String> record = new ArrayList<>();
                    LocalTime time = initialTime.plusSeconds(tick);
                    record.add(time.format(DateTimeFormatter.ISO_LOCAL_TIME));
                    roadStretchNameOccupation.forEach((roadStretchName, occupation) -> {
                        record.add(occupation.toString().replace(".", ","));
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
            roadStretchOccupation.put(CityConstant.VEHICLES_IN, 0.0);
            roadStretchOccupation.put(CityConstant.VEHICLES_OUT, 0.0);

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

            tickRoadStretchOccupation.put(i, roadStretchOccupationSorted);
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
        headers.add(CityConstant.TIME);
        headers.addAll(roadStretchesNames);
        headers.add(CityConstant.VEHICLES_IN);
        headers.add(CityConstant.VEHICLES_OUT);
        for (Map.Entry<Integer, CrossroadModel> entry : crossroads.entrySet()) {
            CrossroadModel crossroadModel = entry.getValue();
            headers.add(CityConstant.VEHICLES_IN + crossroadModel.getName());
            headers.add(CityConstant.VEHICLES_OUT + crossroadModel.getName());
        }
        for (Map.Entry<Integer, CrossroadModel> entry : crossroads.entrySet()) {
            CrossroadModel crossroadModel = entry.getValue();
            headers.add(CityConstant.STATE + crossroadModel.getName());
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

}
