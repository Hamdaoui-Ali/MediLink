import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { BlockedSlot, BlockedSlotRequest } from '../models/blocked-slot.model';
import { ApiService } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class BlockedSlotService {
  private readonly api = inject(ApiService);

  listDoctorBlockedSlots(): Observable<BlockedSlot[]> {
    return this.api.get<BlockedSlot[]>('/v1/doctor/blocked-slots').pipe(
      map((response) => (response.data ?? response) as BlockedSlot[])
    );
  }

  createBlockedSlot(request: BlockedSlotRequest): Observable<BlockedSlot> {
    return this.api.post<BlockedSlot>('/v1/doctor/blocked-slots', request).pipe(
      map((response) => (response.data ?? response) as BlockedSlot)
    );
  }

  updateBlockedSlot(id: number, request: BlockedSlotRequest): Observable<BlockedSlot> {
    return this.api.patch<BlockedSlot>(`/v1/doctor/blocked-slots/${id}`, request).pipe(
      map((response) => (response.data ?? response) as BlockedSlot)
    );
  }

  deleteBlockedSlot(id: number): Observable<void> {
    return this.api.delete<void>(`/v1/doctor/blocked-slots/${id}`).pipe(
      map(() => undefined)
    );
  }
}
