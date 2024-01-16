package mkurz.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import mkurz.model.WarehouseData;

@RestController
@CrossOrigin
public class WarehouseController {

  @RequestMapping("/center")
  public String warehouseMain() {
    return
        "<h1>Warehouse Center</h1></br>"
        +
        "<a href='http://localhost:8082/center/data'>Show Data</a><br/>";
  }

  @RequestMapping(value = "/center/data", produces = MediaType.APPLICATION_JSON_VALUE)
  public HashMap<String, ArrayList<WarehouseData>> warehouseData() {
    HashMap<String, ArrayList<WarehouseData>> data = new HashMap<>();
    Registration.updateRegistrations();
    HashSet<String> keys = new HashSet<String>(Registration.keys());
    for (String key : keys) {
      data.put(key, Registration.get(key).getMessage());
    }
    return data;
  }
}
