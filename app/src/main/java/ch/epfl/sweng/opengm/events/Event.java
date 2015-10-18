package ch.epfl.sweng.opengm.events;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by André on 14/10/2015.
 */
public class Event {

    //TODO : gestion d'erreur
    //TODO : gestion serveur
    private String name;
    private String place;
    private GregorianCalendar date;
    private String description;
    private List<OpenGMMember> participants;

    public Event(){
        participants = new ArrayList<>();
        date = new GregorianCalendar ();
    }

    public Event(String name, String place, GregorianCalendar date, String description, List<OpenGMMember> participants){
        this.name=name;
        this.participants=participants;
        this.date=date;
        this.place=place;
        this.description=description;

    }

    public void setName(String name){
        this.name = name;
    }

    public void setPlace(String place){
        this.place = place;
    }

    public void setDate(GregorianCalendar  date){
        this.date = date;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setParticipants(List<OpenGMMember> participants){
        this.participants = participants;
    }

    public String getName() {
        return name;
    }

    public String getPlace() {
        return place;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public List<OpenGMMember> getParticipants() {
        return participants;
    }



    private class OpenGMMember{

    }
}
