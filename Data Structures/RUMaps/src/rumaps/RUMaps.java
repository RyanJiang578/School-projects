package rumaps;

import java.util.*;

/**
 * This class represents the information that can be attained from the Rutgers University Map.
 * 
 * The RUMaps class is responsible for initializing the network, streets, blocks, and intersections in the map.
 * 
 * You will complete methods to initialize blocks and intersections, calculate block lengths, find reachable intersections,
 * minimize intersections between two points, find the fastest path between two points, and calculate a path's information.
 * 
 * Provided is a Network object that contains all the streets and intersections in the map
 * 
 * @author Vian Miranda
 * @author Anna Lu
 */
public class RUMaps {
    
    private Network rutgers;

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Constructor for the RUMaps class. Initializes the streets and intersections in the map.
     * For each block in every street, sets the block's length, traffic factor, and traffic value.
     * 
     * @param mapPanel The map panel to display the map
     * @param filename The name of the file containing the street information
     */
    public RUMaps(MapPanel mapPanel, String filename) {
        StdIn.setFile(filename);
        int numIntersections = StdIn.readInt();
        int numStreets = StdIn.readInt();
        StdIn.readLine();
        rutgers = new Network(numIntersections, mapPanel);
        ArrayList<Block> blocks = initializeBlocks(numStreets);
        initializeIntersections(blocks);

        for (Block block: rutgers.getAdjacencyList()) {
            Block ptr = block;
            while (ptr != null) {
                ptr.setLength(blockLength(ptr));
                ptr.setTrafficFactor(blockTrafficFactor(ptr));
                ptr.setTraffic(blockTraffic(ptr));
                ptr = ptr.getNext();
            }
        }
    }

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Overloaded constructor for testing.
     * 
     * @param filename The name of the file containing the street information
     */
    public RUMaps(String filename) {
        this(null, filename);
    }

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Overloaded constructor for testing.
     */
    public RUMaps() { 
        
    }

    /**
     * Initializes all blocks, given a number of streets.
     * the file was opened by the constructor - use StdIn to continue reading the file
     * @param numStreets the number of streets
     * @return an ArrayList of blocks
     */
    public ArrayList<Block> initializeBlocks(int numStreets) {
        // WRITE YOUR CODE HERE
        ArrayList <Block> blocks = new ArrayList<>();
        for (int i=0;i<numStreets;i++){
            String streetName=StdIn.readLine();
            int numBlocks = StdIn.readInt();
            for (int j=0;j<numBlocks;j++){
                int blockNumber=StdIn.readInt();
                int numPoints = StdIn.readInt();
                Double roadSize=StdIn.readDouble();
                Block block = new Block(roadSize,streetName,blockNumber);
                boolean first = true;
                for (int k=0;k<numPoints;k++){
                    int xCord=StdIn.readInt();
                    int yCord=StdIn.readInt();
                    if (k==numPoints-1){
                        StdIn.readLine();
                    }
                    Coordinate cords = new Coordinate(xCord, yCord);
                    if (first==true){
                        block.startPoint(cords);
                        first=false;
                    }
                    else{
                        block.nextPoint(cords);
                    }
                }
                blocks.add(block);
            }
        }
        return blocks; // Replace this line, it is provided so the code compiles
    }

