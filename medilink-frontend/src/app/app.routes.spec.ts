import { routes } from './app.routes';

describe('app routes', () => {
  it('should protect admin doctor management for admin users only', () => {
    const route = routes.find((item) => item.path === 'admin/doctors');

    expect(route).toBeTruthy();
    expect(route?.canActivate?.length).toBeGreaterThan(0);
    expect(route?.data?.['roles']).toEqual(['ADMIN']);
  });
});
