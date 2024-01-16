package mkurz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import mkurz.model.WarehouseData;

@RestController
@CrossOrigin
public class WarehouseController {

  private static MOMSender registration = new MOMSender("registration");
  @Autowired private WarehouseService service;

  @RequestMapping("/")
  public String warehouseMain() {
    return  "This is the warehouse application! (DEZSYS_WAREHOUSE_REST) <br/><br/>" +
            "<a href='http://localhost:8080/warehouse/001/data'>Link to warehouse/001/data</a><br/>" +
            "<a href='http://localhost:8080/warehouse/001/xml'>Link to warehouse/001/xml</a><br/>" +
            "<a href='http://localhost:8080/warehouse/001/transfer'>Link to warehouse/001/transfer</a><br/>";
  }

  @RequestMapping(value = "/warehouse/{inID}/data", produces = MediaType.APPLICATION_JSON_VALUE)
  public WarehouseData warehouseData(@PathVariable String inID) {
    return service.getWarehouseData(inID);
  }

  @RequestMapping(value = "/warehouse/{inID}/xml", produces = MediaType.APPLICATION_XML_VALUE)
  public WarehouseData warehouseDataXML(@PathVariable String inID) {
    return service.getWarehouseData(inID);
  }

  @RequestMapping("/warehouse/{inID}/send")
  public String sendData(@PathVariable String inID) {
    registration.sendMessage("Warehouse" + inID);
    MOMSender sender = new MOMSender("Warehouse" + inID);
    WarehouseData data = service.getWarehouseData(inID); 
    sender.sendMessage(data);
    sender.stop();
    return "The data from Warehouse " + inID + " has been sent.";
  }

  @RequestMapping("/warehouse/{inID}/transfer")
  public String warehouseTransfer(@PathVariable String inID) {
    HttpHeaders responseHeaders = new HttpHeaders();
    return service.getGreetings("Warehouse.Transfer!");
  }
}
