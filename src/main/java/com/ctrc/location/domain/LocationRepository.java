package com.ctrc.location.domain;

import java.util.Optional;

public interface LocationRepository {

    Long insert(Location location);

    Optional<Location> findById(Long locationId);
}
