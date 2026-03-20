package com.smartalarm.service;

import com.alibaba.fastjson.JSONObject;
import com.smartalarm.config.WechatConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatMessageService {
    
    private final WechatConfig wechatConfig;
    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    
    private static final String ACCESS_TOKEN_URL = 
        "https://api.weixin.qq.com/cgi-bin/token";
    
    private static final String MESSAGE_SEND_URL = 
        "https://api.weixin.qq.com/cgi-bin/message/subscribe/send";
    
    public String getAccessToken() {
        String cacheKey = "wechat:access_token";
        
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        String url = String.format("%s?grant_type=client_credential&appid=%s&secret=%s",
                ACCESS_TOKEN_URL, wechatConfig.getAppid(), wechatConfig.getSecret());
        
        String response = restTemplate.getForObject(url, String.class);
        JSONObject json = JSONObject.parseObject(response);
        
        String accessToken = json.getString("access_token");
        int expiresIn = json.getIntValue("expires_in");
        
        redisTemplate.opsForValue().set(cacheKey, accessToken, expiresIn - 300, TimeUnit.SECONDS);
        
        return accessToken;
    }
    
    public void sendSubscribeMessage(String openId, String templateId, 
                                     Map<String, String> data, String page) {
        String accessToken = getAccessToken();
        
        JSONObject message = new JSONObject();
        message.put("touser", openId);
        message.put("template_id", templateId);
        message.put("page", page != null ? page : "pages/index/index");
        
        JSONObject messageData = new JSONObject();
        data.forEach((key, value) -> {
            JSONObject item = new JSONObject();
            item.put("value", value);
            messageData.put(key, item);
        });
        message.put("data", messageData);
        
        String url = MESSAGE_SEND_URL + "?access_token=" + accessToken;
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, message.toString(), String.class);
        
        JSONObject result = JSONObject.parseObject(response.getBody());
        if (result.getIntValue("errcode") != 0) {
            log.error("发送订阅消息失败：{}", result.getString("errmsg"));
        }
    }
    
    public void sendAlarmReminder(String openId, String alarmTime, String alarmName) {
        Map<String, String> data = new HashMap<>();
        data.put("thing1", alarmName);
        data.put("time2", alarmTime);
        data.put("thing3", "记得起床哦");
        
        sendSubscribeMessage(openId, "YOUR_TEMPLATE_ID", data, "pages/index/index");
    }
}
