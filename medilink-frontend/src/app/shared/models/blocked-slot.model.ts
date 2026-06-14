export interface BlockedSlot {
  id: number;
  doctorId: number;
  blockDate: string;
  startTime: string;
  endTime: string;
  reason: string | null;
}

export interface BlockedSlotRequest {
  blockDate: string;
  startTime: string;
  endTime: string;
  reason: string | null;
}
