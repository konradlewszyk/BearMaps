import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /**
     * The max image depth level.
     */
    public static final int MAX_DEPTH = 7;
    private String[][] results;
    private static double d0LongDPP = 0.00034332275390625;
    private static int levelOfInput = 0;


    private static ArrayList<Double> levelDPPMap = new ArrayList<>();
    Map<Integer, Double> xlevelIncrementMap = new HashMap<>();
    Map<Integer, Double> ylevelIncrementMap = new HashMap<>();

    public static void lonDPPer() {
        for (int i = 0; i < 8; i++) {
            levelDPPMap.add(d0LongDPP / Math.pow(2, i));
        }
    }

    public void xlevelator() {
        for (int i = 0; i < 8; i = i + 1) {
            xlevelIncrementMap.put(i, 0.087890625 / Math.pow(2, i));
        }
    }

    public void ylevelator() {
        for (int i = 0; i < 8; i = i + 1) {
            ylevelIncrementMap.put(i, 0.06939311371 / Math.pow(2, i));
        }
    }


    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     * possible, while still covering less than or equal to the amount of longitudinal distance
     * per pixel in the query box for the user viewport size.</li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the above
     * condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @return A valid RasterResultParams containing the computed results.
     */

    public RasterResultParams getMapRaster(RasterRequestParams params) {

        lonDPPer();
        xlevelator();
        ylevelator();
        double dppOfInput = lonDPP(params.lrlon, params.ullon, params.w);
        if (dppOfInput < levelDPPMap.get(7)) {
            levelOfInput = 7;
        } else {
            for (int i = 1; i <= levelDPPMap.size(); i = i + 1) {
                if (levelDPPMap.get(levelDPPMap.size() - i) < dppOfInput) {
                    levelOfInput = levelDPPMap.indexOf(levelDPPMap.get(levelDPPMap.size() - i));
                } else {
                    break;
                }
            }
        }

        double leftSideDifference = Math.abs(MapServer.ROOT_ULLON - params.ullon);
        double rightSideDifference = Math.abs(MapServer.ROOT_ULLON - params.lrlon);
        double topSideDifference = Math.abs(MapServer.ROOT_ULLAT - params.ullat);
        double bottomSideDifference = Math.abs(MapServer.ROOT_ULLAT - params.lrlat);

        /** box length is 100% correct */
        double xBoxLength = xlevelIncrementMap.get(levelOfInput);
        double yBoxLength = ylevelIncrementMap.get(levelOfInput);

        int leftSide = (int) Math.floor(leftSideDifference / xBoxLength);
        int topSide = (int) Math.floor(topSideDifference / yBoxLength);
        int bottomSide = (int) Math.floor(bottomSideDifference / yBoxLength);
        int rightSide = (int) Math.floor(rightSideDifference / xBoxLength);


        String[][] returnArray = new
                String[Math.abs(bottomSide - topSide) + 1][Math.abs(rightSide - leftSide) + 1];

        for (int k = topSide; k <= bottomSide; k = k + 1) {
            for (int i = leftSide; i <= rightSide; i = i + 1) {
                returnArray[k - topSide][i - leftSide] = "d" + Integer.toString(levelOfInput)
                        + "_x" + Integer.toString(i) + "_y" + Integer.toString(k) + ".png";
            }
        }

        RasterResultParams.Builder builderFunction = new RasterResultParams.Builder();

        System.out.println("right side:" + rightSide);
        System.out.println("left side:" + leftSide);
        System.out.println("bottom side:" + bottomSide);
        System.out.println("top side:" + topSide);

        builderFunction.setRenderGrid(returnArray);
        builderFunction.setDepth(levelOfInput);
        builderFunction.setRasterLrLat(MapServer.ROOT_ULLAT - (yBoxLength * (bottomSide + 1)));

        builderFunction.setRasterLrLon(MapServer.ROOT_ULLON + (xBoxLength * (rightSide + 1)));

        builderFunction.setRasterUlLat(MapServer.ROOT_ULLAT - (yBoxLength * (topSide)));

        builderFunction.setRasterUlLon(MapServer.ROOT_ULLON + (xBoxLength * (leftSide)));

        builderFunction.setQuerySuccess(true);
        return builderFunction.create();
    }


    /**
     * Calculates the lonDPP of an image or query box
     *
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }
}
