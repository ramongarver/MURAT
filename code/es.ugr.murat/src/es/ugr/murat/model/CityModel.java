package es.ugr.murat.model;

/**
 * Clase representando el modelo de la ciudad.
 *
 * @author Ramón García Verjaga
 * @version v0.0.1
 */
public class CityModel {

    private String name; // Nombre de la ciudad
    private String description; // Descripción de la ciudad/escenario

    public CityModel(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

}
