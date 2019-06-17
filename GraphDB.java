import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Kevin Lowe, Antares Chen, Kevin Lin
 */
public class GraphDB {
    /**
     * This constructor creates and starts an XML parser, cleans the nodes, and prepares the
     * data structures for processing. Modify this constructor to initialize your data structures.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        File inputFile = new File(dbPath);
        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(inputStream, new GraphBuildingHandler(this));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    /**
     * mapping from key:NodeID to its value: Node Object that has longitude and latitude
     */
    HashMap<Long, Node> nodeHashMap = new HashMap<>();

    private HashMap<Long, Node> clearedNodeHashMap = new HashMap<>();

    private LinkedList<Long> nodeList = new LinkedList<>();

    /**
     * mapping from key: Node Object to its value: NodeID
     */
    private HashMap<Node, Long> nodeIDHashMap = new HashMap<>();

    private HashMap<Long, Edge> edgeHashMap = new HashMap<>();

    public ArrayList<Node> nodelistfortree = new ArrayList<>();


    /**
     * mapping from key:WayID to its value: Set of all edges that the NodeID is connected to
     */

    static class Node {
        long nodeID;
        private double latitude;
        private double longitude;
        LinkedList<Long> adjList;

        Node(long nodeID, double latitude, double longitude) {
            this.nodeID = nodeID;
            this.latitude = latitude;
            this.longitude = longitude;
            this.adjList = new LinkedList<>();
        }
    }

    public static class TreeNode {
        public Node location;
        public TreeNode leftchild;
        public TreeNode rightchild;
        public TreeNode parent;

        TreeNode(Node location, TreeNode rightchild, TreeNode leftchild, TreeNode parent) {
            this.location = location;
            this.rightchild = rightchild;
            this.leftchild = leftchild;
            this.parent = parent;
        }
    }

    public static class NodeWithOutID {
        private double latitude;
        private double longitude;

