package com.meta.zoom.web3;


import com.meta.zoom.web3.entity.Message;
import com.meta.zoom.web3.entity.TypedData;

public interface OnSignTypedMessageListener {
    void onSignTypedMessage(Message<TypedData[]> message);
}
