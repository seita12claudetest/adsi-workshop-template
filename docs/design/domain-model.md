# ドメインモデル設計

## ドメイン概要図

```
┌─────────────────────────────────────────────────────────────────┐
│                        勤怠管理ドメイン                            │
├─────────────┬──────────────┬──────────────┬────────────────────┤
│  組織・社員   │   打刻・勤怠   │  申請・承認    │     通知          │
│             │              │              │                    │
│ Organization│ TimeRecord   │ Application  │ Notification       │
│ Department  │ DailyWork    │ Approval     │                    │
│ Section     │ MonthlyWork  │              │                    │
│ Employee    │              │              │                    │
│ Office      │              │              │                    │
└─────────────┴──────────────┴──────────────┴────────────────────┘
```

## Entity 一覧

### 組織・社員ドメイン

#### Employee（社員）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 社員ID（自動採番） |
| employeeCode | String | 社員コード（表示用） |
| name | String | 氏名 |
| email | String | メールアドレス（ログインID） |
| password | String | パスワード（BCryptハッシュ） |
| role | Role (Enum) | EMPLOYEE / MANAGER / ADMIN |
| sectionId | Long (FK) | 所属課 |
| hireDate | LocalDate | 入社日 |
| active | boolean | 有効フラグ |
| version | Long | 楽観ロック |

#### Organization（本部）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 本部ID |
| name | String | 本部名 |
| code | String | 本部コード |

#### Department（部）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 部ID |
| name | String | 部名 |
| code | String | 部コード |
| organizationId | Long (FK) | 所属本部 |

#### Section（課）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 課ID |
| name | String | 課名 |
| code | String | 課コード |
| departmentId | Long (FK) | 所属部 |
| managerId | Long (FK) | 課長（この課の社員の上長） |

#### Office（拠点）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 拠点ID |
| name | String | 拠点名 |
| address | String | 住所 |
| latitude | Double | 緯度 |
| longitude | Double | 経度 |
| radiusMeters | Integer | 許可半径（メートル） |

### 打刻・勤怠ドメイン

#### TimeRecord（打刻記録）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 打刻ID |
| employeeId | Long (FK) | 社員ID |
| type | TimeRecordType (Enum) | CLOCK_IN / CLOCK_OUT / BREAK_START / BREAK_END |
| recordedAt | LocalDateTime | 打刻日時 |
| latitude | Double | 打刻時の緯度 |
| longitude | Double | 打刻時の経度 |
| officeId | Long (FK, nullable) | 最寄り拠点（エリア内の場合） |
| withinArea | boolean | 許可エリア内か |
| date | LocalDate | 打刻対象日 |

#### DailyAttendance（日次勤怠）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 日次勤怠ID |
| employeeId | Long (FK) | 社員ID |
| date | LocalDate | 対象日 |
| clockIn | LocalTime | 出勤時刻 |
| clockOut | LocalTime | 退勤時刻 |
| breakStart | LocalTime | 休憩開始 |
| breakEnd | LocalTime | 休憩終了 |
| workingMinutes | Integer | 実労働時間（分） |
| overtimeMinutes | Integer | 残業時間（分） |
| breakMinutes | Integer | 休憩時間（分） |
| status | DailyStatus (Enum) | NORMAL / ABSENT / HOLIDAY / PAID_LEAVE |
| version | Long | 楽観ロック |

#### MonthlyAttendance（月次勤怠）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 月次勤怠ID |
| employeeId | Long (FK) | 社員ID |
| yearMonth | YearMonth | 対象年月 |
| totalWorkingMinutes | Integer | 総労働時間（分） |
| totalOvertimeMinutes | Integer | 総残業時間（分） |
| workingDays | Integer | 出勤日数 |
| paidLeaveDays | BigDecimal | 有給取得日数（半休=0.5） |
| status | MonthlyStatus (Enum) | OPEN / SUBMITTED / APPROVED |
| version | Long | 楽観ロック |

### 申請・承認ドメイン

#### Application（申請）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 申請ID |
| applicantId | Long (FK) | 申請者（社員ID） |
| type | ApplicationType (Enum) | LEAVE / OVERTIME / TIME_CORRECTION / MONTHLY_CONFIRM |
| status | ApplicationStatus (Enum) | PENDING / APPROVED / REJECTED |
| appliedAt | LocalDateTime | 申請日時 |
| reason | String | 申請理由 |
| version | Long | 楽観ロック |

#### LeaveApplication（休暇申請明細）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 休暇申請ID |
| applicationId | Long (FK) | 申請ID |
| leaveType | LeaveType (Enum) | ANNUAL / HALF_AM / HALF_PM / HOURLY / CONDOLENCE / CHILDCARE / NURSING / MENSTRUAL |
| startDate | LocalDate | 開始日 |
| endDate | LocalDate | 終了日 |
| hours | BigDecimal | 時間有給の場合の時間数 |

#### OvertimeApplication（残業申請明細）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 残業申請ID |
| applicationId | Long (FK) | 申請ID |
| date | LocalDate | 残業予定日 |
| expectedMinutes | Integer | 予定残業時間（分） |
| applicationType | OvertimeType (Enum) | PRE / POST |

