package investigation;

import java.util.ArrayList; 

/*  
 * This class represents a cyber crime investigation.  It contains a directory of hackers, which is a resizing
 * hash table. The hash table is an array of HNode objects, which are linked lists of Hacker objects.  
 * 
 * The class contains methods to add a hacker to the directory, remove a hacker from the directory.
 * You will implement these methods, to create and use the HashTable, as well as analyze the data in the directory.
 * 
 * @author Colin Sullivan
 */
public class CyberCrimeInvestigation {
       
    private HNode[] hackerDirectory;
    private int numHackers = 0; 

    public CyberCrimeInvestigation() {
        hackerDirectory = new HNode[10];
    }

    /**
     * Initializes the hacker directory from a file input.
     * @param inputFile
     */
    public void initializeTable(String inputFile) { 
        // DO NOT EDIT
        StdIn.setFile(inputFile);  
        while(!StdIn.isEmpty()){
            addHacker(readSingleHacker());
        }
    }

    /**
     * Reads a single hackers data from the already set file,
     * Then returns a Hacker object with the data, including 
     * the incident data.
     * 
     * StdIn.setFile() has already been called for you.
     * 
     * @param inputFile The name of the file to read hacker data from.
     */
     public Hacker readSingleHacker(){ 
        // WRITE YOUR CODE HERE
        String name = StdIn.readLine();
        String IP = StdIn.readLine();
        String location = StdIn.readLine();
        String OS = StdIn.readLine();
        String server = StdIn.readLine();
        String date = StdIn.readLine();
        String URL = StdIn.readLine();
        Incident incident = new Incident(OS, server, date, location, IP, URL);
        Hacker hacker = new Hacker(name);
        hacker.addIncident(incident);
        return hacker; // Replace this line
    }

    /**
     * Adds a hacker to the directory.  If the hacker already exists in the directory,
     * instead adds the given Hacker's incidents to the existing Hacker's incidents.
     * 
     * After a new insertion (NOT if a hacker already exists), checks if the number of 
     * hackers in the table is >= table length divided by 2. If so, calls resize()
     * 
     * @param toAdd
     */
    public void overLoad(){
        if (numHackers>=hackerDirectory.length/2){
            resize();
        }
    }
    public void addHacker(Hacker toAdd) {
        // WRITE YOUR CODE HERE
        int hashIndex=Math.abs(toAdd.hashCode())%hackerDirectory.length;
        if (hackerDirectory[hashIndex]==null){
            HNode hacker = new HNode(toAdd);
            hackerDirectory[hashIndex]=hacker;
            numHackers+=1;
            overLoad();
            return;
        }
        HNode hacker = hackerDirectory[hashIndex];
        if (hacker.getHacker().getName().equals(toAdd.getName())){
            hacker.getHacker().getIncidents().addAll(toAdd.getIncidents());
            return;
        }
        while (hacker.getNext()!=null){
            hacker=hacker.getNext();
            if (hacker.getHacker().getName().equals(toAdd.getName())){
                hacker.getHacker().getIncidents().addAll(toAdd.getIncidents());
                return;
            }
        }
        HNode newHacker = new HNode(toAdd);
        hacker.setNext(newHacker);
        numHackers+=1;
        overLoad();
    }

    /**
     * Resizes the hacker directory to double its current size.  Rehashes all hackers
     * into the new doubled directory.
     */
    private void resize() {
        HNode[] temp = hackerDirectory;
        numHackers=0;
        int size=temp.length*2;
        hackerDirectory = new HNode[size];
        for (int i=0;i<temp.length;i++){
            if (temp[i]!=null){
                HNode list = temp[i];
                while (list!=null){
                    addHacker(list.getHacker());
                    list=list.getNext();
                }
            }
        }
    }

    /**
     * Searches the hacker directory for a hacker with the given name.
     * Returns null if the Hacker is not found
     * 
     * @param toSearch
     * @return The hacker object if found, null otherwise.
     */
    public Hacker search(String toSearch) {
        // WRITE YOUR CODE HERE 
        int hashIndex=Math.abs(toSearch.hashCode())%hackerDirectory.length;
        HNode hacker = hackerDirectory[hashIndex];
        while (hacker!=null){
            if (hacker.getHacker().getName().equals(toSearch)){
                return hacker.getHacker();
            }
            hacker=hacker.getNext();
        }

        return null;
    }

