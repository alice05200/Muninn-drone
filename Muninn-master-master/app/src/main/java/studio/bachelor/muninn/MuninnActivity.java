package studio.bachelor.muninn;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import studio.bachelor.draft.DraftDirector;
import studio.bachelor.draft.toolbox.Toolbox;

public class MuninnActivity extends AppCompatActivity {
    private static final int SELECT_PICTURE = 21101;
    private static final int SELECT_ZIP = 21102;
    private Toolbox.Tool currentTool = Toolbox.Tool.HAND_MOVE, preTool = Toolbox.Tool.HAND_MOVE;
    private RelativeLayout layout = null;
    private int picMode = 0;
    public static int width = 0;
    private ContextThemeWrapper contextThemeWrapper;
    public static TextView message = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_muninn);
        width = Muninn.getContext().getResources().getDisplayMetrics().widthPixels;
        contextThemeWrapper = new ContextThemeWrapper(MuninnActivity.this, R.style.dialog);

        Toast toast = Toast.makeText(getApplicationContext(), "↖請選擇圖片", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        LinearLayout toastView = (LinearLayout) toast.getView();
        TextView messageTextView = (TextView) toastView .getChildAt(0);
        messageTextView.setTextSize(width/40);
        toast.show();

        final Context context = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);//預設為拖曳模式
        findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_2);
        layout = (RelativeLayout)findViewById(R.id.rootLayout);
        layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkMessage();
                return false;
            }
        });
        findViewById(R.id.move_huginn).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Muninn.mVibrator.vibrate(100);
                checkMessage();
                if(appInstalledOrNot("studio.bachelor.huginn")) {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("studio.bachelor.huginn");
                    startActivity(intent);
                }else {
                    showToast("尚未安裝Huginn");
                }
            }
        });
        findViewById(R.id.select_photo).setOnClickListener(new OnClickListener() {//選擇影像
            public void onClick(View view) {//選擇照片
                Muninn.mVibrator.vibrate(100);
                checkMessage();
                if(DraftDirector.instance.getSaveState()) {
                    switchToGallery();
                    DraftDirector.instance.selectTool(currentTool);
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                    builder.setTitle("確定離開")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage(Html.fromHtml("<font color='#ffffff'>尚未儲存更新<br>確定離開？</font>"))
                            .setPositiveButton(R.string.yes_to_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Muninn.mVibrator.vibrate(100);
                                    DraftDirector.instance.setSaveState(true);
                                    switchToGallery();
                                    DraftDirector.instance.selectTool(currentTool);
                                }
                            })
                            .setNeutralButton(R.string.not_to_delete_line, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Muninn.mVibrator.vibrate(100);
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    Window window = alertDialog.getWindow();
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.alpha = 0.7f;
                    window.setAttributes(lp);
                    alertDialog.show();
                    TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                    textView.setTextSize(width / 55);
                    Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setTextColor(Color.WHITE);
                    Button nButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                    nButton.setTextColor(Color.WHITE);
                }
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
                checkMessage();
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
                checkMessage();
                showToast("開始儲存，靜候完成訊息。");
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
                checkMessage();
                showToast("點兩下新增標籤");
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
                v.clearAnimation();
                checkMessage();
                showToast("點兩下新增標線");
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
                checkMessage();
                showToast("點兩下新增比例尺");
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
                checkMessage();
                changeMode(currentTool, Toolbox.Tool.PATH_MODE);
                showToast("鉛筆功能");
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
                checkMessage();
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
                checkMessage();
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
                checkMessage();
                //final PopupMenu popupmenu = new PopupMenu(MuninnActivity.this, findViewById(R.id.delete_button));
                Context wrapper = new ContextThemeWrapper(context, R.style.myPopupMenuStyle);
                PopupMenu popupmenu = new PopupMenu(wrapper, v);
                popupmenu.getMenuInflater().inflate(R.menu.menu, popupmenu.getMenu());
                popupmenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() { // 設定popupmenu項目點擊傾聽者
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.clear_eraser:
                                Muninn.mVibrator.vibrate(100);
                                AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                                builder.setTitle(R.string.sure_to_delete_line)
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setMessage(Html.fromHtml("<font color='#ffffff'>警告：<br>清除所有草稿線後<br>無法復原</font>"))
                                        .setPositiveButton(R.string.yes_to_delete, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Muninn.mVibrator.vibrate(100);
                                                DraftDirector.instance.selectTool(Toolbox.Tool.CLEAR_PATH);
                                                showToast("已清除草稿");
                                            }
                                        })
                                        .setNeutralButton(R.string.not_to_delete_line, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Muninn.mVibrator.vibrate(100);
                                            }
                                        });
                                AlertDialog alertDialog = builder.create();
                                Window window = alertDialog.getWindow();
                                WindowManager.LayoutParams lp = window.getAttributes();
                                lp.alpha = 0.7f;
                                window.setAttributes(lp);
                                //window.setBackgroundDrawableResource(R.drawable.rec_cir);
                                alertDialog.show();
                                TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                                textView.setTextSize(width / 55);
                                Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                pbutton.setTextColor(Color.WHITE);
                                Button nButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                                nButton.setTextColor(Color.WHITE);
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
                                showToast("長按物件的端點移除");
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
                checkMessage();
                changeMode(currentTool, Toolbox.Tool.HAND_MOVE);
                showToast("拖曳模式");
                findViewById(R.id.move_mode).setBackgroundResource(R.drawable.ic_hand_2);
                picMode = 0;
                changePic(preTool);
                DraftDirector.instance.selectTool(Toolbox.Tool.HAND_MOVE);
            }
        });
        findViewById(R.id.select_zip_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Muninn.mVibrator.vibrate(100);
                checkMessage();
                if(DraftDirector.instance.getSaveState()){
                    switchToZIPBrowsing();
                    DraftDirector.instance.selectTool(currentTool);
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
                    builder.setTitle("確定離開")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setMessage(Html.fromHtml("<font color='#ffffff'>尚未儲存更新<br>確定離開？</font>"))
                            .setPositiveButton(R.string.yes_to_delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Muninn.mVibrator.vibrate(100);
                                    DraftDirector.instance.setSaveState(true);
                                    switchToZIPBrowsing();
                                    DraftDirector.instance.selectTool(currentTool);
                                }
                            })
                            .setNeutralButton(R.string.not_to_delete_line, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Muninn.mVibrator.vibrate(100);
                                }
                            });
                    // instantiate the dialog with the custom Theme
                    AlertDialog alertDialog = builder.create();
                    Window window = alertDialog.getWindow();
                    WindowManager.LayoutParams lp = window.getAttributes();
                    lp.alpha = 0.7f;
                    window.setAttributes(lp);
                    alertDialog.show();
                    TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                    textView.setTextSize(width / 55);
                    Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    pbutton.setTextColor(Color.WHITE);
                    Button nButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
                    nButton.setTextColor(Color.WHITE);
                }
            }
        });
        findViewById(R.id.select_zip_button).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    v.setBackgroundResource(R.drawable.ic_zip_2);
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    v.setBackgroundResource(R.drawable.ic_zip_1);
                }
                return false;
            }
        });

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   //確定按下退出鍵
            ConfirmExit(); //呼叫ConfirmExit()函數
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    public void ConfirmExit(){
        AlertDialog.Builder ad = new AlertDialog.Builder(contextThemeWrapper);
        ad.setTitle("確定離開？")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(Html.fromHtml("<font color='#ffffff'>離開後將不會<br>儲存圖片</font>"))
                .setPositiveButton("是", new DialogInterface.OnClickListener() { //按"是",則退出應用程式
            public void onClick(DialogInterface dialog, int i) {
                MuninnActivity.this.finish();//關閉activity
            }
        });
        ad.setNegativeButton("否",new DialogInterface.OnClickListener() { //按"否",則不執行任何操作
            public void onClick(DialogInterface dialog, int i) {
            }
        });
        AlertDialog alertDialog = ad.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.7f;
        window.setAttributes(lp);
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(width / 55);
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.WHITE);
        Button nButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nButton.setTextColor(Color.WHITE);
    }
    /*清除標線警告dialog*/
    private void ClearLineDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(contextThemeWrapper);
        builder.setTitle(R.string.sure_to_delete_line)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(Html.fromHtml("<font color='#ffffff'>警告：<br>清除所有標線標籤<br>後無法復原</font>"))
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
                });
        final AlertDialog alertDialog = builder.create();
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.alpha = 0.7f;
        window.setAttributes(lp);
        alertDialog.show();
        TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
        textView.setTextSize(width / 55);
        Button pbutton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
        pbutton.setTextColor(Color.WHITE);
        Button nButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);
        nButton.setTextColor(Color.WHITE);
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
        startActivityForResult(Intent.createChooser(intent, "請選擇ZIP檔"), SELECT_ZIP);
    }

    private void switchToSetting() {
        Intent act = new Intent(getApplicationContext(), SettingActivity.class);
        startActivity(act);
    }

    @Override
    protected void onDestroy() {
        DraftDirector.instance.cleanBirdviewLine();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri uri = data.getData();
                DraftDirector.instance.setBirdviewImageByUri(uri);
                message = (TextView)findViewById(R.id.function_message);
                message.setTextSize(width / 55);
                message.setVisibility(View.VISIBLE);
                final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
                animation.setDuration(500); // duration - half a second
                animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
                animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
                animation.setRepeatMode(Animation.REVERSE);
                final ImageButton btn = (ImageButton) findViewById(R.id.auto_button);
                btn.startAnimation(animation);
                /*Toast toast = Toast.makeText(getApplicationContext(), "請選擇功能編輯↓", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                LinearLayout toastView = (LinearLayout) toast.getView();
                TextView messageTextView = (TextView) toastView .getChildAt(0);
                messageTextView.setTextSize(width/40);
                toast.show();*/
            }
            else if(requestCode == SELECT_ZIP) {
                Uri uri = data.getData();
                if(!DraftDirector.instance.unpackZip(uri))
                    showToast("壓縮檔讀取失敗");
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
    private void showToast(String string){
        Toast toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0,0);
        LinearLayout linearLayout = (LinearLayout) toast.getView();
        TextView messageTextView = (TextView) linearLayout.getChildAt(0);
        messageTextView.setTextSize(width / 40);
        toast.show();
    }
    private void checkMessage(){
        if(message != null) {
            if (message.getVisibility() == View.VISIBLE) {
                message.setVisibility(View.INVISIBLE);
            }
        }
    }
}
