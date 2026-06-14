import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BlockedSlot, BlockedSlotRequest } from '../../shared/models/blocked-slot.model';
import { BlockedSlotService } from '../../shared/services/blocked-slot.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './doctor-blocked-slots.page.html',
  styleUrl: './doctor-blocked-slots.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorBlockedSlotsPage implements OnInit {
  private readonly blockedSlotService = inject(BlockedSlotService);
  private readonly formBuilder = inject(FormBuilder);

  readonly blockedSlots = signal<BlockedSlot[]>([]);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  readonly slotForm = this.formBuilder.nonNullable.group({
    blockDate: ['', Validators.required],
    startTime: ['', Validators.required],
    endTime: ['', Validators.required],
    reason: ['']
  });

  ngOnInit(): void {
    this.loadBlockedSlots();
  }

  loadBlockedSlots(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.blockedSlotService.listDoctorBlockedSlots().subscribe({
      next: (slots) => {
        this.blockedSlots.set(slots);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to load blocked slots.'));
        this.isLoading.set(false);
      }
    });
  }

  createBlockedSlot(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.slotForm.markAllAsTouched();

    if (this.slotForm.invalid || this.isSaving()) {
      return;
    }

    const request = this.toRequest();

    this.isSaving.set(true);

    this.blockedSlotService.createBlockedSlot(request).subscribe({
      next: (slot) => {
        this.blockedSlots.set([slot, ...this.blockedSlots()]);
        this.slotForm.reset({
          blockDate: '',
          startTime: '',
          endTime: '',
          reason: ''
        });
        this.slotForm.markAsUntouched();
        this.successMessage.set('Blocked slot created.');
        this.isSaving.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to create blocked slot.'));
        this.isSaving.set(false);
      }
    });
  }

  deleteBlockedSlot(slot: BlockedSlot): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    this.blockedSlotService.deleteBlockedSlot(slot.id).subscribe({
      next: () => {
        this.blockedSlots.set(
          this.blockedSlots().filter((item) => item.id !== slot.id)
        );
        this.successMessage.set('Blocked slot removed.');
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to delete blocked slot.'));
      }
    });
  }

  hasError(controlName: keyof typeof this.slotForm.controls): boolean {
    const control = this.slotForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  getMinDate(): string {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  formatTime(time: string): string {
    if (!time) {
      return '';
    }
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  }

  formatDate(date: string): string {
    if (!date) {
      return '';
    }
    const parsed = new Date(date + 'T00:00:00');
    return parsed.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  private toRequest(): BlockedSlotRequest {
    const formValue = this.slotForm.getRawValue();
    return {
      blockDate: formValue.blockDate,
      startTime: formValue.startTime,
      endTime: formValue.endTime,
      reason: formValue.reason.trim() || null
    };
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 403) {
        return 'You do not have permission to manage blocked slots.';
      }
      if (error.status === 401) {
        return 'Your session has expired. Please log in again.';
      }
      if (error.status === 400) {
        return 'Please check your input — times must be valid and the date cannot be in the past.';
      }
    }
    return fallback;
  }
}
