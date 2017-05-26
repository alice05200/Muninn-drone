package studio.bachelor.draft.marker;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

import studio.bachelor.draft.utility.MapStringSupport;
import studio.bachelor.draft.utility.Position;

/**
 * Created by bachelor on 2016/3/8.
 */
public class AnchorMarker extends LinkMarker implements MapStringSupport {
    static private final AnchorMarker instance = new AnchorMarker();
    static public AnchorMarker getInstance() {return instance;}
    private double realDistance, picRealDistance = 0;
    private Position picPositionA = null, picPositionB = null;
    public static LinkedList<Double> historyDistancesUndo = new LinkedList<Double>();
    public static LinkedList<Double> historyDistancesRedo = new LinkedList<Double>();

    private AnchorMarker() {
        super();
        this.link = new ControlMarker();
    }

    @Override
    public String getObjectMappedString() {
        return String.valueOf(realDistance);
    }

    public double getScale() {
        if(picPositionA != null && picPositionB != null && picRealDistance == realDistance)
            return picRealDistance / picPositionA.getDistanceTo(picPositionB);
        else
            return realDistance / position.getDistanceTo(this.link.position);

    }

    public void setRealDistance(double real_distance) {
        this.realDistance = real_distance > 0.0 ? real_distance : 0.0;
    }

    public double getPicRealDistance(){
        return picRealDistance;
    }
    public void setPicDisPos(double d, Position p1, Position p2){
        picRealDistance = d;
        picPositionA = p1;
        picPositionB = p2;
    }
    public double getRealDistance() {
        return this.realDistance;
    }

    public String getElementName() {
        return "AnchorMarker";
    }

    @Override
    public Node transformStateToDOMNode(Document document) {
        return super.transformStateToDOMNode(document);
    }
}
