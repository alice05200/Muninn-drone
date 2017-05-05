package studio.bachelor.muninn;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

/**
 * Created by BACHELOR on 2016/03/14.
 */
public class SettingActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        SharedPreferences getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String current_color = Muninn.getColorSetting(R.string.key_marker_line_color, R.string.default_marker_line_color);
        String background_chooser = getPrefs.getString("@string/key_marker_line_color", current_color);
        Log.d("AAAAAA", background_chooser);
        View view = this.getCurrentFocus();
        if (background_chooser.equals("#ffffff")) {
            view.setBackgroundColor(Color.WHITE);
        } else if (background_chooser.equals("#ff0000")) {
            view.setBackgroundColor(Color.RED);
        } else if (background_chooser.equals("#0000ff")) {
            view.setBackgroundColor(Color.BLUE);
        } else if(background_chooser.equals("#00ff00")){
            view.setBackgroundColor(Color.GREEN);
        }
        else {
            view.setBackgroundColor(Color.YELLOW);
        }
        super.onWindowFocusChanged(hasFocus);
    }*/

    /**
     * Populate the activity with the top-level headers.
     */
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.setting, target);
    }

    @Override
    protected boolean isValidFragment(String fragment_name) {
        boolean is_valid =
                ColorSettings.class.getName().equals(fragment_name) |
                        SizeSettings.class.getName().equals(fragment_name) |
                        ServerSettings.class.getName().equals(fragment_name);
        return is_valid;
    }

    /**
     * This fragment contains a second-level set of preference that you
     * can get to by tapping an item in the first preferences fragment.
     */
    public static class ColorSettings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.color);

        }



    }

    public static class SizeSettings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.size);
        }
    }

    public static class ServerSettings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.server);
        }
    }
}
