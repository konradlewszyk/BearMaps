import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides a <code>shortestPath</code> method and <code>routeDirections</code> for
 * finding routes between two points on the map.
 */
public class Router {
    /**
     * Return a <code>List</code> of vertex IDs corresponding to the shortest path from a given
     * starting coordinate and destination coordinate.
     *
     * @param g       <code>GraphDB</code> data source.
     * @param stlon   The longitude of the starting coordinate.
     * @param stlat   The latitude of the starting coordinate.
     * @param destlon The longitude of the destination coordinate.
     * @param destlat The latitude of the destination coordinate.
     * @return The <code>List</code> of vertex IDs corresponding to the shortest path.
     */
    public static List<Long> shortestPath(GraphDB g,
                                          double stlon, double stlat,
                                          double destlon, double destlat) {

        //HashSet<Long> visitedOfEverything = new HashSet<>();
        HashSet<Long> visited = new HashSet<>();
        HashMap<Long, Double> distanceToMap = new HashMap<>();
        HashMap<Long, Long> edgeToMap = new HashMap<>();
        PriorityQueue<PriorityQueueObject> fringe = new PriorityQueue(g.nodeHashMap.size(),
                new PriorityQueueObjectComparator());


        Long startingID = g.closest(stlon, stlat);
        Long endingID = g.closest(destlon, destlat);

        GraphDB.Node startingNode = g.nodeHashMap.get(startingID);
        GraphDB.Node destinationNode = g.nodeHashMap.get(endingID);

        /** Initializes the hashmaps */
        for (Long key : g.nodeHashMap.keySet()) {
            distanceToMap.put(g.nodeHashMap.get(key).nodeID, Double.MAX_VALUE);
            edgeToMap.put(g.nodeHashMap.get(key).nodeID, null);
            PriorityQueueObject newObject =
                    new PriorityQueueObject(g.nodeHashMap.get(key).nodeID, Double.MAX_VALUE);
            fringe.add(newObject);
            //visitedOfEverything.add(g.nodeHashMap.get(key).nodeID);
        }

        /** Puts starting vertex */
        long vertexValue = startingNode.nodeID;
        visited.add(vertexValue);
        LinkedList<Long> listOfNeighbors = startingNode.adjList;
        for (int i = 0; i < listOfNeighbors.size(); i = i + 1) {
            fringe.add(new PriorityQueueObject(listOfNeighbors.get(i),
                    g.distance(startingNode.nodeID, listOfNeighbors.get(i))));
            distanceToMap.replace(listOfNeighbors.get(i),
                    g.distance(startingNode.nodeID, listOfNeighbors.get(i)));
            edgeToMap.replace(listOfNeighbors.get(i), startingNode.nodeID);
        }
        /** Iterates through the rest of the vertexes */
        while (visited.size() != g.nodeHashMap.size()) {
            PriorityQueueObject poppedObject = fringe.poll();
            visited.add(poppedObject.nodeID);
            GraphDB.Node nextNode = g.nodeHashMap.get(poppedObject.nodeID);
            for (int i = 0; i < nextNode.adjList.size(); i = i + 1) {
                if (visited.contains(nextNode.adjList.get(i))) {
                    continue;
                }
                /** Updates priority queue if a smaller distance was found */
                double newDistance = distanceToMap.get(nextNode.nodeID)
                        + g.distance(nextNode.nodeID, nextNode.adjList.get(i));
                if (newDistance < distanceToMap.get(nextNode.adjList.get(i))) {
                    distanceToMap.replace(nextNode.adjList.get(i), newDistance);
                    edgeToMap.replace(nextNode.adjList.get(i), poppedObject.nodeID);
                    fringe.add(new PriorityQueueObject(nextNode.adjList.get(i), newDistance));
                }
            }
        }
        /** Adds to the list */
        LinkedList<Long> shortestPathList = new LinkedList<>();
        shortestPathList.addFirst(destinationNode.nodeID);
        Long copy = destinationNode.nodeID;
        while (!shortestPathList.contains(startingNode.nodeID)) {
            long edge = edgeToMap.get(copy);
            shortestPathList.addFirst(edge);
            copy = edge;
        }
        return shortestPathList;
    }

    public static class PriorityQueueObjectComparator implements Comparator<PriorityQueueObject> {
        // Overriding compare()method of Comparator
        // for descending order of cgpa

