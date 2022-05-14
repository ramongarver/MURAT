package es.ugr.murat.util;

import com.eclipsesource.json.Json;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import java.util.Iterator;

public class Logger {

    public static final String NULL_VALUE = "";

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

    public static void info(String content) {
        System.out.println(content);
    }

    public static void info(String action, String agentType, String agentLocalName) {
        System.out.println("||" + action + "||Agent-" + agentType + "::" + agentLocalName);
    }

    public static void info(String action, String agentType, String agentLocalName, String extra) {
        String str = "||" + action + "||Agent-" + agentType + "::" + agentLocalName;
        if (extra != null) str += "||" + extra;
        System.out.println(str);
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

    /**
     * Obtenido de ACLMessageTools.java por Anatoli-Grishenko.
     * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/messaging/ACLMessageTools.java
     */
    public static String fancyWriteACLM(ACLMessage original, boolean simple) {
        String res = "", sep = "|";
        ACLMessage msg = (ACLMessage) original.clone();
        res += (msg.getSender() == null ? NULL_VALUE : "||SND" + sep + msg.getSender().getLocalName());
        Iterator it;
        it = msg.getAllReceiver();
        res += "||RCV" + sep;
        while (it.hasNext()) {
            res += ((AID) it.next()).getLocalName() + " ";
        }
        res = res + "||CNT" + sep + (isJsonACLM(msg) ? trimString(msg.getContent(), 255) : msg.getContent()) + "||";
        if (!simple) {
            it = msg.getAllReplyTo();
            res = "||PFM" + sep + ACLMessage.getPerformative(msg.getPerformative()) + res;
            res += "RPT" + sep;
            while (it.hasNext()) {
                res += ((AID) it.next()).getLocalName() + " ";
            }
            res += (msg.getProtocol() == null ? NULL_VALUE : "||PRT" + sep + msg.getProtocol())
                    + (msg.getConversationId() == null ? NULL_VALUE : "||CNV" + sep + msg.getConversationId())
                    + (msg.getEncoding() == null ? NULL_VALUE : "||ENC" + sep + msg.getEncoding())
                    + (msg.getReplyWith() == null ? NULL_VALUE : "||RPW" + sep + msg.getReplyWith())
                    + (msg.getInReplyTo() == null ? NULL_VALUE : "||IRT" + sep + msg.getInReplyTo())
                    + (msg.getLanguage() == null ? NULL_VALUE : "||LAN" + sep + trimString(msg.getLanguage(), 10))
                    + (msg.getOntology() == null ? NULL_VALUE : "||ONT" + sep + msg.getOntology());
            res += "||";
        }
        return res;
    }

    /**
     * Obtenido de ACLMessageTools.java por Anatoli-Grishenko.
     * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/messaging/ACLMessageTools.java
     */
    public static String fancyWriteACLM(ACLMessage original) {
        return fancyWriteACLM(original, false);
    }

    /**
     * Obtenido de ACLMessageTools.java por Anatoli-Grishenko.
     * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/messaging/ACLMessageTools.java
     */
    public static boolean isJsonString(String s) {
        boolean res = false;

        if (s == null) {
            return false;
        }
        try {
            res = s.length() > 0 && s.charAt(0) == '{' && Json.parse(s) != null;
        } catch (Exception ex) {
            System.err.println("isJsonString() " + ex + " " + s);
        }
        return res;

    }

    /**
     * Obtenido de ACLMessageTools.java por Anatoli-Grishenko.
     * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/messaging/ACLMessageTools.java
     */
    public static boolean isJsonACLM(ACLMessage m) {

        if (m == null || m.getContent() == null) {
            return false;
        }
        return isJsonString(m.getContent());
    }

    /**
     * Obtenido de Logger.java por Anatoli-Grishenko.
     * https://github.com/Anatoli-Grishenko/es.ugr.larva.core/blob/master/src/disk/Logger.java
     */
    public static String trimString(String original, int max) {
        String s = original + "";
        if (s.length() > max) {
            return s.substring(0, Math.min(max, s.length())) + "...";
        } else {
            return s;
        }
    }

}