    /**
     * Removes a hacker from the directory.  Returns the removed hacker object.
     * If the hacker is not found, returns null.
     * 
     * @param toRemove
     * @return The removed hacker object, or null if not found.
     */
    public Hacker remove(String toRemove) {
        // WRITE YOUR CODE HERE 

        int hashIndex=Math.abs(toRemove.hashCode())%hackerDirectory.length;
        HNode hacker = hackerDirectory[hashIndex];
        if (hacker.getHacker().getName().equals(toRemove)){
            Hacker remove = hacker.getHacker();
            hackerDirectory[hashIndex]=hacker.getNext();
            return remove;
        }
        while (hacker!=null){
            if (hacker.getHacker().getName().equals(toRemove) && hacker.getNext()==null){
                HNode prev=hackerDirectory[hashIndex];
                Hacker remove = hacker.getHacker();
                while (prev.getNext()!=hacker){
                    prev=prev.getNext();
                }
                prev.setNext(null);
                return remove;
            }
            else if (hacker.getHacker().getName().equals(toRemove)){
                HNode prev=hackerDirectory[hashIndex];
                Hacker remove = hacker.getHacker();
                while (prev.getNext()!=hacker){
                    prev=prev.getNext();
                }
                prev.setNext(hacker.getNext());
                return remove;
            }
            hacker=hacker.getNext();
        }
        return null;
    }


    /**
     * Merges two hackers into one based on number of incidents.
     * 
     * @param hacker1 One hacker
     * @param hacker2 Another hacker to attempt merging with
     * @return True if the merge was successful, false otherwise.
     */
    public boolean mergeHackers(String hacker1, String hacker2) {  
        // WRITE YOUR CODE HERE 
        Hacker merge1 = search(hacker1);
        Hacker merge2 = search(hacker2);
        if (merge1==null || merge2==null){
            return false;
        }
        if (merge1.numIncidents()>=merge2.numIncidents()){
            merge1.getIncidents().addAll(merge2.getIncidents());
            merge1.addAlias(hacker2);
            remove(hacker2);
            return true;
        }
        if (merge1.numIncidents()<merge2.numIncidents()){
            merge2.getIncidents().addAll(merge1.getIncidents());
            merge2.addAlias(hacker1);
            remove(hacker1);
            return true;
        }
        return false; // Replace this line
    }

    /**
     * Gets the top n most wanted Hackers from the directory, and
     * returns them in an arraylist. 
     * 
     * You should use the provided MaxPQ class to do this. You can
     * add all hackers, then delMax() n times, to get the top n hackers.
     * 
     * @param n
     * @return Arraylist containing top n hackers
     */
    public ArrayList<Hacker> getNMostWanted(int n) {
        // WRITE YOUR CODE HERE 
        MaxPQ<Hacker> hackerlist = new MaxPQ<>();
        ArrayList<Hacker> mostWanted = new ArrayList<>();
        for (int i=0; i<hackerDirectory.length;i++){
            if (hackerDirectory[i]!=null){
                HNode hacker = hackerDirectory[i];
                while (hacker!=null){
                    hackerlist.insert(hacker.getHacker());
                    hacker=hacker.getNext();
                }
            }
        }
        for (int i=0;i<n;i++){
            mostWanted.add(hackerlist.delMax());
        }
        return mostWanted; // Replace this line
    }

    /**
     * Gets all hackers that have been involved in incidents at the given location.
     * 
     * You should check all hackers, and ALL of each hackers incidents.
     * You should not add a single hacker more than once.
     * 
     * @param location
     * @return Arraylist containing all hackers who have been involved in incidents at the given location.
     */
    public ArrayList<Hacker> getHackersByLocation(String location) {
        // WRITE YOUR CODE HERE 
        ArrayList<Hacker> hackers = new ArrayList<>();
        for (int i=0;i<hackerDirectory.length;i++){
            if (hackerDirectory[i]!=null){
                HNode hackerNode = hackerDirectory[i];
                while(hackerNode!=null){
                    Hacker hacker = hackerNode.getHacker();
                    ArrayList<Incident> incidentList = hacker.getIncidents();
                    boolean found=false;
                    for (int j=0; j<hacker.numIncidents();j++){
                        if (incidentList.get(j).getLocation().equals(location)&&found==false){
                            hackers.add(hacker);
                            found=true;
                        }
                    }                
                    hackerNode=hackerNode.getNext();
                }
            }
        }
        return hackers; // Replace this line
    }
  

    /**
     * PROVIDED--DO NOT MODIFY!
     * Outputs the entire hacker directory to the terminal. 
     */
     public void printHackerDirectory() { 
        System.out.println(toString());
    } 

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.hackerDirectory.length; i++) {
            HNode headHackerNode = hackerDirectory[i];
            while (headHackerNode != null) {
                if (headHackerNode.getHacker() != null) {
                    sb.append(headHackerNode.getHacker().toString()).append("\n");
                    ArrayList<Incident> incidents = headHackerNode.getHacker().getIncidents();
                    for (Incident incident : incidents) {
                        sb.append("\t" +incident.toString()).append("\n");
                    }
                }
                headHackerNode = headHackerNode.getNext();
            } 
        }
        return sb.toString();
    }

    public HNode[] getHackerDirectory() {
        return hackerDirectory;
    }
}
