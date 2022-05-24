package es.ugr.murat.agent;

import com.eclipsesource.json.Json;
import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.CityConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.model.CrossroadModel;
import es.ugr.murat.simulation.Simulation;
import es.ugr.murat.util.Logger;
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
    private List<String> crossroadsNames; // Nombres de cruces de la ciudad
    private List<String> roadStretchesNames; // Nombres de calles de la ciudad

    // TODO RGV: ordenar atributos y comentarlos
    private Integer sampleTime;
    private Map<Integer, Map<String, Double>> tickRoadStretchOccupation;
    private LocalTime initialTime; // Hora de inicio de la simulación

    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        status = CityConstant.LOAD_DATA;
        crossroads = null;
        crossroadsNames = null;
        sampleTime = null;
        tickRoadStretchOccupation = new HashMap<>();
        initialTime = null;
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
            // Obtenemos datos de los nombres de las calles de la ciudad
        roadStretchesNames = Simulation.simulation.getCityRoadStretchesNames();
            // Obtenemos datos del tiempo de muestreo
        sampleTime = Simulation.simulation.getCityConfigurationSampleTime();
            // Obtenemos datos del tiempo de inicio de la simulación
        initialTime = Simulation.simulation.getCityConfigurationInitialTime();
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
        try {
            //TODO: Ponemos a "Dormir" el programa durante los ms que queremos
            Thread.sleep(6*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Enviamos un mensaje a cada semáforo para pedirle que finalice
//        crossroads.forEach((crossroadId, crossroadModel) ->
//                this.sendACLMessage(ACLMessage.REQUEST, this.getAID(),
//                        new AID(CrossroadConstant.AGENT_NAME + crossroadId, AID.ISLOCALNAME),
//                        MessageConstant.FINALIZE));
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
                        Map<String, Double> roadStretchOccupation = new HashMap<>();
                        tick.getValue().asObject().forEach((roadStretch) -> {
                            roadStretchOccupation.put(roadStretch.getName(), Double.parseDouble(roadStretch.getValue().asString().replace(",", ".")));
                        });

                        // Ordenamos el HashMap por el valor de sus keys (orden alfabético ascendente)
                        Map<String, Double> roadStretchOccupationSorted = roadStretchOccupation.entrySet()
                                .stream()
                                .sorted(Map.Entry.comparingByKey())
                                .collect(
                                        Collectors.toMap(
                                                Map.Entry::getKey,
                                                Map.Entry::getValue,
                                                (e1, e2) -> e1,
                                                LinkedHashMap::new)
                                );

                        tickRoadStretchOccupation.put(Integer.parseInt(tick.getName()), roadStretchOccupationSorted);
                        if (Integer.parseInt(tick.getName()) == 3600) {
                            status = CityConstant.GENERATE_SIMULATION_REPORT;
                        }
                    });
                } else { // Manejamos mensajes no conocidos
                    Logger.info(ActionConstant.MESSAGE_UNKNOWN_RECEIVED, this.getClass().getSimpleName(), this.getLocalName()); // TODO: pensar si manejar esto de otra forma
                }
            }
        }
    }

    private void createCSVFile() throws IOException {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String currentDateTimeString = currentDateTime.format(formatter);

        final String PATH = "resources/data/result/";
        String fileName = PATH + "simulation-" + currentDateTimeString + ".csv";
        FileWriter out = new FileWriter(fileName);

        List<String> headers = new ArrayList<>();
        headers.add("Time");
        headers.addAll(roadStretchesNames);
        headers.add("Vin");
        headers.add("Vout");

        try (CSVPrinter printer = new CSVPrinter(out,
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

}
