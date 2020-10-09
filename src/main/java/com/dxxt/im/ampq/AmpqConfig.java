package com.dxxt.im.ampq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmpqConfig {

    public static final String messgaeQueue = "daxin.im.message.queue";
    public static final String exchangeName = "amq.direct";

    @Bean
    public Queue messageQueue() {
        return new Queue(messgaeQueue, true);
    }

    @Bean
    Binding marketingBinding(Queue messageQueue) {
        return BindingBuilder.bind(messageQueue).to(new DirectExchange(exchangeName)).with(messgaeQueue);
    }
}
