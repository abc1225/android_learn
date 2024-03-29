package com.example.myapplication;

 import android.app.ActivityManager;
 import android.content.ComponentName;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.LauncherActivityInfo;
 import android.content.pm.LauncherApps;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.content.pm.ProviderInfo;
 import android.content.pm.ResolveInfo;
 import android.content.pm.ShortcutInfo;
 import android.content.pm.ShortcutManager;
 import android.database.Cursor;
 import android.graphics.drawable.Icon;
 import android.net.Uri;
 import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

 import android.os.UserHandle;
 import android.provider.Settings;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.TextView;

 import java.util.ArrayList;
 import java.util.List;

 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.pm.ShortcutInfo;
 import android.content.pm.ShortcutManager;
 import android.graphics.drawable.Icon;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;



public class MainActivity extends AppCompatActivity {

    private TextView mTextView;
    private static final String ID_DYNAMIC_1 = "id_dynamic_1";

    private int num = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textview_first);

        findViewById(R.id.button_first).setOnClickListener((view -> {
            mTextView.setText("点击第" + (num++) + "次" + " ping: " + PingUtils.getRTT("8.8.8.8"));


//            String alias = getCurrentLauncher();
            String alias = "com.example.myapplication.MainActivity5";
            Log.d("tag", "getCurrentLauncher: " + alias);
            changeLauncher(alias);

//            if(null != alias){
//                changeLauncher(alias);
//            }

//            //  创建桌面图标
//            // 点击按钮后，再返回长按图标，右侧显示快捷方式-》百度搜索
//            setDynamicShortcuts();
//            // 创建桌面快捷方式
//            createPinnedShortcuts();


//            // 隐藏图标
//            //adb shell settings get global show_hidden_icon_apps_enabled
//            try {
//                int showHidden = Settings.Global.getInt(getContentResolver(),
//                        "show_hidden_icon_apps_enabled", 1);
//                Log.d("MainActivity", "showHidden: " + showHidden);
//                if (showHidden != 0) {
//                    Settings.Global.putInt(getContentResolver(), "show_hidden_icon_apps_enabled", 0);
//                    Log.i("MainActivity", "set showHidden: ");
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }



        }));



    }



    private String getCurrentLauncher() {
        PackageManager pm = getPackageManager();
        String packageName = getPackageName();
        String[] aliasNames = {
                ".MainActivity",
                ".MainActivity2"
        };
        for (String alias : aliasNames) {
            ComponentName componentName = new ComponentName(packageName, packageName + alias);
            int enabledSetting = pm.getComponentEnabledSetting(componentName);

            if (enabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                // 如果这个别名被启用了，返回它的名称
                return alias;
            }
        }
        // 如果没有找到启用的别名，返回null或者默认的别名
        return null;
    }

    // 禁止显示
    private void setDisableAlias(String name){
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(MainActivity.this, name),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private void changeLauncher(String name) {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(getComponentName(),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(MainActivity.this, name),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        //Intent 重启 Launcher 应用
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        List<ResolveInfo> resolves = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo res : resolves) {
            if (res.activityInfo != null) {
                ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                am.killBackgroundProcesses(res.activityInfo.packageName);
            }
        }
    }





    public void show(View view) {
        String clazzName = "com.android.ui.ActivityAliasN";
        enableComponent(this, clazzName);
        enableComponent(this, "cn.test.hideicon.Alias1Activity");
        finish();
    }

    public void hide(View view) {
        String clazzName = "com.android.ui.ActivityAliasN";
        disableComponent(this, clazzName);
        disableComponent(this, "cn.test.hideicon.Alias1Activity");
        finish();
    }

    /**
     * 启动组件
     */
    public static void enableComponent(Context context, String clazzName) {
        ComponentName componentName = new ComponentName(context, clazzName);
        PackageManager mPackageManager = context.getPackageManager();
        mPackageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    /**
     * 禁用组件
     */
    public static void disableComponent(Context context, String clazzName) {
        ComponentName componentName = new ComponentName(context, clazzName);
        PackageManager mPackageManager = context.getPackageManager();
        mPackageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }














    // 直达 “百度搜索 ”链接
    private ShortcutInfo createShortcutInfo(){
        return new ShortcutInfo.Builder(this, ID_DYNAMIC_1)
                .setShortLabel(getString(R.string.dynamic_shortcut_short_label1))
                .setLongLabel(getString(R.string.dynamic_shortcut_long_label1))
                .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher_qk))
                .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.baidu.com/")))
                .build();
    }
    private void setDynamicShortcuts() {
        try{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                List<ShortcutInfo> shortcutInfo = new ArrayList<>();
                shortcutInfo.add(createShortcutInfo());
                if (shortcutManager != null) {
                    shortcutManager.setDynamicShortcuts(shortcutInfo);
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }
    //创建桌面快捷方式，可单独显示
    private void createPinnedShortcuts() {
        try{
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
                if (shortcutManager != null && shortcutManager.isRequestPinShortcutSupported()) {
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.putExtra("key", "fromPinnedShortcut");
                    ShortcutInfo pinShortcutInfo = new ShortcutInfo.Builder(this, "my-shortcut")
                            .setShortLabel(getString(R.string.pinned_shortcut_short_label2))
                            .setLongLabel(getString(R.string.pinned_shortcut_long_label2))
                            .setIcon(Icon.createWithResource(this, R.mipmap.ic_launcher_qk))
                            .setIntent(intent)
                            .build();
                    Intent pinnedShortcutCallbackIntent = shortcutManager.createShortcutResultIntent(pinShortcutInfo);
                    PendingIntent successCallback = PendingIntent.getActivity(this, 0, pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE);
                    boolean b = shortcutManager.requestPinShortcut(pinShortcutInfo, successCallback.getIntentSender());

                    Log.d("tag", "set pinned shortcuts " + (b ? "success" : "failed") + "!");
                }
            }
        }catch (Exception e){e.printStackTrace();}
    }





}