#### TimeCorrectionApplication（打刻修正申請明細）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 修正申請ID |
| applicationId | Long (FK) | 申請ID |
| date | LocalDate | 対象日 |
| originalClockIn | LocalTime | 修正前出勤 |
| originalClockOut | LocalTime | 修正前退勤 |
| correctedClockIn | LocalTime | 修正後出勤 |
| correctedClockOut | LocalTime | 修正後退勤 |

#### Approval（承認）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 承認ID |
| applicationId | Long (FK) | 申請ID |
| approverId | Long (FK) | 承認者（上長 or 管理者） |
| action | ApprovalAction (Enum) | APPROVED / REJECTED |
| comment | String | コメント（差戻時必須） |
| decidedAt | LocalDateTime | 承認・差戻日時 |

### 有給管理ドメイン

#### LeaveBalance（有給残高）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 有給残高ID |
| employeeId | Long (FK) | 社員ID |
| fiscalYear | Integer | 年度（付与年度） |
| grantedDays | BigDecimal | 付与日数 |
| usedDays | BigDecimal | 消化日数 |
| remainingDays | BigDecimal | 残日数 |
| grantDate | LocalDate | 付与日 |
| expiryDate | LocalDate | 失効日（付与日+2年） |
| version | Long | 楽観ロック |

### 通知ドメイン

#### Notification（通知）

| フィールド | 型 | 説明 |
|-----------|------|------|
| id | Long (PK) | 通知ID |
| recipientId | Long (FK) | 受信者（社員ID） |
| type | NotificationType (Enum) | CLOCK_REMINDER / APPROVAL_REQUEST / APPROVAL_RESULT / LEAVE_BALANCE_ALERT |
| title | String | 通知タイトル |
| message | String | 通知本文 |
| read | boolean | 既読フラグ |
| createdAt | LocalDateTime | 作成日時 |
| relatedApplicationId | Long (FK, nullable) | 関連申請ID |

## Value Object

| 名前 | 型 | 説明 |
|------|------|------|
| WorkingTime | int (minutes) | 労働時間（分単位で保持） |
| Location | (latitude, longitude) | 位置情報 |
| YearMonth | java.time.YearMonth | 対象年月 |
| DateRange | (startDate, endDate) | 日付範囲 |

## Enum 定義

| Enum | 値 |
|------|------|
| Role | EMPLOYEE, MANAGER, ADMIN |
| TimeRecordType | CLOCK_IN, CLOCK_OUT, BREAK_START, BREAK_END |
| DailyStatus | NORMAL, ABSENT, HOLIDAY, PAID_LEAVE |
| MonthlyStatus | OPEN, SUBMITTED, APPROVED |
| ApplicationType | LEAVE, OVERTIME, TIME_CORRECTION, MONTHLY_CONFIRM |
| ApplicationStatus | PENDING, APPROVED, REJECTED |
| ApprovalAction | APPROVED, REJECTED |
| LeaveType | ANNUAL, HALF_AM, HALF_PM, HOURLY, CONDOLENCE, CHILDCARE, NURSING, MENSTRUAL |
| OvertimeType | PRE, POST |
| NotificationType | CLOCK_REMINDER, APPROVAL_REQUEST, APPROVAL_RESULT, LEAVE_BALANCE_ALERT |

## Service 一覧

| Service | 責務 |
|---------|------|
| AuthService | 認証（ログイン / JWT 発行 / リフレッシュ） |
| TimeRecordService | 打刻処理、位置情報判定 |
| AttendanceService | 日次・月次勤怠計算・集計 |
| ApplicationService | 申請作成・取消 |
| ApprovalService | 承認・差戻処理 |
| LeaveBalanceService | 有給付与・消化・残高計算 |
| NotificationService | 通知生成・既読管理 |
| EmployeeService | 社員 CRUD |
| OrganizationService | 組織 CRUD |
| OfficeService | 拠点 CRUD・距離計算 |

## Repository 一覧

| Repository | 主なメソッド |
|-----------|------------|
| EmployeeRepository | findByEmail, findBySectionId, findByManagerId |
| OrganizationRepository | findAll |
| DepartmentRepository | findByOrganizationId |
| SectionRepository | findByDepartmentId |
| OfficeRepository | findAll |
| TimeRecordRepository | findByEmployeeIdAndDate |
| DailyAttendanceRepository | findByEmployeeIdAndDate, findByEmployeeIdAndYearMonth |
| MonthlyAttendanceRepository | findByEmployeeIdAndYearMonth |
| ApplicationRepository | findByApplicantId, findPendingByApproverId |
| LeaveApplicationRepository | findByApplicationId |
| OvertimeApplicationRepository | findByApplicationId |
| TimeCorrectionApplicationRepository | findByApplicationId |
| ApprovalRepository | findByApplicationId |
| LeaveBalanceRepository | findByEmployeeIdAndFiscalYear, findActiveByEmployeeId |
| NotificationRepository | findByRecipientIdAndReadFalse, countUnreadByRecipientId |
