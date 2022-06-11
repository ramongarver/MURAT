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

    private Integer trafficLightId; // Identificador del semáforo
    private Integer crossroadId; // Identificador del cruce al que está asociado el semáforo
    private String light; // Color de la luz del semáforo
    private String roadStretchInName; // Tramo de calle de entrada al cruce que regula el semáforo

    //*************** Ciclo de vida del agente ***************//
    @Override
    protected void setup() {
        super.setup();
        status = TrafficLightConstant.LOAD_DATA;
        trafficLightId = Integer.parseInt(this.getLocalName().split(TrafficLightConstant.AGENT_NAME)[1]);
        crossroadId = null;
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
        roadStretchInName = Simulation.simulation.getTrafficLightRoadStretchInName(trafficLightId);
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = TrafficLightConstant.LISTEN_CROSSROAD;
    }

    protected void listenCrossroad() {
        Logger.info(ActionConstant.LISTENING_CROSSROAD, this.getClass().getSimpleName(), this.getLocalName());
        this.listenMessages();
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
                    this.sendACLMessage(ACLMessage.INFORM, this.getAID(), incomingMessage.getSender(),
                            TrafficLightConstant.RED.equals(light) ? MessageConstant.SET_LIGHT_TO_RED : MessageConstant.SET_LIGHT_TO_GREEN);
                    Logger.info(ActionConstant.LIGHT_CHANGED, this.getClass().getSimpleName(), this.getLocalName(), "||Light::" + light);
                }
                // Ponemos el semáforo en rojo
                else if (MessageConstant.SET_LIGHT_TO_RED.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.RED;
                    this.sendACLMessage(ACLMessage.INFORM, this.getAID(), incomingMessage.getSender(), MessageConstant.SET_LIGHT_TO_RED);
                    Logger.info(ActionConstant.LIGHT_SET_TO_RED, this.getClass().getSimpleName(), this.getLocalName(), "||Light::" + light);
                }
                // Ponemos el semáforo en verde
                else if (MessageConstant.SET_LIGHT_TO_GREEN.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.GREEN;
                    this.sendACLMessage(ACLMessage.INFORM, this.getAID(), incomingMessage.getSender(), MessageConstant.SET_LIGHT_TO_GREEN);
                    Logger.info(ActionConstant.LIGHT_SET_TO_GREEN, this.getClass().getSimpleName(), this.getLocalName(), "||Light::" + light);
                }
                // Finalizamos el agente
                else if (MessageConstant.FINALIZE.equals(incomingMessage.getContent())) {
                    status = TrafficLightConstant.EXIT;
                    // TODO: INFORM
                } else { // Manejamos mensajes no conocidos
                    Logger.info(ActionConstant.MESSAGE_UNKNOWN_RECEIVED, this.getClass().getSimpleName(), this.getLocalName());
                }
            }
        }
    }
    //**************************************************//

}
