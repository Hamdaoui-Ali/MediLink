import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DoctorAvailability, DAY_NAMES } from '../../shared/models/doctor-availability.model';
import { DoctorAvailabilityService } from '../../shared/services/doctor-availability.service';

interface SlotWithDuration extends DoctorAvailability {
  duration: string;
}

interface DayGroup {
  day: string;
  slots: SlotWithDuration[];
}

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './doctor-availability.page.html',
  styleUrl: './doctor-availability.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorAvailabilityPage implements OnInit {
  private readonly availabilityService = inject(DoctorAvailabilityService);
  private readonly formBuilder = inject(FormBuilder);

  readonly slots = signal<DoctorAvailability[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly saving = signal(false);
  readonly fieldError = signal<string | null>(null);
  readonly successMessage = signal<string | null>(null);
  readonly editingSlot = signal<DoctorAvailability | null>(null);

  readonly availabilityForm = this.formBuilder.nonNullable.group({
    dayOfWeek: [1, [Validators.required, Validators.min(1), Validators.max(7)]],
    startTime: ['09:00', Validators.required],
    endTime: ['17:00', Validators.required]
  });

  readonly groupedSlots = computed<DayGroup[]>(() => {
    const groups = new Map<number, DoctorAvailability[]>();
    for (const slot of this.slots()) {
      const existing = groups.get(slot.dayOfWeek) ?? [];
      existing.push(slot);
      groups.set(slot.dayOfWeek, existing);
    }

    return Array.from(groups.entries())
      .sort(([a], [b]) => a - b)
      .map(([day, daySlots]) => ({
        day: DAY_NAMES[day] ?? `Day ${day}`,
        slots: daySlots
          .sort((a, b) => a.startTime.localeCompare(b.startTime))
          .map((s): SlotWithDuration => ({
            ...s,
            duration: this.computeDuration(s.startTime, s.endTime)
          }))
      }));
  });

  ngOnInit(): void {
    this.loadAvailability();
  }

  loadAvailability(): void {
    this.loading.set(true);
    this.error.set(null);

    this.availabilityService.list().subscribe({
      next: (data) => {
        this.slots.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load availability. Please try again.');
        this.loading.set(false);
      }
    });
  }

  submitForm(): void {
    if (this.availabilityForm.invalid) return;

    this.fieldError.set(null);
    this.successMessage.set(null);
    this.saving.set(true);

    const formValue = this.availabilityForm.getRawValue();
    const request = {
      dayOfWeek: formValue.dayOfWeek,
      startTime: formValue.startTime + ':00',
      endTime: formValue.endTime + ':00'
    };

    const editing = this.editingSlot();

    if (editing) {
      this.availabilityService.update(editing.id, request).subscribe({
        next: (updated) => this.handleSaveSuccess(updated, 'Availability updated.'),
        error: (err) => this.handleSaveError(err)
      });
    } else {
      this.availabilityService.add(request).subscribe({
        next: (created) => this.handleSaveSuccess(created, 'Availability slot added.'),
        error: (err) => this.handleSaveError(err)
      });
    }
  }

  startEdit(slot: DoctorAvailability): void {
    this.editingSlot.set(slot);
    this.fieldError.set(null);
    this.successMessage.set(null);

    this.availabilityForm.setValue({
      dayOfWeek: slot.dayOfWeek,
      startTime: slot.startTime.substring(0, 5),
      endTime: slot.endTime.substring(0, 5)
    });
  }

  cancelEdit(): void {
    this.editingSlot.set(null);
    this.fieldError.set(null);
    this.availabilityForm.setValue({ dayOfWeek: 1, startTime: '09:00', endTime: '17:00' });
  }

  confirmRemove(slot: DoctorAvailability): void {
    this.fieldError.set(null);
    this.successMessage.set(null);
    this.saving.set(true);

    this.availabilityService.remove(slot.id).subscribe({
      next: () => {
        this.slots.update((s) => s.filter((sl) => sl.id !== slot.id));
        this.successMessage.set('Availability slot removed.');
        this.saving.set(false);
      },
      error: () => {
        this.fieldError.set('Failed to remove the slot. Please try again.');
        this.saving.set(false);
      }
    });
  }

  private handleSaveSuccess(saved: DoctorAvailability, message: string): void {
    this.slots.update((s) => {
      const index = s.findIndex((sl) => sl.id === saved.id);
      if (index >= 0) {
        const updated = [...s];
        updated[index] = saved;
        return updated.filter((sl) => sl.isActive);
      }
      return [...s, saved];
    });
    this.successMessage.set(message);
    this.editingSlot.set(null);
    this.availabilityForm.setValue({ dayOfWeek: 1, startTime: '09:00', endTime: '17:00' });
    this.saving.set(false);
  }

  private handleSaveError(err: any): void {
    const msg = err?.error?.error?.message ?? err?.message ?? 'An error occurred. Please try again.';
    this.fieldError.set(msg);
    this.saving.set(false);
  }

  private computeDuration(start: string, end: string): string {
    const [sh, sm] = start.split(':').map(Number);
    const [eh, em] = end.split(':').map(Number);
    const minutes = (eh * 60 + em) - (sh * 60 + sm);
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (mins === 0) return `${hours}h`;
    return `${hours}h ${mins}m`;
  }
}
