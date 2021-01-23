package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.stream.Stream;


@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    private ShipService shipService;

    public ShipController() {
    }
    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping({"", "/count"})
    public Object getAllShipsAndCount(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "planet", required = false) String planet,
            @RequestParam(value = "shipType", required = false) ShipType shipType,
            @RequestParam(value = "after", required = false) Long after,
            @RequestParam(value = "before", required = false) Long before,
            @RequestParam(value = "isUsed", required = false) Boolean isUsed,
            @RequestParam(value = "minSpeed", required = false) Double minSpeed,
            @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam(value = "minRating", required = false) Double minRating,
            @RequestParam(value = "maxRating", required = false) Double maxRating,
            @RequestParam(value = "order", required = false) ShipOrder order,
            @RequestParam(value = "pageNumber", required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", required = false) Integer pageSize,
            HttpServletRequest request
    ) {
        Stream<Ship> ships = shipService.getShipCount(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating);
        if (request.getRequestURI().endsWith("/count")) {
            return ships.count();
        }
        else {
            return shipService.getShips(order, pageNumber, pageSize, order, ships);
        }
    }

    @PostMapping("")
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {

        Calendar calendarAfter = Calendar.getInstance();
        calendarAfter.set(Calendar.YEAR, 2800);
        Calendar  calendarBefore = Calendar.getInstance();
        calendarBefore.set(Calendar.YEAR, 3019);

        if (!(ship != null && ship.getName() != null && !ship.getName().isEmpty() && ship.getName().length() <= 50
                && ship.getPlanet() != null && !ship.getPlanet().isEmpty() && ship.getPlanet().length() <= 50
                && ship.getSpeed() != null  && ship.getSpeed().compareTo(0.99) <= 0 && ship.getSpeed().compareTo(0.01) >= 0
                && ship.getCrewSize() != null && ship.getCrewSize().compareTo(1) >= 0 && ship.getCrewSize().compareTo(9999) <= 0
                && ship.getProdDate() != null && ship.getProdDate().after(calendarAfter.getTime()) && ship.getProdDate().before(calendarBefore.getTime()))){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (ship.getUsed() == null) ship.setUsed(false);
        ship.setSpeed(ship.getSpeed());
        double rating = shipService.calculateRating(ship.getSpeed(), ship.getUsed(), ship.getProdDate());
        ship.setRating(rating);
        Ship savedShip = shipService.saveShip(ship);
        return new ResponseEntity<>(savedShip, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable(value = "id") String pathId) {
        Long id;
        try {
            id = Long.parseLong(pathId);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (id == null || id <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Ship ship = shipService.getShip(id);
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Ship> updateShip(
            @PathVariable(value = "id") String pathId,
            @RequestBody Ship ship
    ) {
        ResponseEntity<Ship> entity = getShip(pathId);
        Ship savedShip = entity.getBody();
        if (savedShip == null) {
            return entity;
        }
        Ship result;
        try {
            result = shipService.updateShip(savedShip, ship);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable(value = "id") String pathId) {
        ResponseEntity<Ship> entity = getShip(pathId);
        Ship savedShip = entity.getBody();
        if (savedShip == null) {
            return entity;
        }
        shipService.deleteShip(savedShip);
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
