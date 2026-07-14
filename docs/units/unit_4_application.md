# Unit 4: 申請・承認

## Phase: C（Unit 1, 2 完了後）

## 目的

休暇申請・残業申請・打刻修正申請の作成と、上長による承認・差戻ワークフローを提供する。

## ユーザーストーリー

- 一般社員として、有給休暇や特別休暇を申請したい（F-005）
- 一般社員として、残業を事前/事後申請したい（F-007）
- 一般社員として、打刻忘れ・誤打刻を修正申請したい（F-008）
- 上長として、部下の各種申請を承認・差戻したい（F-009）
- 管理者として、全社員の申請を承認・差戻したい（F-009）

## スコープ

### Entity

- Application（申請 — 共通親）
- LeaveApplication（休暇申請明細）
- OvertimeApplication（残業申請明細）
- TimeCorrectionApplication（打刻修正申請明細）
- Approval（承認）

### Service

- ApplicationService: 各種申請の作成、取消、一覧取得
- ApprovalService: 承認・差戻処理、承認者の妥当性チェック（直属の上長 or ADMIN）

### 承認ロジック

1. 申請者の所属課の課長（`sections.manager_id`）を承認者とする
2. ADMIN ロールは全員の申請を承認可能
3. 差戻時はコメント必須
4. 承認後:
   - 休暇申請 → LeaveBalance の消化日数を更新（Unit 5 と連携）
   - 打刻修正 → DailyAttendance を修正（Unit 2 と連携）

### Repository

- ApplicationRepository
- LeaveApplicationRepository
- OvertimeApplicationRepository
- TimeCorrectionApplicationRepository
- ApprovalRepository

## テーブル

- applications
- leave_applications
- overtime_applications
- time_correction_applications
- approvals

## API

| メソッド | パス | 説明 |
|----------|------|------|
| POST | /api/v1/applications/leave | 休暇申請 |
| POST | /api/v1/applications/overtime | 残業申請 |
| POST | /api/v1/applications/time-correction | 打刻修正申請 |
| GET | /api/v1/applications | 自分の申請一覧 |
| GET | /api/v1/applications/{id} | 申請詳細 |
| DELETE | /api/v1/applications/{id} | 申請取消 |
| GET | /api/v1/approvals/pending | 未処理承認一覧 |
| POST | /api/v1/approvals/{applicationId}/approve | 承認 |
| POST | /api/v1/approvals/{applicationId}/reject | 差戻 |

## 依存

- Unit 0（共通基盤）
- Unit 1（組織・社員）— 上長の解決（課長の取得）
- Unit 2（打刻・勤怠）— 打刻修正承認後の DailyAttendance 更新

## 完了条件

- [ ] 休暇/残業/打刻修正の申請が作成できる
- [ ] PENDING 状態の申請を取消できる
- [ ] 上長が部下の申請を承認・差戻できる
- [ ] ADMIN が全社員の申請を承認・差戻できる
- [ ] 差戻時にコメントが必須である
- [ ] 承認後に関連データが更新される
- [ ] テストカバレッジ 80% 以上
