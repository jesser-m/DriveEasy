import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ApiError } from '../models/models';
import { ToastService } from '../services/toast.service';

/** Transforme les erreurs de l'API (ApiError JSON des microservices) en notifications lisibles. */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      const api = err.error as Partial<ApiError> | null;
      let message: string;
      if (err.status === 0) {
        message = "Impossible de joindre l'API Gateway (http://localhost:8080). Le backend est-il demarre ?";
      } else {
        message = api?.message ?? `Erreur ${err.status}`;
        if (api?.details?.length) message += ' : ' + api.details.join(' ; ');
      }
      toast.error(message);
      return throwError(() => err);
    }),
  );
};
