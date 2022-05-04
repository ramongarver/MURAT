package es.ugr.murat.agent;

import es.ugr.murat.constant.MessageConstant;
import es.ugr.murat.constant.TrafficLightConstant;
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

    @Override
    protected void setup() {
        super.setup();
        status = TrafficLightConstant.LOAD_DATA;
        light = TrafficLightConstant.RED;
        message = null;
        System.out.println("||Launched||" + this.getClass().getSimpleName() + "::" + this.getAID().getLocalName());
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
        System.out.println("||--Estado de carga de datos");
        System.out.println("||--Datos cargados");
        status = TrafficLightConstant.LISTEN_CROSSROAD;

    }

    protected void listenCrossroad() {
        System.out.println("||--Estado de escucha al cruce");

        message = blockingReceive();
        switch (message.getPerformative()) {
            case ACLMessage.REQUEST -> {
                if (MessageConstant.CHANGE_LIGHT.equals(message.getContent())) {
                    light = light == TrafficLightConstant.RED ? TrafficLightConstant.GREEN : TrafficLightConstant.RED;
                    // TODO: INFORM
                }
                else if (MessageConstant.SET_LIGHT_TO_RED.equals(message.getContent())) {
                    light = TrafficLightConstant.RED;
                    // TODO: INFORM
                }
                else if (MessageConstant.SET_LIGHT_TO_GREEN.equals(message.getContent())) {
                    light = TrafficLightConstant.GREEN;
                    // TODO: INFORM
                }
                else if (MessageConstant.FINALIZE.equals(message.getContent())) {
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
            System.out.println(e);
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