    /**
     * This method traverses through each block and finds
     * the block's start and end points to create intersections. 
     * 
     * It then adds intersections as vertices to the "rutgers" graph if
     * they are not already present, and adds UNDIRECTED edges to the adjacency
     * list.
     * 
     * Note that .addEdge(__) ONLY adds edges in one direction (a -> b). 
     */
    public void initializeIntersections(ArrayList<Block> blocks) {
        // WRITE YOUR CODE HERE
        for (int i=0;i<blocks.size();i++){
            Block block = blocks.get(i);
            Coordinate start = block.getCoordinatePoints().get(0);
            Coordinate end = block.getCoordinatePoints().get(block.getCoordinatePoints().size()-1);
            int findStart = rutgers.findIntersection(start);
            int findEnd = rutgers.findIntersection(end);
            Intersection intersectA=null;
            Intersection intersectB=null;
            if (findStart==-1){
                Intersection intersection = new Intersection(start);
                intersectA=intersection;
                block.setFirstEndpoint(intersection);
                rutgers.addIntersection(intersection);
            }
            if (findEnd==-1){
                Intersection intersection = new Intersection(end);
                intersectB=intersection;
                block.setLastEndpoint(intersection);
                rutgers.addIntersection(intersection);

            }
            if (findStart!=-1){
                Intersection[] list = rutgers.getIntersections();
                list[findStart].getCoordinate().setX(start.getX());
                list[findStart].getCoordinate().setY(start.getY());
                intersectA=list[findStart];
                rutgers.setIntersections(list);
            }
            if (findEnd!=-1){
                Intersection[] list = rutgers.getIntersections();
                list[findEnd].getCoordinate().setX(end.getX());
                list[findEnd].getCoordinate().setY(end.getY());
                intersectB=list[findEnd];
                rutgers.setIntersections(list);
            }
            int indexA=rutgers.findIntersection(intersectA.getCoordinate());
            int indexB=rutgers.findIntersection(intersectB.getCoordinate());
            Block copyA = block.copy();
            Block copyB = block.copy();
            copyA.setFirstEndpoint(intersectA);
            copyA.setLastEndpoint(intersectB);
            copyB.setFirstEndpoint(intersectB);
            copyB.setLastEndpoint(intersectA);
            rutgers.addEdge(indexA, copyA);
            rutgers.addEdge(indexB, copyB);
        }
     }

    /**
     * Calculates the length of a block by summing the distances between consecutive points for all points in the block.
     * 
     * @param block The block whose length is being calculated
     * @return The total length of the block
     */
    public double blockLength(Block block) {
        // WRITE YOUR CODE HERE
        double distance=0;
        for (int i=0;i<block.getCoordinatePoints().size()-1;i++){
            distance+=coordinateDistance(block.getCoordinatePoints().get(i), block.getCoordinatePoints().get(i+1));
        }
        return distance;
         // Replace this line, it is provided so the code compiles
    }

    /**
     * Use a DFS to traverse through blocks, and find the order of intersections
     * traversed starting from a given intersection (as source).
     * 
     * Implement this method recursively, using a helper method.
     */
    private boolean search(ArrayList<Intersection> list , Intersection item){
        for (int i=0;i<list.size();i++){
            if (list.get(i).equals(item))
            return true;
        }
        return false;
    }
    private void DFS(Intersection intersection, ArrayList<Intersection> list){
        if (intersection==null){
            return;
        }
        int index = rutgers.findIntersection(intersection.getCoordinate());
        Block[] adjList = rutgers.getAdjacencyList();
        Block ptr=adjList[index];
        while (ptr!=null){
            Intersection next = ptr.other(intersection);
            if (search(list,next)==false){
                list.add(next);
                DFS(next,list);
            }
            ptr=ptr.getNext();
        }

    }
    public ArrayList<Intersection> reachableIntersections(Intersection source) {
        // WRITE YOUR CODE HERE
        ArrayList<Intersection> intersections = new ArrayList<>();
        intersections.add(source);
        DFS(source,intersections);
        for (int i=0;i<intersections.size();i++){
            System.out.println(intersections.get(i).getCoordinate());
        }

        return intersections; // Replace this line, it is provided so the code compiles
    }
     

