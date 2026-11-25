export type StatutVehicule = 'DISPONIBLE' | 'LOUE' | 'MAINTENANCE';
export type Carburant = 'ESSENCE' | 'DIESEL' | 'HYBRIDE' | 'ELECTRIQUE';
export type Boite = 'MANUELLE' | 'AUTOMATIQUE';
export type Categorie = 'ECONOMIQUE' | 'BERLINE' | 'SUV' | 'LUXE';
export type CategorieClient = 'STANDARD' | 'FIDELE' | 'VIP';
export type StatutLocation = 'RESERVEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE';

export interface Vehicule {
  id: number;
  immatriculation: string;
  marque: string;
  modele: string;
  annee: number;
  couleur: string | null;
  categorie: Categorie;
  carburant: Carburant;
  boite: Boite;
  nbPlaces: number;
  kilometrage: number;
  prixJournalier: number;
  statut: StatutVehicule;
  imageUrl: string | null;
}
export type VehiculeRequest = Omit<Vehicule, 'id'>;

export interface VehiculeFilters {
  categorie?: Categorie | '';
  carburant?: Carburant | '';
  statut?: StatutVehicule | '';
  prixMax?: number | null;
}

export interface Client {
  id: number;
  cin: string;
  nom: string;
  prenom: string;
  dateNaissance: string;
  email: string;
  telephone: string | null;
  adresse: string | null;
  numeroPermis: string;
  dateExpirationPermis: string;
  categorieClient: CategorieClient;
  dateInscription: string;
}

export interface Eligibilite {
  clientId: number;
  eligible: boolean;
  age: number;
  permisValide: boolean;
  motifs: string[];
}

export interface Location {
  id: number;
  clientId: number;
  vehiculeId: number;
  dateDebut: string;
  dateFinPrevue: string;
  dateRetourEffectif: string | null;
  kmDepart: number | null;
  kmRetour: number | null;
  prixJournalierApplique: number;
  montantTotal: number;
  penalite: number;
  statut: StatutLocation;
  dateCreation: string;
}

export interface LocationDetail {
  location: Location;
  client: Client | null;
  vehicule: Vehicule | null;
}

export interface LocationRequest {
  clientId: number;
  vehiculeId: number;
  dateDebut: string;
  dateFinPrevue: string;
}

export interface RetourRequest {
  kmRetour: number;
  dateRetourEffectif: string;
}

export interface PriceDetail {
  jours: number;
  prixJournalier: number;
  categorieVehicule: string;
  coefficientCategorie: number;
  sousTotal: number;
  categorieClient: string;
  remisePourcent: number;
  montantRemise: number;
  montantTotal: number;
}

export interface LocationFilters {
  statut?: StatutLocation | '';
  clientId?: number | null;
  dateDebut?: string;
  dateFin?: string;
  page?: number;
  size?: number;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface TopVehicule {
  vehiculeId: number;
  libelle: string;
  nombreLocations: number;
}

export interface Statistiques {
  debut: string;
  fin: string;
  chiffreAffaires: number;
  tauxOccupation: number;
  nombreVehicules: number;
  locationsParStatut: Partial<Record<StatutLocation, number>>;
  topVehicules: TopVehicule[];
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  path: string;
  details: string[];
}
