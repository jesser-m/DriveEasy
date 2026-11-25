import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { LocationService } from '../services/location.service';
import { ToastService } from '../services/toast.service';

/** /locations/:id/retour n'est accessible que pour une location EN_COURS. */
export const retourAllowedGuard: CanActivateFn = (route) => {
  const locations = inject(LocationService);
  const router = inject(Router);
  const toast = inject(ToastService);
  const back = router.createUrlTree(['/locations']);
  return locations.get(route.paramMap.get('id')!).pipe(
    map((d) => {
      if (d.location.statut === 'EN_COURS') return true;
      toast.error(`Le retour est impossible : la location est ${d.location.statut}.`);
      return back;
    }),
    catchError(() => of(back)),
  );
};
