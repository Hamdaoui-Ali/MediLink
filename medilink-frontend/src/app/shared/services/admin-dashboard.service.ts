import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { DashboardOverview } from '../models/dashboard-overview.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class AdminDashboardService {
  private readonly api = inject(ApiService);

  getOverview(): Observable<DashboardOverview> {
    return this.api.get<DashboardOverview>('/v1/admin/dashboard').pipe(
      map((response) => (response.data ?? response) as DashboardOverview)
    );
  }
}
