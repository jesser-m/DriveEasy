package com.driveeasy.client.repository;

import com.driveeasy.client.model.Client;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/** Depot en memoire (aucune base de donnees). */
@Repository
public class ClientRepository {

    private final Map<Long, Client> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();

    public List<Client> findAll() {
        return store.values().stream().sorted(Comparator.comparing(Client::getId)).toList();
    }

    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsByCin(String cin, Long excludeId) {
        return store.values().stream().anyMatch(c -> c.getCin().equals(cin) && !c.getId().equals(excludeId));
    }

    public boolean existsByEmail(String email, Long excludeId) {
        return store.values().stream().anyMatch(c -> c.getEmail().equalsIgnoreCase(email) && !c.getId().equals(excludeId));
    }

    public boolean existsByNumeroPermis(String numero, Long excludeId) {
        return store.values().stream().anyMatch(c -> c.getNumeroPermis().equalsIgnoreCase(numero) && !c.getId().equals(excludeId));
    }

    public Client save(Client c) {
        if (c.getId() == null) {
            c.setId(sequence.incrementAndGet());
        }
        store.put(c.getId(), c);
        return c;
    }

    public void deleteById(Long id) {
        store.remove(id);
    }
}
