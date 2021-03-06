package studio.bachelor.draft.toolbox;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import studio.bachelor.draft.DraftDirector;

/**
 * Created by BACHELOR on 2016/02/24.
 */
public class Toolbox{
    static private final DraftDirector director = DraftDirector.instance;
    static private final Toolbox instance = new Toolbox();
    static public Toolbox getInstance() {
        return instance;
    }
    public enum Tool {
        DELETER, MAKER_TYPE_LINK, MAKER_TYPE_ANCHOR, MARKER_TYPE_LABEL,
        PATH_MODE, CLEAR_PATH, EDIT_UNDO, EDIT_REDO, HAND_MOVE, CLEAR_LINE,
        SELECT_PHOTO, SETTINGS, UPLOAD_CLOUD, SAVE_DRAFT, SIGNATURE, ERASER
    }
    /**移除標線、自動標線、比例尺、標籤
             草稿線、清除草稿、復原、取消復原、拖曳模式、清除標線
     　　選擇照片、設定、上傳雲端、儲存草稿、簽名、橡皮擦**/

    public final ArrayList<Tool> tools = new ArrayList<>(Arrays.asList(Tool.values()));

    private Toolbox() {

    }
}
