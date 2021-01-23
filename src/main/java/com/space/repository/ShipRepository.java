package com.space.repository;

import com.space.model.Ship;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
/**
 * Repository for {@link Ship} class
 */
@Repository
public interface ShipRepository extends CrudRepository<Ship, Long> {
}