        NodeWithOutID(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public static class Edge {

        private long wayID;
        private static LinkedList<Long> listOfVertices;
        private String name;

        Edge(long wayID, LinkedList<Long> listOfVertices, String name) {

            this.wayID = wayID;
            this.listOfVertices = listOfVertices;
            this.name = name;
        }
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */

    /**
     * Daddy Hug hints to use adjacency method
     */
    private void clean() {
        for (Long key : nodeHashMap.keySet()) {
            if (!nodeHashMap.get(key).adjList.isEmpty()) {
                Node a = new Node(nodeHashMap.get(key).nodeID, nodeHashMap.get(key).latitude,
                        nodeHashMap.get(key).longitude);
                a.adjList = nodeHashMap.get(key).adjList;
                clearedNodeHashMap.put(nodeHashMap.get(key).nodeID, a);
            }
        }
        nodeHashMap = clearedNodeHashMap;
    }

    /**
     * Returns the longitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The longitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lon(long v) {

        return nodeHashMap.get(v).longitude;
    }

    /**
     * Returns the latitude of vertex <code>v</code>.
     *
     * @param v The ID of a vertex in the graph.
     * @return The latitude of that vertex, or 0.0 if the vertex is not in the graph.
     */
    double lat(long v) {

        return nodeHashMap.get(v).latitude;
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     *
     * @return An iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        LinkedList<Long> vertices = new LinkedList<>();
        for (Long key : nodeHashMap.keySet()) {
            vertices.add(key);
        }
        return vertices;
    }

    /**
     * Returns an iterable over the IDs of all vertices adjacent to <code>v</code>.
     *
     * @param v The ID for any vertex in the graph.
     * @return An iterable over the IDs of all vertices adjacent to <code>v</code>, or an empty
     * iterable if the vertex is not in the graph.
     */
    Iterable<Long> adjacent(long v) {
        return nodeHashMap.get(v).adjList;
    }

    /**
     * Returns the great-circle distance between two vertices, v and w, in miles.
     * Assumes the lon/lat methods are implemented properly.
     *
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The great-circle distance between vertices and w.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */
    public double distance(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double dphi = Math.toRadians(lat(w) - lat(v));
        double dlambda = Math.toRadians(lon(w) - lon(v));

        double a = Math.sin(dphi / 2.0) * Math.sin(dphi / 2.0);
        a += Math.cos(phi1) * Math.cos(phi2) * Math.sin(dlambda / 2.0) * Math.sin(dlambda / 2.0);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Returns the ID of the vertex closest to the given longitude and latitude.
     *
     * @param lon The given longitude.
     * @param lat The given latitude.
     * @return The ID for the vertex closest to the <code>lon</code> and <code>lat</code>.
     */
    /**
    public long closest(double lon, double lat) {
        Node a = new Node(123, lat, lon);
        nodeHashMap.put(123L, a);
        double smallestDistance = 10000000;
        long closestNode = 124;
        for (Node value : nodeHashMap.values()) {
            if (value.nodeID != 123) {
                if (smallestDistance > distance(value.nodeID, nodeHashMap.get(123L).nodeID)) {
                    smallestDistance = distance(value.nodeID, nodeHashMap.get(123L).nodeID);
                    closestNode = value.nodeID;
                }
            }
        }
        nodeHashMap.remove(123L);
        return closestNode;
    }
     */

    public void latsorter(ArrayList<Node> nodelistfortree){
//        nodelistfortree.sort(Comparator.comparingDouble(Node :: getLatitude));
        Collections.sort(nodelistfortree, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if(o1.latitude == o2.latitude) {
                    return 0;
                }
                if (o1.latitude > o2.latitude){
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    public void lonsorter(ArrayList<Node> nodelistfortree){
//        nodelistfortree.sort(Comparator.comparingDouble(Node :: getLongitude));
        Collections.sort(nodelistfortree, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                if(o1.longitude == o2.longitude) {
                    return 0;
                }
                if (o1.longitude > o2.longitude){
                    return 1;
                } else {
                    return -1;
                }
            }
        });
    }

    public long closest(double lon, double lat) {
        TreeNode target = kdtree(nodelistfortree, 0);
        return nearestneighbor(lon, lat, target, true, null, 1000, null, null);
    }

    public long nearestneighbor(double lon, double lat, TreeNode tree, boolean switcher, Node champion, double best, TreeNode parent, String direction){
        double x = projectToX(lon, lat);
        double y = projectToY(lon, lat);


        double currentX = projectToX(tree.location.longitude, tree.location.latitude);
        double currentY = projectToY(tree.location.longitude, tree.location.latitude);

        if (euclidean(currentX, x , currentY, y) < best){
            champion = tree.location;
            best = euclidean(currentX, x , currentY, y);
        }
        if(tree.rightchild == null && tree.leftchild == null) {
            if (parent != null) {
                double parentX = projectToX(parent.location.longitude, parent.location.latitude);
                double parentY = projectToY(parent.location.longitude, parent.location.latitude);
                if (switcher) {
                    if (Math.abs(parentX - x) < euclidean(currentX, x, currentY, y)) {
                        if (direction == "left") {
                            return nearestneighbor(lon, lat, parent.rightchild, !switcher, champion, best, null, "right");
                        } else {
                            return nearestneighbor(lon, lat, parent.leftchild, !switcher, champion, best, null, "left");
                        }
                    }
                } else {
                    if (Math.abs(parentY - y) < euclidean(currentX, x, currentY, y)) {
                        if (direction == "left") {
                            return nearestneighbor(lon, lat, parent.rightchild, !switcher, champion, best, null, "right");
                        } else {
                            return nearestneighbor(lon, lat, parent.leftchild, !switcher, champion, best, null, "left");
                        }
                    }
                }

            }
            return champion.nodeID;
        }

        if (switcher){
            if(x < currentX){
                return nearestneighbor(lon, lat, tree.leftchild, !switcher, champion, best, tree, "left");
            } else {
                return nearestneighbor(lon, lat, tree.rightchild, !switcher, champion, best, tree, "right");
            }
        } else {
//            if target X < current X -> go left!!!
            if(y < currentY){
                return nearestneighbor(lon, lat, tree.leftchild, !switcher, champion, best, tree, "left" );
            } else {
                return nearestneighbor(lon, lat, tree.rightchild, !switcher, champion, best, tree, "right");
            }
        }
    }

    public TreeNode kdtree(ArrayList nodelistfortree, int depth){
        if (nodelistfortree.size() == 0){
            return null;
        }

        int fuck = nodelistfortree.size()/2;

        if (depth % 2 == 0){
            latsorter(nodelistfortree);
        } else {
            lonsorter(nodelistfortree);
        }

        Node median;
        if (nodelistfortree.size() % 2 == 0){
            median = (Node) nodelistfortree.get(fuck - 1);
        } else {
            median = (Node) nodelistfortree.get(fuck);
        }

        ArrayList<Node> leftlist = new ArrayList<>();
        ArrayList<Node> rightlist = new ArrayList<>();
        for (int i = 0; i < nodelistfortree.indexOf(median); i++) {
            leftlist.add((Node) nodelistfortree.get(i));
        }
        for (int i = nodelistfortree.indexOf(median) + 1; i < nodelistfortree.size(); i++) {
            rightlist.add((Node) nodelistfortree.get(i));
        }
        return new TreeNode(median, kdtree(leftlist, depth + 1), kdtree(rightlist, depth + 1), null);
    }

    static double euclidean(double x1, double x2, double y1, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public void addNode(long nodeID, double latitude, double longitude) {
        Node a = new Node(nodeID, latitude, longitude);
        nodeHashMap.put(nodeID, a);
        nodeList.add(nodeID);
        nodelistfortree.add(a);
    }


    public void addEdge(Edge newEdge) {
        edgeHashMap.put(newEdge.wayID, newEdge);
        int sizeOfNodeList = newEdge.listOfVertices.size();
        for (int i = 0; i < sizeOfNodeList - 1; i = i + 1) {
            long firstNodeID = newEdge.listOfVertices.get(i);
            long secondNodeID = newEdge.listOfVertices.get(i + 1);
            Node firstNode = nodeHashMap.get(firstNodeID);
            Node secondNode = nodeHashMap.get(secondNodeID);
            firstNode.adjList.add(secondNodeID);
            secondNode.adjList.add(firstNodeID);
        }
    }

    public void removeNode(Long nodeID) {
        nodeHashMap.remove(nodeID);
    }


    /**
     * Return the Euclidean x-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean x-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToX(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double b = Math.sin(dlon) * Math.cos(phi);
        return (K0 / 2) * Math.log((1 + b) / (1 - b));
    }

    /**
     * Return the Euclidean y-value for some point, p, in Berkeley. Found by computing the
     * Transverse Mercator projection centered at Berkeley.
     *
     * @param lon The longitude for p.
     * @param lat The latitude for p.
     * @return The flattened, Euclidean y-value for p.
     * @source https://en.wikipedia.org/wiki/Transverse_Mercator_projection
     */
    static double projectToY(double lon, double lat) {
        double dlon = Math.toRadians(lon - ROOT_LON);
        double phi = Math.toRadians(lat);
        double con = Math.atan(Math.tan(phi) / Math.cos(dlon));
        return K0 * (con - Math.toRadians(ROOT_LAT));
    }

    /**
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     *
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        return Collections.emptyList();
    }

    /**
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     *
     * @param locationName A full name of a location searched for.
     * @return A <code>List</code> of <code>LocationParams</code> whose cleaned name matches the
     * cleaned <code>locationName</code>
     */
    public List<LocationParams> getLocations(String locationName) {
        return Collections.emptyList();
    }

    /**
     * Returns the initial bearing between vertices <code>v</code> and <code>w</code> in degrees.
     * The initial bearing is the angle that, if followed in a straight line along a great-circle
     * arc from the starting point, would take you to the end point.
     * Assumes the lon/lat methods are implemented properly.
     *
     * @param v The ID for the first vertex.
     * @param w The ID for the second vertex.
     * @return The bearing between <code>v</code> and <code>w</code> in degrees.
     * @source https://www.movable-type.co.uk/scripts/latlong.html
     */

    double bearing(long v, long w) {
        double phi1 = Math.toRadians(lat(v));
        double phi2 = Math.toRadians(lat(w));
        double lambda1 = Math.toRadians(lon(v));
        double lambda2 = Math.toRadians(lon(w));

        double y = Math.sin(lambda2 - lambda1) * Math.cos(phi2);
        double x = Math.cos(phi1) * Math.sin(phi2);
        x -= Math.sin(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Radius of the Earth in miles.
     */
    private static final int R = 3963;
    /**
     * Latitude centered on Berkeley.
     */
    private static final double ROOT_LAT = (MapServer.ROOT_ULLAT + MapServer.ROOT_LRLAT) / 2;
    /**
     * Longitude centered on Berkeley.
     */
    private static final double ROOT_LON = (MapServer.ROOT_ULLON + MapServer.ROOT_LRLON) / 2;
    /**
     * Scale factor at the natural origin, Berkeley. Prefer to use 1 instead of 0.9996 as in UTM.
     *
     * @source https://gis.stackexchange.com/a/7298
     */
    private static final double K0 = 1.0;
}
