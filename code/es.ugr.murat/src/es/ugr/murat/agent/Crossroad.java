package es.ugr.murat.agent;

import es.ugr.murat.constant.CrossroadConstant;

/**
 * Class representing a Crossroad agent.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class Crossroad {

    /**
     * Status of the Crossroad agent.
     * The following values are possible:
     * TODO...
     */
    Integer status;

    public Crossroad() {
        this.status = CrossroadConstant.STATUS;
        System.out.println("Crossroad agent created!");
    }


}
