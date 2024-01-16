package mkurz.controller;

import java.util.ArrayList;
import java.util.List;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import mkurz.model.WarehouseData;

public class MOMReceiver {

  private Session session = null;
  private Connection connection = null;
  private MessageConsumer consumer = null;
  private Destination destination = null;

  public MOMReceiver(String subject) {
    try {
      ActiveMQConnectionFactory connectionFactory =
              new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                      ActiveMQConnection.DEFAULT_PASSWORD,
                      ActiveMQConnection.DEFAULT_BROKER_URL);
      connectionFactory.setTrustedPackages(
              List.of("mkurz.model", "java.util"));
      connection = connectionFactory.createConnection();
      connection.start();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      destination = session.createQueue(subject);
      consumer = session.createConsumer(destination);
    } catch (Exception e) {
      System.err.print(e);
      stop();
    }
  }

  // Method to stop the WarehouseReceiver
  public void stop() {
    try {
      consumer.close();
      session.close();
      connection.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public ArrayList<WarehouseData> getMessage() {
    ArrayList<WarehouseData> msgList = new ArrayList<>();
    try {
      ObjectMessage message = (ObjectMessage)consumer.receiveNoWait();
      while (message != null) {
        // Extract WarehouseData from the JMS message
        WarehouseData value = (WarehouseData)message.getObject();
        msgList.add(value);

        message = (ObjectMessage)consumer.receiveNoWait();

      }
    } catch (JMSException e) {
      System.err.println(e);
      return null;
    }
    return msgList;
  }
}
