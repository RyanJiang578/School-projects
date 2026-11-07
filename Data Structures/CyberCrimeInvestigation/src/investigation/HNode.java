package investigation;

public class HNode {
    private Hacker hacker;
    private HNode next;
 
    public HNode(Hacker hacker){
        if (hacker == null) {
            throw new IllegalArgumentException("HNode requires a non-null Hacker object to instantiate");
        }
        this.hacker = hacker;
        this.next = null;
    }

    public Hacker getHacker(){
        return this.hacker;
    } 

    public HNode getNext(){
        return this.next;
    }
    
    public void setNext(HNode hackerNode){
        this.next = hackerNode;
    }
}
