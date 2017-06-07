package studio.bachelor.draft.utility.renderer.primitive;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.utility.Position;
import studio.bachelor.draft.utility.Renderable;
import studio.bachelor.muninn.Muninn;
import studio.bachelor.muninn.R;

/**
 * Created by BACHELOR on 2016/03/01.
 */
public class Point implements Renderable {
    private float radius;
    public final Position position;
    public final Paint paint = new Paint();

    {
        radius = Muninn.getSizeSetting(R.string.key_marker_line_width, R.string.default_marker_line_width) * 5 / 3;
        String color = Muninn.getColorSetting(R.string.key_marker_line_color, R.string.default_marker_line_color);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.parseColor(color));
        paint.setAlpha(180);
        setRadius(radius);
    }

    public Point() {
        position = new Position();
    }

    public Point(Position position) {
        this.position = position;
    }

    public Point(Position position, float size, String color){
        this.position = position;
        radius = size * 5 / 3;
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.parseColor(color));
        paint.setAlpha(180);
        setRadius(radius);
    }

    public void setRadius(float radius) {
        this.radius = radius;
        paint.setStrokeWidth(this.radius * 2);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawPoint((float)position.x, (float)position.y, paint);
    }
}
