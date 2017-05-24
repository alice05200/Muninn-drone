package studio.bachelor.draft.utility.renderer.layer;

import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import studio.bachelor.draft.utility.Position;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.MuninnActivity;

/**
 * Created by BACHELOR on 2016/03/03.
 */
public class ScaleLayer extends Layer {
    private final String TAG = "ScaleLayer";
    private float currentScale = 1.0f;
    private boolean biggest = false, smallest = false;

    public ScaleLayer(float width, float height) {
        super(width, height);
    }

    @Override
    public Position getPositionOfLayer(final Position screen_position) {
        Position original = super.getPositionOfLayer(screen_position); //取得目前screen與中心點的關係
        Position shift = super.getCenterOffset(); //取得中心點位移
        double x = (original.x - shift.x) / currentScale;
        double y = (original.y - shift.y) / currentScale;
        return new Position(x, y);
    }
    public void scale(float factor) {
        float tmp=currentScale;
        currentScale = currentScale + factor > 0.0f ? (currentScale + factor) : currentScale;//判斷式?true回傳:false回傳
        if (currentScale>3)//盈如 設定了圖片放大縮小的限制
        {
            if(!biggest) {
                Toast toast = Toast.makeText(Muninn.getContext(), "已縮放到最大", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0,0);
                LinearLayout linearLayout = (LinearLayout) toast.getView();
                TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                messageTextView.setTextSize(MuninnActivity.width / 40);
                toast.show();
                biggest = true;
            }
            currentScale=3;
        }else if(currentScale<0.2){
            if(!smallest) {
                Toast toast = Toast.makeText(Muninn.getContext(), "已縮放到最小", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP, 0,0);
                LinearLayout linearLayout = (LinearLayout) toast.getView();
                TextView messageTextView = (TextView) linearLayout.getChildAt(0);
                messageTextView.setTextSize(MuninnActivity.width / 40);
                toast.show();
                smallest = true;
            }
            currentScale=0.2f;
        }else{
            biggest = false;
            smallest = false;
        }
        Log.d(TAG, "currentScale: " + currentScale);
    }

    public float getScale() {
        return currentScale;
    }

    public void setScale(float f){ currentScale = f; }
}
