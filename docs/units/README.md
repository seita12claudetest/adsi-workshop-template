# Unit of Work 分割 — 勤怠管理アプリ

## 依存図

```
Phase A: インターフェース定義（共通基盤）
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Unit 0: 共通基盤（Flyway, Entity, Enum, 認証）

Phase B: 独立ドメイン（並列実装可能）
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Unit 1: 組織・社員管理  ←── Unit 0
  Unit 2: 打刻・勤怠      ←── Unit 0
  Unit 3: 拠点管理        ←── Unit 0

Phase C: 依存ドメイン（Phase B 完了後）
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Unit 4: 申請・承認      ←── Unit 1, Unit 2
  Unit 5: 有給管理        ←── Unit 1, Unit 4
  Unit 6: 通知           ←── Unit 4, Unit 5

Phase D: 統合・UI
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  Unit 7: フロントエンド   ←── Unit 1〜6（API が揃った後）
```

## 依存関係グラフ

```
Unit 0 (共通基盤)
  │
  ├──▶ Unit 1 (組織・社員)
  │        │
  ├──▶ Unit 2 (打刻・勤怠) ──┐
  │                          │
  ├──▶ Unit 3 (拠点) ────────┤（Unit 2 が位置判定で使う）
  │                          │
  │    Unit 4 (申請・承認) ◀──┘◀── Unit 1
  │        │
  │    Unit 5 (有給管理) ◀── Unit 1, Unit 4
  │        │
  │    Unit 6 (通知) ◀── Unit 4, Unit 5
  │
  └──▶ Unit 7 (フロントエンド) ◀── 全 Unit
```

## Phase 一覧

| Phase | Unit | 並列可否 | 見積もり |
|-------|------|---------|---------|
| A | Unit 0: 共通基盤 | - | 小 |
| B | Unit 1: 組織・社員管理 | 並列可 | 中 |
| B | Unit 2: 打刻・勤怠 | 並列可 | 大 |
| B | Unit 3: 拠点管理 | 並列可 | 小 |
| C | Unit 4: 申請・承認 | 並列可（Unit 5と） | 大 |
| C | Unit 5: 有給管理 | Unit 4 の一部完了後 | 中 |
| C | Unit 6: 通知 | Unit 4, 5 完了後 | 中 |
| D | Unit 7: フロントエンド | 全 API 完了後 | 大 |

## Unit ファイル

- [unit_0_common.md](unit_0_common.md) — 共通基盤
- [unit_1_organization.md](unit_1_organization.md) — 組織・社員管理
- [unit_2_attendance.md](unit_2_attendance.md) — 打刻・勤怠
- [unit_3_office.md](unit_3_office.md) — 拠点管理
- [unit_4_application.md](unit_4_application.md) — 申請・承認
- [unit_5_leave_balance.md](unit_5_leave_balance.md) — 有給管理
- [unit_6_notification.md](unit_6_notification.md) — 通知
- [unit_7_frontend.md](unit_7_frontend.md) — フロントエンド
