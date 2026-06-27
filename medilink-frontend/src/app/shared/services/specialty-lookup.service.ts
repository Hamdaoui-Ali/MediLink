import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { SpecialtyOption } from '../models/admin-doctor.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class SpecialtyLookupService {
  private readonly api = inject(ApiService);

  listActive(): Observable<SpecialtyOption[]> {
    return this.api.get<SpecialtyOption[]>('/v1/specialties?activeOnly=true').pipe(
      map((response) => (response.data ?? response) as SpecialtyOption[])
    );
  }
}
