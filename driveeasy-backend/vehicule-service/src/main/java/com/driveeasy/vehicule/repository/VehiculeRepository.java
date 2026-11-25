package com.driveeasy.vehicule.repository;

import com.driveeasy.vehicule.model.Vehicule;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/** Depot en memoire (aucune base de donnees). */
@Repository
public class VehiculeRepository {

    private final Map<Long, Vehicule> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<Vehicule> findAll() {
        return store.values().stream().sorted(Comparator.comparing(Vehicule::getId)).toList();
    }

    public Optional<Vehicule> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsByImmatriculation(String immatriculation, Long excludeId) {
        return store.values().stream().anyMatch(v ->
                v.getImmatriculation().equalsIgnoreCase(immatriculation) && !v.getId().equals(excludeId));
    }

    public Vehicule save(Vehicule v) {
        if (v.getId() == null) {
            v.setId(sequence.incrementAndGet());
        }
        store.put(v.getId(), v);
        return v;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
