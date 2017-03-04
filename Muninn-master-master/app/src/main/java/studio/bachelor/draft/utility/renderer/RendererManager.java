package studio.bachelor.draft.utility.renderer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import studio.bachelor.draft.Draft;
import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.marker.AnchorMarker;
import studio.bachelor.draft.marker.LinkMarker;
import studio.bachelor.draft.utility.MapString;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.draft.utility.renderer.builder.MarkerRendererBuilder;

/**
 * Created by BACHELOR on 2016/02/25.
 */
public class RendererManager {
    private final String TAG = "Rendermanager";
    private static final DraftDirector director = DraftDirector.instance;
    private static final RendererManager instance = new RendererManager();
    public static RendererManager getInstance() {
        return instance;
    }
    private Bitmap bitmap;
    private Paint paint = new Paint();


    public final List<Renderable> renderObjects = new LinkedList<Renderable>();


    private RendererManager() {

    }
    public void setBitmap(Bitmap b){
        bitmap = b;
    }

    public void addRenderer(final Renderable render_object) {
        if(render_object != null && !renderObjects.contains(render_object)) {
            Log.d(TAG, "addRenderer()");
            renderObjects.add(render_object);
        }

    }

    public void removeRenderer(final Renderable render_object) {
        if(render_object != null) {
            renderObjects.remove(render_object);
            Log.d(TAG, "removeRenderer()");
        }

    }

    public Bitmap getLineDraft(){
        Bitmap bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas tC = new Canvas(bmp);
        tC.drawBitmap(bitmap, null, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), paint);
        for(Renderable r : renderObjects)
            r.onDraw(tC);
        return bmp;
    }
}
