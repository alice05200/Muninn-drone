package studio.bachelor.muninn;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.DraftView;
import studio.bachelor.draft.marker.AnchorMarker;
import studio.bachelor.draft.marker.LabelMarker;
import studio.bachelor.draft.marker.MeasureMarker;
import studio.bachelor.draft.toolbox.Toolbox;

public class MuninnActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 21101;
    private static final int SELECT_ZIP = 21102;
    private ImageView current_mode;
    private Bitmap b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muninn);
        final Context context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        current_mode = (ImageView)findViewById(R.id.current_mode);
        DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);//預設為拖曳模式

        findViewById(R.id.select_photo).setOnClickListener(new OnClickListener() {//選擇影像
            public void onClick(View view) {//選擇照片
                switchToGallery();
            }
        });
        findViewById(R.id.setting).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {//參數設定
                switchToSetting();
            }
        });
        findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//儲存至本地
                DraftDirector.instance.exportToZip();
                b = getScreenShot();
                Date date = new Date();
                SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMddHHmmss");
                String filename = date_format.format(date);
                try {
                    File file = new File(Environment.getExternalStorageDirectory() + "/zip_file", "B" +  filename + ".png");
                    FileOutputStream output_stream = new FileOutputStream(file);
                    b.compress(Bitmap.CompressFormat.PNG, 100, output_stream);
                    output_stream.flush();
                    output_stream.close();
                    Toast.makeText(getApplicationContext(), "B", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        findViewById(R.id.sign).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//簽名
                DraftDirector.instance.showSignPad(context);
            }
        });
        findViewById(R.id.upload).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//上傳至雲端
                switchToZIPBrowsing();
            }
        });
        findViewById(R.id.btnSound).setSoundEffectsEnabled(false);//不履行預設button音效
        findViewById(R.id.btnSound).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Muninn.sound_Ding.seekTo(0);
                Muninn.sound_Ding.start();
            }
        });
        findViewById(R.id.label_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//標籤
                DraftDirector.instance.selectTool(Toolbox.Tool.MARKER_TYPE_LABEL);
                current_mode.setImageResource(R.drawable.ic_textsms_black_48dp);
            }
        });
        findViewById(R.id.auto_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//自動標線
                DraftDirector.instance.selectTool(Toolbox.Tool.MAKER_TYPE_LINK);
                current_mode.setImageResource(R.drawable.ic_place_black_48dp);
            }
        });
        findViewById(R.id.line_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//標線
                DraftDirector.instance.selectTool(Toolbox.Tool.MAKER_TYPE_ANCHOR);
                current_mode.setImageResource(R.drawable.ic_rate_review_black_48dp);
            }
        });
        findViewById(R.id.pen_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//草稿線
                DraftDirector.instance.selectTool(Toolbox.Tool.PATH_MODE);
                current_mode.setImageResource(R.drawable.ic_gesture_black_48dp);
            }
        });
        findViewById(R.id.clear_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//清除草稿
                DraftDirector.instance.selectTool(Toolbox.Tool.CLEAR_PATH);
            }
        });
        findViewById(R.id.redo_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//取消復原
                DraftDirector.instance.selectTool(Toolbox.Tool.EDIT_REDO);
            }
        });
        findViewById(R.id.undo_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//復原
                DraftDirector.instance.selectTool(Toolbox.Tool.EDIT_UNDO);
            }
        });
        findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//刪除特定標線標籤
                DraftDirector.instance.selectTool(Toolbox.Tool.DELETER);
                current_mode.setImageResource(R.drawable.ic_delete_forever_black_48dp);
            }
        });
        findViewById(R.id.move_mode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//拖曳模式
                DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);
                current_mode.setImageResource(R.drawable.ic_hand_48dp);
            }
        });
    }

    private void switchToGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_photo_string)), SELECT_PICTURE);
    }

    private void switchToZIPBrowsing() {
        Intent intent = new Intent();
        intent.setType("application/zip");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, SELECT_ZIP);
    }

    private void switchToSetting() {
        Intent act = new Intent(getApplicationContext(), SettingActivity.class);
        startActivity(act);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri uri = data.getData();
                DraftDirector.instance.setBirdviewImageByUri(uri);
            }
            else if(requestCode == SELECT_ZIP) {
                Uri uri = data.getData();
                DraftDirector.instance.uploadToServer(uri);
            }
        }
    }
    private Bitmap getScreenShot()
    {
        //藉由View來Cache全螢幕畫面後放入Bitmap
        View mView = getWindow().getDecorView();
        mView.setDrawingCacheEnabled(true);
        mView.buildDrawingCache();
        Bitmap mFullBitmap = mView.getDrawingCache();

        //取得系統狀態列高度
        Rect mRect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(mRect);
        int mStatusBarHeight = mRect.top;

        //取得手機螢幕長寬尺寸
        int mPhoneWidth = getWindowManager().getDefaultDisplay().getWidth();
        int mPhoneHeight = getWindowManager().getDefaultDisplay().getHeight();

        //將狀態列的部分移除並建立新的Bitmap
        Bitmap mBitmap = Bitmap.createBitmap(mFullBitmap, 0, mStatusBarHeight, mPhoneWidth, mPhoneHeight - mStatusBarHeight);
        //將Cache的畫面清除
        mView.destroyDrawingCache();

        return mBitmap;
    }
}
