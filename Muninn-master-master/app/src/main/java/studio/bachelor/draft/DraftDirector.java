package studio.bachelor.draft;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import studio.bachelor.draft.marker.AnchorMarker;
import studio.bachelor.draft.marker.ControlMarker;
import studio.bachelor.draft.marker.LabelMarker;
import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.marker.Marker;
import studio.bachelor.draft.marker.MeasureMarker;
import studio.bachelor.draft.marker.builder.ControlMarkerBuilder;
import studio.bachelor.draft.marker.builder.LabelMarkerBuilder;
import studio.bachelor.draft.marker.builder.LinkMarkerBuilder;
import studio.bachelor.draft.marker.builder.MeasureMarkerBuilder;
import studio.bachelor.draft.toolbox.Toolbox;
import studio.bachelor.draft.utility.BitmapMD5Encoder;
import studio.bachelor.draft.utility.DataStepByStep;
import studio.bachelor.draft.utility.MapString;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.draft.utility.Selectable;
import studio.bachelor.draft.utility.SignPad;
import studio.bachelor.draft.utility.renderer.DraftRenderer;
import studio.bachelor.draft.utility.renderer.RendererManager;
import studio.bachelor.draft.utility.renderer.builder.MarkerRendererBuilder;
import studio.bachelor.muninn.FTPUtils;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.MuninnActivity;
import studio.bachelor.muninn.R;
import studio.bachelor.utility.FTPUploader;
import studio.bachelor.utility.MarkerXMLHandler;

