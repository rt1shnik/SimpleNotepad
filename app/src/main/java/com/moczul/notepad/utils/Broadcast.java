package com.moczul.notepad.utils;

import android.content.Context;
import android.content.Intent;

public class Broadcast {
    public static void requestForGetPaddindForSosButton(Context c) {
        Intent intent = new Intent("com.louka.launcher.sosbutton.padding");
        c.sendBroadcast(intent);
    }

    public static void requestToHideButton(Context c) {
        Intent intent = new Intent("com.louka.launcher.sosbutton.hide");
        c.sendBroadcast(intent);
    }
}
