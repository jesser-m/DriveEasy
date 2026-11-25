package com.driveeasy.client.config;

import com.driveeasy.client.model.CategorieClient;
import com.driveeasy.client.model.Client;
import com.driveeasy.client.repository.ClientRepository;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Jeu de donnees de demonstration (active par driveeasy.client.seed-enabled). */
@Component
@ConditionalOnProperty(name = "driveeasy.client.seed-enabled", havingValue = "true")
public class DataSeeder implements ApplicationRunner {

    private final ClientRepository repository;

    public DataSeeder(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        LocalDate now = LocalDate.now();
        add("12345678", "Ben Ali", "Ahmed", now.minusYears(30), "ahmed.benali@mail.tn", "PERM-0001", now.plusYears(3), CategorieClient.STANDARD);
        add("23456789", "Trabelsi", "Sarra", now.minusYears(27), "sarra.trabelsi@mail.tn", "PERM-0002", now.plusYears(2), CategorieClient.FIDELE);
        add("34567890", "Gharbi", "Mohamed", now.minusYears(45), "m.gharbi@mail.tn", "PERM-0003", now.plusYears(5), CategorieClient.VIP);
        add("45678901", "Jlassi", "Yasmine", now.minusYears(19), "yasmine.jlassi@mail.tn", "PERM-0004", now.plusYears(4), CategorieClient.STANDARD); // trop jeune
        add("56789012", "Mansouri", "Karim", now.minusYears(35), "karim.mansouri@mail.tn", "PERM-0005", now.minusMonths(2), CategorieClient.STANDARD); // permis expire
    }

    private void add(String cin, String nom, String prenom, LocalDate naissance, String email, String permis,
                     LocalDate expiration, CategorieClient categorie) {
        repository.save(Client.builder().cin(cin).nom(nom).prenom(prenom).dateNaissance(naissance).email(email)
                .telephone("+216 20 000 000").adresse("Tunis, Tunisie").numeroPermis(permis)
                .dateExpirationPermis(expiration).categorieClient(categorie).dateInscription(LocalDate.now()).build());
    }
}
