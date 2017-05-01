package studio.bachelor.draft;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Path;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import studio.bachelor.draft.marker.AnchorMarker;
import studio.bachelor.draft.marker.ControlMarker;
import studio.bachelor.draft.marker.LabelMarker;
import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.marker.MarkerManager;
import studio.bachelor.draft.marker.MeasureMarker;
import studio.bachelor.draft.toolbox.Toolbox;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.renderer.layer.Layer;
import studio.bachelor.draft.utility.renderer.layer.ScaleLayer;
import studio.bachelor.muninn.Muninn;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class Draft{
    private final String TAG = "Draft";
    private static final Draft instance = new Draft();
    public final ScaleLayer layer = new ScaleLayer(0, 0);
    public double scale = 1.0;
    private final List<Path> paths = new ArrayList<Path>();
    private final List<Path> eraserPaths = new ArrayList<Path>();
    private Path currentPath = null;
    private double width = 1.0;
    private double height = 1.0;
    public static boolean STYLUS_MODE = false;
    public static boolean FINGER_MODE = true;

    public static Draft getInstance() {
        return instance;
    }

    private Draft() {

    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() { return width; }

    public double getHeight() { return height; }

    public void createPathIfPathMode(Position position) {
        currentPath = new Path(); //建立path
        Position transformed = getDraftPosition(position);
        currentPath.moveTo((float)transformed.x, (float)transformed.y);
    }

    public void recordPath(Position position) {
        if(currentPath != null) {
            Position transformed = getDraftPosition(position);
            currentPath.lineTo((float) transformed.x, (float) transformed.y);
        }
    }

    public void endPath(Position position) {
        if(currentPath != null) {
            Position transformed = getDraftPosition(position);
            currentPath.lineTo((float) transformed.x, (float) transformed.y);
            paths.add(currentPath);
            currentPath = null;
        }
    }

    public void clearPaths() {
        paths.clear();
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public void setWidthAndHeight(float width, float height) {
        this.layer.setWidthAndHeight(width, height);
    }

    public void addMarker(Marker marker) {
        Position tempPosition = getDraftPosition(marker.position); //return new Position
        marker.position.set(tempPosition); //取得Screen上的真實位置
        Log.d(TAG, "addMarker getDraftPosition(): (" + tempPosition.x + ", " + tempPosition.y + ")");
        layer.markerManager.addMarker(marker);
    }

    public void addMarkerLayerPosition(Marker marker) { //根據實際Layer的位置呈現marker
        layer.markerManager.addMarker(marker);
    }


    public void removeMarker(Marker marker) {
        marker.position.set(getDraftPosition(marker.position)); //?Jonas get the marker's position on screen.
        layer.markerManager.removeMarker(marker);
    }

    public void moveMarker(Marker marker, Position position) {
        if(marker != null) {
            position = getDraftPosition(position);
            if (FINGER_MODE) {
                //手指觸控模式
                if (marker instanceof ControlMarker) {
                    //LinkedMarker //include Anchor's and Measure's ControlMarker 更新ControlMarker
                    Marker fatherMarker = ((ControlMarker) marker).getLinksFatherMarker();
                    position.set(getAuxilaryPosition(fatherMarker, position, 150/layer.getScale()));
                } else if (marker instanceof MeasureMarker || marker instanceof AnchorMarker) {

                    position.set(getAuxilaryPosition( ((LinkMarker) marker).getLink(), position, 150/layer.getScale()));
                } else if (marker instanceof LabelMarker) {
                    position.set(new Position(position.x - 150/layer.getScale(), position.y - 150/layer.getScale())); //向左上角移動
                }
            } else if (STYLUS_MODE) {
                //觸控筆模式
            }

            marker.refreshed_tap_position = position; //儲存marker移動的位置(螢幕點選的位置)//?Jonas
            marker.move(position);
        }
    }

    Position getAuxilaryPosition(Marker A, Position holdPosition, double radius) {
        double angle = 0.0;
        double theta;
        double X = holdPosition.x - A.position.x;
        double Y = holdPosition.y - A.position.y ;
        theta = Math.atan2(Y, X);
        angle = Math.toDegrees(theta);
        if (angle < 0) {
            angle += 360;
        }

        if (angle >=270 || angle <= 90) {
            radius += (50/layer.getScale()); //延長半徑，防止手指肥大遮擋
        }

        theta = Math.toRadians(angle);

        double newPositionX = holdPosition.x + radius * Math.cos(theta);
        double newPositionY = holdPosition.y + radius * Math.sin(theta);

        return new Position(newPositionX, newPositionY);
    }


    public Position getDraftPosition(Position position) {
        return layer.getPositionOfLayer(position);
    }

    public Marker getNearestMarker(Position position) {
        Position draft_position = getDraftPosition(position);
        return layer.markerManager.getNearestMarker(draft_position, 64);
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    private Node createMarkerNodeWithLayerScale(Marker marker, Document document, int k) {
        Node marker_node = marker.transformStateToDOMNode(document);
        int length = marker_node.getChildNodes().getLength();
        for(int i = 0; i < length; ++i) {
            Node position_node = marker_node.getChildNodes().item(i);
            if(position_node.getNodeName() == "positionX") {
                Double x = Double.parseDouble(position_node.getTextContent());
                Double scale_x = (x + width / 2) / width; // (width / 2);
                scale_x = (double)(Math.round(scale_x * 1000000)) / 1000000;
                position_node.setTextContent(scale_x.toString());
            }else if(position_node.getNodeName() == "positionY") {
                Double y = Double.parseDouble(position_node.getTextContent());
                Double scale_y = (y + height / 2 ) / height;// (height / 2);
                scale_y = (double)(Math.round(scale_y * 1000000)) / 1000000;
                position_node.setTextContent(scale_y.toString());
            }else if(marker.getClass() == LabelMarker.class && position_node.getNodeName() == "nameLabel"){
                position_node.setTextContent("" + k);
            }
        }
        return marker_node;
    }
    private Node createLabelsNode(Marker marker, Document document, int id) {
        Element node = document.createElement("Label");
        node.setAttribute("ID", "" + id);
        Element nodeType = document.createElement("type");
        Element nodeX = document.createElement("positionX");
        Element nodeY = document.createElement("positionY");
        Element nodeSize = document.createElement("size");
        Element nodeColor = document.createElement("color");
        Element nodeNameLabel = document.createElement("label");

        if(marker.getClass() == LabelMarker.class){
            nodeType.appendChild(document.createTextNode("-1"));
            nodeNameLabel.appendChild(document.createTextNode(((LabelMarker)marker).getLabel()));
        }else if(marker.getClass() == MeasureMarker.class){
            nodeType.appendChild(document.createTextNode("0"));
        }else if(marker.getClass() == AnchorMarker.class){
            nodeType.appendChild(document.createTextNode("1"));
            nodeNameLabel.appendChild(document.createTextNode("" + ((AnchorMarker)marker).getRealDistance()));
        }else
            return null;
        nodeSize.appendChild(document.createTextNode("-1"));
        nodeColor.appendChild(document.createTextNode("-1"));
        nodeX.appendChild(document.createTextNode("0"));
        nodeY.appendChild(document.createTextNode("0"));
        node.appendChild(nodeType);
        node.appendChild(nodeX);
        node.appendChild(nodeY);
        node.appendChild(nodeSize);
        node.appendChild(nodeColor);
        node.appendChild(nodeNameLabel);
        return node;
    }
    private Node createLinesNode(Marker marker, Document document, int labelID, int lineID, int link1ID, int link2ID) {
        Element node = document.createElement("line");
        node.setAttribute("ID", "" + lineID);
        Element nodeSize = document.createElement("size");
        Element nodeColor = document.createElement("color");
        Element nodeLinks = document.createElement("links");
        Element nodeLink1 = document.createElement("link1");
        Element nodeLink2 = document.createElement("link2");
        Element nodeNumLabel = document.createElement("numLabel");

        nodeLink1.appendChild(document.createTextNode("" + link1ID));
        nodeLink2.appendChild(document.createTextNode("" + link2ID));
        nodeLinks.appendChild(nodeLink1);
        nodeLinks.appendChild(nodeLink2);
        nodeSize.appendChild(document.createTextNode("-1"));
        nodeColor.appendChild(document.createTextNode("-1"));
        nodeNumLabel.appendChild(document.createTextNode("" + labelID));

        node.appendChild(nodeSize);
        node.appendChild(nodeColor);
        node.appendChild(nodeLinks);
        node.appendChild(nodeNumLabel);
        return node;
    }
    public Node writeDOM(Document document) {
        Element root = document.createElement("Draft");
        Element markers = document.createElement("markers");
        Element lines = document.createElement("lines");
        Element labels = document.createElement("labels");
        int labelNum = 0, lineNum = 0;
        for(Marker marker : layer.markerManager.markers) {
            Node marker_node = createMarkerNodeWithLayerScale(marker, document, labelNum);
            markers.appendChild(marker_node);
            Node label_node = createLabelsNode(marker, document, labelNum);
            if(label_node != null) {
                labels.appendChild(label_node);
                labelNum++;
            }
            if(marker.getClass() == ControlMarker.class){
                Node lines_node = createLinesNode(marker, document, labelNum - 1, lineNum, marker.getID(), ((ControlMarker)marker).getLinksFatherMarker().getID());
                lines.appendChild(lines_node);
                lineNum++;
            }
        }
        root.appendChild(markers);
        root.appendChild(lines);
        root.appendChild(labels);
        return root;
    }
}
