import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { Specialty } from '../models/specialty.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class SpecialtyLookupService {
  private readonly api = inject(ApiService);

  listActive(): Observable<Specialty[]> {
    return this.api.get<Specialty[]>('/v1/specialties?activeOnly=true').pipe(
      map((response) => (response.data ?? response) as Specialty[])
    );
  }
}
