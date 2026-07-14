export type Role = "EMPLOYEE" | "MANAGER" | "ADMIN";

export interface Employee {
  id: number;
  employeeCode: string;
  name: string;
  email: string;
  role: Role;
  sectionId: number;
  hireDate: string;
  active: boolean;
}

export type ApplicationType = "LEAVE" | "OVERTIME" | "TIME_CORRECTION";
export type ApplicationStatus = "PENDING" | "APPROVED" | "REJECTED";
export type ApprovalAction = "APPROVED" | "REJECTED";
export type LeaveType = "ANNUAL" | "HALF_AM" | "HALF_PM" | "HOURLY" | "CONDOLENCE" | "CHILDCARE" | "NURSING" | "MENSTRUAL";
export type OvertimeType = "PRE" | "POST";
export type TimeRecordType = "CLOCK_IN" | "CLOCK_OUT" | "BREAK_START" | "BREAK_END";

export interface ApplicationResponse {
  id: number;
  applicantId: number;
  applicantName: string;
  type: ApplicationType;
  status: ApplicationStatus;
  appliedAt: string;
  reason: string | null;
  detail: unknown;
}

export interface ApprovalResponse {
  applicationId: number;
  action: ApprovalAction;
  decidedAt: string;
}

export interface TimeRecordResponse {
  id: number;
  type: TimeRecordType;
  recordedAt: string;
  withinArea: boolean;
  officeName: string | null;
  warning: string | null;
}

export interface DailyAttendanceResponse {
  date: string;
  clockIn: string | null;
  clockOut: string | null;
  breakStart: string | null;
  breakEnd: string | null;
  workingMinutes: number | null;
  overtimeMinutes: number | null;
  breakMinutes: number | null;
  status: string;
}

export interface MonthlyAttendanceResponse {
  yearMonth: string;
  totalWorkingMinutes: number;
  totalOvertimeMinutes: number;
  workingDays: number;
  paidLeaveDays: number;
  status: string;
}

export interface Organization {
  id: number;
  name: string;
  code: string;
}

export interface Department {
  id: number;
  organizationId: number;
  name: string;
  code: string;
}

export interface Section {
  id: number;
  departmentId: number;
  name: string;
  code: string;
  managerId: number | null;
}

export interface Office {
  id: number;
  name: string;
  address: string;
  latitude: number;
  longitude: number;
  radiusMeters: number;
}
