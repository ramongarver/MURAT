package es.ugr.murat.helper;

/**
 * Clase contenedora de funciones útiles comunes a diferentes partes del programa.
 *
 * @author Ramón García Verjaga
 */
public class Helper {

    // Obtenemos el origen del cruce | Para la forma RSN-RSM obtenemos RSN
    public static String getCrossroadStretchOrigin(String crossroadStretchName) {
        return crossroadStretchName.split("-")[0];
    }
    // Obtenemos el destino del cruce | Para la forma RSN-RSM obtenemos RSM
    public static String getCrossroadStretchDestination(String crossroadStretchName) {
        return crossroadStretchName.split("-")[1];
    }

}
