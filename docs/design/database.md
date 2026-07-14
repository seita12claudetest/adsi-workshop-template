# DB 設計

## ER 図

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│ organizations│     │ departments  │     │  sections    │
│──────────────│     │──────────────│     │──────────────│
│ id        PK │◀────│ org_id    FK │     │ dept_id   FK │──▶│departments│
│ name         │     │ id        PK │◀────│ id        PK │
│ code         │     │ name         │     │ name         │
└──────────────┘     │ code         │     │ code         │
                     └──────────────┘     │ manager_id FK│──┐
                                          └──────────────┘  │
                                                            │
┌──────────────────────────────────────────────────────────┐│
│ employees                                                 ││
│──────────────────────────────────────────────────────────││
│ id           PK                                           │◀┘
│ employee_code   (UNIQUE)                                  │
│ name                                                      │
│ email           (UNIQUE)                                  │
│ password                                                  │
│ role            (EMPLOYEE/MANAGER/ADMIN)                  │
│ section_id   FK → sections                                │
│ hire_date                                                 │
│ active                                                    │
│ version                                                   │
└───────────────────────────────────────────────────────────┘
        │
        │ employee_id
        ▼
┌────────────────┐    ┌─────────────────────┐    ┌───────────────────┐
│ time_records   │    │ daily_attendances   │    │ monthly_attendances│
│────────────────│    │─────────────────────│    │───────────────────│
│ id          PK │    │ id               PK │    │ id             PK │
│ employee_id FK │    │ employee_id      FK │    │ employee_id    FK │
│ type           │    │ date                │    │ year_month        │
│ recorded_at    │    │ clock_in            │    │ total_working_min │
│ latitude       │    │ clock_out           │    │ total_overtime_min│
│ longitude      │    │ break_start         │    │ working_days      │
│ office_id   FK │    │ break_end           │    │ paid_leave_days   │
│ within_area    │    │ working_minutes     │    │ status            │
│ date           │    │ overtime_minutes    │    │ version           │
└────────────────┘    │ break_minutes       │    └───────────────────┘
                      │ status              │
                      │ version             │
                      └─────────────────────┘

┌────────────────┐    ┌─────────────────────┐
│ applications   │    │ approvals           │
│────────────────│    │─────────────────────│
│ id          PK │◀───│ application_id   FK │
│ applicant_id FK│    │ id               PK │
│ type           │    │ approver_id      FK │
│ status         │    │ action              │
│ applied_at     │    │ comment             │
│ reason         │    │ decided_at          │
│ version        │    └─────────────────────┘
└────────────────┘
        │
        │ application_id
        ▼
┌────────────────────┐  ┌──────────────────────┐  ┌─────────────────────────────┐
│ leave_applications │  │ overtime_applications │  │ time_correction_applications │
│────────────────────│  │──────────────────────│  │─────────────────────────────│
│ id              PK │  │ id                PK │  │ id                       PK │
│ application_id  FK │  │ application_id    FK │  │ application_id           FK │
│ leave_type         │  │ date                 │  │ date                        │
│ start_date         │  │ expected_minutes     │  │ original_clock_in           │
│ end_date           │  │ overtime_type        │  │ original_clock_out          │
│ hours              │  └──────────────────────┘  │ corrected_clock_in          │
└────────────────────┘                            │ corrected_clock_out         │
                                                  └─────────────────────────────┘

┌────────────────┐    ┌────────────────┐
│ leave_balances │    │ notifications  │
│────────────────│    │────────────────│
│ id          PK │    │ id          PK │
│ employee_id FK │    │ recipient_id FK│
│ fiscal_year    │    │ type           │
│ granted_days   │    │ title          │
│ used_days      │    │ message        │
│ remaining_days │    │ read           │
│ grant_date     │    │ created_at     │
│ expiry_date    │    │ related_app_id │
│ version        │    └────────────────┘
└────────────────┘

