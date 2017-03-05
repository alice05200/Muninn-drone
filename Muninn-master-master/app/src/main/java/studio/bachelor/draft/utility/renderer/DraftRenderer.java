package studio.bachelor.draft.utility.renderer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import studio.bachelor.draft.Draft;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.muninn.Muninn;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class DraftRenderer implements Renderable {
    private Draft draft;
    private Bitmap birdview;
    private final Paint paint = new Paint(); //for image
    private final Paint pathPaint = new Paint(); //for path(草稿線)
    private Paint eraserPaint = new Paint();
    private boolean ableToPaint = true;
    private boolean toMiddle = true;

    { //path會依據Paint的設定，呈現不同線條
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeWidth(5.0f);
        pathPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setAlpha(0);
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        eraserPaint.setAntiAlias(true);
        eraserPaint.setDither(true);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeJoin(Paint.Join.ROUND);
        eraserPaint.setStrokeWidth(5.0f);
    }

    public DraftRenderer(Draft draft) {
        this.draft = draft;
    }

    public void setBirdview(Uri uri) {
        try {
            birdview = MediaStore.Images.Media.getBitmap(Muninn.getContext().getContentResolver(), uri);
        } catch (Exception e) {
            Log.d("DraftRenderer", "setBirdview(Uri uri)" + e.toString());
        }
    }

    public void setBirdview(Bitmap bitmap) {
        birdview = bitmap;
    }

    public void setAbleToPaint(boolean b){ableToPaint = b;}

    public boolean getAbleToPaint(){return ableToPaint;}

    public Bitmap getBirdview() {
        return birdview;
    }

    public void onDraw(Canvas canvas) {
        Position translate = draft.layer.getTranslate();//?Jonas
        float scale = draft.layer.getScale();
        canvas.translate((float)translate.x, (float)translate.y);//位移
        canvas.scale(scale, scale);
        if(birdview != null)
            canvas.drawBitmap(birdview, -birdview.getWidth() / 2, -birdview.getHeight() / 2, paint);

        Path current_path = draft.getCurrentPath();
        if(current_path != null)
            canvas.drawPath(current_path, pathPaint);//?Jonas

        List<Path> paths = draft.getPaths();
        for (Path path : paths) //show all the gesture paths
            canvas.drawPath(path, pathPaint);
    }
    public Bitmap getDraftBitmap() {//儲存畫上草稿線的圖
             if (birdview != null) {
                 Bitmap bmp = Bitmap.createBitmap(birdview.getWidth(), birdview.getHeight(), Bitmap.Config.ARGB_8888);
                 Canvas tC = new Canvas(bmp);
                 tC.drawBitmap(birdview, null, new Rect(0, 0, birdview.getWidth(), birdview.getHeight()), paint);//將背景圖加入Canvas
                 tC.translate(birdview.getWidth() / 2, birdview.getHeight() / 2);
                 //將全部繪製的路線加入Canvas
                 List<Path> paths = draft.getPaths();
                 for (Path path : paths) //show all the gesture paths
                     tC.drawPath(path, pathPaint);
                 Toast.makeText(Muninn.getContext(), "草稿線儲存成功", Toast.LENGTH_SHORT).show();
                 return bmp;
             } else if (draft.getPaths().isEmpty()) {
                     return null;
             } else {
                 Bitmap bmp = Bitmap.createBitmap(birdview.getWidth(), birdview.getHeight(), Bitmap.Config.ARGB_8888);
                 Canvas c = new Canvas(bmp);
                 List<Path> paths = draft.getPaths();
                 for (Path path : paths) //show all the gesture paths
                     c.drawPath(path, pathPaint);
                 return bmp;
             }
         }
}
