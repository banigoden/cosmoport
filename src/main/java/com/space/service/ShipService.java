//package com.space.service;
//
//import com.space.model.Ship;
//import com.space.model.ShipType;
//
//import java.util.Date;
//import java.util.List;
//
//public interface ShipService {
//
//    Ship getByID(Long id);
//    Ship getShipCount();
//    Ship createShip(Ship ship);
//    void updateShip(Long id,Ship updateShip);
//    String deleteByID(Long id);
//
//    List<Ship> getShipList();
//
//    Double computeRating(double speed, boolean isUsed, Date prod);
//}
package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public interface ShipService {

    Ship saveShip(Ship ship);

    Ship getShip(Long id);

    Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException;

    void deleteShip(Ship ship);

    Stream<Ship> getShipCount(
            String name,
            String planet,
            ShipType shipType,
            Long after,
            Long before,
            Boolean isUsed,
            Double minSpeed,
            Double maxSpeed,
            Integer minCrewSize,
            Integer maxCrewSize,
            Double minRating,
            Double maxRating
    );
    List<Ship> getShips(ShipOrder order, Integer pageNumber, Integer pageSize, ShipOrder order1, Stream<Ship> ships);

    double calculateRating(double speed, boolean isUsed, Date prod);


}