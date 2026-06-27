import { AuthRole } from './auth.model';

export interface NavigationItem {
  label: string;
  route?: string;
  description?: string;
  roles?: AuthRole[];
  guestOnly?: boolean;
  comingSoon?: boolean;
  action?: 'logout';
}
