package cn.djzhao.floatingnote;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import cn.djzhao.floatingnote.common.Common;
import cn.djzhao.floatingnote.common.PrefTool;

/**
 * FloatingService
 *
 * @author djzhao
 * @date 22/03/09
 */
public class FloatingServiceGFG extends Service {

    private ViewGroup floatView;
    private int LAYOUT_TYPE;
    private WindowManager windowManager;
    private WindowManager.LayoutParams floatingWindowLayoutParam;
    private Button saveBtn;
    private Button maximizeBtn;
    private EditText descET;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        floatView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.floating_layout, null);
        maximizeBtn = floatView.findViewById(R.id.buttonMaximize);
        saveBtn = floatView.findViewById(R.id.saveBtn);
        descET = floatView.findViewById(R.id.descEditText);

        Common.currentDesc = PrefTool.getString(this, "");
        descET.setText(Common.currentDesc);
        descET.setSelection(descET.getText().toString().length());
        descET.setCursorVisible(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_TOAST;
        }

        floatingWindowLayoutParam = new WindowManager.LayoutParams((int) (width * 0.55f),
                (int) (height * 0.58f),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        floatingWindowLayoutParam.gravity = Gravity.CENTER;

        floatingWindowLayoutParam.x = 0;
        floatingWindowLayoutParam.y = 0;

        windowManager.addView(floatView, floatingWindowLayoutParam);

        maximizeBtn.setOnClickListener(v -> {
            stopSelf();

            windowManager.removeView(floatView);

            Intent backHome = new Intent(this, MainActivity.class);
            backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(backHome);
        });

        descET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Common.currentDesc = descET.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        descET.setOnTouchListener((v, event) -> {
            descET.setCursorVisible(true);
            floatingWindowLayoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            windowManager.updateViewLayout(floatView, floatingWindowLayoutParam);
            return false;
        });

        floatView.setOnTouchListener(new View.OnTouchListener() {

            private double previousX, previousY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        previousX = event.getRawX();
                        previousY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        floatingWindowLayoutParam.x += (int) (event.getRawX() - previousX);
                        floatingWindowLayoutParam.y += (int) (event.getRawY() - previousY);
                        windowManager.updateViewLayout(floatView, floatingWindowLayoutParam);
                        previousX = event.getRawX();
                        previousY = event.getRawY();
                        break;
                }
                return false;
            }
        });

        saveBtn.setOnClickListener(v -> {
            PrefTool.putString(this, descET.getText().toString());

            descET.setCursorVisible(false);

            floatingWindowLayoutParam.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            windowManager.updateViewLayout(floatView, floatingWindowLayoutParam);

            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(floatView.getApplicationWindowToken(), 0);

            Toast.makeText(this, "文本已保存", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        windowManager.removeView(floatView);
    }
}
