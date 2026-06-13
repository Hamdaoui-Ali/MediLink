import { routes } from './app.routes';
import { authGuard } from './shared/guards/auth.guard';

describe('app routes', () => {
  it('should protect the admin specialty management route for admins', () => {
    const route = routes.find((candidate) => candidate.path === 'admin/specialties');

    expect(route).toBeTruthy();
    expect(route?.canActivate).toContain(authGuard);
    expect(route?.data?.['roles']).toEqual(['ADMIN']);
  });
});
