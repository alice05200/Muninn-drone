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
import android.view.MotionEvent;
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
    private Toolbox.Tool currentTool = Toolbox.Tool.HAND_MOVE, preTool = Toolbox.Tool.HAND_MOVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muninn);
        final Context context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);//預設為拖曳模式
        findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_2);

        findViewById(R.id.select_photo).setOnClickListener(new OnClickListener() {//選擇影像
            public void onClick(View view) {//選擇照片
                cleanAll();
                switchToGallery();
                Toast.makeText(getApplicationContext(), "請選擇照片", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.select_photo).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_gallery_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_gallery_1);
                }
                return false;
            }
        });
        findViewById(R.id.setting).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {//參數設定
                switchToSetting();
            }
        });
        findViewById(R.id.setting).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_setting_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_setting_1);
                }
                return false;
            }
        });
        findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//儲存至本地
                DraftDirector.instance.exportToZip();
            }
        });
        findViewById(R.id.save).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_save_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_save_1);
                }
                return false;
            }
        });
        findViewById(R.id.sign).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//簽名
                changeMode(currentTool, Toolbox.Tool.SIGNATURE);
                findViewById(R.id.sign).setBackgroundResource(R.drawable.ic_sign_2);
                changePic(preTool);
                DraftDirector.instance.showSignPad(context);
                changeMode(currentTool, preTool);
                changePic(preTool);
            }
        });
        findViewById(R.id.upload).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//上傳至雲端
                switchToZIPBrowsing();
            }
        });
        findViewById(R.id.upload).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_upload_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_upload_1);
                }
                return false;
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
                changeMode(currentTool, Toolbox.Tool.MARKER_TYPE_LABEL);
                findViewById(R.id.label_button).setBackgroundResource(R.drawable.ic_text_2);
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.MARKER_TYPE_LABEL);
            }
        });
        findViewById(R.id.auto_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//自動標線
                changeMode(currentTool, Toolbox.Tool.MAKER_TYPE_LINK);
                findViewById(R.id.auto_button).setBackgroundResource(R.drawable.ic_distance_auto_2);
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.MAKER_TYPE_LINK);
            }
        });
        findViewById(R.id.line_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//標線
                changeMode(currentTool, Toolbox.Tool.MAKER_TYPE_ANCHOR);
                findViewById(R.id.line_button).setBackgroundResource(R.drawable.ic_distance_2);
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.MAKER_TYPE_ANCHOR);
            }
        });
        findViewById(R.id.pen_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//草稿線
                changeMode(currentTool, Toolbox.Tool.PATH_MODE);
                findViewById(R.id.pen_button).setBackgroundResource(R.drawable.ic_pencil_2);
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.PATH_MODE);
            }
        });
        findViewById(R.id.clear_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//清除草稿
                DraftDirector.instance.selectTool(Toolbox.Tool.CLEAR_PATH);
            }
        });
        findViewById(R.id.clear_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_erase_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_erase_1);
                }
                return false;
            }
        });
        findViewById(R.id.redo_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//取消復原
                DraftDirector.instance.selectTool(Toolbox.Tool.EDIT_REDO);
            }
        });
        findViewById(R.id.redo_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_redo_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_redo_1);
                }
                return false;
            }
        });
        findViewById(R.id.undo_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//復原
                DraftDirector.instance.selectTool(Toolbox.Tool.EDIT_UNDO);
            }
        });
        findViewById(R.id.undo_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_undo_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_undo_1);
                }
                return false;
            }
        });
        findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//刪除特定標線標籤
                changeMode(currentTool, Toolbox.Tool.DELETER);
                findViewById(R.id.delete_button).setBackgroundResource(R.drawable.ic_delete_2);
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.DELETER);
                Toast.makeText(getApplicationContext(), "長按物件點移除", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.move_mode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//拖曳模式
                changeMode(currentTool, Toolbox.Tool.HAND_MOVE);
                findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_2);
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);
            }
        });
        findViewById(R.id.line_restart_button).setOnClickListener(new OnClickListener() {//清除標線
            @Override
            public void onClick(View v) {
                DraftDirector.instance.selectTool(Toolbox.Tool.CLEAR_LINE);
            }
        });
        findViewById(R.id.line_restart_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_refresh_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_refresh_1);
                }
                return false;
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

    public void cleanAll(){
        DraftDirector.instance.selectTool(Toolbox.Tool.CLEAR_PATH);
        DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);
    }
    public void changeMode(Toolbox.Tool tool1, Toolbox.Tool tool2){
        preTool = tool1;
        currentTool = tool2;
    }
    public void changePic(Toolbox.Tool tool){
        if(preTool != currentTool) {
            switch (tool) {
                case DELETER:
                    findViewById(R.id.delete_button).setBackgroundResource(R.drawable.ic_delete_1);
                    break;
                case MAKER_TYPE_LINK:
                    findViewById(R.id.auto_button).setBackgroundResource(R.drawable.ic_distance_auto_1);
                    break;
                case MAKER_TYPE_ANCHOR:
                    findViewById(R.id.line_button).setBackgroundResource(R.drawable.ic_distance_1);
                    break;
                case MARKER_TYPE_LABEL:
                    findViewById(R.id.label_button).setBackgroundResource(R.drawable.ic_text_1);
                    break;
                case PATH_MODE:
                    findViewById(R.id.pen_button).setBackgroundResource(R.drawable.ic_pencil_1);
                    break;
                case CLEAR_PATH:
                    break;
                case HAND_MOVE:
                    findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_1);
                    break;
                case SIGNATURE:
                    findViewById(R.id.sign).setBackgroundResource(R.drawable.ic_sign_1);
                    break;
            }
        }
    }
}
