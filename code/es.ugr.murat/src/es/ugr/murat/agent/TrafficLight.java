package es.ugr.murat.agent;

import es.ugr.murat.constant.TrafficLightConstant;

/**
 * Class representing a Traffic Light agent.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class TrafficLight extends MURATBaseAgent {

    /**
     * Status of the Traffic Light agent.
     * The following values are possible:
     * TODO...
     */
    Integer status;

    /**
     * Light of the Traffic Light agent.
     * The following values are possible:
     * OFF, GREEN, AMBER or RED.
     */
    Integer light; // TODO: check if changing type to an enum or string or something it would be better...

    @Override
    protected void setup() {
        super.setup();
        System.out.println("Created||" + this.getClass().getSimpleName() + "::" + this.getAID().getName());
        status = TrafficLightConstant.LOAD_DATA;
        execute();
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
        System.out.println("Estado de carga de datos");
        status = TrafficLightConstant.LISTEN_CROSSROAD;
    }

    protected void listenCrossroad() {
        System.out.println("Estado de escucha al cruce");
        status = TrafficLightConstant.EXIT;
        try {
            //Ponemos a "Dormir" el programa durante los ms que queremos
            Thread.sleep(3*1000);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    protected void exit() {
        System.out.println("Estado de escucha al cruce");
        exit = true;
    }


    @Override
    protected void takeDown() {
        super.takeDown();
        status = TrafficLightConstant.EXIT;
        System.out.println("Eliminando agente " + this.getName());
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
