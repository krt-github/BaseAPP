package com.krt.basemodule.bean;

import com.krt.basemodule.view.ChatInputView;

public interface IChatInputListener extends IPressToSpeakListener{
    void onShowPanelTypeChanged(@ChatInputView.ShowType int showType);
    void onSendTextClick(String content);
    String getSaveFilePath();
    void onInputLengthExceed(CharSequence s, int maxLength);
    void onPermissionResult(boolean success, String tip);
}
