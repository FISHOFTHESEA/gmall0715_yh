package com.atguigu.gmall0715.payment.controller;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

public class ProducerTest {

    public static void main(String[] args) throws JMSException {
        //创建连接工厂
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory("tcp://192.168.3.215:61616");
        Connection connection = activeMQConnectionFactory.createConnection();
        connection.start();
        //创建session第一个参数表示是否支持事务
        //false时，第二个参数session_AUTO_ACKNOWLEDGE
        //Session_CLIENT_ACKNOWLEDGE,DUPS_OK_ACKNOWLEDGE其中一个
        //第一个参数为true时，第二个参数可忽略服务器设置为SESSION_TRANSACTED
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue atguigu = session.createQueue("Atguigu");

        MessageProducer producer = session.createProducer(atguigu);
        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("hello tomorrow");
        //发送消息
        producer.send(activeMQTextMessage);
        producer.close();
        connection.close();
    }
}
