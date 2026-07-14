# Unit 2: 打刻・勤怠

## Phase: B（Unit 0 完了後、並列実装可能）

## 目的

出退勤・休憩の打刻と、日次・月次の勤務時間自動計算を提供する。

## ユーザーストーリー

- 一般社員として、Web画面のボタンで出勤・退勤を打刻したい（F-001）
- 一般社員として、休憩の開始・終了を打刻したい（F-002）
- 一般社員として、日次・月次の勤務時間を自動計算してほしい（F-003）
- 一般社員として、自分の月次勤怠一覧を確認したい（F-004）
- 上長として、部下の勤怠一覧を確認したい

## スコープ

### Entity

- TimeRecord（打刻記録）
- DailyAttendance（日次勤怠）
- MonthlyAttendance（月次勤怠）

### Value Object

- WorkingTime（分単位の労働時間）

### Service

- TimeRecordService: 打刻処理、位置情報の記録
- AttendanceService: 日次勤怠の計算（実労働時間、残業時間）、月次集計

### 計算ロジック

- 実労働時間 = 退勤 - 出勤 - 休憩時間
- 残業時間 = max(0, 実労働時間 - 435分)（所定7:15 = 435分）
- 休憩時間 = 休憩終了 - 休憩開始（未打刻の場合は所定60分）
- 月次集計: 日次の合算

### Repository

- TimeRecordRepository
- DailyAttendanceRepository
- MonthlyAttendanceRepository

## テーブル

- time_records
- daily_attendances
- monthly_attendances

## API

| メソッド | パス | 説明 |
|----------|------|------|
| POST | /api/v1/time-records | 打刻する |
| GET | /api/v1/time-records?date={date} | 当日の打刻一覧 |
| GET | /api/v1/time-records/status | 現在の打刻状態 |
| GET | /api/v1/attendances/daily?date={date} | 指定日の勤怠 |
| GET | /api/v1/attendances/daily?yearMonth={YYYY-MM} | 月内の日次一覧 |
| GET | /api/v1/attendances/daily/subordinates?yearMonth={YYYY-MM} | 部下の日次一覧 |
| GET | /api/v1/attendances/monthly?yearMonth={YYYY-MM} | 月次集計 |
| GET | /api/v1/attendances/monthly/subordinates?yearMonth={YYYY-MM} | 部下の月次集計 |

## 依存

- Unit 0（共通基盤）
- Unit 3（拠点管理）— 打刻時の位置判定で OfficeRepository を参照（※インターフェースのみ依存。Unit 3 未完了でも位置判定をスキップして実装可能）

## 完了条件

- [ ] 出勤/退勤/休憩開始/休憩終了の打刻ができる
- [ ] 打刻時に位置情報（緯度・経度）が記録される
- [ ] 日次の実労働時間・残業時間が自動計算される
- [ ] 月次の集計（総労働時間、総残業時間、出勤日数）が取得できる
- [ ] 部下の勤怠一覧が取得できる（MANAGER, ADMIN）
- [ ] テストカバレッジ 80% 以上
