package com.dxxt.im.controller;

import com.dxxt.im.config.Zookeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Gateway")
public class Gateway {

    @Autowired
    private Zookeeper zookeeper;

    @ResponseBody
    @GetMapping("/getImServerNode")
    public Object getImServerNode() {
        return zookeeper.chooseImServerNode();
    }
}
