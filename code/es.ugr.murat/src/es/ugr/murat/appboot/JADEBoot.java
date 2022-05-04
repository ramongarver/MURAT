package es.ugr.murat.appboot;

import es.ugr.murat.constant.JADEBootConstant;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JADEBoot {

    private Runtime runtime;
    private Profile profile;
    private ContainerController containerController;
    private String host;
    private String port;
    private String containerName;
    private List<String> agentNames;
    private Map<String, AgentController> agentControllers;

    public JADEBoot() {
        this.host = JADEBootConstant.HOST;
        this.port = JADEBootConstant.PORT;
        this.containerName = JADEBootConstant.CONTAINER_NAME;
        this.agentNames = new ArrayList<>();
        this.agentControllers = new HashMap<>();
        this.setupJADEConnection(this.host, this.port, this.containerName);
    }

    protected void setupJADEConnection (String host, String port, String containerName) {
        this.runtime = Runtime.instance();
        this.profile = new ProfileImpl();
        profile.setParameter(Profile.MAIN_HOST, host);
        profile.setParameter(Profile.MAIN_PORT, port);
        profile.setParameter(Profile.CONTAINER_NAME, containerName);
        this.containerController = runtime.createAgentContainer(profile);
    }

    public void launchAgent(String name, Class clazz) {
        System.out.println("||Launching||Agent-" + clazz.getSimpleName() + "::" + name);
        try {
            AgentController agentController;
            agentController = containerController.createNewAgent(name, clazz.getName(), null);
            agentController.start();
            agentNames.add(name);
            agentControllers.put(name, agentController);
        } catch (StaleProxyException e) {
            e.printStackTrace();
        }
    }


}
