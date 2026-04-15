package iu.piisj.eventmanager_sose2026.dto;

public class EventDTO {

    private String name;
    private String location;
    private String date;
    private String state;

    public EventDTO() {}

    public EventDTO(String name, String location, String date, String state){
        this.name = name;
        this.location = location;
        this.date = date;
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setState(String state) {
        this.state = state;
    }
}
