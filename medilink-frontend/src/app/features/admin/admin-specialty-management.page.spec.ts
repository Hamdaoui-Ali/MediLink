import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Specialty } from '../../shared/models/specialty.model';
import { SpecialtyManagementService } from '../../shared/services/specialty-management.service';
import { AdminSpecialtyManagementPage } from './admin-specialty-management.page';

describe('AdminSpecialtyManagementPage', () => {
  let fixture: ComponentFixture<AdminSpecialtyManagementPage>;
  let component: AdminSpecialtyManagementPage;
  let specialtyService: {
    listAll: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    activate: ReturnType<typeof vi.fn>;
    deactivate: ReturnType<typeof vi.fn>;
  };

  const specialties: Specialty[] = [
    { id: 1, name: 'Cardiology', description: 'Heart care', status: 'ACTIVE' },
    { id: 2, name: 'Dermatology', description: null, status: 'INACTIVE' }
  ];

  beforeEach(async () => {
    specialtyService = {
      listAll: vi.fn().mockReturnValue(of(specialties)),
      create: vi.fn(),
      update: vi.fn(),
      activate: vi.fn(),
      deactivate: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [AdminSpecialtyManagementPage],
      providers: [
        provideRouter([]),
        {
          provide: SpecialtyManagementService,
          useValue: specialtyService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminSpecialtyManagementPage);
    component = fixture.componentInstance;
  });

  it('should load all specialties on init', () => {
    fixture.detectChanges();

    expect(specialtyService.listAll).toHaveBeenCalled();
    expect(component.specialties()).toEqual(specialties);
    expect(component.isLoading()).toBeFalsy();
  });

  it('should normalize form values into a request', () => {
    component.specialtyForm.setValue({
      name: '  Cardiology  ',
      description: '   '
    });

    expect(component.toRequest()).toEqual({
      name: 'Cardiology',
      description: null
    });
  });

  it('should create a specialty and add it to the list', () => {
    fixture.detectChanges();
    const created: Specialty = { id: 3, name: 'Neurology', description: 'Brain care', status: 'ACTIVE' };
    specialtyService.create.mockReturnValue(of(created));
    component.specialtyForm.setValue({
      name: ' Neurology ',
      description: ' Brain care '
    });

    component.submit();

    expect(specialtyService.create).toHaveBeenCalledWith({
      name: 'Neurology',
      description: 'Brain care'
    });
    expect(component.specialties().map((specialty) => specialty.id)).toContain(3);
    expect(component.successMessage()).toBe('Specialty created.');
  });

  it('should edit a selected specialty without creating a new one', () => {
    fixture.detectChanges();
    const updated: Specialty = { id: 1, name: 'Cardiology Updated', description: null, status: 'ACTIVE' };
    specialtyService.update.mockReturnValue(of(updated));

    component.startEdit(specialties[0]);
    component.specialtyForm.setValue({
      name: 'Cardiology Updated',
      description: ''
    });
    component.submit();

    expect(specialtyService.update).toHaveBeenCalledWith(1, {
      name: 'Cardiology Updated',
      description: null
    });
    expect(specialtyService.create).not.toHaveBeenCalled();
    expect(component.specialties().find((specialty) => specialty.id === 1)?.name).toBe('Cardiology Updated');
  });

  it('should activate and deactivate specialties from the list', () => {
    fixture.detectChanges();
    const activated: Specialty = { ...specialties[1], status: 'ACTIVE' };
    const deactivated: Specialty = { ...specialties[0], status: 'INACTIVE' };
    specialtyService.activate.mockReturnValue(of(activated));
    specialtyService.deactivate.mockReturnValue(of(deactivated));

    component.activate(specialties[1]);
    component.deactivate(specialties[0]);

    expect(specialtyService.activate).toHaveBeenCalledWith(2);
    expect(specialtyService.deactivate).toHaveBeenCalledWith(1);
    expect(component.specialties().find((specialty) => specialty.id === 1)?.status).toBe('INACTIVE');
    expect(component.specialties().find((specialty) => specialty.id === 2)?.status).toBe('ACTIVE');
  });

  it('should show permission errors when loading is forbidden', () => {
    specialtyService.listAll.mockReturnValue(throwError(() => ({ status: 403 })));

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('You do not have permission to manage specialties.');
    expect(component.isLoading()).toBeFalsy();
  });
});
