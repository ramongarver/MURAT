package es.ugr.murat.agent;

import es.ugr.murat.constant.ActionConstant;
import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.constant.TrafficLightConstant;
import es.ugr.murat.util.Logger;
import jade.lang.acl.ACLMessage;

/**
 * Clase representando al agente semáforo (TrafficLight).
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class TrafficLight extends MURATBaseAgent {

    private Integer status;
    private Integer light;
    private String roadStretchInName;

    @Override
    protected void setup() {
        super.setup();
        status = TrafficLightConstant.LOAD_DATA;
        light = TrafficLightConstant.RED;
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
        // TODO: Cargar datos del semáforo
        Logger.info(ActionConstant.LOADED_DATA, this.getClass().getSimpleName(), this.getLocalName());
        status = TrafficLightConstant.LISTEN_CROSSROAD;
    }

    protected void listenCrossroad() {
        System.out.println("||--Estado de escucha al cruce");

        incomingMessage = this.blockingReceive();
        switch (incomingMessage.getPerformative()) {
            case ACLMessage.REQUEST -> {
                if (MessageConstant.CHANGE_LIGHT.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.RED.equals(light) ? TrafficLightConstant.GREEN : TrafficLightConstant.RED;
                    System.out.println("||Light changed||Agent-" + this.getClass().getSimpleName() + "::" + this.getAID().getLocalName() + "||Light::" + light);
                    // TODO: INFORM
                }
                else if (MessageConstant.SET_LIGHT_TO_RED.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.RED;
                    // TODO: INFORM
                }
                else if (MessageConstant.SET_LIGHT_TO_GREEN.equals(incomingMessage.getContent())) {
                    light = TrafficLightConstant.GREEN;
                    // TODO: INFORM
                }
                else if (MessageConstant.FINALIZE.equals(incomingMessage.getContent())) {
                    status = TrafficLightConstant.EXIT;
                    // TODO: INFORM
                }
                else {
                    System.out.println("Mensaje no conocido"); // TODO: pensar si manejar esto de otra forma
                }
            }
        }

        try {
            //Ponemos a "Dormir" el programa durante los ms que queremos
            Thread.sleep(3*1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("||--" + this.getName() + "::light::" + light);
    }

    protected void exit() {
        exit = true;
    }

    /**
     * {@link TrafficLight#status}
     * @return status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * {@link TrafficLight#light}
     * @return light
     */
    public Integer getLight() {
        return light;
    }

}
