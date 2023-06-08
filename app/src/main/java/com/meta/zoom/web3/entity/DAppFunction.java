package com.meta.zoom.web3.entity;


public interface DAppFunction
{
    void DAppError(Throwable error, Message<String> message);
    void DAppReturn(byte[] data, Message<String> message);
}
