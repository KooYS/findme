package kr.koo.findme.card;


import android.view.View;

import kr.koo.findme.NFCButtonOnClickListener;

public class CardItem<function> {

    private String mTextResource;
    private String mTitleResource;
    private String mItemId;
    private  NFCButtonOnClickListener callback;

    public CardItem(String title, String text, String id,  NFCButtonOnClickListener callback) {
        mTitleResource = title;
        mTextResource = text;
        mItemId = id;
        this.callback = callback;
    }

    public String getText() {
        return mTextResource;
    }

    public String getTitle() {
        return mTitleResource;
    }

    public String getId() {
        return mItemId;
    }


    public NFCButtonOnClickListener getCallBack() {
        return callback;
    }


}
