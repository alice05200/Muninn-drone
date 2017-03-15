package studio.bachelor.utility;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.marker.AnchorMarker;
import studio.bachelor.draft.marker.ControlMarker;
import studio.bachelor.draft.marker.LabelMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.marker.MeasureMarker;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.MuninnActivity;
import studio.bachelor.muninn.R;

/**
 * Created by User on 2017/3/8.
 */
public class MarkerXMLHandler {
    private Marker marker;
    private ArrayList<Marker> markers;
    private ArrayList<Position> positions;
    private String markerType, position_X, position_Y, label_text, link;
    private int id;
    private float px, py;
    private double distance;
    private int mode = 0;//0 x 1 y 2 label 3 real_distance

    public MarkerXMLHandler(){
        markers = new ArrayList<Marker>();
        positions = new ArrayList<Position>();
    }

    public void cleanList(){
        markers.clear();
        positions.clear();
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
                        if(tagName.equals("LabelMarker")){
                            marker = new LabelMarker();
                            String id = parser.getAttributeValue(0);
                            this.id = Integer.parseInt(id);
                            Log.d("marker", tagName);
                        }else if(tagName.equals("AnchorMarker")){
                            marker = AnchorMarker.getInstance();
                            String id = parser.getAttributeValue(0);
                            this.id = Integer.parseInt(id);
                            Log.d("marker", tagName);
                        }else if(tagName.equals("MeasureMarker")){
                            marker = new MeasureMarker();
                            String id = parser.getAttributeValue(0);
                            this.id = Integer.parseInt(id);
                            Log.d("marker", tagName);
                        }else if(tagName.equals("ControlMarker")){
                            marker = new ControlMarker();
                            String id = parser.getAttributeValue(0);
                            this.id = Integer.parseInt(id);
                            Log.d("marker", tagName);
                        }else if(tagName.equals("x")){
                            mode = 0;
                            Log.d("X position", tagName);
                        }else if(tagName.equals("y")){
                            mode = 1;
                            Log.d("Y position", tagName);
                        }else if(tagName.equals("label")){
                            mode = 2;
                            Log.d("label", tagName);
                        }else if(tagName.equals("real_distance")){
                            mode = 3;
                            Log.d("real distance", tagName);
                        }else if(tagName.equals("distance")){
                            mode = 3;
                        }else if(tagName.equals("link")){
                            mode = 4;
                            Log.d("Link", tagName);
                        }else if(tagName.equals("code")){
                            mode = 5;
                        }
                        break;
                    case XmlPullParser.TEXT://內容
                        switch(mode) {
                            case 0:
                                position_X = parser.getText();
                                px = Float.parseFloat(position_X);
                                Log.d("X position", parser.getText());
                                break;
                            case 1:
                                position_Y = parser.getText();
                                py = Float.parseFloat(position_Y);
                                Log.d("Y position", parser.getText());
                                break;
                            case 2:
                                label_text = parser.getText();
                                Log.d("label", parser.getText());
                                break;
                            case 3:
                                distance = Double.parseDouble(parser.getText());
                                Log.d("distance", parser.getText());
                                break;
                        }
                        break;
                    case XmlPullParser.END_TAG://結尾
                        if(tagName.equals("LabelMarker") && marker.getClass() == LabelMarker.class){
                            marker = new LabelMarker(label_text);
                            marker.setID(this.id);
                            markers.add(marker);
                            Position p = new Position(px, py);
                            positions.add(p);
                        }else if(tagName.equals("AnchorMarker") && marker.getClass() == AnchorMarker.class){
                            ((AnchorMarker)marker).setRealDistance(distance);
                            marker.setID(this.id);
                            markers.add(marker);
                            Position p = new Position(px, py);
                            positions.add(p);
                        }else if(tagName.equals("MeasureMarker") && marker.getClass() == MeasureMarker.class){
                            marker.setID(this.id);
                            markers.add(marker);
                            Position p = new Position(px, py);
                            positions.add(p);
                        }else if(tagName.equals("ControlMarker") && marker.getClass() == ControlMarker.class){
                            marker.setID(this.id);
                            markers.add(marker);
                            Position p = new Position(px, py);
                            positions.add(p);
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
        return markers;
    }
}
