package com.atguigu.gmall0715.payment.controller;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ConsumerTest {

    public static void main(String[] args) throws JMSException {
        ActiveMQConnectionFactory activeMQConnectionFactory =
                new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                ActiveMQConnection.DEFAULT_PASSWORD,
                "tcp://192.168.3.215:61616");
        //创建连接
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();

        //创建会话
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue atguigu = session.createQueue("Atguigu");
        //创建consumer
        MessageConsumer consumer = session.createConsumer(atguigu);
        //接受消息
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                //参数就是收到的消息
                if (message instanceof TextMessage){
                    try {
                        String text = ((TextMessage)message).getText();
                        System.out.println(text+"接受的消息");
                    } catch (JMSException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }
}
