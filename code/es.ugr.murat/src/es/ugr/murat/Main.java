package es.ugr.murat;

import es.ugr.murat.agent.Crossroad;
import es.ugr.murat.agent.TrafficLight;
import es.ugr.murat.appboot.JADEBoot;
import es.ugr.murat.constant.CrossroadConstant;
import es.ugr.murat.constant.TrafficLightConstant;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        System.out.println("¡Hello MURAT!");
        JADEBoot connection = new JADEBoot();
        connection.launchAgent(TrafficLightConstant.AGENT_NAME + new Random().nextInt(5), TrafficLight.class);
        connection.launchAgent(TrafficLightConstant.AGENT_NAME + "10", TrafficLight.class);
        connection.launchAgent(TrafficLightConstant.AGENT_NAME + "11", TrafficLight.class);
        connection.launchAgent(CrossroadConstant.AGENT_NAME + "1", Crossroad.class);
        connection.launchAgent(CrossroadConstant.AGENT_NAME + "2", Crossroad.class);
        connection.launchAgent(CrossroadConstant.AGENT_NAME + "3", Crossroad.class);
    }
}
