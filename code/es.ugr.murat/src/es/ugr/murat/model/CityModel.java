package es.ugr.murat.model;

public class CityModel {

    private String name;
    private String description;

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
