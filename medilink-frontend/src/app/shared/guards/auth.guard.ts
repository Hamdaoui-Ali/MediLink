import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthRole } from '../models/auth.model';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login'], {
      queryParams: {
        returnUrl: state.url
      }
    });
  }

  const roles = route.data['roles'] as AuthRole[] | undefined;

  if (roles?.length && !authService.hasAnyRole(roles)) {
    const role = authService.currentRole();
    return router.createUrlTree([role ? authService.getDefaultRouteForRole(role) : '/']);
  }

  return true;
};

export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const role = authService.currentRole();

  return role ? router.createUrlTree([authService.getDefaultRouteForRole(role)]) : true;
};
