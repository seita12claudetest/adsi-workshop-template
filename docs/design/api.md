# API 設計

## 共通仕様

- ベース URL: `/api/v1`
- 認証: JWT Bearer トークン（`Authorization: Bearer <token>`）
- レスポンス形式: JSON
- エラーレスポンス: `{ "error": "コード", "message": "メッセージ", "details": [...] }`
- ページネーション: `?page=0&size=20`（デフォルト20件）

## エンドポイント一覧

### 認証 API

| メソッド | パス | 説明 | 認証 |
|----------|------|------|------|
| POST | /api/v1/auth/login | ログイン | 不要 |
| POST | /api/v1/auth/refresh | トークンリフレッシュ | 不要（refreshToken使用） |
| POST | /api/v1/auth/logout | ログアウト | 必要 |

#### POST /api/v1/auth/login

Request:
```json
{
  "email": "tanaka@example.com",
  "password": "password123"
}
```

Response (200):
```json
{
  "accessToken": "eyJhbG...",
  "refreshToken": "eyJhbG...",
  "expiresIn": 3600,
  "employee": {
    "id": 1,
    "name": "田中太郎",
    "email": "tanaka@example.com",
    "role": "EMPLOYEE"
  }
}
```

---

### 打刻 API

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| POST | /api/v1/time-records | 打刻する | 全ロール |
| GET | /api/v1/time-records?date={date} | 当日の打刻一覧 | 全ロール |
| GET | /api/v1/time-records/status | 現在の打刻状態 | 全ロール |

#### POST /api/v1/time-records

Request:
```json
{
  "type": "CLOCK_IN",
  "latitude": 35.6812,
  "longitude": 139.7671
}
```

Response (201):
```json
{
  "id": 1,
  "type": "CLOCK_IN",
  "recordedAt": "2026-07-14T09:15:00",
  "withinArea": true,
  "officeName": "本社",
  "warning": null
}
```

Response (201, エリア外):
```json
{
  "id": 2,
  "type": "CLOCK_IN",
  "recordedAt": "2026-07-14T09:15:00",
  "withinArea": false,
  "officeName": null,
  "warning": "許可エリア外での打刻です。最寄り拠点: 本社（距離: 1.2km）"
}
```

---

### 日次勤怠 API

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/attendances/daily?date={date} | 指定日の勤怠 | 全ロール |
| GET | /api/v1/attendances/daily?yearMonth={YYYY-MM} | 月内の日次一覧 | 全ロール |
| GET | /api/v1/attendances/daily/subordinates?yearMonth={YYYY-MM} | 部下の日次一覧 | MANAGER, ADMIN |

#### GET /api/v1/attendances/daily?date=2026-07-14

Response (200):
```json
{
  "date": "2026-07-14",
  "clockIn": "09:15",
  "clockOut": "17:30",
  "breakStart": "12:00",
  "breakEnd": "13:00",
  "workingMinutes": 435,
  "overtimeMinutes": 0,
  "breakMinutes": 60,
  "status": "NORMAL"
}
```

---

### 月次勤怠 API

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/attendances/monthly?yearMonth={YYYY-MM} | 月次集計 | 全ロール |
| GET | /api/v1/attendances/monthly/subordinates?yearMonth={YYYY-MM} | 部下の月次集計 | MANAGER, ADMIN |

#### GET /api/v1/attendances/monthly?yearMonth=2026-07

Response (200):
```json
{
  "yearMonth": "2026-07",
  "totalWorkingMinutes": 8700,
  "totalOvertimeMinutes": 120,
  "workingDays": 20,
  "paidLeaveDays": 1.0,
  "status": "OPEN"
}
```

---

### 申請 API

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| POST | /api/v1/applications/leave | 休暇申請 | 全ロール |
| POST | /api/v1/applications/overtime | 残業申請 | 全ロール |
| POST | /api/v1/applications/time-correction | 打刻修正申請 | 全ロール |
| GET | /api/v1/applications | 自分の申請一覧 | 全ロール |
| GET | /api/v1/applications/{id} | 申請詳細 | 申請者 or 承認者 |
| DELETE | /api/v1/applications/{id} | 申請取消（PENDING のみ） | 申請者 |

#### POST /api/v1/applications/leave

Request:
```json
{
  "leaveType": "ANNUAL",
  "startDate": "2026-07-20",
  "endDate": "2026-07-20",
  "reason": "私用のため"
}
```

Response (201):
```json
{
  "id": 1,
  "type": "LEAVE",
  "status": "PENDING",
  "appliedAt": "2026-07-14T10:00:00",
  "leaveDetail": {
    "leaveType": "ANNUAL",
    "startDate": "2026-07-20",
    "endDate": "2026-07-20"
  }
}
```

#### POST /api/v1/applications/overtime

Request:
```json
{
  "date": "2026-07-14",
  "expectedMinutes": 60,
  "overtimeType": "PRE",
  "reason": "納期対応のため"
}
```

#### POST /api/v1/applications/time-correction

