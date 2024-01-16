# DEZSYS_GK72_WAREHOUSE_MOM

## Fragestellungen

### Nennen Sie mindestens 4 Eigenschaften der Message Oriented Middleware?

1. **Nachrichtenorientiert:** MOM ermöglicht die Kommunikation zwischen Anwendungen durch den Austausch von Nachrichten.
2. **Asynchron:** Die Kommunikation zwischen Anwendungen erfolgt unabhängig von Zeit und Raum.
3. **Zuverlässigkeit:** MOM gewährleistet die zuverlässige Zustellung von Nachrichten, selbst bei Ausfällen.
4. **Nachrichtenvermittlung:** Nachrichten werden über einen zentralen Vermittlungsdienst weitergeleitet.

### Was versteht man unter einer transienten und synchronen Kommunikation?

- **Transiente Kommunikation:** Kurzlebige, vorübergehende Kommunikation ohne dauerhafte Speicherung der Nachrichten.
- **Synchrone Kommunikation:** Echtzeitkommunikation, bei der Sender und Empfänger synchronisiert sind und auf eine sofortige Antwort warten.

### Beschreiben Sie die Funktionsweise einer JMS Queue?

- Eine JMS Queue ist eine warteschlangenbasierte Nachrichtenstruktur.
- Sender senden Nachrichten an die Queue.
- Empfänger (Consumer) lesen Nachrichten in der Reihenfolge ihres Eintreffens und verarbeiten sie.

### JMS Overview - Beschreiben Sie die wichtigsten JMS Klassen und deren Zusammenhang?

- **ConnectionFactory:** Erzeugt Verbindungen zu JMS-Providern.
- **Connection:** Repräsentiert eine Verbindung zu einem JMS-Provider.
- **Session:** Sitzung für die Erstellung von Produzenten und Konsumenten.
- **Destination:** Stellt das Ziel für den Nachrichtenaustausch dar.
- **MessageProducer:** Sendet Nachrichten an ein Ziel.
- **MessageConsumer:** Empfängt Nachrichten von einem Ziel.

### Beschreiben Sie die Funktionsweise eines JMS Topic?

- JMS Topic ermöglicht die Veröffentlichung (Publish) und Abonnement (Subscribe) von Nachrichten zu einem bestimmten Thema.
- Sender senden Nachrichten an ein Thema, und alle Abonnenten dieses Themas erhalten die Nachricht.

### Was versteht man unter einem lose gekoppelten verteilten System? Nennen Sie ein Beispiel dazu. Warum spricht man hier von lose?

- In einem lose gekoppelten verteilten System sind die Komponenten unabhängig voneinander und kommunizieren über definierte Schnittstellen.
- Beispiel: Web Services, bei denen Dienste über standardisierte Protokolle wie HTTP kommunizieren.
- Lose Kopplung ermöglicht Flexibilität und Änderungen in einem Teil des Systems, ohne andere Teile zu beeinträchtigen.

## Sender

Create the queue and connect to it
```java
try {

      ConnectionFactory connectionFactory =
          new ActiveMQConnectionFactory(user, password, url);
      connection = connectionFactory.createConnection();
      connection.start();

      // Create the session
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      destination = session.createQueue(subject);

      // Create the producer.
      producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

      // Create the message
    } catch (Exception e) {
      System.out.println("[MessageProducer] Caught: " + e);
      e.printStackTrace();
    }
    System.out.println("Sender finished.");
```

Send message
```java
public void sendMessage(Serializable obj) {
    try {

      ObjectMessage message = session.createObjectMessage(obj);
      producer.send(message);
      System.out.println("Message sent");
    } catch (JMSException e) {
      e.printStackTrace();
    }
  }
```

Controller
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

Hier habe ich einen MOMSender für das jeweilige Warehouse erstellt. Ich hole mir die Daten von diesem Warehouse und schicke eine Nachricht mit diesen Daten. Dabei wird die "sendMessage()" Methode vom MOMSender aufgerufen.

