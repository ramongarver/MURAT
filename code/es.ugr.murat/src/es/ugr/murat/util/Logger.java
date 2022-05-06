package es.ugr.murat.util;

public class Logger {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);

    public static void severe(String msg) {
        LOGGER.severe(msg);
    }

    public static void warning(String msg) {
        LOGGER.warning(msg);
    }

//    public static void info(String msg) {
//        LOGGER.info(msg);
//    }

    public static void info(String action, String agentType, String agentLocalName) {
        System.out.println("||" + action + "||Agent-" + agentType + "::" + agentLocalName);
    }

    public static void config(String msg) {
        LOGGER.config(msg);
    }

    public static void fine(String msg) {
        LOGGER.fine(msg);
    }

    public static void finer(String msg) {
        LOGGER.finer(msg);
    }

    public static void finest(String msg) {
        LOGGER.finest(msg);
    }

}
