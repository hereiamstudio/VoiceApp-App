package com.voiceapp.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.voiceapp.Const;

import java.util.Random;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class PinHelper {

    private static final String KEY_PIN = "pin";
    private static final String KEY_LOCKED = "locked";
    private static final String KEY_NEXT_TIMEOUT = "timeout";
    private static final String KEY_ATTEMPTS = "attempts";
    private static final String FILENAME = "myProperties";
    private final Context context;

    public long getResumedTime() {
        return resumedTime;
    }
    public long getLeaveTime() {
        return leaveTime;
    }

    private static long resumedTime = 0;
    private static long leaveTime = 0;

    @Inject
    public PinHelper(Context context){
        this.context = context;
    }

    private SharedPreferences getSharedPrefs() {
        return context.getSharedPreferences(FILENAME, Activity.MODE_PRIVATE);
    }

    public void writeToProperties(String key, String value) {
        try {
            SharedPreferences.Editor editor = getSharedPrefs().edit();
            editor.putString(key, encode(value));
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writePin(String pin) {
        writeToProperties(KEY_PIN, pin);
    }

    public void savePin(String inputPassword) {
        long ran = getRandomNumber();
        String myStringRan = Long.toString(ran);

        StringBuilder myPin = new StringBuilder(myStringRan);
        myPin.setCharAt(4, inputPassword.charAt(0));
        myPin.setCharAt(7, inputPassword.charAt(1));
        myPin.setCharAt(10, inputPassword.charAt(2));
        myPin.setCharAt(12, inputPassword.charAt(3));
        myPin.setCharAt(3, inputPassword.charAt(4));
        myPin.setCharAt(9, inputPassword.charAt(5));

        writePin(myPin.toString());

        setAttempts(0);
    }

    public boolean testPin(@NonNull String pin){
        String realPin = getPin();

        if (TextUtils.isEmpty(realPin) || TextUtils.isEmpty(pin)){
            Timber.e("pin cannot be empty");
            return false;
        }

        if (getAttempts() > Const.MAX_PIN_ATTEMPTS){
            Timber.e("Maximum attempts exceeded");
            return false;
        }

        boolean result = pin.equals(getPin());

        if (!result){
            // Increment attempts
            setAttempts(getAttempts() + 1);
        }
        else{
            // Reset attempts
            setAttempts(0);
            setLocked(false);
        }

        return result;
    }

    public boolean hasPin() {
        return !TextUtils.isEmpty(readProperties(KEY_PIN));
    }

    public String getPin() {
        return readProperties(KEY_PIN);
    }

    public String readProperties(String key) {
        String outValue = "";
        StringBuilder myPin = new StringBuilder();

        try {
            outValue = getSharedPrefs().getString(key, "");

            if (TextUtils.isEmpty(outValue)){
                return null;
            }

            String pw = decode(outValue);

            myPin.append(pw.charAt(4));
            myPin.append(pw.charAt(7));
            myPin.append(pw.charAt(10));
            myPin.append(pw.charAt(12));
            myPin.append(pw.charAt(3));
            myPin.append(pw.charAt(9));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return myPin.toString();
    }

    public String encode(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    public String decode(String input) {
        return new String(Base64.decode(input, Base64.DEFAULT));
    }

    public long getRandomNumber() {
        long min = 1000000000000000L;
        long max = 9999999999999999L;
        Random random = new Random();
        return random.nextLong() % (max - min) + max;
    }

    public void setAttempts(int attempts) {
        getSharedPrefs().edit().putInt(KEY_ATTEMPTS, attempts).apply();
    }

    public int getAttempts() {
        return getSharedPrefs().getInt(KEY_ATTEMPTS, 0);
    }

    public void setLocked(boolean locked) {
        getSharedPrefs().edit().putBoolean(KEY_LOCKED, locked).apply();
        if (!locked){
            leaveTime = 0;
        }
    }

    public long getNextTimeoutMillis() {
        return getSharedPrefs().getLong(KEY_NEXT_TIMEOUT, Const.APP_LOCK_TIMEOUT);
    }

    public void setNextTimeoutMillis(Long timeout){
        getSharedPrefs().edit().putLong(KEY_NEXT_TIMEOUT, timeout).apply();
    }

    public boolean getLocked() {
        return getSharedPrefs().getBoolean(KEY_LOCKED, true);
    }

    public void setResumedTime(long resumeTime){
        Timber.d("Resume %.0f ", resumeTime/1000.0f);

        long timeout = getNextTimeoutMillis();
        setNextTimeoutMillis(Const.APP_LOCK_TIMEOUT);

        if (leaveTime != 0 && resumeTime > leaveTime + timeout){
            Timber.d("Resume time greater than threshold, locking... %.0f %.0f", resumeTime /1000.0f, leaveTime/1000.0f);
            setLocked(true);
        }
        else{
            Timber.d("Resumed within grace period");
            leaveTime = 0;
        }
    }

    public void setLeaveTime(long leaveTime){
        Timber.d("Leave %.0f ", leaveTime/1000.0f);
        this.leaveTime = leaveTime;
    }

    public void clearAll() {
        getSharedPrefs()
                .edit()
                .clear()
                .apply();
    }
}