        public int compare(PriorityQueueObject s1, PriorityQueueObject s2) {
            if (s1.distance > s2.distance) {
                return 1;
            } else if (s2.distance > s1.distance) {
                return -1;
            }
            return 0;
        }
    }

    public static class PriorityQueueObject {

        Long nodeID;
        double distance;

        PriorityQueueObject(Long nodeID, double distance) {
            this.nodeID = nodeID;
            this.distance = distance;
        }
    }


    /**
     * Given a <code>route</code> of vertex IDs, return a <code>List</code> of
     * <code>NavigationDirection</code> objects representing the travel directions in order.
     *
     * @param g     <code>GraphDB</code> data source.
     * @param route The shortest-path route of vertex IDs.
     * @return A new <code>List</code> of <code>NavigationDirection</code> objects.
     */
    public static List<NavigationDirection> routeDirections(GraphDB g, List<Long> route) {
        // TODO
        return Collections.emptyList();
    }

    /**
     * Class to represent a navigation direction, which consists of 3 attributes:
     * a direction to go, a way, and the distance to travel for.
     */
    public static class NavigationDirection {

        /**
         * Integer constants representing directions.
         */
        public static final int START = 0, STRAIGHT = 1, SLIGHT_LEFT = 2, SLIGHT_RIGHT = 3,
                RIGHT = 4, LEFT = 5, SHARP_LEFT = 6, SHARP_RIGHT = 7;

        /**
         * Number of directions supported.
         */
        public static final int NUM_DIRECTIONS = 8;

        /**
         * A mapping of integer values to directions.
         */
        public static final String[] DIRECTIONS = new String[NUM_DIRECTIONS];

        static {
            DIRECTIONS[START] = "Start";
            DIRECTIONS[STRAIGHT] = "Go straight";
            DIRECTIONS[SLIGHT_LEFT] = "Slight left";
            DIRECTIONS[SLIGHT_RIGHT] = "Slight right";
            DIRECTIONS[RIGHT] = "Turn right";
            DIRECTIONS[LEFT] = "Turn left";
            DIRECTIONS[SHARP_LEFT] = "Sharp left";
            DIRECTIONS[SHARP_RIGHT] = "Sharp right";
        }

        /**
         * The direction represented.
         */
        int direction;
        /**
         * The name of this way.
         */
        String way;
        /**
         * The distance along this way.
         */
        double distance = 0.0;

        public String toString() {
            return String.format("%s on %s and continue for %.3f miles.",
                    DIRECTIONS[direction], way, distance);
        }

        /**
         * Returns a new <code>NavigationDirection</code> from a string representation.
         *
         * @param dirAsString <code>String</code> instructions for a navigation direction.
         * @return A new <code>NavigationDirection</code> based on the string, or <code>null</code>
         * if unable to parse.
         */
        public static NavigationDirection fromString(String dirAsString) {
            String regex = "([a-zA-Z\\s]+) on ([\\w\\s]*) and continue for ([0-9\\.]+) miles\\.";
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(dirAsString);
            NavigationDirection nd = new NavigationDirection();
            if (m.matches()) {
                String direction = m.group(1);
                if (direction.equals("Start")) {
                    nd.direction = NavigationDirection.START;
                } else if (direction.equals("Go straight")) {
                    nd.direction = NavigationDirection.STRAIGHT;
                } else if (direction.equals("Slight left")) {
                    nd.direction = NavigationDirection.SLIGHT_LEFT;
                } else if (direction.equals("Slight right")) {
                    nd.direction = NavigationDirection.SLIGHT_RIGHT;
                } else if (direction.equals("Turn right")) {
                    nd.direction = NavigationDirection.RIGHT;
                } else if (direction.equals("Turn left")) {
                    nd.direction = NavigationDirection.LEFT;
                } else if (direction.equals("Sharp left")) {
                    nd.direction = NavigationDirection.SHARP_LEFT;
                } else if (direction.equals("Sharp right")) {
                    nd.direction = NavigationDirection.SHARP_RIGHT;
                } else {
                    return null;
                }

                nd.way = m.group(2);
                try {
                    nd.distance = Double.parseDouble(m.group(3));
                } catch (NumberFormatException e) {
                    return null;
                }
                return nd;
            } else {
                // Not a valid nd
                return null;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof NavigationDirection) {
                return direction == ((NavigationDirection) o).direction
                        && way.equals(((NavigationDirection) o).way)
                        && distance == ((NavigationDirection) o).distance;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(direction, way, distance);
        }
    }
}