Request:
```json
{
  "date": "2026-07-13",
  "correctedClockIn": "09:15",
  "correctedClockOut": "18:30",
  "reason": "退勤打刻忘れ"
}
```

---

### 承認 API

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/approvals/pending | 未処理の承認依頼一覧 | MANAGER, ADMIN |
| POST | /api/v1/approvals/{applicationId}/approve | 承認 | MANAGER, ADMIN |
| POST | /api/v1/approvals/{applicationId}/reject | 差戻 | MANAGER, ADMIN |

#### POST /api/v1/approvals/{applicationId}/approve

Request:
```json
{
  "comment": "承認します"
}
```

Response (200):
```json
{
  "applicationId": 1,
  "action": "APPROVED",
  "decidedAt": "2026-07-14T11:00:00"
}
```

#### POST /api/v1/approvals/{applicationId}/reject

Request:
```json
{
  "comment": "日付を確認してください"
}
```

---

### 有給残高 API

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/leave-balances | 自分の有給残高 | 全ロール |
| GET | /api/v1/leave-balances/{employeeId} | 指定社員の有給残高 | MANAGER（部下のみ）, ADMIN |

#### GET /api/v1/leave-balances

Response (200):
```json
{
  "balances": [
    {
      "fiscalYear": 2026,
      "grantedDays": 20.0,
      "usedDays": 5.0,
      "remainingDays": 15.0,
      "grantDate": "2026-04-01",
      "expiryDate": "2028-03-31"
    },
    {
      "fiscalYear": 2025,
      "grantedDays": 18.0,
      "usedDays": 12.0,
      "remainingDays": 6.0,
      "grantDate": "2025-04-01",
      "expiryDate": "2027-03-31"
    }
  ],
  "totalRemaining": 21.0
}
```

---

### 通知 API

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/notifications | 通知一覧 | 全ロール |
| GET | /api/v1/notifications/unread-count | 未読数 | 全ロール |
| PUT | /api/v1/notifications/{id}/read | 既読にする | 全ロール |
| PUT | /api/v1/notifications/read-all | 全て既読 | 全ロール |

#### GET /api/v1/notifications

Response (200):
```json
{
  "notifications": [
    {
      "id": 1,
      "type": "APPROVAL_REQUEST",
      "title": "休暇申請の承認依頼",
      "message": "田中太郎さんから休暇申請が届いています",
      "read": false,
      "createdAt": "2026-07-14T10:00:00",
      "relatedApplicationId": 1
    }
  ],
  "totalCount": 5,
  "unreadCount": 2
}
```

---

### 社員管理 API（ADMIN のみ）

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/employees | 社員一覧 | MANAGER, ADMIN |
| GET | /api/v1/employees/{id} | 社員詳細 | MANAGER（部下のみ）, ADMIN |
| POST | /api/v1/employees | 社員登録 | ADMIN |
| PUT | /api/v1/employees/{id} | 社員更新 | ADMIN |
| DELETE | /api/v1/employees/{id} | 社員無効化 | ADMIN |
| GET | /api/v1/employees/me | 自分の情報 | 全ロール |

---

### 組織管理 API（ADMIN のみ）

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/organizations | 本部一覧 | 全ロール |
| POST | /api/v1/organizations | 本部作成 | ADMIN |
| PUT | /api/v1/organizations/{id} | 本部更新 | ADMIN |
| DELETE | /api/v1/organizations/{id} | 本部削除 | ADMIN |
| GET | /api/v1/departments?organizationId={id} | 部一覧 | 全ロール |
| POST | /api/v1/departments | 部作成 | ADMIN |
| PUT | /api/v1/departments/{id} | 部更新 | ADMIN |
| DELETE | /api/v1/departments/{id} | 部削除 | ADMIN |
| GET | /api/v1/sections?departmentId={id} | 課一覧 | 全ロール |
| POST | /api/v1/sections | 課作成 | ADMIN |
| PUT | /api/v1/sections/{id} | 課更新 | ADMIN |
| DELETE | /api/v1/sections/{id} | 課削除 | ADMIN |

---

### 拠点管理 API（ADMIN のみ）

| メソッド | パス | 説明 | 権限 |
|----------|------|------|------|
| GET | /api/v1/offices | 拠点一覧 | 全ロール |
| POST | /api/v1/offices | 拠点登録 | ADMIN |
| PUT | /api/v1/offices/{id} | 拠点更新 | ADMIN |
| DELETE | /api/v1/offices/{id} | 拠点削除 | ADMIN |

---

## HTTP ステータスコード

| コード | 用途 |
|--------|------|
| 200 | 正常（取得・更新） |
| 201 | 作成成功 |
| 400 | バリデーションエラー |
| 401 | 未認証 |
| 403 | 権限不足 |
| 404 | リソースなし |
| 409 | 競合（楽観ロック） |
| 500 | サーバーエラー |

## エラーレスポンス形式

```json
{
  "error": "VALIDATION_ERROR",
  "message": "入力内容に誤りがあります",
  "details": [
    {
      "field": "startDate",
      "message": "開始日は今日以降を指定してください"
    }
  ]
}
```
