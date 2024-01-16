package mkurz.controller;

import mkurz.model.WarehouseData;

public class WarehouseSimulation {

  private double getRandomDouble( int inMinimum, int inMaximum ) {

    double number = ( Math.random() * ( (inMaximum-inMinimum) + 1 )) + inMinimum;
    double rounded = Math.round(number * 100.0) / 100.0;
    return rounded;

  }

  private int getRandomInt( int inMinimum, int inMaximum ) {

    double number = ( Math.random() * ( (inMaximum-inMinimum) + 1 )) + inMinimum;
    Long rounded = Math.round(number);
    return rounded.intValue();

  }

  public WarehouseData getData(String inID ) {

    String[][] cities = {
            {"Wuernitz", "2112", "Robert Stolz Gasse 1", "Wuernitz Lager", "Österreich"},
            {"Salzburg", "5020", "Hauptstraße 5", "Salzburg Lager", "Österreich"},
            {"Wien", "1010", "Bruno Kreisky Platz 1", "Wien Lager", "Österreich"}
    };

    int r = this.getRandomInt(0,2);

    WarehouseData data = new WarehouseData();
    data.setID(inID);
    data.setName(cities[r][3]);
    data.setStreet(cities[r][2]);
    data.setPlz(cities[r][1]);
    data.setCity(cities[r][0]);
    data.setCountry(cities[r][4]);

    return data;

  }
}