Und folgende Codezeile brauche ich, damit ich einen anderen Port benutzen kann:
```java
app.setDefaultProperties(Collections.singletonMap("server.port", "8081"));
```

## Receiver

Der Receiver erstellt in dem Konstruktor erstmal eine Verbindung zum ActiveMQ. Dies mache ich wie folgt:
```java
ActiveMQConnectionFactory connectionFactory =
              new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_USER,
                      ActiveMQConnection.DEFAULT_PASSWORD,
                      ActiveMQConnection.DEFAULT_BROKER_URL);
      connectionFactory.setTrustedPackages(
              List.of("tgm.jscarlata.model", "java.util"));
      connection = connectionFactory.createConnection();
      connection.start();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
      destination = session.createQueue(subject);
      consumer = session.createConsumer(destination);
```

Ich erstelle eine Connection und starte dann eine Session. Diese Session ist notwendig, um eine Queue zu erstellen. Falls es die Queue schon bereits gibt, wird die vorhandene genommen. Und dann muss ich auch noch einen Consumer erstellen, an den die Nachricht gerichtet ist.

Ebenfalls gibt es eine getMessage Methode, die es einem erlaubt die Nachricht einzulesen, die zuletzt geschickt wurde.
Diese schaut wie folgt aus:

```java
public ArrayList<WarehouseData> getMessage() {
    ArrayList<WarehouseData> msgList = new ArrayList<>();
    try {
      ObjectMessage message = (ObjectMessage)consumer.receiveNoWait();
      while (message != null) {
        // Extract WarehouseData from the JMS message
        WarehouseData value = (WarehouseData)message.getObject();
        System.out.println(value.toString());
        msgList.add(value);
        // Acknowledge the receipt of the message
        message.acknowledge();

        // ack
        sendAcknowledgment(value);

        message = (ObjectMessage)consumer.receiveNoWait();
      }
    } catch ...
    return msgList;
  }
```

Für Acknowledgements habe ich ebenfalls eine Methode geschrieben, die eine ACK Queue erstellt und zeigt, dass die Nachricht angekommen ist.

```java
private void sendAcknowledgment(WarehouseData data) {
    try {
      // Create the acknowledgment sender
      MOMSender acknowledgmentSender = new MOMSender("ACKNOWLEDGMENT_QUEUE");

      // Send acknowledgment message
      acknowledgmentSender.sendMessage("Received: " + data.toString());

      // Stop the acknowledgment sender after sending the message
      acknowledgmentSender.stop();
    } catch ...
  }
```

Es gibt auch einen Controller für den Receiver, da ich ja irgendwo zeigen muss, dass die Nachrichten angezeigt werden können.

```java
@RequestMapping(value = "/center/data",
                  produces = MediaType.APPLICATION_JSON_VALUE)
  public HashMap<String, ArrayList<WarehouseData>>
  warehouseData() {
    HashMap<String, ArrayList<WarehouseData>> data = new HashMap<>();
    System.out.println("Update Registration Started");
    Registration.updateRegistrations();
    HashSet<String> keys = new HashSet<String>(Registration.keys());
    for (String key : keys) {
      data.put(key, Registration.get(key).getMessage());
    }
    return data;
  }
```

Wichtig ist dabei diese Zeile:
```java
Registration.updateRegistrations();
```

### Registration

Für die einfache Verwaltung und Anmeldung habe ich eine extra Klasse gemacht, die diese macht.

Wichtig ist die updateRegistrations Methode, die folgend ausschaut:
```java
try {
      // Receive and process JMS messages
      ObjectMessage message = (ObjectMessage)instance.consumer.receiveNoWait();
      while (message != null) {
        String value = (String) message.getObject();
        System.out.println("Message received: " + value);
        message.acknowledge();
        if (clients.get(value) == null)
          clients.put(value, new MOMReceiver(value));
        message = (ObjectMessage)instance.consumer.receiveNoWait();
      }
    } catch ...
      return;
    }
```

Ich checke die ganze Zeit, ob eine Nachricht mit einem Inhalt da ist und erstelle einen Receiver, wenn dem so sei.