    /**
     * Finds and returns the path with the least number of intersections (nodes) from the start to the end intersection.
     * 
     * - If no path exists, return an empty ArrayList.
     * - This graph is large. Find a way to eliminate searching through intersections that have already been visited.
     * 
     * @param start The starting intersection
     * @param end The destination intersection
     * @return The path with the least number of turns, or an empty ArrayList if no path exists
     */
    private void explore(Intersection area, Intersection target, ArrayList<Intersection> searched, Intersection[] intersections,int[]distances,Queue<Intersection> map){
        map.enqueue(area);
        searched.add(area);
        Block[] adjList=rutgers.getAdjacencyList();
        while (map.isEmpty()==false){
            Intersection dropped = map.dequeue();
            int droppedIndex=rutgers.findIntersection(dropped.getCoordinate());
            int distance=distances[droppedIndex];
            Block ptr=adjList[droppedIndex];
            while (ptr!=null){
                Intersection near = ptr.other(dropped);
                if (searched.contains(near)==false){
                    map.enqueue(near);
                    searched.add(near);
                    int nearIndex=rutgers.findIntersection(near.getCoordinate());
                    distances[nearIndex]=distance+1;
                }
                ptr=ptr.getNext();
            }
            
        }
    }

    public ArrayList<Intersection> minimizeIntersections(Intersection start, Intersection end) {
        // WRITE YOUR CODE HERE
        ArrayList<Intersection> path = new ArrayList<>();
        Block[] adjList = rutgers.getAdjacencyList();
        ArrayList<Intersection> searched = new ArrayList<>();
        Queue<Intersection> map = new Queue<>();
        Intersection[] intersections = rutgers.getIntersections();
        int[] distances = new int[intersections.length];
        int index=rutgers.findIntersection(end.getCoordinate());
        distances[index]=0;
        explore(end, start, searched, intersections, distances,map);
        int startIndex=rutgers.findIntersection(start.getCoordinate());
        int startDistance=distances[startIndex];
        Block ptr=adjList[startIndex];
        Intersection pathway = start;
        path.add(start);
        while (startDistance!=0){
            int pathIndex=rutgers.findIntersection(pathway.getCoordinate());
            ptr=adjList[pathIndex];
            while (ptr!=null){
                Intersection test=ptr.other(pathway);
                int testIndex=rutgers.findIntersection(test.getCoordinate());
                if (distances[testIndex]==startDistance-1){
                    path.add(test);
                    pathway=test;
                    startDistance-=1;
                    break;
                }
                ptr=ptr.getNext();
            }
        }



        return path; // Replace this line, it is provided so the code compiles
    }

    /**
     * Finds the path with the least traffic from the start to the end intersection using a variant of Dijkstra's algorithm.
     * The traffic is calculated as the sum of traffic of the blocks along the path.
     * 
     * What is this variant of Dijkstra?
     * - We are using traffic as a cost - we extract the lowest cost intersection from the fringe.
     * - Once we add the target to the done set, we're done. 
     * 
     * @param start The starting intersection
     * @param end The destination intersection
     * @return The path with the least traffic, or an empty ArrayList if no path exists
     */

    public ArrayList<Intersection> fastestPath(Intersection start, Intersection end) {
        ArrayList<Intersection> path = new ArrayList<>();
        ArrayList<Intersection> searched = new ArrayList<>();
        ArrayList <Intersection> searchList = new ArrayList<>();
        Intersection[] intersections = rutgers.getIntersections();
        Intersection[] prev = new Intersection[intersections.length];
        double[] distances = new double[intersections.length];
        for (int i=0;i<distances.length;i++){
            distances[i]=1000000000.0;
        }
        Block[] adjList = rutgers.getAdjacencyList();
        int startIndex=rutgers.findIntersection(start.getCoordinate());
        distances[startIndex]=0;
        searchList.add(start);
        while (searchList.isEmpty()==false){
            Intersection min = null;
            double minDistance=832190832901849081.0;
            for (int i=0;i<searchList.size();i++){
                Intersection current=searchList.get(i);
                int currIndex=rutgers.findIntersection(current.getCoordinate());
                Double currentDistance=distances[currIndex];
                if (currentDistance<minDistance){
                    min=current;
                    minDistance=currentDistance;
                }
            }
            searched.add(min);
            for (int i=0;i<searchList.size();i++){
                if (searchList.get(i).equals(min)){
                    searchList.remove(i);
                }
            }
            int minIndex=rutgers.findIntersection(min.getCoordinate());
            Block ptr = adjList[minIndex];
            while (ptr!=null){
                Intersection next = ptr.other(min);
                int nextIndex=rutgers.findIntersection(next.getCoordinate());
                if (searched.contains(next)==false){
                    if (distances[nextIndex]==1000000000.0){
                        distances[nextIndex]=minDistance+ptr.getTraffic();
                        searchList.add(next);
                        prev[nextIndex]=min;
                    }
                else if (distances[nextIndex]>(distances[minIndex]+ptr.getTraffic())){
                    distances[nextIndex]=distances[minIndex]+ptr.getTraffic();
                    prev[nextIndex]=min;
                }
                }
                ptr=ptr.getNext();
            }
        }
        Intersection target=end;
        while(target!=null){
            path.add(target);
            int targetIndex=rutgers.findIntersection(target.getCoordinate());
            target=prev[targetIndex];
        }
        Collections.reverse(path);

        // WRITE YOUR CODE HERE
        
        return path; // Replace this line, it is provided so the code compiles
    }

