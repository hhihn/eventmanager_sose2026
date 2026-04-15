package iu.piisj.eventmanager_sose2026.dto;

public class ParticipantDTO {

    private String name;
    private String firstname;
    private String email;
    private String state;

    public String getName() {
        return name;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getEmail() {
        return email;
    }

    public String getState() {
        return state;
    }

    public ParticipantDTO(String name, String firstname, String email, String state){
        this.email = email;
        this.name = name;
        this.firstname = firstname;
        this.email = email;
        this.state = state;
    }
}
