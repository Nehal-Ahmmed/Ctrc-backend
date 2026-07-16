package com.ctrc.location.domain;

import java.util.Optional;

public interface LocationRepository {

    // inserts a location and returns the generated location_id
    Long insert(Location location);

    Optional<Location> findById(Long locationId);
}
