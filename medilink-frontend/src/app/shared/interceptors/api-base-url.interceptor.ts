import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export const apiBaseUrlInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/')) {
    return next(request);
  }

  return next(request.clone({
    url: `${environment.apiBaseUrl}${request.url}`
  }));
};
