# Unit 1: 組織・社員管理

## Phase: B（Unit 0 完了後、並列実装可能）

## 目的

組織構造（本部→部→課）と社員の CRUD を提供する。課長＝上長の関係を管理する。

## ユーザーストーリー

- 管理者として、本部/部/課を作成・編集・削除したい（F-013）
- 管理者として、社員を登録・編集・無効化したい（F-012）
- 管理者として、課の長を指定し、その課の社員の上長を自動決定したい（F-012）
- 全ロールとして、自分の所属情報を確認したい

## スコープ

### Entity

- Organization（本部）
- Department（部）
- Section（課）— managerId で課長を指定
- Employee（社員）— sectionId で所属課を参照

### Service

- OrganizationService: 本部/部/課の CRUD
- EmployeeService: 社員 CRUD、上長の解決（課長取得）

### Repository

- OrganizationRepository
- DepartmentRepository
- SectionRepository
- EmployeeRepository

## テーブル

- organizations
- departments
- sections
- employees

## API

| メソッド | パス | 説明 |
|----------|------|------|
| GET | /api/v1/organizations | 本部一覧 |
| POST | /api/v1/organizations | 本部作成 |
| PUT | /api/v1/organizations/{id} | 本部更新 |
| DELETE | /api/v1/organizations/{id} | 本部削除 |
| GET | /api/v1/departments?organizationId={id} | 部一覧 |
| POST | /api/v1/departments | 部作成 |
| PUT | /api/v1/departments/{id} | 部更新 |
| DELETE | /api/v1/departments/{id} | 部削除 |
| GET | /api/v1/sections?departmentId={id} | 課一覧 |
| POST | /api/v1/sections | 課作成 |
| PUT | /api/v1/sections/{id} | 課更新 |
| DELETE | /api/v1/sections/{id} | 課削除 |
| GET | /api/v1/employees | 社員一覧 |
| GET | /api/v1/employees/{id} | 社員詳細 |
| GET | /api/v1/employees/me | 自分の情報 |
| POST | /api/v1/employees | 社員登録 |
| PUT | /api/v1/employees/{id} | 社員更新 |
| DELETE | /api/v1/employees/{id} | 社員無効化 |

## 依存

- Unit 0（共通基盤: Entity, Flyway, 認証）

## 完了条件

- [ ] 組織（本部/部/課）の CRUD が動作する
- [ ] 社員の CRUD が動作する
- [ ] 課長を指定すると、その課の社員の上長が自動的に決まる
- [ ] ロール別アクセス制御が効いている（ADMIN のみ作成/更新/削除可能）
- [ ] テストカバレッジ 80% 以上