    /**
     * Calculates the total length, average experienced traffic factor, and total traffic for a given path of blocks.
     * 
     * You're given a list of intersections (vertices); you'll need to find the edge in between each pair.
     * 
     * Compute the average experienced traffic factor by dividing total traffic by total length.
     *  
     * @param path The list of intersections representing the path
     * @return A double array containing the total length, average experienced traffic factor, and total traffic of the path (in that order)
     */
    public double[] pathInformation(ArrayList<Intersection> path) {
        Block[] adjList = rutgers.getAdjacencyList();
        double[] pathInfo = new double[3];
        for (int i=0;i<path.size()-1;i++){
            Intersection intersection = path.get(i);
            Intersection nextIntersection = path.get(i+1);
            Block ptr = adjList[rutgers.findIntersection(intersection.getCoordinate())];
            while (ptr!=null){
                if (ptr.other(intersection).equals(nextIntersection)){
                    pathInfo[0]+=ptr.getLength();
                    pathInfo[2]+=ptr.getTraffic();
                }
                ptr=ptr.getNext();
            }
        }
        pathInfo[1]=pathInfo[2]/pathInfo[0];

        // WRITE YOUR CODE HERE
        
        return pathInfo; // Replace this line, it is provided so the code compiles
    }

    /**
     * Calculates the Euclidean distance between two coordinates.
     * PROVIDED - do not modify
     * 
     * @param a The first coordinate
     * @param b The second coordinate
     * @return The Euclidean distance between the two coordinates
     */
    private double coordinateDistance(Coordinate a, Coordinate b) {
        // PROVIDED METHOD

        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * **DO NOT MODIFY THIS METHOD**
     * 
     * Calculates and returns a randomized traffic factor for the block based on a Gaussian distribution.
     * 
     * This method generates a random traffic factor to simulate varying traffic conditions for each block:
     * - < 1 for good (faster) conditions
     * - = 1 for normal conditions
     * - > 1 for bad (slower) conditions
     * 
     * The traffic factor is generated with a Gaussian distribution centered at 1, with a standard deviation of 0.2.
     * 
     * Constraints:
     * - The traffic factor is capped between a minimum of 0.5 and a maximum of 1.5 to avoid extreme values.
     * 
     * @param block The block for which the traffic factor is calculated
     * @return A randomized traffic factor for the block
     */
    public double blockTrafficFactor(Block block) {
        double rand = StdRandom.gaussian(1, 0.2);
        rand = Math.max(rand, 0.5);
        rand = Math.min(rand, 1.5);
        return rand;
    }

    /**
     * Calculates the traffic on a block by the product of its length and its traffic factor.
     * 
     * @param block The block for which traffic is being calculated
     * @return The calculated traffic value on the block
     */
    public double blockTraffic(Block block) {
        // PROVIDED METHOD
        
        return block.getTrafficFactor() * block.getLength();
    }

    public Network getRutgers() {
        return rutgers;
    }




    
    








}
