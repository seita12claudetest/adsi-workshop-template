# Unit 7: フロントエンド

## Phase: D（全 API 完了後、ただし部分的に並行着手可能）

## 目的

Next.js (TypeScript) で全画面を実装する。API と結合し、ユーザーが操作できる UI を提供する。

## ユーザーストーリー

- 全ユーザーストーリー（F-001〜F-013）のフロントエンド実装

## スコープ

### プロジェクト構成

- Next.js (App Router) + TypeScript
- Tailwind CSS
- SWR or TanStack Query（サーバーステート）
- Zod（バリデーション）

### 画面（15画面）

| 画面 | パス | 優先度 |
|------|------|--------|
| ログイン | /login | 高 |
| ダッシュボード | / | 高 |
| 打刻 | /time-record | 高 |
| 月次勤怠一覧 | /attendance/monthly | 高 |
| 休暇申請 | /applications/leave | 高 |
| 残業申請 | /applications/overtime | 中 |
| 打刻修正申請 | /applications/time-correction | 中 |
| 申請一覧 | /applications | 中 |
| 有給残高 | /leave-balance | 中 |
| 承認一覧 | /approvals | 高 |
| 部下勤怠一覧 | /attendance/subordinates | 中 |
| 社員管理 | /admin/employees | 低 |
| 組織管理 | /admin/organizations | 低 |
| 拠点管理 | /admin/offices | 低 |
| 通知一覧 | /notifications | 中 |

### 共通コンポーネント

- Layout（ヘッダー + サイドバー + コンテンツ）
- Header（ロゴ、ナビ、通知ベル、ユーザーメニュー）
- Sidebar（ロール別メニュー表示）
- NotificationBell（未読数バッジ）
- StatusBadge / Toast / ConfirmDialog / Pagination

### API クライアント

- fetch ラッパー（JWT 自動付与、エラーハンドリング、basePath 対応）
- 型定義（API レスポンスの型）

### 認証

- ログイン画面 → JWT 取得 → localStorage or httpOnly cookie に保存
- 未認証時のリダイレクト
- ロール別のルートガード

## テーブル

なし（フロントエンドのみ）

## API

全 API を利用

## 依存

- Unit 0〜6（全 Backend API）

## 完了条件

- [ ] 全15画面が実装されている
- [ ] ログイン・ログアウトが動作する
- [ ] 打刻（GPS 付き）が動作する
- [ ] 月次勤怠一覧が表示される
- [ ] 各種申請の作成・一覧表示が動作する
- [ ] 承認・差戻が動作する
- [ ] 通知ベルに未読数が表示される
- [ ] ロール別にメニュー・操作が制御されている
- [ ] SageMaker プレビュー（basePath）対応済み
- [ ] レスポンシブ対応（モバイル打刻を想定）
