package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {

    private ShipRepository shipRepository;

    public ShipServiceImpl() {}

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public Ship saveShip(Ship ship) {
        return shipRepository.save(ship);
    }

    @Override
    public Ship getShip(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteShip(Ship ship) {
        shipRepository.delete(ship);
    }

    @Override
    public Stream<Ship> getShipCount(
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
    ) {

        Stream<Ship> ships = StreamSupport.stream(shipRepository.findAll().spliterator(), false)
                .filter((ship) -> (name == null || ship.getName().contains(name)) &&
                        (planet == null || ship.getPlanet().contains(planet)) &&
                        (shipType == null || ship.getShipType() == shipType) &&
                        (after == null || !ship.getProdDate().before(new Date(after))) &&
                        (before == null || !ship.getProdDate().after(new Date(before))) &&
                        (isUsed == null || ship.getUsed() == isUsed) &&
                        (minSpeed == null || ship.getSpeed() >= minSpeed) &&
                        (maxSpeed == null || ship.getSpeed() <= maxSpeed) &&
                        (minCrewSize == null || ship.getCrewSize() >= minCrewSize) &&
                        (maxCrewSize == null || ship.getCrewSize() <= maxCrewSize) &&
                        (minRating == null || ship.getRating() >= minRating) &&
                        (maxRating == null || ship.getRating() <= maxRating)
                );
        return ships;
    }

    @Override
    public List<Ship> getShips(ShipOrder order, Integer pageNumber, Integer pageSize, ShipOrder order1, Stream<Ship> ships) {

        long storedSize = 3L;
        ShipOrder storedOrder = order;
        if (pageSize != null) {
            storedSize = pageSize;
        }
        if (pageSize == null) {
            storedSize = 3L;
        }
        return ships
                .sorted((o1, o2) ->
                        storedOrder == ShipOrder.SPEED ? o1.getSpeed().compareTo(o2.getSpeed()) :
                                storedOrder == ShipOrder.DATE ? o1.getProdDate().compareTo(o2.getProdDate()) :
                                        storedOrder == ShipOrder.RATING ? o1.getRating().compareTo(o2.getRating()) :
                                                o1.getId().compareTo(o2.getId()))
                .skip(pageNumber == null ? 0L : pageNumber * storedSize)
                .limit(storedSize)
                .collect(Collectors.toList());
    }


    @Override
    public double calculateRating(double speed, boolean isUsed, Date prod) {
        double rating = 80 * speed * (isUsed ? 0.5 : 1) / (3019 - getProdDate(prod) + 1);
        return Math.round(rating * 100) / 100D;
    }

    @Override
    public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {
        boolean shouldChangeRating = false;

        final String name = newShip.getName();
        if (name != null) {
            if ( !name.isEmpty() && name.length() <= 50) {
                oldShip.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }
        final String planet = newShip.getPlanet();
        if (planet != null) {
            if ( !planet.isEmpty() && planet.length() <= 50) {
                oldShip.setPlanet(planet);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }
        final Date prodDate = newShip.getProdDate();
        Calendar calendarAfter = Calendar.getInstance();
        calendarAfter.set(Calendar.YEAR, 2800);
        Calendar  calendarBefore = Calendar.getInstance();
        calendarBefore.set(Calendar.YEAR, 3019);
        if (prodDate != null) {
            if (prodDate.after(calendarAfter.getTime()) && prodDate.before(calendarBefore.getTime())) {
                oldShip.setProdDate(prodDate);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            shouldChangeRating = true;
        }
        final Double speed = newShip.getSpeed();
        if (speed != null) {
            if ( speed.compareTo(0.01) >= 0 && speed.compareTo(0.99) <= 0) {
                oldShip.setSpeed(speed);
                shouldChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        final Integer crewSize = newShip.getCrewSize();
        if (crewSize != null) {
            if ( crewSize.compareTo(1) >= 0 && crewSize.compareTo(9999) <= 0) {
                oldShip.setCrewSize(crewSize);
            } else {
                throw new IllegalArgumentException();
            }
        }
        if (shouldChangeRating) {
            final double rating = calculateRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate());
            oldShip.setRating(rating);
        }
        shipRepository.save(oldShip);
        return oldShip;
    }

    private int getProdDate(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
}