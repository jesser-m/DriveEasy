import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, startWith, switchMap } from 'rxjs';
import { Client } from '../../core/models/models';
import { todayIso } from '../../core/utils/date-utils';
import { ClientService } from '../../core/services/client.service';
import { Pagination } from '../../shared/pagination';
import { StatusBadge } from '../../shared/status-badge';

const PAGE_SIZE = 8;

@Component({
  selector: 'app-client-list',
  imports: [RouterLink, DatePipe, StatusBadge, Pagination],
  template: `
    <header class="page-head">
      <div><h1>Clients</h1><p class="muted">{{ clients().length }} client(s)</p></div>
    </header>

    <section class="card filters">
      <label class="grow">Recherche (nom, CIN ou email)
        <input type="search" placeholder="Ex. Trabelsi, 12345678, ahmed@..." (input)="search$.next($any($event.target).value)" />
      </label>
    </section>

    <section class="card table-card">
      <table>
        <thead>
          <tr><th>Client</th><th>CIN</th><th>Email</th><th>Categorie</th><th>Permis valable jusqu'au</th><th></th></tr>
        </thead>
        <tbody>
          @for (c of pageItems(); track c.id) {
            <tr>
              <td><strong>{{ c.prenom }} {{ c.nom }}</strong></td>
              <td>{{ c.cin }}</td>
              <td>{{ c.email }}</td>
              <td><app-badge [value]="c.categorieClient" /></td>
              <td>
                {{ c.dateExpirationPermis | date: 'dd/MM/yyyy' }}
                @if (expire(c)) { <span class="alert-chip">Permis expire</span> }
              </td>
              <td class="right"><a class="btn small" [routerLink]="['/clients', c.id]">Fiche</a></td>
            </tr>
          } @empty {
            <tr><td colspan="6" class="empty">Aucun client trouve.</td></tr>
          }
        </tbody>
      </table>
    </section>
    <app-pagination [page]="page()" [totalPages]="totalPages()" (pageChange)="page.set($event)" />
  `,
})
export class ClientList {
  private readonly service = inject(ClientService);
  private readonly today = todayIso();

  protected readonly search$ = new Subject<string>();
  protected readonly clients = signal<Client[]>([]);
  protected readonly page = signal(0);
  protected readonly totalPages = computed(() => Math.ceil(this.clients().length / PAGE_SIZE));
  protected readonly pageItems = computed(() => this.clients().slice(this.page() * PAGE_SIZE, (this.page() + 1) * PAGE_SIZE));

  constructor() {
    this.search$.pipe(
      startWith(''), debounceTime(250), distinctUntilChanged(),
      switchMap((q) => this.service.list(q)), takeUntilDestroyed(),
    ).subscribe((list) => { this.clients.set(list); this.page.set(0); });
  }

  protected expire(c: Client): boolean {
    return c.dateExpirationPermis < this.today;
  }
}
