package com.krt.basemodule.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaRecorder;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.krt.basemodule.base.BaseArrayListRecyclerAdapter;
import com.krt.basemodule.base.ViewHolder;
import com.krt.basemodule.bean.ChatInputMediaBean;
import com.krt.basemodule.bean.IChatInputListener;
import com.krt.basemodule.bean.PressRecordResult;
import com.krt.basemodule.debug.Debug;
import com.krt.basemodule.utils.PermissionUtils;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;
import com.yichat.base.R;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

public class ChatInputView extends LinearLayout {
    @IntDef(value = {
            SHOW_TYPE_CLOSE,
            SHOW_TYPE_TEXT,
            SHOW_TYPE_VOICE,
            SHOW_TYPE_EMOJI,
            SHOW_TYPE_PANEL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShowType {}

    public static final int SHOW_TYPE_CLOSE = 0;
    public static final int SHOW_TYPE_TEXT = 1;
    public static final int SHOW_TYPE_VOICE = 2;
    public static final int SHOW_TYPE_EMOJI = 3;
    public static final int SHOW_TYPE_PANEL = 4;

    private Adapter mAdapter;
    private RecyclerView mMediaPanel;
    private View mEmojiPanel;
    private View mVoice;
    private View mEmoji;
    private View mSendMedia;
    private View mSendText;
    private TextView mSpeak;
    private EditText mTextInput;
    private EmojiPopup mEmojiView;

    private int mMediaIconWidth = 100;
    private int mMediaIconHeight = 100;
    private int mCancelOffset = 100; //px
    private int mSamplingRate = 8000;
    private int mEncodingBitRate = 96000; //192000;

    private InputLengthFilter mInputLengthFilter;
    private IChatInputListener mChatInputListener;

    public ChatInputView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public ChatInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context){
        setOrientation(VERTICAL);
//        setLayoutTransition(new LayoutTransition());
        inflate(context, R.layout.chat_input_layout, this);
        mEmojiPanel = findViewById(R.id.emoji_panel);
        mMediaPanel = findViewById(R.id.media_panel);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 4);
        mMediaPanel.setLayoutManager(layoutManager);
        mAdapter = new Adapter();
        mAdapter.setClickable(true);
        mMediaPanel.setAdapter(mAdapter);

        ClickListener l = new ClickListener();
        mVoice = findViewById(R.id.voice);
        mEmoji = findViewById(R.id.emoji);
        mSendMedia = findViewById(R.id.send_media);
        mSendText = findViewById(R.id.send_text);
        mSpeak = findViewById(R.id.press_speak);

        mVoice.setOnClickListener(l);
        mEmoji.setOnClickListener(l);
        mSendMedia.setOnClickListener(l);
        mSendText.setOnClickListener(l);
        mSpeak.setOnTouchListener(new OnSpeakTouchListener());

        mTextInput = findViewById(R.id.text_input);
        setMaxInputLength(10000);
        mEmojiView = EmojiPopup.Builder.fromRootView(mEmojiPanel)
                .setOnEmojiPopupDismissListener(() -> mEmoji.setActivated(false))
                .build((EmojiEditText) mTextInput);
        mTextInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                boolean canSendText = s.toString().trim().length() > 0;
                mSendText.setVisibility(canSendText ? View.VISIBLE : View.GONE);
                mSendMedia.setVisibility(canSendText ? View.GONE : View.VISIBLE);
            }
        });
        mTextInput.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                hidePanel();
                deactivateAllButton();
            }
        });
        mTextInput.setOnClickListener(v -> {
            if(mEmoji.isActivated()){
                onEmojiClick();
            }
        });
    }

    public EditText getEditText(){
        return mTextInput;
    }

    public boolean isPanelShow(){
        return /*VISIBLE == mEmojiPanel.getVisibility() || */VISIBLE == mMediaPanel.getVisibility();
    }

    private void showEmojiPanel(){
        mMediaPanel.setVisibility(GONE);
        showEmojiView(true);
    }

    private void showEmojiView(boolean show){
        mEmojiPanel.setVisibility(show ? VISIBLE : GONE);
        if (show) {
            mEmojiView.toggle();
        } else {
            mEmojiView.dismiss();
        }
    }

    private void showMediaPanel(){
        showEmojiView(false);
        mMediaPanel.setVisibility(VISIBLE);
    }

    private void hidePanel(){
        mEmojiPanel.setVisibility(GONE);
        mMediaPanel.setVisibility(GONE);
    }

    private void hidePanelKeepHeight(){
        mEmojiPanel.setVisibility(INVISIBLE);
        mMediaPanel.setVisibility(INVISIBLE);
    }

    public void resetPanel(){
        deactivateAllButton();
        hidePanel();
        showSpeakButton(false);
        showKeyboard(false);
        if(null != mChatInputListener){
            mChatInputListener.onShowPanelTypeChanged(SHOW_TYPE_CLOSE);
        }
    }

    public boolean isSpeakButtonShow(){
        return mVoice.isActivated();
    }

    public boolean isEmojiPanelShow(){
        return mEmoji.isActivated();
    }

    public boolean isMediaPanelShow(){
        return mSendMedia.isActivated();
    }

    private void showSpeakButton(boolean show){
        mSpeak.setVisibility(show ? VISIBLE : GONE);
        mTextInput.setVisibility(show ? GONE : VISIBLE);
    }

    public void setMediaData(ArrayList<ChatInputMediaBean> data){
        mAdapter.setData(data);
        mAdapter.notifyDataSetChanged();
    }

    public void setMediaIconSize(int width, int height){
        mMediaIconWidth = width;
        mMediaIconHeight = height;
    }

    public void setEditText(CharSequence text){
        mTextInput.setText(text);
    }

    public void setCancelOffset(int offset){
        mCancelOffset = offset;
    }

    public void setMaxInputLength(int length){
        if(null == mInputLengthFilter){
            mInputLengthFilter = new InputLengthFilter(length);
        }
        mInputLengthFilter.setMaxLength(length);
        InputFilter[] filters = new InputFilter[]{mInputLengthFilter};
        mTextInput.setFilters(filters);
    }

    public void setSamplingRate(int rate){
        mSamplingRate = rate;
    }

    public void setEncodingBitRate(int rate){
        mEncodingBitRate = rate;
    }

    public void setChatInputListener(IChatInputListener l){
        mChatInputListener = l;
    }

    public void setHint(@StringRes int hint){
        mTextInput.setHint(hint);
    }

    public void setHint(CharSequence hint){
        mTextInput.setHint(hint);
    }

    public void setHintColor(@ColorInt int color){
        mTextInput.setHintTextColor(color);
    }

    private class InputLengthFilter implements InputFilter{
        private int mMax;

        InputLengthFilter(int max) {
            mMax = max;
        }

        void setMaxLength(int max){
            mMax = max;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                   int dstart, int dend) {
//            int keep = mMax - (dest.length() - (dend - dstart));
//            if (keep <= 0) {
//                if(null != mChatInputListener){
//                    mChatInputListener.onInputLengthExceed(source, mMax);
//                }
//                return "";
//            } else if (keep >= end - start) {
//                return null; // keep original
//            } else {
//                keep += start;
//                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
//                    --keep;
//                    if (keep == start) {
//                        return "";
//                    }
//                }
//                CharSequence res = source.subSequence(start, keep);
//                if(res.length() < source.length() && null != mChatInputListener){
//                    mChatInputListener.onInputLengthExceed(source, mMax);
//                }
//                return res;
//            }


            int dindex = 0;
            int count = 0;

            while (count <= mMax && dindex < dest.length()) {
                char c = dest.charAt(dindex++);
                if (c < 128) {
                    count = count + 1;
                } else {
                    count = count + 2;
                }
            }

            if (count > mMax) {
                if(null != mChatInputListener){
                    mChatInputListener.onInputLengthExceed(source, mMax);
                }
                return dest.subSequence(0, dindex - 1);
            }

            int sindex = 0;
            while (count <= mMax && sindex < source.length()) {
                char c = source.charAt(sindex++);
                if (c < 128) {
                    count = count + 1;
                } else {
                    count = count + 2;
                }
            }

            if (count > mMax) {
                sindex--;
            }

            CharSequence res = source.subSequence(0, sindex);
            if(res.length() < source.length() && null != mChatInputListener){
                mChatInputListener.onInputLengthExceed(source, mMax);
            }
            return res;
        }

        /**
         * @return the maximum length enforced by this input filter
         */
        public int getMax() {
            return mMax;
        }
    }

    private class Adapter extends BaseArrayListRecyclerAdapter<ChatInputMediaBean> {

        @Override
        protected void onBindData(ViewHolder holder, int position, ChatInputMediaBean data) {
            TextView textView = holder.getView(R.id.media);
            if(0 != data.nameResId) {
                textView.setText(data.nameResId);
            }else{
                textView.setText(data.name);
            }

            Drawable icon = holder.getContext().getDrawable(data.iconResId);
            if(null != icon) {
                icon.setBounds(0, 0, mMediaIconWidth, mMediaIconHeight);
                textView.setCompoundDrawables(null, icon, null, null);
            }
        }

        @Override
        protected int getLayoutId(int viewType) {
            return R.layout.list_chat_input_media_layout;
        }

        @Override
        protected void onItemClick(int position, ChatInputMediaBean data, ViewHolder viewHolder) {
            if(null != data.clickEvent) {
                data.clickEvent.run();
            }
        }
    }

    private void showKeyboard(boolean show){
        Object service = getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(service instanceof InputMethodManager) {
            InputMethodManager imm = (InputMethodManager) service;
            if (show) {
                mTextInput.requestFocus();
                imm.showSoftInput(mTextInput, 0);
            } else {
                imm.hideSoftInputFromWindow(mTextInput.getWindowToken(), 0);
            }
        }
    }

    private void requestPermissions(String[] permissions, PermissionUtils.IPermissionResult callback){
        Context context = getContext();
        if(context instanceof FragmentActivity){
            PermissionUtils.request((FragmentActivity) context, permissions, callback);
        }else{
            printe("--- permission request failed ---");
            if(null != mChatInputListener){
                mChatInputListener.onPermissionResult(false, getResources().getString(R.string.permission_failed));
            }
        }
    }

    private void printe(String s){
        Debug.e("ChatInputView", s);
    }

    private void onShowPanelTypeChanged(int showType){
        if(null != mChatInputListener){
            mChatInputListener.onShowPanelTypeChanged(showType);
        }
    }

    private final String[] recordVoicePermissions = {Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private void onVoiceClick(){
        requestPermissions(recordVoicePermissions, granted -> {
            if(granted){
                hidePanel();

                boolean activated = mVoice.isActivated();
                deactivateAllButton();
                mVoice.setActivated(!activated);

                showSpeakButton(!activated);
                showKeyboard(activated);

                onShowPanelTypeChanged(mVoice.isActivated() ? SHOW_TYPE_VOICE : SHOW_TYPE_TEXT);
            }else{
                tipPermissionDenied();
            }
        });
    }

    private void tipPermissionDenied(){
        try {
            String permission = getResources().getString(R.string.permission_record);
            PermissionUtils.tipPermissionDenied((Activity) getContext(), permission);
        }catch(Exception e){
            if(null != mChatInputListener){
                mChatInputListener.onPermissionResult(false, getResources().getString(R.string.permission_deny));
            }
        }
    }

    private void onEmojiClick(){
        showSpeakButton(false);
        showKeyboard(true); //for emoji get keyboard height

        boolean activated = mEmoji.isActivated();
        deactivateAllButton();

        if(activated) {
            hidePanel();
        }else{
            showEmojiPanel();
        }
        mEmoji.setActivated(!activated);
        onShowPanelTypeChanged(mEmoji.isActivated() ? SHOW_TYPE_EMOJI : SHOW_TYPE_TEXT);

        if(activated) {
            mEmojiView.dismiss();
        }
    }

    private void onSendMediaClick(){
        showSpeakButton(false);

        boolean activated = mSendMedia.isActivated();
        deactivateAllButton();

        if(activated){
            hidePanel();
        }else{
            showMediaPanel();
        }
        mSendMedia.setActivated(!activated);
        onShowPanelTypeChanged(mSendMedia.isActivated() ? SHOW_TYPE_PANEL : SHOW_TYPE_TEXT);

        showKeyboard(activated);
    }

    private void onSendTextClick(){
        if(null != mChatInputListener){
            Editable text = mTextInput.getText();
            if(null != text) {
                String content = text.toString();
                if (content.trim().length() > 0) {
                    mChatInputListener.onSendTextClick(content);
                }
            }
        }
        mTextInput.setText("");
    }

    private void deactivateAllButton(){
        mVoice.setActivated(false);
        mEmoji.setActivated(false);
        mSendMedia.setActivated(false);
    }

    private class ClickListener implements OnClickListener{
        public void onClick(View v) {
            int i = v.getId();
            if (i == R.id.voice) {
                mTextInput.clearFocus();
                onVoiceClick();
            } else if (i == R.id.emoji) {
                onEmojiClick();
            } else if (i == R.id.send_media) {
                mTextInput.clearFocus();
                onSendMediaClick();
            } else if (i == R.id.send_text) {
                onSendTextClick();
            }
        }
    }

    private File mRecordFile;
    private MediaRecorder mRecorder;
    private void startRecordVoice(){
        printe("-----startRecordVoice-----");
        if(null == mChatInputListener) {
            return;
        }

        mRecordFile = new File(mChatInputListener.getSaveFilePath());
        try {
            boolean mkdirs = mRecordFile.getParentFile().mkdirs();
            boolean newFile = mRecordFile.createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(!mRecordFile.exists() || !mRecordFile.isFile()){
            printe("--- Is not file or isn't exist --- " + mRecordFile.getAbsolutePath());
            mChatInputListener.onReleased(true,
                    new PressRecordResult(false, PressRecordResult.ERROR_FILE_CREATE_FAILED));
            return;
        }

        mUpdateRecordSecondsRunnable.start();
        mUpdateRecordSecondsRunnable.run();

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile(mRecordFile.getAbsolutePath());
        mRecorder.setAudioChannels(1);
        mRecorder.setAudioSamplingRate(mSamplingRate);
        mRecorder.setAudioEncodingBitRate(mEncodingBitRate);

        try {
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
        } catch (Exception e) {
            e.printStackTrace();
            resetSpeakButton();
            stopRecordVoice(true);
            mChatInputListener.onReleased(true,
                    new PressRecordResult(false, PressRecordResult.ERROR_RECORDER_ERROR));
            String message = e.getMessage();
            if(null != message && message.toLowerCase().contains("permission")){
                tipPermissionDenied();
            }
        }
    }

    private void cancelRecordVoice(){
        printe("-----cancelRecordVoice-----");
        try {
            boolean delete = mRecordFile.delete();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private PressRecordResult stopRecordVoice(boolean isCancel){
        printe("-----stopRecordVoice----- isCancel: " + isCancel);
        isRecording = false;
        removeCallbacks(mUpdateRecordSecondsRunnable);

        if(null != mRecorder) {
            try {
                mRecorder.stop();
                mRecorder.release();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        PressRecordResult result = new PressRecordResult();
        if(isCancel){
            result.success = false;
            result.errorCode = PressRecordResult.ERROR_CANCEL;
            cancelRecordVoice();
        }else{
            int seconds = mUpdateRecordSecondsRunnable.getSeconds();
            if(seconds <= mMinSeconds){
                result.success = false;
                result.errorCode = PressRecordResult.ERROR_TOO_SHORT;
                cancelRecordVoice();
            }else {
                result.success = true;
                result.errorCode = PressRecordResult.ERROR_NO_ERROR;
                result.recordFilePath = mRecordFile.getAbsolutePath();
                result.duration = seconds;
            }
        }
        mRecordFile = null;
        mRecorder = null;

        return result;
    }

    public void setMinSeconds(int minSecond){
        if(minSecond >= 0){
            mMinSeconds = minSecond;
        }
    }

    public void setMaxSeconds(int maxSecond){
        if(maxSecond >= 0){
            mMaxSeconds = maxSecond;
        }
    }

    private boolean isRecording = false;
    private int mMinSeconds = 1;
    private int mMaxSeconds = 0;
    private UpdateRecordSecondsRunnable mUpdateRecordSecondsRunnable = new UpdateRecordSecondsRunnable();
    private class UpdateRecordSecondsRunnable implements Runnable{
        private int seconds = 0;
        public void run() {
            printe("-----------seconds: " + seconds);
            if(null != mChatInputListener){
                mChatInputListener.onRecording(seconds);
            }
            seconds++;
            if(0 == mMaxSeconds || seconds < mMaxSeconds){
                postDelayed(this, 1000);
            }else{
                onStopRecord(false);
            }
        }
        public void start(){
            seconds = 0;
        }
        public int getSeconds(){
            return seconds;
        }
    }

    private void onStopRecord(boolean isOutside){
        if(isRecording) {
            resetSpeakButton();
            mChatInputListener.onReleased(isOutside, stopRecordVoice(isOutside));
        }
    }

    private void resetSpeakButton(){
        mSpeak.setText(R.string.press_to_speak);
        mSpeak.setBackgroundResource(R.drawable.press_to_speak_bg_nor);
    }

    private class OnSpeakTouchListener implements OnTouchListener{
        private boolean isOutsideOld = false;
        public boolean onTouch(View v, MotionEvent event) {
            if(null == mChatInputListener) {
                return false;
            }

            boolean isOutside = isOutside(v, event);
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    isOutsideOld = false;
                    v.setBackgroundResource(R.drawable.press_to_speak_bg_pressed);
                    mSpeak.setText(R.string.release_to_send);
                    mChatInputListener.onPressed();
                    startRecordVoice();
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    isOutsideOld = false;
                    onStopRecord(isOutside);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(isRecording) {
                        if (isOutsideOld != isOutside) {
                            mSpeak.setText(isOutside ? R.string.release_to_cancel : R.string.release_to_send);
                            mChatInputListener.onWantToCanceled(isOutside);
                        }
                        isOutsideOld = isOutside;
                    }
                    break;
            }
            return true;
        }

        private boolean isOutside(View v, MotionEvent event){
            float x = event.getRawX();
            float y = event.getY();
            return !(x >= v.getLeft() && x <= v.getRight()
                    && y >= v.getTop() - mCancelOffset && y <= v.getBottom());
        }
    }

}
