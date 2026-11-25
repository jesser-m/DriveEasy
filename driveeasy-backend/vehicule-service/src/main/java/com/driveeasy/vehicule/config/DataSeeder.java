
package com.driveeasy.vehicule.config;

import com.driveeasy.vehicule.model.Boite;
import com.driveeasy.vehicule.model.Carburant;
import com.driveeasy.vehicule.model.Categorie;
import com.driveeasy.vehicule.model.StatutVehicule;
import com.driveeasy.vehicule.model.Vehicule;
import com.driveeasy.vehicule.repository.VehiculeRepository;

import java.math.BigDecimal;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Jeu de donnees de demonstration.
 * Active par driveeasy.vehicule.seed-enabled=true
 */
@Component
@ConditionalOnProperty(
        name = "driveeasy.vehicule.seed-enabled",
        havingValue = "true"
)
public class DataSeeder implements ApplicationRunner {

    private final VehiculeRepository repository;

    public DataSeeder(VehiculeRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {

        String base = "https://commons.wikimedia.org/wiki/Special:FilePath/";

        add("123 TUN 4567", "Renault", "Clio 5",
                2022, "Blanc", Categorie.ECONOMIQUE,
                Carburant.ESSENCE, Boite.MANUELLE,
                5, 18500, "90", StatutVehicule.DISPONIBLE,
                base + "Renault_Clio_V_1X7A0309.jpg?width=800");

        add("234 TUN 1122", "Peugeot", "208",
                2023, "Gris", Categorie.ECONOMIQUE,
                Carburant.ESSENCE, Boite.MANUELLE,
                5, 9200, "95", StatutVehicule.DISPONIBLE,
                base + "Peugeot_208_PureTech_130_EAT8_Allure_GT-Line_(II)_%E2%80%93_f_17102021.jpg?width=800");

        add("345 TUN 9988", "Volkswagen", "Golf 8",
                2022, "Noir", Categorie.BERLINE,
                Carburant.DIESEL, Boite.AUTOMATIQUE,
                5, 30200, "140", StatutVehicule.DISPONIBLE,
                base + "VW_Golf_1.5_TSI_GOAL_(VIII,_Facelift)_%E2%80%93_f_31122024.jpg?width=800");

        add("456 TUN 7766", "Kia", "Sportage",
                2023, "Bleu", Categorie.SUV,
                Carburant.HYBRIDE, Boite.AUTOMATIQUE,
                5, 12000, "190", StatutVehicule.DISPONIBLE,
                base + "Kia_Sportage_Hybrid_NQ5_HEV_Besta_Blue_(1).jpg?width=800");

        add("567 TUN 3344", "Hyundai", "Tucson",
                2021, "Rouge", Categorie.SUV,
                Carburant.DIESEL, Boite.AUTOMATIQUE,
                5, 45800, "180", StatutVehicule.DISPONIBLE,
                base + "Hyundai_Tucson_(NX4)_PHEV_IMG_4375.jpg?width=800");

        add("678 TUN 5522", "Mercedes", "Classe C",
                2023, "Noir", Categorie.LUXE,
                Carburant.DIESEL, Boite.AUTOMATIQUE,
                5, 8000, "320", StatutVehicule.DISPONIBLE,
                base + "Mercedes-Benz_W206_IMG_5254.jpg?width=800");

        add("789 TUN 8811", "Tesla", "Model 3",
                2024, "Blanc", Categorie.LUXE,
                Carburant.ELECTRIQUE, Boite.AUTOMATIQUE,
                5, 5000, "350", StatutVehicule.DISPONIBLE,
                base + "Tesla_Model_3_Highland_Pearl_White_Multi-Coat_(1).jpg?width=800");

        add("890 TUN 6600", "Dacia", "Sandero",
                2020, "Vert", Categorie.ECONOMIQUE,
                Carburant.ESSENCE, Boite.MANUELLE,
                5, 72000, "70", StatutVehicule.MAINTENANCE,
                base + "Dacia_Sandero_III_(France)_front_view.jpg?width=800");
    }

    private void add(
            String immat,
            String marque,
            String modele,
            int annee,
            String couleur,
            Categorie cat,
            Carburant carb,
            Boite boite,
            int places,
            int km,
            String prix,
            StatutVehicule statut,
            String imageUrl
    ) {

        repository.save(
                Vehicule.builder()
                        .immatriculation(immat)
                        .marque(marque)
                        .modele(modele)
                        .annee(annee)
                        .couleur(couleur)
                        .categorie(cat)
                        .carburant(carb)
                        .boite(boite)
                        .nbPlaces(places)
                        .kilometrage(km)
                        .prixJournalier(new BigDecimal(prix))
                        .statut(statut)
                        .imageUrl(imageUrl)
                        .build()
        );
    }
}
