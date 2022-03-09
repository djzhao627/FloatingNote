package cn.djzhao.floatingnote;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import cn.djzhao.floatingnote.common.Common;
import cn.djzhao.floatingnote.common.PrefTool;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OVERLAY = -1;
    private Button minimizeBtn, saveBtn;
    private AlertDialog alertDialog;
    private EditText descEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        minimizeBtn = findViewById(R.id.buttonMinimize);
        saveBtn = findViewById(R.id.saveBtn);
        descEditText = findViewById(R.id.descEditText);

        if (isMyServiceRunning()) {
            stopService(new Intent(this, FloatingServiceGFG.class));
        }

        Common.currentDesc = PrefTool.getString(this, "");
        descEditText.setText(Common.currentDesc);
        descEditText.setSelection(descEditText.getText().toString().length());

        descEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Common.currentDesc = descEditText.getText().toString();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        saveBtn.setOnClickListener(v -> {
            PrefTool.putString(this, descEditText.getText().toString());
            descEditText.setCursorVisible(false);
            descEditText.clearFocus();
            Toast.makeText(this, "文本已保存", Toast.LENGTH_SHORT).show();
        });

        minimizeBtn.setOnClickListener( v -> {
            if (checkOverlayDisplayPermission()) {
                startService(new Intent(this, FloatingServiceGFG.class));
                finish();
            } else {
                requestOverlayDisplayPermission();
            }
        });
    }

    private boolean isMyServiceRunning() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningService : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (runningService.service.getClassName().equals(FloatingServiceGFG.class.getName())) {
                return true;
            }
        }
        return false;
    }

    private void requestOverlayDisplayPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        alertDialog = builder.setCancelable(true)
                .setTitle("请求展示到其他应用上层权限")
                .setMessage("在系统设置中允许本应用展示在其他应用上层")
                .setPositiveButton("打开设置", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE_OVERLAY);
                })
                .create();
        alertDialog.show();
    }

    private boolean checkOverlayDisplayPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }
}