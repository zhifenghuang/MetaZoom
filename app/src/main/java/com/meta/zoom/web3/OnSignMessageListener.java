package com.meta.zoom.web3;


import com.meta.zoom.web3.entity.Message;

public interface OnSignMessageListener {
    void onSignMessage(Message<String> message);
}
