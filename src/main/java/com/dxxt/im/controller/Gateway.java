package com.dxxt.im.controller;

import com.dxxt.im.ampq.MessageSender;
import com.dxxt.im.config.Zookeeper;
import com.dxxt.im.util.JwtTokenUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/Gateway")
public class Gateway {

    @Autowired
    private Zookeeper zookeeper;

    @Autowired
    private MessageSender messageSender;

    @ResponseBody
    @GetMapping("/getImServerNode")
    public Object getImServerNode() {
        Map<String, Object> result = new HashMap<>();
        result.put("imserver", zookeeper.chooseImServerNode());
        result.put("jwttoken", JwtTokenUtil.getJwtToken("A01"));
        return result;
    }

    @ResponseBody
    @GetMapping("/sendMessage")
    public Object sendMessage(String clientId, String message) {
        Map<String, Object> data = new HashMap<>();
        data.put("clientId", clientId);
        data.put("message", message);

        try {
            messageSender.sendMessage(data);
        } catch (JsonProcessingException e) {
            return "error";
        }
        return "success";
    }
}
