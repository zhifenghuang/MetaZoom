package com.common.lib.bean;

public class LoginBean {


    /**
     * token : eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJzaG9wLmN4bSIsImF1ZCI6InNob3AuY3htIiwiaWF0IjoxNTczMTg1MDkzLCJuYmYiOjE1NzMxODUwOTMsImV4cCI6MTU3MzE5NTg5MywidWlkIjoyMDAwMDAwfQ.coMuI8EP68b1NbMs1OqP1JKYThufplb2m6dJtfc9Yp4
     * expires_time : 2019-11-08 14:51:33
     */

    private String token;
    private String expires_time;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpires_time() {
        return expires_time;
    }

    public void setExpires_time(String expires_time) {
        this.expires_time = expires_time;
    }
}
