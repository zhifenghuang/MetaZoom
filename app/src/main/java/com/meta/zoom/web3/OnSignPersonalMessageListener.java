package com.meta.zoom.web3;

import com.meta.zoom.web3.entity.Message;

public interface OnSignPersonalMessageListener {
    void onSignPersonalMessage(Message<String> message);
}
