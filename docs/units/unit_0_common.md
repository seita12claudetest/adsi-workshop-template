# Unit 0: 共通基盤

## Phase: A（最初に実装）

## 目的

全 Unit が依存する基盤レイヤーを構築する。プロジェクトスケルトン、Flyway マイグレーション（全テーブル）、共通 Entity/Enum、認証基盤を含む。

## ユーザーストーリー

- 開発者として、Spring Boot プロジェクトが起動し DB 接続できる状態にしたい
- 利用者として、ID/パスワードでログインし JWT トークンを取得したい

## スコープ

### プロジェクト構成

- Spring Boot プロジェクト初期化（Gradle）
- パッケージ構成: `com.example.attendance.{domain}.{layer}`
- 共通設定: application.yml, SecurityFilterChain, CORS, 例外ハンドラ

### Flyway マイグレーション（全テーブル）

依存順に作成:
1. `V1__create_organizations.sql`
2. `V2__create_departments.sql`
3. `V3__create_sections.sql`
4. `V4__create_employees.sql`
5. `V5__create_offices.sql`
6. `V6__create_time_records.sql`
7. `V7__create_daily_attendances.sql`
8. `V8__create_monthly_attendances.sql`
9. `V9__create_applications.sql`
10. `V10__create_leave_applications.sql`
11. `V11__create_overtime_applications.sql`
12. `V12__create_time_correction_applications.sql`
13. `V13__create_approvals.sql`
14. `V14__create_leave_balances.sql`
15. `V15__create_notifications.sql`

### 共通 Enum

- Role, TimeRecordType, DailyStatus, MonthlyStatus
- ApplicationType, ApplicationStatus, ApprovalAction
- LeaveType, OvertimeType, NotificationType

### 認証（AuthService）

- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout
- JWT 発行・検証フィルタ
- SecurityFilterChain（エンドポイント別権限設定）

### 共通例外ハンドラ

- `@RestControllerAdvice` でバリデーションエラー、認証エラー、Not Found 等を統一レスポンス

## テーブル

全テーブル（DDL のみ。データ操作は各 Unit が担当）

## API

- POST /api/v1/auth/login
- POST /api/v1/auth/refresh
- POST /api/v1/auth/logout

## 依存

なし（最初に実装する）

## 完了条件

- [ ] `./gradlew bootRun` でアプリが起動する
- [ ] Flyway で全テーブルが作成される
- [ ] ログイン API で JWT が発行される
- [ ] 認証が必要な API に未認証でアクセスすると 401 が返る
