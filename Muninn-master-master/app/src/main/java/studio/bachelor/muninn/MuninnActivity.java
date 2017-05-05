package studio.bachelor.muninn;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.toolbox.Toolbox;

public class MuninnActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 21101;
    private static final int SELECT_ZIP = 21102;
    private Toolbox.Tool currentTool = Toolbox.Tool.HAND_MOVE, preTool = Toolbox.Tool.HAND_MOVE;
    private int picMode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muninn);


        Toast toast = Toast.makeText(getApplicationContext(), "  請選擇圖片", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.LEFT | Gravity.TOP,-20,50);
        LinearLayout toastView = (LinearLayout) toast.getView();
        toastView.setBackgroundColor(getResources().getColor(R.color.none));
        ImageView imageCodeProject = new ImageView(getApplicationContext());
        imageCodeProject.setImageResource(R.drawable.ic_up);
        toastView.addView(imageCodeProject, 0);
        toast.show();


        final Context context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);//預設為拖曳模式
        findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_2);
        findViewById(R.id.move_huginn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Muninn.mVibrator.vibrate(100);
                if(appInstalledOrNot("studio.bachelor.huginn")) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("studio.bachelor.huginn");
                    startActivity(intent);
                }else
                    Toast.makeText(getApplicationContext(), "尚未安裝Huginn", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.select_photo).setOnClickListener(new OnClickListener() {//選擇影像
            public void onClick(View view) {//選擇照片
                Muninn.mVibrator.vibrate(100);
                switchToGallery();
                DraftDirector.instance.selectTool(currentTool);

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
                Muninn.mVibrator.vibrate(100);
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
                Muninn.mVibrator.vibrate(100);
                Toast.makeText(getApplicationContext(), "開始儲存，靜候完成訊息。", Toast.LENGTH_SHORT).show();
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
                Muninn.mVibrator.vibrate(100);

                DraftDirector.instance.showSignPad(context);
            }
        });
        findViewById(R.id.sign).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_sign_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_sign_1);
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
                Muninn.mVibrator.vibrate(100);
                Toast.makeText(getApplicationContext(), "點兩下新增標籤", Toast.LENGTH_SHORT).show();
                changeMode(currentTool, Toolbox.Tool.MARKER_TYPE_LABEL);
                findViewById(R.id.label_button).setBackgroundResource(R.drawable.ic_text_2);
                picMode = 0;
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.MARKER_TYPE_LABEL);
            }
        });
        findViewById(R.id.auto_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//自動標線
                Muninn.mVibrator.vibrate(100);
                Toast.makeText(getApplicationContext(), "點兩下新增標線", Toast.LENGTH_SHORT).show();
                changeMode(currentTool, Toolbox.Tool.MAKER_TYPE_LINK);
                findViewById(R.id.auto_button).setBackgroundResource(R.drawable.ic_distance_auto_2);
                picMode = 0;
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.MAKER_TYPE_LINK);
            }
        });
        findViewById(R.id.line_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//標線
                Muninn.mVibrator.vibrate(100);
                Toast.makeText(getApplicationContext(), "點兩下新增比例尺", Toast.LENGTH_SHORT).show();
                changeMode(currentTool, Toolbox.Tool.MAKER_TYPE_ANCHOR);
                findViewById(R.id.line_button).setBackgroundResource(R.drawable.ic_distance_2);
                picMode = 0;
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.MAKER_TYPE_ANCHOR);
            }
        });
        findViewById(R.id.pen_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//草稿線
                Muninn.mVibrator.vibrate(100);
                changeMode(currentTool, Toolbox.Tool.PATH_MODE);
                Toast.makeText(getApplicationContext(), "鉛筆功能", Toast.LENGTH_SHORT).show();
                findViewById(R.id.pen_button).setBackgroundResource(R.drawable.ic_pencil_2);
                picMode = 0;
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.PATH_MODE);
            }
        });
        findViewById(R.id.redo_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//取消復原
                Muninn.mVibrator.vibrate(100);
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
                Muninn.mVibrator.vibrate(100);
                
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
                Muninn.mVibrator.vibrate(100);
                final PopupMenu popupmenu = new PopupMenu(MuninnActivity.this, findViewById(R.id.delete_button));
                popupmenu.getMenuInflater().inflate(R.menu.menu, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() { // 設定popupmenu項目點擊傾聽者
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.clear_eraser:
                                Muninn.mVibrator.vibrate(100);
                                DraftDirector.instance.selectTool(Toolbox.Tool.CLEAR_PATH);
                                Toast.makeText(getApplicationContext(), "已清除草搞", Toast.LENGTH_SHORT).show();
                                break;
                            case R.id.clear_line:
                                Muninn.mVibrator.vibrate(100);
                                ClearLineDialog();
                                break;
                            case R.id.delete_line:
                                Muninn.mVibrator.vibrate(100);
                                DraftDirector.instance.selectTool(Toolbox.Tool.DELETER);
                                changeMode(currentTool, Toolbox.Tool.DELETER);
                                findViewById(R.id.delete_button).setBackgroundResource(R.drawable.ic_delete_2);
                                picMode = 0;
                                changePic(preTool);
                                Toast.makeText(getApplicationContext(), "長按物件點移除", Toast.LENGTH_SHORT).show();
                                break;
                        }
                        return true;
                    }
                });
                popupmenu.show();
            }
        });
        findViewById(R.id.delete_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_delete_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_delete_1);
                }
                return false;
            }
        });
        findViewById(R.id.move_mode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {//拖曳模式
                Muninn.mVibrator.vibrate(100);
                changeMode(currentTool, Toolbox.Tool.HAND_MOVE);
                Toast.makeText(getApplicationContext(), "拖曳模式", Toast.LENGTH_SHORT).show();
                findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_2);
                picMode = 0;
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);
            }
        });
        /*findViewById(R.id.line_restart_button).setOnClickListener(new OnClickListener() {//清除標線
            @Override
            public void onClick(View v) {
                ClearLineDialog();//警告
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
        });*/

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    /*清除標線警告dialog*/
    private void ClearLineDialog(){
        new AlertDialog.Builder(MuninnActivity.this)
                .setTitle(R.string.sure_to_delete_line)
                .setMessage(R.string.alert_delete_line)
                .setPositiveButton(R.string.yes_to_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Muninn.mVibrator.vibrate(100);
                        DraftDirector.instance.selectTool(Toolbox.Tool.CLEAR_LINE);
                    }
                })
                .setNeutralButton(R.string.not_to_delete_line, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Muninn.mVibrator.vibrate(100);
                    }
                })
                .show();
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
    public void changeMode(Toolbox.Tool tool1, Toolbox.Tool tool2){
        preTool = tool1;
        currentTool = tool2;
    }
    public void changePic(Toolbox.Tool tool){
        if(preTool != currentTool) {
            if(picMode == 0) {
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
                    case HAND_MOVE:
                        findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_1);
                        break;
                    case SIGNATURE:
                        findViewById(R.id.sign).setBackgroundResource(R.drawable.ic_sign_1);
                        break;
                }
            }else if(picMode == 1){
                switch (tool) {
                    case DELETER:
                        findViewById(R.id.delete_button).setBackgroundResource(R.drawable.ic_delete_2);
                        break;
                    case MAKER_TYPE_LINK:
                        findViewById(R.id.auto_button).setBackgroundResource(R.drawable.ic_distance_auto_2);
                        break;
                    case MAKER_TYPE_ANCHOR:
                        findViewById(R.id.line_button).setBackgroundResource(R.drawable.ic_distance_2);
                        break;
                    case MARKER_TYPE_LABEL:
                        findViewById(R.id.label_button).setBackgroundResource(R.drawable.ic_text_2);
                        break;
                    case PATH_MODE:
                        findViewById(R.id.pen_button).setBackgroundResource(R.drawable.ic_pencil_2);
                        break;
                    case HAND_MOVE:
                        findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_2);
                        break;
                    case SIGNATURE:
                        findViewById(R.id.sign).setBackgroundResource(R.drawable.ic_sign_2);
                        break;
                }
            }
        }
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return false;
    }
}
