package es.ugr.murat.agent;

import es.ugr.murat.constant.TrafficLightConstant;

/**
 * Class representing a Traffic Light agent.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class TrafficLight {

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
    Integer light;

    public TrafficLight() {
        this.status = TrafficLightConstant.STATUS;
        this.light = TrafficLightConstant.OFF;
        System.out.println("TrafficLight agent created!");
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
