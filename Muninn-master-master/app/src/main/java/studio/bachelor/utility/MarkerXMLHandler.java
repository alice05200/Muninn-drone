package studio.bachelor.utility;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import studio.bachelor.draft.marker.AnchorMarker;
import studio.bachelor.draft.marker.ControlMarker;
import studio.bachelor.draft.marker.LabelMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.marker.MeasureMarker;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.muninn.Muninn;

/**
 * Created by User on 2017/3/8.
 */
public class MarkerXMLHandler {
    private Marker marker;
    private ArrayList<Marker> markers;
    private ArrayList<Position> positions, tempPositions;
    private ArrayList<Integer> nameLabels, markerIDs, numLabels, link1s, link2s, labelIDs, labelTypes;
    private ArrayList<String> labelTexts;
    private String  position_X, position_Y, nameLabel, labelText;
    private int id, labelID, markerNum = 0, linkNum = 0;
    private float px, py;
    private int mode = 0, numLabel = -1, link1 = -1, link2 = -1;
    private boolean markerDone = false;

    public MarkerXMLHandler(){
        markers = new ArrayList<Marker>();
        positions = new ArrayList<Position>();
        nameLabels = new ArrayList<Integer>();
        numLabels = new ArrayList<Integer>();
        link1s = new ArrayList<Integer>();
        link2s = new ArrayList<Integer>();
        labelIDs = new ArrayList<Integer>();
        labelTypes = new ArrayList<Integer>();
        labelTexts = new ArrayList<String>();
        markerIDs = new ArrayList<Integer>();
        tempPositions = new ArrayList<Position>();
    }

    public void cleanList(){
        markers.clear();
        positions.clear();
        nameLabels.clear();
        numLabels.clear();
        link1s.clear();
        link2s.clear();
        labelIDs.clear();
        labelTypes.clear();
        labelTexts.clear();
        markerIDs.clear();
        tempPositions.clear();
        markerNum = 0;
        mode = 0;
        numLabel = -1;
        link1 = -1;
        link2 = -1;
        linkNum = 0;
        markerDone = false;
    }
    public ArrayList<Position> getPositions(){
        return positions;
    }

    public ArrayList<Marker> getMarkers(){
        return markers;
    }

