package com.smartalarm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WechatConfig {
    
    private String appid;
    private String secret;
    private String token;
    private String encodingAesKey;
    
    public String getAppid() { return appid; }
    public void setAppid(String appid) { this.appid = appid; }
    
    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getEncodingAesKey() { return encodingAesKey; }
    public void setEncodingAesKey(String encodingAesKey) { this.encodingAesKey = encodingAesKey; }
}
