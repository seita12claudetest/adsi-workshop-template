# Unit 3: 拠点管理

## Phase: B（Unit 0 完了後、並列実装可能）

## 目的

許可拠点の CRUD と、打刻時の距離判定ロジックを提供する。

## ユーザーストーリー

- 管理者として、許可拠点を登録・管理したい（F-011）
- 一般社員として、打刻時に許可エリア内かどうかを確認したい（F-001）

## スコープ

### Entity

- Office（拠点）

### Service

- OfficeService: 拠点 CRUD、距離計算（Haversine 公式）、エリア判定

### 距離計算ロジック

- Haversine 公式で2点間の距離（メートル）を算出
- 打刻位置と全拠点の距離を計算し、最寄り拠点を特定
- 最寄り拠点の `radius_meters` 以内なら `withinArea = true`

### Repository

- OfficeRepository

## テーブル

- offices

## API

| メソッド | パス | 説明 |
|----------|------|------|
| GET | /api/v1/offices | 拠点一覧 |
| POST | /api/v1/offices | 拠点登録 |
| PUT | /api/v1/offices/{id} | 拠点更新 |
| DELETE | /api/v1/offices/{id} | 拠点削除 |

## 依存

- Unit 0（共通基盤）

## 完了条件

- [ ] 拠点の CRUD が動作する（ADMIN のみ作成/更新/削除）
- [ ] 緯度・経度から2点間の距離が正しく計算される
- [ ] 指定座標が拠点の許可半径内かどうか判定できる
- [ ] テストカバレッジ 80% 以上
