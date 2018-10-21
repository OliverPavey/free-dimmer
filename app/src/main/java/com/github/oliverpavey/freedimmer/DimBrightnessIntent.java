package com.github.oliverpavey.freedimmer;

import android.app.Application;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

public class DimBrightnessIntent extends BroadcastReceiver {

    static boolean checkSystemWritePermission(Context context) {
        boolean retVal = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            retVal = Settings.System.canWrite(context);
            if(!retVal){
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
                retVal = Settings.System.canWrite(context);
                if(!retVal) {
                    Toast.makeText(context, context.getString(R.string.msg_brightness_permission_denied),
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        return retVal;
    }

    public static int getBrightness(Context context) {
        ContentResolver cResolver = context.getContentResolver();
        try {
            return Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            throw new IllegalStateException(context.getString(R.string.msg_screen_brightness_setting_not_found));
        }
    }

    public static void setBrightness(Context context, int brightness){
        brightness = brightness < 0 ? 0 : brightness > 255 ? 255 : brightness;

        if ( null != context ) {
            ContentResolver cResolver = context.getContentResolver();
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        setBrightness(context, 0);
    }
}
