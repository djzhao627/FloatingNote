package cn.djzhao.floatingnote.common;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * PreferenceTool
 *
 * @author djzhao
 * @date 22/03/09
 */
public class PrefTool {

    public static final String INPUT_CONTENT = "INPUT_CONTENT";

    public static boolean putString(Context context, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(INPUT_CONTENT, value);
        return editor.commit();
    }

    public static String getString(Context context, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return sharedPreferences.getString(INPUT_CONTENT, defaultValue);
    }
}