import static java.lang.Math.abs;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class DraftDirector {
    private final String TAG = "DraftDirector";
    public static final DraftDirector instance = new DraftDirector();
    private Draft draft;
    private DraftRenderer draftRenderer;
    private RendererManager rendererManager;
    private Map<Object, Renderable> renderableMap = new HashMap<Object, Renderable>();
    public LinkedList<Marker> RedoTempLL = new LinkedList<Marker>();
    public static LinkedList<DataStepByStep> StepByStepUndo = new LinkedList<DataStepByStep>();
    public static LinkedList<DataStepByStep> StepByStepRedo = new LinkedList<DataStepByStep>();
    //private final Toolbox toolbox = Toolbox.getInstance();
    //private ToolboxRenderer toolboxRenderer;
    private Type markerType = MeasureMarker.class;
    private Marker markerHold;
    private Marker markerSelecting;
    private Marker markerSelected;
    private Toolbox.Tool tool;
    private final Paint paint = new Paint();
    //private final Paint pathPaint = new Paint();
    private Context context;
    private int nextObjectID = 0;
    private Uri birdViewUri;
    private Bitmap birdview;
    private BitmapMD5Encoder MD5Encoder;
    private Thread MD5EncoderThread;
    private List<File> signFiles = new LinkedList<File>();
    private Toolbox.Tool currentMode = Toolbox.Tool.HAND_MOVE, preMode = Toolbox.Tool.HAND_MOVE;
    private MarkerXMLHandler markerXMLHandler = new MarkerXMLHandler();

    private boolean readytoSave = true, undoredo = false;
    private String filename = null, droneHeight = null;
    private double droneFOV = 78.8;
    private final String MUNINN_FILE = "Muninn", HUGINN_FILE = "Pictures/DroneAlbum/";
    boolean firstTime = true; //Create後，firstTime = false; Delete後，firstTime = true
    boolean edit_mode = false;
    int i = 0;

    {
        draft = Draft.getInstance();
        draftRenderer = new DraftRenderer(draft);
        rendererManager = RendererManager.getInstance();
    }

    private DraftDirector() {

    }
    public void setViewContext(Context context) {
        this.context = context;
    }

    public int allocateObjectID() {
        return nextObjectID++;
    }

    public void setBirdviewImageByUri(Uri uri) {//設定選取的圖片
        cleanBirdviewLine();
        try {
            birdview = MediaStore.Images.Media.getBitmap(Muninn.getContext().getContentResolver(), uri);
            birdViewUri = uri;
            Log.d(TAG, Environment.getExternalStorageDirectory().toString() + uri.toString());
            /*if(!edit_mode) {
                MD5Encoder = new BitmapMD5Encoder(birdview); //建立Runnable類別，依據BitMap圖檔編碼MD5
                MD5EncoderThread = new Thread(MD5Encoder); //建立Thread
                MD5EncoderThread.start();
            }*/
        } catch (Exception e) {
            Log.d("DraftRenderer", "setBirdview(Uri uri)" + e.toString());
        }
        draftRenderer.setBirdview(birdview); //設定選取好的圖片
        draft.setWidth(birdview.getWidth()); //setting the width-size of draft according to the birdview.
        draft.setHeight(birdview.getHeight());
        rendererManager.setBitmap(birdview);
        if(uri.getLastPathSegment().lastIndexOf("-") != -1 && uri.getLastPathSegment().lastIndexOf(".") != -1){
            droneHeight = uri.getLastPathSegment().substring(uri.getLastPathSegment().lastIndexOf("-") + 1, uri.getLastPathSegment().lastIndexOf("."));
            if(droneHeight.matches("[0-9]+")) {
                addAnchorMarker(new Position(-birdview.getWidth() / 2 + draft.layer.getCenter().x, birdview.getHeight() / 2 + draft.layer.getCenter().y), firstTime);
                firstTime = false;
            }
            else
                droneHeight = null;
        }
        //if(MD5EncoderThread != null)
        //    MD5EncoderThread.interrupt();
        //birdViewUri = uri;
        Date current_time = new Date();
        SimpleDateFormat simple_date_format = new SimpleDateFormat("yyyyMMddHHmmss");
        filename = "Draft" + simple_date_format.format(current_time);//資料夾名稱預設Draft+時間
    }

    public void cleanBirdviewLine(){
        signFiles.clear();
        clearAllLine();
        draft.clearPaths();
        draft.layer.setScale(1);
        edit_mode = false;
        Position screenCenter = new Position(0,0);
        this.draft.layer.moveLayerto(screenCenter);//設定位置到中間
        if(birdview != null) {
            birdview = null;
            rendererManager.setBitmap(birdview);
            draftRenderer.setBirdview(birdview);
            draft.setWidth(1);
            draft.setHeight(1);
            readytoSave = true;
        }

    }

    public boolean getSaveState(){
        return readytoSave;
    }

    public void setSaveState(boolean b){
        readytoSave = b;
    }
    public void markerRestore(){
        markerXMLHandler.cleanList();
        i = 0;
        edit_mode = true;
        ArrayList<Marker> tempMarkers = markerXMLHandler.parse();
        if(tempMarkers != null) {
            for (Marker m : tempMarkers) {
                if (m.getClass() == LabelMarker.class) {
                    Log.d("LabelMarker復原", "" + i);
                    addLabelMarker(new Position(markerXMLHandler.getPositions().get(i).x * birdview.getWidth() - birdview.getWidth() / 2 + draft.layer.getCenter().x,
                            markerXMLHandler.getPositions().get(i).y * birdview.getHeight() - birdview.getHeight() / 2 + draft.layer.getCenter().y));
                } else if (m.getClass() == AnchorMarker.class) {
                    firstTime = false;
                    Log.d("AnchorMarker復原", "" + i);
                    addAnchorMarker(new Position(markerXMLHandler.getPositions().get(i).x * birdview.getWidth() - birdview.getWidth() / 2 + draft.layer.getCenter().x,
                            markerXMLHandler.getPositions().get(i).y * birdview.getHeight() - birdview.getHeight() / 2 + draft.layer.getCenter().y), true);
                } else if (m.getClass() == MeasureMarker.class) {
                    Log.d("MeasureMarker復原", "" + i);
                    addMeasureMarker(new Position(markerXMLHandler.getPositions().get(i).x * birdview.getWidth() - birdview.getWidth() / 2 + draft.layer.getCenter().x,
                            markerXMLHandler.getPositions().get(i).y * birdview.getHeight() - birdview.getHeight() / 2 + draft.layer.getCenter().y));
                }
                i++;
            }
        }else
            showToast("Error");
        edit_mode = false;
    }
    public void setWidthAndHeight(float width, float height) {
        this.draft.setWidthAndHeight(width, height);
    }
    /*public void setToolboxRenderer(Position upper_left_corner, float width, float height) {
        toolboxRenderer = new ToolboxRenderer(toolbox, upper_left_corner, width, height);
    }*/
    public void createPathIfPathMode(Position position) { //curve
        if (enablePosition(position)){
        if (tool == Toolbox.Tool.PATH_MODE) {
            draft.createPathIfPathMode(position);
        }
        }
    }


    public void recordPath(Position position) {
        if (enablePosition(position)){
        if (tool == Toolbox.Tool.PATH_MODE) {
            draft.recordPath(position);
        }
        }else{
            if (tool == Toolbox.Tool.PATH_MODE) {
                draft.endPath(pathEndFix(position));
            }
        }
    }

    public void endPath(Position position) {
        if (enablePosition(position)){
        if (tool == Toolbox.Tool.PATH_MODE) {
            draft.endPath(position);
            DataStepByStep pencil = new DataStepByStep(draft.getPaths().get(draft.getPaths().size() - 1), Selectable.CRUD.CREATE);
            StepByStepUndo.add(pencil);
        }}
    }
    private Position pathEndFix(Position position){
        if(birdview != null) {
            float birdviewHeight = birdview.getHeight() * this.draft.layer.getScale();
            float birdviewWidth = birdview.getWidth() * this.draft.layer.getScale();

            if (abs(position.x - this.draft.layer.getCenter().x) > birdviewWidth / 2) {
                if (position.x - this.draft.layer.getCenter().x > 0) {
                    Position tmp = new Position(this.draft.layer.getCenter().x + birdviewWidth / 2, position.y);
                    return tmp;
                } else {
                    Position tmp = new Position(this.draft.layer.getCenter().x - birdviewWidth / 2, position.y);
                    return tmp;
                }
            }

            if (abs(position.y - this.draft.layer.getCenter().y) > birdviewHeight / 2) {
                if (position.y - this.draft.layer.getCenter().y > 0) {
                    Position tmp = new Position(position.x, this.draft.layer.getCenter().y + birdviewHeight / 2);
                    return tmp;
                } else {
                    Position tmp = new Position(position.x, this.draft.layer.getCenter().y - birdviewHeight / 2);
                    return tmp;
                }
            }
            return position;
        }
        return null;
    }
    private Position prevLayerPosition = new Position();
    private boolean enableMoving = false;

    public void moveLayerCreate(Position p) {
        if (getTool() != Toolbox.Tool.PATH_MODE && enableMoving == false) {
            enableMoving = true;
            prevLayerPosition.set(p);
        }

    }

    public void moveLayerStart(Position p) {
        if (enableMoving && markerHold == null) {
            double x = p.x - prevLayerPosition.x;
            double y = p.y - prevLayerPosition.y;
            Position offset = new Position(x, y);
            moveDraft(offset);
            prevLayerPosition.set(p);
        }

    }

    public void moveLayerStop() {
            enableMoving = false;
    }

    public void addMarker(Position position) {
        Muninn.sound_Ding.seekTo(0);
        Muninn.sound_Ding.start();
        if(undoredo){
            StepByStepRedo.clear();
            undoredo = false;
        }
        if(tool != Toolbox.Tool.PATH_MODE)
            Muninn.mVibrator.vibrate(100);
      if(enablePosition(position))
      {
          if(readytoSave)
              readytoSave = false;
        if (markerType == MeasureMarker.class) {
            addMeasureMarker(position);
        } else if (markerType == AnchorMarker.class) {
            if (firstTime) {
                firstTime = false; //global var
                addAnchorMarker(position, true);
            } else {
                addAnchorMarkerDialoag(position);
            }

        } else if (markerType == LabelMarker.class) {
            addLabelMarker(position);
        }
      }
    }
    public void addAnchorMarkerDialoag(final Position position ){
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.dialog);
        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(contextThemeWrapper);
        dialog_builder
                .setTitle(R.string.new_mark)
                .setMessage(R.string.alert_new_mark)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Muninn.mVibrator.vibrate(100);
                        addAnchorMarker(position, false);  //已存在一個AnchorMarker，重新新增的
                    }
                })
                .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Muninn.mVibrator.vibrate(100);
                    }
                });
        AlertDialog alertDialog = dialog_builder.create();
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(MuninnActivity.width / 40);
    }

    private void addLabelMarker(Position position) {
        LabelMarkerBuilder lb = new LabelMarkerBuilder();
        final Marker marker = lb.
                setPosition(new Position(position.x, position.y)).
                build();

        Log.d("LabelMarker position", "x:" + position.x + ", y:" + position.y);
        if(!edit_mode) {
            final EditText edit_text = new EditText(context);
            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.dialog);
            AlertDialog.Builder dialog_builder = new AlertDialog.Builder(contextThemeWrapper);
            dialog_builder.setCancelable(false);
            dialog_builder
                    .setTitle("標籤資訊")
                    .setView(edit_text)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Muninn.mVibrator.vibrate(100);
                            String label_str = edit_text.getText().toString();
                            if (label_str.isEmpty()) {
                                return;
                            }
                            ((LabelMarker) marker).setLabel(label_str);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Muninn.mVibrator.vibrate(100);
                            ((LabelMarker) marker).remove();
                        }
                    });
            AlertDialog alertDialog = dialog_builder.create();
            alertDialog.show();
            TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
            textView.setTextSize(MuninnActivity.width / 40);
                //marker.setSizeColor("" +  Muninn.getColorSetting(R.string.key_marker_line_color, R.string.default_marker_line_color), Muninn.getColorSetting(R.string.key_marker_line_color, R.string.default_marker_line_color));
        }else {
            ((LabelMarker) marker).setLabel(((LabelMarker) markerXMLHandler.getMarkers().get(i)).getLabel());
        }
        draft.addMarker(marker);

        marker.refreshed_Layer_position.set(marker.position);
        marker.historyLayerPositionsUndo.add(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //After getting Layer-position
        Log.d(TAG, "marker.position = (" + marker.position.x + ", " + marker.position.y + ")");
        Log.d(TAG, "marker.refreshed_Layer_position = (" + marker.refreshed_Layer_position.x + ", " + marker.refreshed_Layer_position.y + ")");
        Log.d(TAG, "historyLayerPositionsUndo = (" + marker.historyLayerPositionsUndo.getLast().x + ", " + marker.historyLayerPositionsUndo.getLast().y + ")");

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        if(edit_mode)
            marker.setSizeColor(markerXMLHandler.getMarkers().get(i).getSize(), markerXMLHandler.getMarkers().get(i).getColor(), markerXMLHandler.getMarkers().get(i).getText_color());

        Renderable marker_renderer = mrb.
                setReference(marker).
                setPoint(marker, Float.parseFloat(marker.getSize()), marker.getColor()).
                setText(new MapString((LabelMarker) marker), marker.position, Float.parseFloat(marker.getSize()), marker.getText_color()).
                build();

        rendererManager.addRenderer(marker_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);

        StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.CREATE));
    }


    //2016/10/17 By Jonas
    private void updateLabelMarker(DataStepByStep data) {
        Marker tMarker = data.getMarker();

        if (renderableMap.containsKey(tMarker)) {
            rendererManager.removeRenderer(renderableMap.get(tMarker));
        }

        tMarker.position.set(tMarker.refreshed_Layer_position);

        ((LabelMarker) tMarker).setLabel(((LabelMarker) tMarker).getLabel());

        draft.addMarkerLayerPosition(tMarker);

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setReference(tMarker).
                setPoint(tMarker, Float.parseFloat(tMarker.getSize()), tMarker.getColor()).
                setText(new MapString((LabelMarker) tMarker), tMarker.position, Float.parseFloat(tMarker.getSize()), tMarker.getText_color()).
                build();

        rendererManager.addRenderer(marker_renderer);

        //  建立對應關係
        renderableMap.put(tMarker, marker_renderer);

    }

    private void addAnchorMarker(Position position, Boolean firstTime) {
        //  取得AnchorMarker與ControlMaker
        final Marker marker = AnchorMarker.getInstance();
               Marker linked;
        if(droneHeight != null){
            marker.setSizeColor("7", "#ff0000", "#ffffff");
        }else if(edit_mode) {
            marker.setSizeColor(markerXMLHandler.getMarkers().get(i).getSize(), markerXMLHandler.getMarkers().get(i).getColor(), markerXMLHandler.getMarkers().get(i).getText_color());
            markerXMLHandler.getMarkers().get(i + 1).position.set(new Position(markerXMLHandler.getPositions().get(i + 1).x * birdview.getWidth() - birdview.getWidth() / 2 + draft.layer.getCenter().x,
                    markerXMLHandler.getPositions().get(i + 1).y * birdview.getHeight()  - birdview.getHeight() / 2 + draft.layer.getCenter().y));
            markerXMLHandler.getMarkers().get(i + 1).refreshed_tap_position.set(new Position(markerXMLHandler.getPositions().get(i + 1).x * birdview.getWidth() - birdview.getWidth() / 2 + draft.layer.getCenter().x,
                    markerXMLHandler.getPositions().get(i + 1).y * birdview.getHeight()  - birdview.getHeight() / 2 + draft.layer.getCenter().y));
            ((AnchorMarker) marker).setLink(markerXMLHandler.getMarkers().get(i + 1));
        }else{
            marker.setSizeColor("" + (int)Muninn.getSizeSetting(R.string.key_marker_line_width, R.string.default_marker_line_width),
                    Muninn.getColorSetting(R.string.key_marker_line_color, R.string.default_marker_line_color),
                    Muninn.getColorSetting(R.string.key_marker_text_color, R.string.default_marker_text_color));
        }
        linked = AnchorMarker.getInstance().getLink(); //this link was created by marker.
        if(edit_mode)
            linked.setSizeColor(markerXMLHandler.getMarkers().get(i).getSize(), markerXMLHandler.getMarkers().get(i).getColor(), markerXMLHandler.getMarkers().get(i).getText_color());
        else
            linked.setSizeColor(marker.getSize(),
                    marker.getColor(),
                    marker.getText_color());
        if (renderableMap.containsKey(marker) && renderableMap.containsKey(linked)) {
            rendererManager.removeRenderer(renderableMap.get(marker));
            rendererManager.removeRenderer(renderableMap.get(linked));
        }

        if(droneHeight != null){
            double realD = Double.parseDouble(droneHeight) * Math.tan(droneFOV / 2 / 180 * Math.PI) * 2;
            realD = realD * 16 / Math.pow(337.0, 0.5);
            realD = (double)(Math.round(realD * 1000000)) / 1000000;
            ((AnchorMarker) marker).setRealDistance(realD);
            AnchorMarker.historyDistancesUndo.addLast(realD);
            showToast("已新增比例尺");
            ((AnchorMarker)marker).setPicDisPos(realD, position, new Position(birdview.getWidth() / 2 + draft.layer.getCenter().x, birdview.getHeight() / 2 + draft.layer.getCenter().y));
            //droneHeight = null;
        }else if(!edit_mode) {
            final EditText edit_text = new EditText(context);
            edit_text.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, R.style.dialog);
            final AlertDialog.Builder dialog_builder = new AlertDialog.Builder(contextThemeWrapper);
            dialog_builder.setCancelable(false);
            dialog_builder
                    .setTitle("真實距離")
                    .setView(edit_text)
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Muninn.mVibrator.vibrate(100);
                            String distance_str = edit_text.getText().toString();
                            Log.d(TAG, "distance string=======================================> " + distance_str);
                            if (edit_text.getText().toString().isEmpty() || Double.parseDouble(edit_text.getText().toString()) <= 0) {
                                distance_str = "10";
                                showToast("請輸入有效數字");
                            }
                            ((AnchorMarker) marker).setRealDistance(Double.parseDouble(distance_str));
                            AnchorMarker.historyDistancesUndo.addLast(Double.parseDouble(distance_str)); //新增第一個distance至history
                        }
                    });
            AlertDialog alertDialog = dialog_builder.create();
            alertDialog.show();
            TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
            textView.setTextSize(MuninnActivity.width / 40);
        }else {
            ((AnchorMarker) marker).setRealDistance(((AnchorMarker) markerXMLHandler.getMarkers().get(i)).getRealDistance());
            AnchorMarker.historyDistancesUndo.addLast(((AnchorMarker) markerXMLHandler.getMarkers().get(i)).getRealDistance());
        }
        marker.position.set(position);
        if(droneHeight != null) {
            linked.position.set(new Position(birdview.getWidth() / 2 + draft.layer.getCenter().x, birdview.getHeight() / 2 + draft.layer.getCenter().y));
            droneHeight = null;
        }
        else if(!edit_mode)
            linked.position.set(new Position(position.x + 50, position.y + 50)); //linked位移(x, y) = (50, 50)
        marker.refreshed_tap_position.set(position);
        if(!edit_mode)
            linked.refreshed_tap_position.set(new Position(position.x + 50, position.y + 50)); //linked位移(x, y) = (50, 50)
        ((ControlMarker)linked).setMarker(marker); //tell linked who is his daddy, marker

        draft.addMarker(marker); //add to markerList by MarkerManager
        draft.addMarker(linked); //add to markerList by MarkerManager

        marker.refreshed_Layer_position.set(marker.position);
        marker.historyLayerPositionsUndo.add(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //After getting Layer-position
        linked.refreshed_Layer_position.set(linked.position);
        linked.historyLayerPositionsUndo.add(new Position(linked.refreshed_Layer_position.x, linked.refreshed_Layer_position.y)); //After getting Layer-position

        Position[] positions = {marker.position, linked.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                    setLinkLine((LinkMarker) marker, Float.parseFloat(marker.getSize()), marker.getColor()). //set head and tail
                    setReference(marker). //參考marker
                    setPoint(marker, Float.parseFloat(marker.getSize()), marker.getColor()).
                    setText(new MapString((AnchorMarker) marker), position_list, Float.parseFloat(marker.getSize()), marker.getText_color()).
                    build();

        Renderable link_renderer = mrb.
                setReference(linked).
                build();

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linked, link_renderer);

        //第一次為Create，之後的皆視為Update
        if (firstTime) {
            StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.CREATE));
        } else {
            StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.UPDATE));
        }
    }

    //更新Anchor點的位置
    private void updateAnchorMarker() {
        //  取得AnchorMarker與ControlMaker
        final Marker marker = AnchorMarker.getInstance();
        Marker linked = AnchorMarker.getInstance().getLink(); //this link was created by marker.

        if (renderableMap.containsKey(marker) && renderableMap.containsKey(linked)) {
            rendererManager.removeRenderer(renderableMap.get(marker));
            rendererManager.removeRenderer(renderableMap.get(linked));
        }
        ((AnchorMarker) marker).setRealDistance( ((AnchorMarker)marker).getRealDistance() );

        marker.position.set(marker.refreshed_Layer_position);
        linked.position.set(linked.refreshed_Layer_position);

        draft.addMarkerLayerPosition(marker); //add to markerList by MarkerManager
        draft.addMarkerLayerPosition(linked); //add to markerList by MarkerManager

        Position[] positions = {marker.position, linked.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setLinkLine((LinkMarker) marker, Float.parseFloat(marker.getSize()), marker.getColor()). //set head and tail
                setReference(marker). //參考marker
                setPoint(marker, Float.parseFloat(marker.getSize()), marker.getColor()).
                setText(new MapString((AnchorMarker) marker), position_list, Float.parseFloat(marker.getSize()), marker.getText_color()).
                build();

        Renderable link_renderer = mrb.
                setReference(linked).
                build();

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linked, link_renderer);

    }

    public void removeMarker(Marker marker) {
        if(undoredo){
            StepByStepRedo.clear();
            undoredo = false;
        }
        Log.d(TAG, "removeMarker(Marker marker)");
        if (marker == null) //abstract "Marker" will call one time.
            return;
        if (renderableMap.containsKey(marker)) { //檢查Map是否有此marker，有則刪除
            if(marker.getClass() == AnchorMarker.class) {
                firstTime = true;
                ((AnchorMarker)marker).setRealDistance(((AnchorMarker)marker).getPicRealDistance());
            }
            Log.d(TAG, "renderableMap contain!!");
            Renderable renderable = renderableMap.get(marker); //取得此marker的renderable
            rendererManager.removeRenderer(renderable); //刪除renderObjects裡的render_object
            renderableMap.remove(marker);
            if(readytoSave)
                readytoSave = false;
        }
        draft.removeMarker(marker);
    }

    /*
        (linked)*---------*(marker)
     */

    private void addMeasureMarker(Position position) {
        //  Step1.1:建立 ControlMarkerBuilder 與Step1.2:建立 LinkMarkerBuilder
        ControlMarkerBuilder cb = new ControlMarkerBuilder();
        Marker linked;
        if(!edit_mode)
            linked = cb.
                setPosition(new Position(position.x - 100, position.y)).
                build(); //return Marker
        else
            linked = cb.
                    setPosition(new Position(markerXMLHandler.getPositions().get(i + 1).x * birdview.getWidth() - birdview.getWidth() / 2 + draft.layer.getCenter().x,
                            markerXMLHandler.getPositions().get(i + 1).y * birdview.getHeight() - birdview.getHeight() / 2 + draft.layer.getCenter().y)).build(); //return Marker
        LinkMarkerBuilder lb = new MeasureMarkerBuilder();
        Marker marker = lb.
                setPosition(position).
                setLink(linked). //儲存linked marker，並且告知linked誰是他老爸marker
                build(); //return Marker
        int a = marker.getID();
        marker.setID(linked.getID());
        linked.setID(a);

        if(edit_mode){
            marker.setSizeColor(markerXMLHandler.getMarkers().get(i).getSize(), markerXMLHandler.getMarkers().get(i).getColor(), markerXMLHandler.getMarkers().get(i).getText_color());
        }

        ((ControlMarker)linked).setMarker(marker); //tell linked who is his daddy

        Log.d(TAG, "linked: (" + linked.refreshed_tap_position.x + ", " + linked.refreshed_tap_position.y + ") marker: (" + marker.refreshed_tap_position.x + ", " + marker.refreshed_tap_position.y + ")");

        draft.addMarker(marker);//this will be adjusted the position of marker in draft
        draft.addMarker(linked);//this will be adjusted the position of marker in draft

        marker.refreshed_Layer_position.set(marker.position);
        marker.historyLayerPositionsUndo.add(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //After getting Layer-position
        linked.refreshed_Layer_position.set(linked.position);
        linked.historyLayerPositionsUndo.add(new Position(linked.refreshed_Layer_position.x, linked.refreshed_Layer_position.y)); //After getting Layer-position


        Position[] positions = {marker.position, linked.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  Step2: 建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                    setLinkLine((LinkMarker) marker, Float.parseFloat(marker.getSize()), marker.getColor()).
                    setReference(marker).
                    setPoint(marker, Float.parseFloat(marker.getSize()), marker.getColor()).
                    setText(new MapString((MeasureMarker) marker), position_list, Float.parseFloat(marker.getSize()), marker.getText_color()).
                    build(); //product will be cleared

        Renderable link_renderer = mrb.
                setReference(linked). //create the relationship between marker and render
                build(); //product will be cleared

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linked, link_renderer);

        StepByStepUndo.add(new DataStepByStep(marker, Selectable.CRUD.CREATE));
    }


    private void updateMeasureMarker(DataStepByStep data) {//ISSUE:因改為更新舊有資料，並非新增一個新的MeasureMarker
        Marker marker = data.getMarker();
        Marker linker = ((LinkMarker)marker).getLink();

        if (renderableMap.containsKey(marker) && renderableMap.containsKey(linker)) {
            rendererManager.removeRenderer(renderableMap.get(marker));
            rendererManager.removeRenderer(renderableMap.get(linker));
        }

        marker.position.set(marker.refreshed_Layer_position);
        linker.position.set(linker.refreshed_Layer_position);

        draft.addMarkerLayerPosition(marker); //會取得Layer上的位置
        draft.addMarkerLayerPosition(linker); //會取得Layer上的位置

        Position[] positions = {marker.position, linker.position};
        List<Position> position_list = new ArrayList<Position>(Arrays.asList(positions));

        //  建立MakerRenderer
        MarkerRendererBuilder mrb = new MarkerRendererBuilder();
        Renderable marker_renderer = mrb.
                setLinkLine((LinkMarker) marker, Float.parseFloat(marker.getSize()), marker.getColor()).
                setReference(marker).
                setPoint(marker, Float.parseFloat(marker.getSize()), marker.getColor()).
                setText(new MapString((MeasureMarker) marker), position_list, Float.parseFloat(marker.getSize()), marker.getText_color()).
                build();

        Renderable link_renderer = mrb.
                setReference(linker).
                build();

        rendererManager.addRenderer(marker_renderer);
        rendererManager.addRenderer(link_renderer);

        //  建立對應關係
        renderableMap.put(marker, marker_renderer);
        renderableMap.put(linker, link_renderer);

    }


    public Marker getNearestMarker(Position position) {
        return draft.getNearestMarker(position); //點選時，取得最接近的Marker原件
    }

    /*public Toolbox.Tool getNearestTool(Position position) {
        return toolboxRenderer.getInstance(position, 64); //點選時，取得最接近的tool原件
    }*/

    public void render(Canvas canvas) {
        canvas.save();
        draftRenderer.onDraw(canvas);//畫birdview

        for (Renderable renderable : rendererManager.renderObjects) { //畫上所有物件e.g Line, Anchor, Label and etc.
            renderable.onDraw(canvas);
        }
        canvas.restore();


        //if (toolboxRenderer != null)
            //toolboxRenderer.onDraw(canvas);//畫toolbox

        //if (tool != null) {
        //    Bitmap bitmap = ToolboxRenderer.getToolIcon(tool); //依據tool(key)取得icon resource(value)
        //    canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth(), canvas.getHeight() - bitmap.getHeight(), paint); //將icon放置右下角
        //}
    }

    public void selectTool(Toolbox.Tool tool) {
        preMode = currentMode;
        currentMode = tool;
        if (tool == Toolbox.Tool.CLEAR_PATH) {
            Muninn.sound_Punch.seekTo(0);
            Muninn.sound_Punch.start();
            draft.clearPaths(); //清除草稿線(PATH_MODE)
        }
        else {
            Muninn.sound_Ding.seekTo(0); //重至0毫秒
            Muninn.sound_Ding.start();
            this.tool = tool; //assigned selected component

        }

        switch (tool) {
            case MAKER_TYPE_LINK:
                this.markerType = MeasureMarker.class;
                break;
            case MAKER_TYPE_ANCHOR:
                this.markerType = AnchorMarker.class;
                break;
            case MARKER_TYPE_LABEL:
                this.markerType = LabelMarker.class;
                break;
            case EDIT_UNDO:
                doUndoTask();
                currentMode = preMode;
                selectTool(currentMode);
                break;
            case EDIT_REDO:
                doRedoTask();
                currentMode = preMode;
                selectTool(currentMode);
                break;
            case HAND_MOVE:
                markerType = null;
                break;
            case DELETER:
                markerType = null;
                break;
            case CLEAR_LINE:
                clearAllLine();
                currentMode = preMode;
                selectTool(currentMode);
                break;
            case PATH_MODE:
                markerType = null;
                break;
        }
    }


    private void clearAllLine(){
        for(Marker marker : draft.layer.markerManager.markers){
            if(marker.getClass() == AnchorMarker.class)
                ((AnchorMarker)marker).setRealDistance(0);
        }
        renderableMap.clear();
        rendererManager.renderObjects.clear();
        LinkedList<DataStepByStep> temp = new LinkedList<DataStepByStep>();
        for(DataStepByStep d : StepByStepUndo){
            if(d.getMarker() == null)
                temp.add(d);
        }
        StepByStepRedo.clear();
        StepByStepUndo.clear();
        while(temp.size() > 0)
            StepByStepUndo.add(temp.pollLast());
        draft.layer.markerManager.markers.clear();
        firstTime=true;
        if(readytoSave)
            readytoSave = false;
    }//清除所有標線、標籤
    private void doUndoTask() {
        undoredo = true;
        Log.d(TAG, "EDIT_UNDO2====================================");
        if (!StepByStepUndo.isEmpty()) { //不為空
            if(readytoSave)
                readytoSave = false;
            DataStepByStep data = StepByStepUndo.pollLast();
            Marker dataMarker = data.getMarker();

            switch (data.getCRUDstate()) {
                case CREATE:
                    //do delete
                    undoredo = false;
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "LabelMarker");
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.DELETE));
                        this.removeMarker(dataMarker);
                    } else if (dataMarker instanceof MeasureMarker){ //MeasureMarker, AnchorMarker
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.DELETE));
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    } else if (dataMarker instanceof AnchorMarker) {
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.DELETE));
                        firstTime = true;
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    }else if(dataMarker == null){
                        Log.d(TAG, "鉛筆刪除");
                        Path p = data.getPath();
                        StepByStepRedo.add(new DataStepByStep(p, Selectable.CRUD.DELETE));
                        draft.removePath(p);
                    }
                    undoredo = true;
                    break;
                case UPDATE:
                    //Looking up old data
                    undoredo = false;
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "Undo Update: LabelMarker");
                        Log.d(TAG, "historyLayerPositionsUndo.size(): " + dataMarker.historyLayerPositionsUndo.size());
                        if (dataMarker.historyLayerPositionsUndo.size() > 1) { //第一個是原始位置
                            StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                            this.removeMarker(dataMarker); //刪掉最新的位置renderer，還原前一位置

                            Position redoPositionRef = dataMarker.historyLayerPositionsUndo.pollLast(); //give this to redoLL
                            Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                            dataMarker.historyLayerPositionsRedo.addLast(redoPosition); //!!important!! new one Position
                            dataMarker.refreshed_Layer_position.set(dataMarker.historyLayerPositionsUndo.getLast()); //update tap-position
                            this.updateLabelMarker(data);
                        }

                    } else if (dataMarker instanceof MeasureMarker) { //MeasureMarker
                        Log.d(TAG, "Undo Update: MeasureMarker");
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position redoPositionRef = dataMarker.historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                        dataMarker.historyLayerPositionsRedo.addLast(redoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(dataMarker.historyLayerPositionsUndo.getLast()); //update tap-position

                        //Linked (ControlMarker)
                        Position redoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPositionLink = new Position(redoPositionRefLink.x, redoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.addLast(redoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set( ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.getLast() ); //update tap-position

                        this.updateMeasureMarker(data);

                    } else if (dataMarker instanceof AnchorMarker) { //AnchorMarker
                        Log.d(TAG, "Undo Update: MeasureMarker");
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position redoPositionRef = dataMarker.historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                        dataMarker.historyLayerPositionsRedo.addLast(redoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(dataMarker.historyLayerPositionsUndo.getLast()); //update tap-position

                        //Linked (ControlMarker)
                        Position redoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.pollLast(); //give this to redoLL
                        Position redoPositionLink = new Position(redoPositionRefLink.x, redoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.addLast(redoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set( ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.getLast() ); //update tap-position

                        //deal with Label
                        if (AnchorMarker.historyDistancesUndo.size() > 1) { //防呆避免underflow
                            double redoDistance = AnchorMarker.historyDistancesUndo.pollLast(); //抓取並刪除history最新distance
                            AnchorMarker.historyDistancesRedo.addLast(redoDistance);
                            ((AnchorMarker)dataMarker).setRealDistance(AnchorMarker.historyDistancesUndo.getLast()); //還原history裡上一個distance
                            this.updateAnchorMarker();
                        }

                    }
                    undoredo = true;
                    break;
                case DELETE:
                    //do re-create()
                    if ( dataMarker instanceof MeasureMarker) {
                        this.updateMeasureMarker(data);
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    } else if ( dataMarker instanceof AnchorMarker) {
                        this.updateAnchorMarker();
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));
                    } else if ( dataMarker instanceof LabelMarker) {
                        this.updateLabelMarker(data);
                        StepByStepRedo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    }
                    break;
            }
        }
    }


    private void doRedoTask() {
        undoredo = true;
        Log.d(TAG, "EDIT_REDO2====================================");
        if (!StepByStepRedo.isEmpty()) { //不為空
            if(readytoSave)
                readytoSave = false;
            DataStepByStep data = StepByStepRedo.pollLast();
            Marker dataMarker = data.getMarker();

            switch (data.getCRUDstate()) {
                case CREATE:
                    //do delete();
                    undoredo = false;
                    StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.DELETE));
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "LabelMarker");
                        this.removeMarker(dataMarker);
                    } else if (dataMarker instanceof MeasureMarker){ //MeasureMarker, AnchorMarker
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    } else if (dataMarker instanceof AnchorMarker) {
                        firstTime = true;
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );
                    }
                    undoredo = true;
                    break;
                case UPDATE:
                    //Looking up old data in every marker
                    undoredo = false;
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "Redo: Update: LabelMarker");
                        if (!dataMarker.historyLayerPositionsRedo.isEmpty()) {
                            this.removeMarker(dataMarker); //移除目前的marker，還原至前一位置
                            Position undoPositionRef = dataMarker.historyLayerPositionsRedo.pollLast();
                            Position undoPosition = new Position(undoPositionRef.x, undoPositionRef.y);
                            dataMarker.historyLayerPositionsUndo.addLast(undoPosition);
                            dataMarker.refreshed_Layer_position.set(undoPosition); //update tap-position
                            this.updateLabelMarker(data);
                            StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                        } //if ()

                    } else if (dataMarker instanceof MeasureMarker){ //MeasureMarker
                        Log.d(TAG, "Redo: Update: MeasureMarker");
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position undoPositionRef = dataMarker.historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position undoPosition = new Position(undoPositionRef.x, undoPositionRef.y);
                        dataMarker.historyLayerPositionsUndo.addLast(undoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(undoPosition); //update tap-position

                        //Linked (ControlMarker)
                        Position undoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position undoPositionLink = new Position(undoPositionRefLink.x, undoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.addLast(undoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set(undoPositionLink); //update tap-position

                        this.updateMeasureMarker(data);

                        StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));

                    } else if (dataMarker instanceof AnchorMarker) { //AnchorMarker
                        Log.d(TAG, "Redo: Update: AnchorMarker");
                        this.removeMarker( ((LinkMarker)dataMarker).getLink() );
                        this.removeMarker( dataMarker );

                        Position redoPositionRef = dataMarker.historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position redoPosition = new Position(redoPositionRef.x, redoPositionRef.y);
                        dataMarker.historyLayerPositionsUndo.addLast(redoPosition); //!!important!! new one Position
                        dataMarker.refreshed_Layer_position.set(redoPosition); //update tap-position

                        //Linked (ControlMarker)
                        Position redoPositionRefLink = ((LinkMarker) dataMarker).getLink().historyLayerPositionsRedo.pollLast(); //give this to redoLL
                        Position redoPositionLink = new Position(redoPositionRefLink.x, redoPositionRefLink.y);
                        ((LinkMarker) dataMarker).getLink().historyLayerPositionsUndo.addLast(redoPositionLink); //!!important!! new one Position
                        ((LinkMarker) dataMarker).getLink().refreshed_Layer_position.set(redoPositionLink ); //update tap-position

                        //deal with Label
                        if(AnchorMarker.historyDistancesRedo.size() > 1) {
                            double redoDistance = AnchorMarker.historyDistancesRedo.pollLast(); //抓取並刪除history最新distance
                            AnchorMarker.historyDistancesUndo.addLast(redoDistance);

                            ((AnchorMarker) dataMarker).setRealDistance(redoDistance); //還原history裡上一個distance

                            this.updateAnchorMarker();
                            StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.UPDATE));
                        }

                    }
                    undoredo = true;
                    break;
                case DELETE:
                    //do Re-create()
                    if (dataMarker instanceof LabelMarker) {
                        Log.d(TAG, "REDO: updateLabelMarker()");
                        this.updateLabelMarker(data); //在Redo中使用update來create舊的marker，避免新增新的marker!!若新創marker，會造成後面update與這有關的marker刪除不了之。
                        StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    } else if (dataMarker instanceof AnchorMarker) { //MeasureMarker, AnchorMarker
                        if (firstTime) { //重新create, firstTime為true
                            firstTime = false;
                            this.updateAnchorMarker();
                            StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));
                        }

                        Log.d(TAG, "REDO: updateAnchorMarker()");

                    } else if (dataMarker instanceof MeasureMarker) {
                        this.updateMeasureMarker(data);
                        Log.d(TAG, "REDO: updateMeasureMarker()");
                        StepByStepUndo.add(new DataStepByStep(dataMarker, Selectable.CRUD.CREATE));

                    } else if(dataMarker == null){
                        Log.d(TAG, "鉛筆恢復");
                        Path p = data.getPath();
                        StepByStepUndo.add(new DataStepByStep(p, Selectable.CRUD.CREATE));
                        draft.getPaths().add(p);
                    }
                    break;
            }
        }

    }

    //public void deselectTool() {
    //    this.tool = null;
    //}

    public Toolbox.Tool getTool() {
        return tool;
    }

    public void holdMarker(Marker marker) { //The Marker will be hold after long pressing
        Muninn.sound_Ding.seekTo(0); //重至0毫秒
        Muninn.sound_Ding.start();
        Muninn.mVibrator.vibrate(100);
        markerHold = marker;
    }

    //after releasing the marker by hand
    public void releaseMarker() {
        if (markerHold != null) {
            Muninn.sound_Ding.seekTo(0);
            Muninn.sound_Ding.start();
            if (markerHold instanceof ControlMarker) {
                //LinkedMarker //include Anchor's and Measure's ControlMarker 更新ControlMarker
                Log.d(TAG, "#### Release ControlMarker ####");
                Marker fatherMarker = ((ControlMarker)markerHold).getLinksFatherMarker();

                Marker markerHold_linkedMarker = markerHold;
                DataStepByStep update = new DataStepByStep(fatherMarker, Selectable.CRUD.UPDATE);
                fatherMarker.historyLayerPositionsUndo.addLast(new Position(fatherMarker.historyLayerPositionsUndo.getLast().x, fatherMarker.historyLayerPositionsUndo.getLast().y)); //把最新位置新增至LinkedList最後
                markerHold_linkedMarker.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                markerHold_linkedMarker.historyLayerPositionsUndo.addLast(new Position(markerHold_linkedMarker.refreshed_Layer_position.x, markerHold_linkedMarker.refreshed_Layer_position.y)); //copy original

                if (fatherMarker instanceof AnchorMarker) {
                    AnchorMarker.historyDistancesUndo.addLast(AnchorMarker.historyDistancesUndo.getLast());
                }
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());

                Log.d(TAG, "historySize = (Marker, Linker) = (" + fatherMarker.historyLayerPositionsUndo.size() + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.size() + ")");
                Log.d(TAG, "Marker Position = " + fatherMarker.refreshed_Layer_position.x + ", " + fatherMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Linker Position = " + markerHold_linkedMarker.refreshed_Layer_position.x + ", " + markerHold_linkedMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Undo List: Marker Position = " + fatherMarker.historyLayerPositionsUndo.getLast().x + ", " + fatherMarker.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "Undo List: Linker Position = " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y);Log.d(TAG, "-----------------------------------------------------------------------------");

            } else if (markerHold instanceof MeasureMarker) {
                //MeasureMarker 更新MeasureMarker
                Log.d(TAG, "#### Release MeasureMarker ####");
                Marker markerHold_linkedMarker = ((MeasureMarker) markerHold).getLink();
                DataStepByStep update = new DataStepByStep(markerHold, Selectable.CRUD.UPDATE);

                markerHold.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                markerHold.historyLayerPositionsUndo.addLast(new Position(markerHold.refreshed_Layer_position.x, markerHold.refreshed_Layer_position.y)); //把最新位置新增至LinkedList最後
                markerHold_linkedMarker.historyLayerPositionsUndo.addLast(new Position(markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x, markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y)); //copy original

                Log.d(TAG, "historySize = (Marker, Linker) = (" + markerHold.historyLayerPositionsUndo.size() + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.size() + ")");
                Log.d(TAG, "Marker Position = " + markerHold.refreshed_Layer_position.x + ", " + markerHold.refreshed_Layer_position.y);
                Log.d(TAG, "Linker Position = " + markerHold_linkedMarker.refreshed_Layer_position.x + ", " + markerHold_linkedMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Undo List: Marker Position = " + markerHold.historyLayerPositionsUndo.getLast().x + ", " + markerHold.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "Undo List: Linker Position = " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "-----------------------------------------------------------------------------");
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());

            } else if (markerHold instanceof AnchorMarker) {
                //AnchorMarker
                Log.d(TAG, "#### Release AnchorMarker ####");
                final Marker marker = AnchorMarker.getInstance();
                Marker markerHold_linkedMarker = AnchorMarker.getInstance().getLink(); //this link was created by marker.

                DataStepByStep update = new DataStepByStep(marker, Selectable.CRUD.UPDATE);

                marker.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                marker.historyLayerPositionsUndo.addLast(new Position(marker.refreshed_Layer_position.x, marker.refreshed_Layer_position.y)); //把最新位置新增至LinkedList最後
                markerHold_linkedMarker.historyLayerPositionsUndo.addLast(new Position(markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x, markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y)); //copy original

                AnchorMarker.historyDistancesUndo.addLast(AnchorMarker.historyDistancesUndo.getLast());

                Log.d(TAG, "historySize = (Marker, Linker, Distance) = (" + markerHold.historyLayerPositionsUndo.size() + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.size() + ", " + AnchorMarker.historyDistancesUndo.size() + ")");
                Log.d(TAG, "Marker Position = " + markerHold.refreshed_Layer_position.x + ", " + markerHold.refreshed_Layer_position.y);
                Log.d(TAG, "Linker Position = " + markerHold_linkedMarker.refreshed_Layer_position.x + ", " + markerHold_linkedMarker.refreshed_Layer_position.y);
                Log.d(TAG, "Undo List: Marker Position = " + markerHold.historyLayerPositionsUndo.getLast().x + ", " + markerHold.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "Undo List: Linker Position = " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().x + ", " + markerHold_linkedMarker.historyLayerPositionsUndo.getLast().y);
                Log.d(TAG, "The latest distance = " + AnchorMarker.historyDistancesUndo.getLast());
                Log.d(TAG, "-----------------------------------------------------------------------------");
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());


            } else if (markerHold instanceof LabelMarker) {
                //LabelMarker
                Log.d(TAG, "#### Release LabelMarker ####");

                DataStepByStep update = new DataStepByStep(markerHold, Selectable.CRUD.UPDATE);
                markerHold.refreshed_Layer_position.set(new Position(markerHold.refreshed_tap_position.x, markerHold.refreshed_tap_position.y));
                markerHold.historyLayerPositionsUndo.addLast(new Position(markerHold.refreshed_Layer_position.x, markerHold.refreshed_Layer_position.y)); //把最新位置新增至LinkedList最後
                Log.d(TAG, "historyLayerPositionsUndo Size = " + markerHold.historyLayerPositionsUndo.size());
                Log.d(TAG, "new Position = " + markerHold.refreshed_Layer_position.x + "," + markerHold.refreshed_Layer_position.y);
                StepByStepUndo.add(update);//update the last location of marker
                Log.d(TAG, "StepByStepUndo Size: " + StepByStepUndo.size());
            }



            markerHold = null;
        }

    }

    void dealMeasureMarker(Marker marker, Marker link) {

    }

    public void selectMarker() {
        this.markerSelected = this.markerSelecting; //?Jonas
        this.markerSelecting = null;
        if (this.markerSelected != null) {
            this.markerSelected.select(); //change state
            if(readytoSave)
                readytoSave = false;
        }
    }

    public void deselectMarker() {
        if (this.markerSelected != null)
            this.markerSelected.deselect();
        if (this.markerSelecting != null)
            this.markerSelecting.deselect();
        this.markerSelecting = null;
        this.markerSelected = null;
    }

    public void selectingMarker(Marker marker) {
        if(undoredo){
            StepByStepRedo.clear();
            undoredo = false;
        }
        this.markerSelecting = marker;
        if (this.markerSelecting != null)
            this.markerSelecting.selecting();
    }

    public void setMarkerType(Type type) {
        if (type.toString().contains("Marker"))
           this.markerType = type;
    }

    public void moveHoldMarker(Position position) {
        if (this.markerHold != null) {
            Log.d(TAG, "#### Moving ControlMarker ####");

            draft.moveMarker(markerHold, position);
//            markerHold.refreshed_tap_position = position; //儲存marker移動的位置(螢幕點選的位置)//?Jonas
        }
    }

    double getAngle(Marker A, Marker B) {
        double angle = 0.0;
        double X = A.position.x - B.position.x;
        double Y = A.position.y - B.position.y;
        angle = Math.atan(Y/X);

        return angle;
    }


    public void zoomDraft(float scale_offset) {
        if(tool != Toolbox.Tool.PATH_MODE && markerHold == null) {
            if (scale_offset<0&&this.draft.layer.getScale()<=1) {
                if (scale_offset<0&&this.draft.layer.getScale()<=0.5)
                    this.draft.layer.scale(-0.008f);
                else
                    this.draft.layer.scale(-0.018f);
            }else{
                this.draft.layer.scale(scale_offset);}
          edge();
        }
    }

    public void moveDraft(Position offset) {
        if(tool != Toolbox.Tool.PATH_MODE && markerHold == null){
            this.draft.layer.moveLayer(offset);
            edge();
        }
    }

    /*判斷點是否在圖片內*/
    private boolean enablePosition(Position position){
        if(birdview != null){
        float birdviewHeight=birdview.getHeight()*this.draft.layer.getScale();
        float birdviewWidth=birdview.getWidth()*this.draft.layer.getScale();

        if (abs(position.x-this.draft.layer.getCenter().x)<birdviewWidth / 2   )
            if (abs(position.y-this.draft.layer.getCenter().y)<birdviewHeight / 2 )
                return true;

    } return false;
    }
    private void edge(){//邊界回彈功能
        if(birdview != null){
            float toolbar=120;
            //邊界問題
            float birdviewHeight=birdview.getHeight()*this.draft.layer.getScale();
            float birdviewWidth=birdview.getWidth()*this.draft.layer.getScale();
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int screenHight = dm.heightPixels;
            int screenWidth = dm.widthPixels;
            Log.d(TAG, "moveDraft: 圖width: "+birdviewWidth+"Height"+birdviewHeight+"// screenW"+screenWidth+"screenH"+screenHight);
            Log.d(TAG, "現在圖片中心位置"+ this.draft.layer.getCenterOffset().x+"_"+this.draft.layer.getCenterOffset().y);

            if (birdviewHeight<screenHight-toolbar*2 && birdviewWidth<screenWidth-toolbar*2){//圖片小於螢幕 則鎖在正中間
                Position screenCenter = new Position(0,0);
                this.draft.layer.moveLayerto(screenCenter);
            }else {//大於螢幕

                if(birdviewHeight<screenHight-toolbar*2){//長形圖
                    if (this.draft.layer.getCenterOffset().y!=0){
                        Position over = new Position(this.draft.layer.getCenterOffset().x, 0);
                        this.draft.layer.moveLayerto(over);
                    }
                    //圖片右邊回彈
                    if (this.draft.layer.getCenterOffset().x + birdviewWidth / 2 < screenWidth / 2-toolbar) {
                        Position over = new Position(screenWidth / 2-toolbar - this.draft.layer.getCenterOffset().x - birdviewWidth / 2, 0);
                        this.draft.layer.moveLayer(over);
                    }
                    //圖片左邊回彈
                    if (this.draft.layer.getCenterOffset().x - birdviewWidth / 2 > -screenWidth / 2+toolbar) {

                        Position over = new Position(-screenWidth / 2+toolbar - this.draft.layer.getCenterOffset().x + birdviewWidth / 2, 0);
                        this.draft.layer.moveLayer(over);
                    }}


              if(birdviewWidth<screenWidth-toolbar*2){//寬形圖
                    if (this.draft.layer.getCenterOffset().x!=0){
                        Position over = new Position(0, this.draft.layer.getCenterOffset().y);
                        this.draft.layer.moveLayerto(over);
                    }
                    //圖片下邊回彈
                    if (this.draft.layer.getCenterOffset().y + birdviewHeight / 2 < screenHight / 2-toolbar) {
                        Position over = new Position(0, screenHight / 2-toolbar - this.draft.layer.getCenterOffset().y - birdviewHeight / 2);
                        this.draft.layer.moveLayer(over);
                    }
                    //圖片上邊回彈
                    if (this.draft.layer.getCenterOffset().y - birdviewHeight / 2 > -screenHight / 2+toolbar) {
                        Position over = new Position(0, -screenHight / 2 +toolbar- this.draft.layer.getCenterOffset().y + birdviewHeight / 2);
                        this.draft.layer.moveLayer(over);
                    }
                }

              if (birdviewHeight>screenHight-toolbar*2 && birdviewWidth>screenWidth-toolbar*2){//長寬都大於螢幕
                    //圖片右邊回彈
                    if (this.draft.layer.getCenterOffset().x + birdviewWidth / 2 < screenWidth / 2-toolbar) {
                        Position over = new Position(screenWidth / 2 -toolbar- this.draft.layer.getCenterOffset().x - birdviewWidth / 2, 0);
                        this.draft.layer.moveLayer(over);
                    }
                    //圖片左邊回彈
                    if (this.draft.layer.getCenterOffset().x - birdviewWidth / 2 > -screenWidth / 2+toolbar) {

                        Position over = new Position(-screenWidth / 2+toolbar - this.draft.layer.getCenterOffset().x + birdviewWidth / 2, 0);
                        this.draft.layer.moveLayer(over);
                    }
                    //圖片下邊回彈
                    if (this.draft.layer.getCenterOffset().y + birdviewHeight / 2 < screenHight / 2-toolbar) {
                        Position over = new Position(0, screenHight / 2-toolbar - this.draft.layer.getCenterOffset().y - birdviewHeight / 2);
                        this.draft.layer.moveLayer(over);
                    }
                    //圖片上邊回彈
                    if (this.draft.layer.getCenterOffset().y - birdviewHeight / 2 > -screenHight / 2+toolbar) {
                        Position over = new Position(0, -screenHight / 2+toolbar- this.draft.layer.getCenterOffset().y + birdviewHeight / 2);
                        this.draft.layer.moveLayer(over);
                    }
                }
            }
        }


    }

    private void showToast(String string) {//顯示提示訊息
        int width = Muninn.getContext().getResources().getDisplayMetrics().widthPixels;
        Toast toast = Toast.makeText(Muninn.getContext(), string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0,0);
        LinearLayout linearLayout = (LinearLayout) toast.getView();
        TextView messageTextView = (TextView) linearLayout.getChildAt(0);
        messageTextView.setTextSize(width/40);
        toast.show();
    }

    private File makeDirectory() {
        File directory = new File(Environment.getExternalStorageDirectory(), MUNINN_FILE);
        if(!directory.exists())//創Muninn資料夾
            directory.mkdir();
        if(filename == null)
            return null;
        File new_directory = new File(directory, filename);
        if (!new_directory.exists())
            new_directory.mkdir();
        return new_directory;
    }

    public void showSignPad(Context context) {//簽名
        final SignPad signpad = new SignPad(Muninn.getContext());
        try {
            //String MD5 = "";
            //if(MD5EncoderThread != null && MD5Encoder != null) {//檢查有沒有開啟影像
                //MD5EncoderThread.join();
                //MD5 = MD5Encoder.getResult();
                final File directory = makeDirectory();
                if(directory == null)
                    showToast("請開啟圖片");
                else {
                    new AlertDialog.Builder(context)
                            .setTitle("簽名 請註記")
                            .setView(signpad)
                            .setPositiveButton("儲存", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Muninn.mVibrator.vibrate(100);
                                    if(readytoSave)
                                        readytoSave = false;
                                    final Bitmap bitmap = signpad.exportBitmapRenderedOnCanvas();
                                    Date date = new Date();
                                    SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMddHHmmss");
                                    String filename = date_format.format(date);
                                    try {
                                        File file = new File(directory, "signature_" + filename + ".png");
                                        FileOutputStream output_stream = new FileOutputStream(file);
                                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output_stream);
                                        output_stream.flush();
                                        output_stream.close();
                                        showToast("簽名儲存成功");
                                        signFiles.add(file);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .show();
                }
            //}
            //else {
            //    showToast("請開啟影像", true);
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File exportToDOM(File zip_directory) {
        File file = new File(zip_directory, "data.xml");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            Node node = draft.writeDOM(document);
            document.appendChild(node);
            /*if(MD5EncoderThread != null && MD5Encoder != null) {
                Node root = document.getElementsByTagName("Draft").item(0);
                Node md5_code_tag = document.createElement("code");
                MD5EncoderThread.join();
                md5_code_tag.setTextContent(MD5Encoder.getResult());
                root.appendChild(md5_code_tag);
            }*/
            TransformerFactory transformer_factory = TransformerFactory.newInstance();
            Transformer transformer = transformer_factory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private void WriteDOMFileToZIP(File DOM_file, ZipOutputStream zip_stream, int BUFFER) {
        try {
            byte data[] = new byte[BUFFER];
            FileInputStream file_input = new FileInputStream(DOM_file);
            BufferedInputStream origin = new BufferedInputStream(file_input, BUFFER);
            ZipEntry entry = new ZipEntry(DOM_file.getName());
            zip_stream.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                zip_stream.write(data, 0, count);
            }
            origin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void WriteBitmapToZIP(String filename, Bitmap bitmap, ZipOutputStream zip_stream, int BUFFER, File zip_directory) {
        byte data[] = new byte[BUFFER];
        if (bitmap != null) {
            try {
                File image_file;
                if(filename.lastIndexOf(".png") == -1)
                    image_file = new File(zip_directory, filename + ".jpg");
                else {
                    File temp = new File(zip_directory, this.filename);
                    image_file = new File(temp, filename);
                }
                FileOutputStream bitmap_file = new FileOutputStream(image_file);
                //bitmap.compress(Bitmap.CompressFormat.PNG, 100, bitmap_file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bitmap_file);
                bitmap_file.flush();
                bitmap_file.close();
                FileInputStream file_input = new FileInputStream(image_file);
                BufferedInputStream origin = new BufferedInputStream(file_input, BUFFER);
                ZipEntry entry;
                if(filename.lastIndexOf(".png") == -1)
                    entry = new ZipEntry(filename + ".jpg");
                else
                    entry = new ZipEntry(filename);
                zip_stream.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    zip_stream.write(data, 0, count);
                }
                origin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void exportToZip() {//儲存至本地 用ZIP檔儲存
        if(birdview == null){
            showToast("儲存失敗，請先開啟圖片");
            return;
        }
        readytoSave = true;
        File directory = new File(Environment.getExternalStorageDirectory(), MUNINN_FILE);
        if(!directory.exists())
            directory.mkdir();
        File data_file = exportToDOM(directory);
        Muninn.soundPlayer.start();
        if (data_file.exists()) {
            try {
                Date current_time = new Date();
                SimpleDateFormat simple_date_format = new SimpleDateFormat("yyyyMMddHHmmss");
                String filename = "Draft" + simple_date_format.format(current_time) + ".zip";
                FileOutputStream destination = new FileOutputStream(new File(directory, filename));
                ZipOutputStream zip_stream = new ZipOutputStream(new BufferedOutputStream(destination));
                final int BUFFER = 256;
                WriteDOMFileToZIP(data_file, zip_stream, BUFFER);
                String birdViewFilename;
                if(birdViewUri.getLastPathSegment().indexOf(":") != -1){
                    birdViewFilename = birdViewUri.getLastPathSegment().substring(birdViewUri.getLastPathSegment().indexOf(":") + 1);
                    Log.d("AAAAAAAAAA", birdViewUri.getLastPathSegment());
                    WriteDOMFileToZIP(new File(Environment.getExternalStorageDirectory(), birdViewFilename), zip_stream, BUFFER);
                }else {
                    birdViewFilename = birdViewUri.getPath();
                    WriteDOMFileToZIP(new File(birdViewFilename), zip_stream, BUFFER);
                }
                Log.d("AAAAAAAAAA", birdViewFilename);
                //final Bitmap bitmap = draftRenderer.getBirdview();//標線圖片

                //WriteBitmapToZIP("birdview", bitmap, zip_stream, BUFFER, directory);
                //for(File file : signFiles) {
                //    Bitmap sign_bitmap = BitmapFactory.decodeFile(file.getPath());
                //    WriteBitmapToZIP(file.getName(), sign_bitmap, zip_stream, BUFFER, directory);
                //}

                zip_stream.close();
                destination.close();
                Muninn.soundPlayer.start();//使用裝置本身提示音
                showToast("儲存成功");
                // 上傳雲端在這做。
                UploadParams params = new UploadParams(directory.getPath()+"/"+filename,filename);
                UploadTask uploadtask = new UploadTask();
                if(params.isConnected()) {
                    if(!uploadtask.flag)
                        showToast("上傳失敗");
                    else {
                        uploadtask.execute(params);
                        showToast("FTP上傳成功");
                    }
                }else
                    showToast("上傳失敗，請檢查網路連線");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }else
            showToast("儲存失敗");
    }

    public boolean unpackZip(Uri uri) {
        showToast("壓縮檔讀取中");
        InputStream is;
        ZipInputStream zis;
        File directory;
        try{
            String file_name;
            directory = new File(Environment.getExternalStorageDirectory(), MUNINN_FILE);
            if(!directory.exists())
                return false;
            if(uri.toString().lastIndexOf("Draft") == -1)
                return false;
            File file = new File(directory, uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
            if(!file.exists())
                return false;
            is = new FileInputStream(file);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            while ((ze = zis.getNextEntry()) != null){
                file_name = ze.getName();
                if(file_name.indexOf("jpg") != -1)
                    file_name = "birdview.jpg";
                if (ze.isDirectory()) {
                    File fmd = new File(file, file_name);
                    fmd.mkdirs();
                    continue;
                }
                if(file_name.lastIndexOf("signature") != -1)
                    continue;
                FileOutputStream fout = new FileOutputStream(new File(directory, file_name));
                while ((count = zis.read(buffer)) != -1){
                    fout.write(buffer, 0, count);
                }
                fout.close();
                zis.closeEntry();
            }
            zis.close();
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
        Uri photo = Uri.fromFile(new File(directory.toString() + "/birdview.jpg"));
        setBirdviewImageByUri(photo);
        markerRestore();
        showToast("讀取完畢");
        return true;
    }

    private static class UploadParams
    {
        String directory_path;
        String file_name;
        UploadParams(String directory_path,String file_name)
        {
            this.directory_path = directory_path;
            this.file_name = file_name;
        }
        public boolean isConnected(){
            ConnectivityManager cm = (ConnectivityManager)Muninn.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                return true;
            }
            return false;
        }
    }

    private class UploadTask extends AsyncTask<UploadParams, Void, Void>
    {
        FTPUtils ftpUtils = FTPUtils.getInstance();
        boolean flag = ftpUtils.initFTPSetting(Muninn.getServerSetting(R.string.key_FTP_upload_IP_address, R.string.default_FTP_upload_IP_address),
                21, Muninn.getServerSetting(R.string.key_FTP_upload_username, R.string.default_FTP_upload_username), Muninn.getServerSetting(R.string.key_FTP_upload_password, R.string.default_FTP_upload_password));

        @Override
        protected Void doInBackground(UploadParams... uploadParamses) {
            if(flag)
                ftpUtils.uploadFile(uploadParamses[0].directory_path,uploadParamses[0].file_name);
            return null;
        }
    }
}





