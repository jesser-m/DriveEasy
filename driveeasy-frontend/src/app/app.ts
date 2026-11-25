import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { LoadingService } from './core/services/loading.service';
import { ToastContainer } from './shared/toast-container';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, ToastContainer],
  template: `
    <div class="shell">
      <aside class="sidebar no-print">
        <div class="brand"><span class="logo">DE</span> DriveEasy</div>
        <nav>
          <a routerLink="/dashboard" routerLinkActive="active">Tableau de bord</a>
          <a routerLink="/vehicules" routerLinkActive="active">Vehicules</a>
          <a routerLink="/clients" routerLinkActive="active">Clients</a>
          <a routerLink="/locations/new" routerLinkActive="active">Nouvelle location</a>
          <a routerLink="/locations" routerLinkActive="active" [routerLinkActiveOptions]="{ exact: true }">Locations</a>
        </nav>
        <div class="sidebar-foot">Espace agent d'agence</div>
      </aside>
      <main class="content">
        <div class="loading-bar no-print" [class.active]="loading.busy()"></div>
        <router-outlet />
      </main>
      <app-toast-container />
    </div>
  `,
})
export class App {
  protected readonly loading = inject(LoadingService);
}