┌────────────────┐
│ offices        │
│────────────────│
│ id          PK │
│ name           │
│ address        │
│ latitude       │
│ longitude      │
│ radius_meters  │
└────────────────┘
```

## テーブル定義

### organizations（本部）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 本部ID |
| name | VARCHAR(100) | NOT NULL | 本部名 |
| code | VARCHAR(20) | NOT NULL, UNIQUE | 本部コード |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

### departments（部）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 部ID |
| organization_id | BIGINT | FK → organizations(id), NOT NULL | 所属本部 |
| name | VARCHAR(100) | NOT NULL | 部名 |
| code | VARCHAR(20) | NOT NULL, UNIQUE | 部コード |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

### sections（課）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 課ID |
| department_id | BIGINT | FK → departments(id), NOT NULL | 所属部 |
| name | VARCHAR(100) | NOT NULL | 課名 |
| code | VARCHAR(20) | NOT NULL, UNIQUE | 課コード |
| manager_id | BIGINT | FK → employees(id), NULL | 課長 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

### employees（社員）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 社員ID |
| employee_code | VARCHAR(20) | NOT NULL, UNIQUE | 社員コード |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | メールアドレス |
| password | VARCHAR(255) | NOT NULL | パスワード（BCrypt） |
| role | VARCHAR(20) | NOT NULL | ロール |
| section_id | BIGINT | FK → sections(id), NOT NULL | 所属課 |
| hire_date | DATE | NOT NULL | 入社日 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE | 有効フラグ |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

### offices（拠点）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 拠点ID |
| name | VARCHAR(100) | NOT NULL | 拠点名 |
| address | VARCHAR(255) | NOT NULL | 住所 |
| latitude | DOUBLE PRECISION | NOT NULL | 緯度 |
| longitude | DOUBLE PRECISION | NOT NULL | 経度 |
| radius_meters | INTEGER | NOT NULL | 許可半径（m） |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

### time_records（打刻記録）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 打刻ID |
| employee_id | BIGINT | FK → employees(id), NOT NULL | 社員ID |
| type | VARCHAR(20) | NOT NULL | 打刻種別 |
| recorded_at | TIMESTAMP | NOT NULL | 打刻日時 |
| latitude | DOUBLE PRECISION | | 緯度 |
| longitude | DOUBLE PRECISION | | 経度 |
| office_id | BIGINT | FK → offices(id), NULL | 最寄り拠点 |
| within_area | BOOLEAN | NOT NULL, DEFAULT FALSE | エリア内フラグ |
| date | DATE | NOT NULL | 対象日 |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |

**インデックス**: `(employee_id, date)`, `(employee_id, date, type)`

### daily_attendances（日次勤怠）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 日次勤怠ID |
| employee_id | BIGINT | FK → employees(id), NOT NULL | 社員ID |
| date | DATE | NOT NULL | 対象日 |
| clock_in | TIME | | 出勤時刻 |
| clock_out | TIME | | 退勤時刻 |
| break_start | TIME | | 休憩開始 |
| break_end | TIME | | 休憩終了 |
| working_minutes | INTEGER | | 実労働時間（分） |
| overtime_minutes | INTEGER | | 残業時間（分） |
| break_minutes | INTEGER | | 休憩時間（分） |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'NORMAL' | ステータス |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

**ユニーク制約**: `(employee_id, date)`
**インデックス**: `(employee_id, date)`

### monthly_attendances（月次勤怠）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 月次勤怠ID |
| employee_id | BIGINT | FK → employees(id), NOT NULL | 社員ID |
| year_month | VARCHAR(7) | NOT NULL | 対象年月（YYYY-MM） |
| total_working_minutes | INTEGER | NOT NULL, DEFAULT 0 | 総労働時間（分） |
| total_overtime_minutes | INTEGER | NOT NULL, DEFAULT 0 | 総残業時間（分） |
| working_days | INTEGER | NOT NULL, DEFAULT 0 | 出勤日数 |
| paid_leave_days | DECIMAL(3,1) | NOT NULL, DEFAULT 0 | 有給取得日数 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'OPEN' | ステータス |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

**ユニーク制約**: `(employee_id, year_month)`

### applications（申請）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 申請ID |
| applicant_id | BIGINT | FK → employees(id), NOT NULL | 申請者 |
| type | VARCHAR(30) | NOT NULL | 申請種別 |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' | ステータス |
| applied_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 申請日時 |
| reason | TEXT | | 申請理由 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

**インデックス**: `(applicant_id, status)`, `(type, status)`

### leave_applications（休暇申請明細）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 休暇申請ID |
| application_id | BIGINT | FK → applications(id), NOT NULL, UNIQUE | 申請ID |
| leave_type | VARCHAR(20) | NOT NULL | 休暇種別 |
| start_date | DATE | NOT NULL | 開始日 |
| end_date | DATE | NOT NULL | 終了日 |
| hours | DECIMAL(4,2) | | 時間有給の時間数 |

### overtime_applications（残業申請明細）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 残業申請ID |
| application_id | BIGINT | FK → applications(id), NOT NULL, UNIQUE | 申請ID |
| date | DATE | NOT NULL | 残業予定日 |
| expected_minutes | INTEGER | NOT NULL | 予定残業時間（分） |
| overtime_type | VARCHAR(10) | NOT NULL | 事前/事後 |

### time_correction_applications（打刻修正申請明細）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 修正申請ID |
| application_id | BIGINT | FK → applications(id), NOT NULL, UNIQUE | 申請ID |
| date | DATE | NOT NULL | 対象日 |
| original_clock_in | TIME | | 修正前出勤 |
| original_clock_out | TIME | | 修正前退勤 |
| corrected_clock_in | TIME | | 修正後出勤 |
| corrected_clock_out | TIME | | 修正後退勤 |

### approvals（承認）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 承認ID |
| application_id | BIGINT | FK → applications(id), NOT NULL | 申請ID |
| approver_id | BIGINT | FK → employees(id), NOT NULL | 承認者 |
| action | VARCHAR(20) | NOT NULL | 承認/差戻 |
| comment | TEXT | | コメント |
| decided_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 決定日時 |

### leave_balances（有給残高）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 有給残高ID |
| employee_id | BIGINT | FK → employees(id), NOT NULL | 社員ID |
| fiscal_year | INTEGER | NOT NULL | 付与年度 |
| granted_days | DECIMAL(4,1) | NOT NULL | 付与日数 |
| used_days | DECIMAL(4,1) | NOT NULL, DEFAULT 0 | 消化日数 |
| remaining_days | DECIMAL(4,1) | NOT NULL | 残日数 |
| grant_date | DATE | NOT NULL | 付与日 |
| expiry_date | DATE | NOT NULL | 失効日 |
| version | BIGINT | NOT NULL, DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| updated_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 更新日時 |

**ユニーク制約**: `(employee_id, fiscal_year)`

### notifications（通知）

| カラム | 型 | 制約 | 説明 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 通知ID |
| recipient_id | BIGINT | FK → employees(id), NOT NULL | 受信者 |
| type | VARCHAR(30) | NOT NULL | 通知種別 |
| title | VARCHAR(200) | NOT NULL | タイトル |
| message | TEXT | NOT NULL | 本文 |
| read | BOOLEAN | NOT NULL, DEFAULT FALSE | 既読フラグ |
| created_at | TIMESTAMP | NOT NULL, DEFAULT NOW() | 作成日時 |
| related_application_id | BIGINT | FK → applications(id), NULL | 関連申請 |

**インデックス**: `(recipient_id, read, created_at DESC)`

## マイグレーション方針

- Flyway で管理（`src/main/resources/db/migration/`）
- 命名: `V{番号}__{説明}.sql`（例: `V1__create_organizations.sql`）
- 本番環境では `ddl-auto=none`（Hibernate による自動生成禁止）
