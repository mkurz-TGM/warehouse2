# Warehouse 2

# Installation von ActiveMQ

## Download Link

activemq.apache.org/download.html

## Tar Datei Extrahieren

```bash
tar xvf apache-activemq*.tar.gz -C /tmp/
```

## Active MQ Installieren

```bash
sudo mv /tmp/apache-activemq* /opt/
sudo chown -R root:root /opt/apache-activemq*
```

# Code

## MOMSender

Erstellt die Notwendige Connection, session, etc.

```java
public MOMSender(String subject) {
    try {
      ConnectionFactory connectionFactory =
          new ActiveMQConnectionFactory(user, password, url);
      connection = connectionFactory.createConnection();
      connection.start();

      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      destination = session.createQueue(subject);

      producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
```

Sendet die Daten zum Reciever

```java
public void sendMessage(Serializable obj) {
    try {
      ObjectMessage message = session.createObjectMessage(obj);
      producer.send(message);
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
```

## Registration

Looped durch die Queue.

Wenn etwas da ist, dann speichert er sich die value davon. Wenn diese noch nicht gespeichert ist, dann speichert er sie in einen neuen Reciever, der auf die Queue zugreift.

```java
public static void updateRegistrations() {
    try {
      ObjectMessage message = (ObjectMessage)instance.consumer.receiveNoWait();
      while (message != null) {
        String value = (String) message.getObject();
        if (clients.get(value) == null)
          clients.put(value, new MOMReceiver(value));
        message = (ObjectMessage)instance.consumer.receiveNoWait();
      }
    } catch (JMSException e) {
      System.err.println(e);
      return;
    }
  }
```

## MOMReciever

Nimmt sich die Messages aus der Queue.

```java
public ArrayList<WarehouseData> getMessage() {
    ArrayList<WarehouseData> msgList = new ArrayList<>();
    try {
      ObjectMessage message = (ObjectMessage)consumer.receiveNoWait();
      while (message != null) {
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
```

## WarehouseController

### Lager

Benachrichtigt die Registration Queue und sendet die Warehouse daten ab.

```java
@RequestMapping("/warehouse/{inID}/send")
  public String sendData(@PathVariable String inID) {
    registration.sendMessage("Warehouse" + inID);
    MOMSender sender = new MOMSender("Warehouse" + inID);
    WarehouseData data = service.getWarehouseData(inID); 
    System.out.println(data.getTimestamp());
    sender.sendMessage(data);
    sender.stop();
    return "The data has been sent to the central.";
  }
```

### Zentrale

Hohlt sich die Reciever von Registration und dann fuer jeden Reciever die Queue gibt sie pro warehouse zurueck.
