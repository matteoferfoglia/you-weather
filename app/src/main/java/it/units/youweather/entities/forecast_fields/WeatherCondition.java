package it.units.youweather.entities.forecast_fields;

public class WeatherCondition {
    
    private int id;
    private String main;
    private String description;
    private String icon;
    
    private WeatherCondition(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return "WeatherCondition{" + "id=" + id + ", main=" + main + ", description=" + description + ", icon=" + icon + '}';
    }

}