    public ArrayList<Marker> parse(String str){
        XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        String file_name = str.substring(str.indexOf("/Muninn"), str.indexOf("birdview.jpg")) + "data.xml";
        Log.d("我我我", file_name);
        try {
            File file = new File(Environment.getExternalStorageDirectory() + file_name);
            if(!file.exists()) {
                Toast.makeText(Muninn.getContext(), "Error", Toast.LENGTH_SHORT).show();
                return null;
            }
            FileInputStream fileInputStream = new FileInputStream(file);
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(fileInputStream, "utf-8");
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG://開頭
                        if(tagName.equals("marker")){
                            String id = parser.getAttributeValue(0);
                            this.id = Integer.parseInt(id);
                            markerIDs.add(this.id);
                            markerNum++;
                            Log.d("Marker復原ID", "" + this.id);
                        }else if(tagName.equals("positionX")){
                            mode = 0;
                        }else if(tagName.equals("positionY")){
                            mode = 1;
                        }else if(tagName.equals("nameLabel")){
                            mode = 2;
                        }else if(tagName.equals("link1")){
                            mode = 3;
                        }else if(tagName.equals("link2")){
                            mode = 4;
                        }else if(tagName.equals("numLabel")){
                            mode = 5;
                        }else if(tagName.equals("Label")){
                            labelID = Integer.parseInt(parser.getAttributeValue(0));
                            labelIDs.add(labelID);
                            Log.d("Label復原ID", "" + labelID);
                        }else if(tagName.equals("type")){
                            mode = 6;
                        }else if(tagName.equals("label")){
                            mode = 7;
                        }else
                            mode = -1;
                        break;
                    case XmlPullParser.TEXT://內容
                        switch(mode){
                            case 0:
                                position_X = parser.getText();
                                Log.d("AAAAAA", parser.getText());
                                px = Float.parseFloat(position_X);
                                break;
                            case 1:
                                position_Y = parser.getText();
                                py = Float.parseFloat(position_Y);
                                break;
                            case 2:
                                nameLabel = parser.getText();
                                nameLabels.add(Integer.parseInt(nameLabel));
                                Log.d("nameLabel復原ID", nameLabel);
                                break;
                            case 3:
                                link1 = Integer.parseInt(parser.getText());
                                link1s.add(link1);
                                Log.d("link1復原ID", "" + link1);
                                break;
                            case 4:
                                link2 = Integer.parseInt(parser.getText());
                                link2s.add(link2);
                                Log.d("link2復原ID", "" + link2);
                                linkNum++;
                                break;
                            case 5:
                                numLabel = Integer.parseInt(parser.getText());
                                numLabels.add(numLabel);
                                Log.d("numLabel復原ID", "" + numLabel);
                                break;
                            case 6:
                                labelTypes.add(Integer.parseInt(parser.getText()));
                                Log.d("labelTypes復原", parser.getText());
                                break;
                            case 7:
                                labelText = parser.getText();
                                Log.d("labelTexts復原", parser.getText());
                                break;
                        }
                        mode = -1;
                        break;
                    case XmlPullParser.END_TAG://結尾
                        if(tagName.equals("positionY") && !markerDone){
                            tempPositions.add(new Position(px, py));
                            Log.d("Position復原", "x" + px + "y" + py);
                        }else if(tagName.equals("markers")){
                            markerDone = true;
                        }else if(tagName.equals("label")){
                            labelTexts.add(labelText);
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Marker數", "" + markerNum);
        Log.d("Line數", "" + linkNum);
        for(int i = 0; i < markerNum; i++){//先恢復LabelMarker
            if(nameLabels.get(i) != -1){
                marker = new LabelMarker();
                ((LabelMarker)marker).setLabel(labelTexts.get(nameLabels.get(i)));
                markers.add(marker);
                positions.add(tempPositions.get(i));
            }
        }
        int anchorMarker = -1;
        if(labelTypes.indexOf(1) != -1)
            anchorMarker = numLabels.indexOf(labelIDs.get(labelTypes.indexOf(1)));//找type為1的marker(AnchorMarker)
        if(anchorMarker != -1) {//如果有找到
            marker = AnchorMarker.getInstance();
            ((AnchorMarker) marker).setRealDistance(Double.parseDouble(labelTexts.get(labelTypes.indexOf(1))));
            markers.add(marker);
            marker = new ControlMarker();
            markers.add(marker);
            if(link1s.get(anchorMarker) > link2s.get(anchorMarker)) {
                positions.add(tempPositions.get(markerIDs.indexOf(link2s.get(anchorMarker))));
                positions.add(tempPositions.get(markerIDs.indexOf(link1s.get(anchorMarker))));
            }else{
                positions.add(tempPositions.get(markerIDs.indexOf(link1s.get(anchorMarker))));
                positions.add(tempPositions.get(markerIDs.indexOf(link2s.get(anchorMarker))));
            }
        }
        for(int i = 0; i < linkNum; i++){//恢復MeasureMarker
            if(i != anchorMarker){
                marker = new MeasureMarker();
                markers.add(marker);
                marker = new ControlMarker();
                markers.add(marker);
                if(link1s.get(i) > link2s.get(i)){
                    positions.add(tempPositions.get(markerIDs.indexOf(link2s.get(i))));
                    positions.add(tempPositions.get(markerIDs.indexOf(link1s.get(i))));
                }else{
                    positions.add(tempPositions.get(markerIDs.indexOf(link1s.get(i))));
                    positions.add(tempPositions.get(markerIDs.indexOf(link2s.get(i))));
                }
            }
        }
        return markers;
    }
}
