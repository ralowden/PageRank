package com.resourcemonitor.common;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Encapsulate the broker interactions.
 */
public class MonitorBroker {
    // session
    private Session session;
    // producer
    private MessageProducer producer;
    // consumer
    private MessageConsumer consumer;
    private String url;
    private String topicName;

    /**
     * Create the broker
     */
    public MonitorBroker(boolean sender) {
		
        url = Configuration.getInstance().getBrokerUrl();
        topicName = Configuration.getInstance().getTopicName();

        try {
            init(sender);
        } catch (MonitorException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize the system..");
            System.exit(1);
        }
    }
    
    public MonitorBroker(String config, boolean sender){
		if(config.equals("tunnel")){
			url = Configuration.getInstance().getTunnelUrl();
		}
		else{
			url = Configuration.getInstance().getBrokerUrl();
		}
        topicName = Configuration.getInstance().getTopicName();

        try {
            init(sender);
        } catch (MonitorException e) {
            e.printStackTrace();
            System.out.println("Failed to initialize the system..");
            System.exit(1);
        }
	}
	
	public Configuration getConfiguration(){
			return Configuration.getInstance();
	}

    /**
     * Initialize the connections. Create the session, producer and consumer
     * @param sender weather this is a producer or a consumer, student can choose
     *               not to create the producer, when sender = false and student can
     *               choose not to create the consumer when sender = true
     * @throws MonitorException
     */
    private void init(boolean sender) throws MonitorException {

        /** implement your code
        It’s similar to the ActiveMQ example in lab
        1. create topic/queue connection session
        2. If it’s sender, create a producer, otherwise, create a consumer
        */
			
		try {
			ActiveMQConnectionFactory cF = new ActiveMQConnectionFactory(url);
			
			Connection connection = cF.createConnection();
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Destination destination = session.createTopic(topicName);
			producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			consumer = session.createConsumer(destination);
		}
		catch(Exception e){
			System.out.println(e);
			e.printStackTrace();
		}

    }

    /**
     * Create a JMS message using the Monitor Data and send using the producer
     * @param data MonitorData instance
     */
    public void send(String text) {
        /** implement your code
           Producer send message to ActiveMQ broker
           1. Construct/set message body by using javax.jms.MapMessage
           2. Send the message
        */
		try {
			TextMessage message = session.createTextMessage(text);
			producer.send(message);
		}
		catch (Exception e){}
    }

    /**
     * Receive a JMS message and convert it to MonitorData. After the monitor data is created call
     * the handler.onMessage(monitorData).
     * @param handler the handler to call with the monitor message
     */
    public void startReceive(final ReceiveHandler handler) {
        /** implement your code
           Consumer receive messages from ActiveMQ broker
           1. If receive any message, deserialize it and fill them into MonitorData object
           2. Using handler.onMessage
        */
			
		while(true){
			try {
			Message message = consumer.receive(1);
			
			if(message instanceof TextMessage){
				String text = ((TextMessage)message).getText();
				handler.onMessage(text);
			}
			
			}
			catch (Exception e){}
			
		}
    }

}
