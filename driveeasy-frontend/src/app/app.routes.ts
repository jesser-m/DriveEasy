import { Routes } from '@angular/router';
import { retourAllowedGuard } from './core/guards/retour-allowed.guard';
import { unsavedChangesGuard } from './core/guards/unsaved-changes.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard', title: 'Tableau de bord - DriveEasy',
    loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    path: 'vehicules', title: 'Vehicules - DriveEasy',
    loadComponent: () => import('./pages/vehicules/vehicule-list').then((m) => m.VehiculeList),
  },
  {
    path: 'vehicules/new', title: 'Nouveau vehicule - DriveEasy', canDeactivate: [unsavedChangesGuard],
    loadComponent: () => import('./pages/vehicules/vehicule-form').then((m) => m.VehiculeForm),
  },
  {
    path: 'vehicules/:id/edit', title: 'Modifier un vehicule - DriveEasy', canDeactivate: [unsavedChangesGuard],
    loadComponent: () => import('./pages/vehicules/vehicule-form').then((m) => m.VehiculeForm),
  },
  {
    path: 'clients', title: 'Clients - DriveEasy',
    loadComponent: () => import('./pages/clients/client-list').then((m) => m.ClientList),
  },
  {
    path: 'clients/:id', title: 'Fiche client - DriveEasy',
    loadComponent: () => import('./pages/clients/client-detail').then((m) => m.ClientDetail),
  },
  {
    path: 'locations', title: 'Locations - DriveEasy',
    loadComponent: () => import('./pages/locations/location-list').then((m) => m.LocationList),
  },
  {
    path: 'locations/new', title: 'Nouvelle location - DriveEasy',
    loadComponent: () => import('./pages/locations/location-new').then((m) => m.LocationNew),
  },
  {
    path: 'locations/:id/retour', title: 'Retour de vehicule - DriveEasy', canActivate: [retourAllowedGuard],
    loadComponent: () => import('./pages/locations/location-retour').then((m) => m.LocationRetour),
  },
  { path: '**', redirectTo: 'dashboard' },
];
