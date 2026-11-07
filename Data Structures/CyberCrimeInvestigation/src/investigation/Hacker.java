package investigation;

import java.util.ArrayList;

public class Hacker implements Comparable<Hacker>{

    private String name;
    private final ArrayList<String> aliases = new ArrayList<>(); 
    private ArrayList<Incident> incidents;  
 
    public Hacker(String name){
        this.name = name; 
        incidents = new ArrayList<>();
    }


    @Override
    public String toString(){
        String suspect = "{Suspect: " + name;
        if (aliases.size() > 0) {
            if (aliases.size() == 1) { 
                suspect += ", Alias: " + aliases.get(0);
            } else { 
                suspect += ", Aliases: {}";
                boolean first = true;
                for (String s : aliases) {
                    if (!first) {
                        suspect += ", ";
                    }
                    first = false;
                    suspect += s;
                } 
            }
        } 
        suspect += ", Num Incidents: " + this.numIncidents();
        suspect += "}";
        return suspect;
    }

    public String getName(){return name;}
    public void addAlias(String alias) {aliases.add(alias);}
    public ArrayList<String> getAliases() {return aliases;}  
    public void setName(String name){this.name = name;} 
    public ArrayList<Incident> getIncidents(){return this.incidents;}
    public void addIncident(Incident toAdd){incidents.add(toAdd);} 
    public int numIncidents(){return incidents.size();} 
 
    @Override
    public int hashCode(){
        return Math.abs(this.getName().hashCode());
    }

    @Override
    // Ignores Incident List
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Hacker other = (Hacker) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (aliases == null) {
            if (other.aliases != null)
                return false;
        } else if (!aliases.equals(other.aliases))
            return false; 
        return true;
    }


    public int compareTo(Hacker b) {
        int res = this.numIncidents() - b.numIncidents();
        if (res == 0) {
            // Compares against string name in cases of ties
            return this.name.compareTo(b.getName());
        }
        return res;
    }
    
} 