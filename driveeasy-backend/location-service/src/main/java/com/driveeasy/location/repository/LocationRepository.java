package com.driveeasy.location.repository;

import com.driveeasy.location.model.Location;
import com.driveeasy.location.model.StatutLocation;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/** Depot en memoire (aucune base de donnees). */
@Repository
public class LocationRepository {

    private final Map<Long, Location> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<Location> findAll() {
        return List.copyOf(store.values());
    }

    public Optional<Location> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public Location save(Location l) {
        if (l.getId() == null) {
            l.setId(sequence.incrementAndGet());
        }
        store.put(l.getId(), l);
        return l;
    }

    private boolean active(Location l) {
        return l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS;
    }

    /** Chevauchement de periodes : [debut, fin[ */
    public boolean existeChevauchement(Long vehiculeId, LocalDate debut, LocalDate fin) {
        return store.values().stream().anyMatch(l -> l.getVehiculeId().equals(vehiculeId) && active(l)
                && l.getDateDebut().isBefore(fin) && debut.isBefore(l.getDateFinPrevue()));
    }

    public Set<Long> vehiculesOccupes(LocalDate debut, LocalDate fin) {
        return store.values().stream().filter(this::active)
                .filter(l -> l.getDateDebut().isBefore(fin) && debut.isBefore(l.getDateFinPrevue()))
                .map(Location::getVehiculeId).collect(Collectors.toSet());
    }

    public long compterActives(Long clientId) {
        return store.values().stream().filter(l -> l.getClientId().equals(clientId) && active(l)).count();
    }

    public long compterTerminees(Long clientId) {
        return store.values().stream()
                .filter(l -> l.getClientId().equals(clientId) && l.getStatut() == StatutLocation.TERMINEE).count();
    }
}
