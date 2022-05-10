package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.constant.TrafficLightConstant;
import es.ugr.murat.simulation.Simulation;
import es.ugr.murat.util.Logger;
import jade.lang.acl.ACLMessage;

/**
 * Clase representando al agente semáforo (TrafficLightAgent).
 *
 * @author Ramón García Verjaga
 */
public class TrafficLightAgent extends MURATBaseAgent {

    private Integer trafficLightId;
    private Integer crossroadId;
    private Integer status;
    private String light;
    private String roadStretchInName;

    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        trafficLightId = Integer.parseInt(this.getLocalName().split(TrafficLightConstant.AGENT_NAME)[1]);
        crossroadId = null;
        status = TrafficLightConstant.LOAD_DATA;
        light = null;
        roadStretchInName = null;
        Logger.info(ActionConstant.LAUNCHED_AGENT, this.getClass().getSimpleName(), this.getLocalName());
    }

    @Override
    protected void execute() {
        switch (status) {
            case TrafficLightConstant.LOAD_DATA -> loadData();
            case TrafficLightConstant.LISTEN_CROSSROAD -> listenCrossroad();
            case TrafficLightConstant.EXIT -> exit();
        }
    }

    protected void loadData() {
        Logger.info(ActionConstant.LOADING_DATA, this.getClass().getSimpleName(), this.getLocalName());
        // Cargamos datos del semáforo
            // Obtenemos datos del cruce al que pertenece
        crossroadId = Simulation.simulation.getTrafficLightCrossroadId(trafficLightId);
            // Obtenemos el nombre del tramo de calle que regula
        roadStretchInName = Simulation.simulation.getTrafficLightRoadStretchInName(crossroadId, trafficLightId);
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = TrafficLightConstant.LISTEN_CROSSROAD;
    }

    protected void listenCrossroad() {
        Logger.info("Estado de escucha al cruce", this.getClass().getSimpleName(), this.getLocalName());
        this.listenMessages();

        try {
            //TODO: Ponemos a "Dormir" el programa durante los ms que queremos
            Thread.sleep(3*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                // Saludamos al cruce TODO: Valorar si al final se implementa
                if (MessageConstant.HELLO.equals(incomingMessage.getContent())) {
                    Logger.info(ActionConstant.HELLO_RECEIVED, this.getClass().getSimpleName(), this.getLocalName());
                    this.sendACLMessage(ACLMessage.INFORM, this.getAID(), incomingMessage.getSender(), MessageConstant.HELLO);
                    Logger.info(ActionConstant.HELLO_SENT, this.getClass().getSimpleName(), this.getLocalName());
                }
            }
            case ACLMessage.REQUEST -> { // Si la performativa es REQUEST
                // Cambiamos el color del semáforo
                if (MessageConstant.CHANGE_LIGHT.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.RED.equals(light) ? TrafficLightConstant.GREEN : TrafficLightConstant.RED;
                    Logger.info(ActionConstant.LIGHT_CHANGED, this.getClass().getSimpleName(), this.getLocalName(), "||Light::" + light);
                    // TODO: INFORM
                }
                // Ponemos el semáforo en rojo
                else if (MessageConstant.SET_LIGHT_TO_RED.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.RED;
                    Logger.info(ActionConstant.LIGHT_SET_TO_RED, this.getClass().getSimpleName(), this.getLocalName(), "||Light::" + light);
                    // TODO: INFORM
                }
                // Ponemos el semáforo en verde
                else if (MessageConstant.SET_LIGHT_TO_GREEN.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.GREEN;
                    Logger.info(ActionConstant.LIGHT_SET_TO_GREEN, this.getClass().getSimpleName(), this.getLocalName(), "||Light::" + light);
                    // TODO: INFORM
                }
                // Finalizamos el agente
                else if (MessageConstant.FINALIZE.equals(incomingMessage.getContent())) {
                    status = TrafficLightConstant.EXIT;
                    // TODO: INFORM
                }
                // Manejamos mensajes no conocidos
                else {
                    Logger.info(ActionConstant.MESSAGE_UNKNOWN_RECEIVED, this.getClass().getSimpleName(), this.getLocalName()); // TODO: pensar si manejar esto de otra forma
                }
            }
        }
    }
    //**************************************************//

}
