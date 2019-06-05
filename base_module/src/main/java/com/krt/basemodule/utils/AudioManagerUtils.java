package com.krt.basemodule.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

public class AudioManagerUtils {

    public static boolean isMute(Context context){
        Object o = context.getSystemService(Context.AUDIO_SERVICE);
        if(o instanceof AudioManager){
            AudioManager audioManager = (AudioManager) o;
            return audioManager.isMicrophoneMute();
        }
        return false;
    }

    public static boolean isSpeakerOn(Context context){
        Object o = context.getSystemService(Context.AUDIO_SERVICE);
        if(o instanceof AudioManager){
            AudioManager audioManager = (AudioManager) o;
            return audioManager.isSpeakerphoneOn();
        }
        return false;
    }

    public static void setMute(Context context, boolean mute) {
        Object o = context.getSystemService(Context.AUDIO_SERVICE);
        if(o instanceof AudioManager){
            setMute((AudioManager) o, mute);
        }
    }

    public static void setMute(AudioManager audioManager, boolean mute){
        if(null == audioManager){
            return;
        }
        audioManager.setMicrophoneMute(mute);
    }

    public static void setSpeakerTurnOn(Context context, boolean turnOn) {
        Object o = context.getSystemService(Context.AUDIO_SERVICE);
        if(o instanceof AudioManager){
            setSpeakerTurnOn((AudioManager) o, turnOn);
        }
    }

    public static void setSpeakerTurnOn(AudioManager audioManager, boolean turnOn){
        if(null == audioManager){
            return;
        }

        if (turnOn) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC), AudioManager.FX_KEY_CLICK);
            audioManager.setSpeakerphoneOn(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
            audioManager.setSpeakerphoneOn(false);
        }
    }

}
