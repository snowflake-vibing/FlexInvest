-- =============================================================================
-- FlexInvest — Demo Data Script
-- Mục đích: Insert data mẫu cho buổi demo
--
-- Tài khoản demo:
--   Admin:    username=admin    / password=admin123  / email=admin@flexinvest.vn
--   Staff:    username=staff    / password=staff123  / email=staff@flexinvest.vn
--   Customer: username=customer / password=cus123    / email=customer@flexinvest.vn
--
-- HƯỚNG DẪN CHẠY:
--   1. Chạy FlexInvest_fixed.sql trước (tạo bảng + seed roles/functions)
--   2. Chạy file này trên Oracle SQL Developer / SQL*Plus
--   3. Script tự COMMIT ở cuối
-- =============================================================================

SET DEFINE OFF

-- =============================================================================
-- 0. Thêm cột payout_method và target_product_id vào INVESTMENT nếu chưa có
-- =============================================================================
BEGIN
    EXECUTE IMMEDIATE 'ALTER TABLE INVESTMENT ADD (payout_method VARCHAR2(10), target_product_id NUMBER(10))';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE = -1430 THEN NULL; -- ORA-01430: column already exists
        ELSE RAISE;
        END IF;
END;
/

-- =============================================================================
-- 1. Xóa data cũ theo thứ tự FK (safe cho môi trường demo)
-- =============================================================================
DELETE FROM LEDGER                 WHERE is_deleted IN (0,1);
DELETE FROM PAYOUT                 WHERE is_deleted IN (0,1);
DELETE FROM EARLY_REDEMPTION_EVENT WHERE is_deleted IN (0,1);
DELETE FROM INVESTMENT             WHERE is_deleted IN (0,1);
DELETE FROM DEPOSIT                WHERE is_deleted IN (0,1);
DELETE FROM WITHDRAW               WHERE is_deleted IN (0,1);
DELETE FROM "TRANSACTION"          WHERE is_deleted IN (0,1);
DELETE FROM EKYC                   WHERE is_deleted IN (0,1);
DELETE FROM BANK_ACCOUNT           WHERE is_deleted IN (0,1);
DELETE FROM WALLET                 WHERE is_deleted IN (0,1);
DELETE FROM ACCOUNT                WHERE is_deleted IN (0,1);
DELETE FROM USERS                  WHERE is_deleted IN (0,1);
DELETE FROM SAVINGS_PRODUCT        WHERE is_deleted IN (0,1);
DELETE FROM MISSIONS               WHERE is_deleted IN (0,1);
DELETE FROM SYS_ROLE;
DELETE FROM SYS_FUNCTION;
COMMIT;

-- =============================================================================
-- 2. USERS
--    role_id: 1=Admin, 2=Staff, 3=Customer
-- =============================================================================
INSERT INTO USERS (role_id, email, password_hash, status, referral_code, created_at, is_deleted)
VALUES (1, 'admin@flexinvest.vn', 'admin123', 'ACTIVE', 'ADMIN001', SYSDATE, 0);

INSERT INTO USERS (role_id, email, password_hash, status, referral_code, created_at, is_deleted)
VALUES (2, 'staff@flexinvest.vn', 'staff123', 'ACTIVE', 'STAFF001', SYSDATE, 0);

INSERT INTO USERS (role_id, email, password_hash, status, referral_code, created_at, is_deleted)
VALUES (3, 'customer@flexinvest.vn', 'cus123', 'ACTIVE', 'CUS001', SYSDATE, 0);

-- =============================================================================
-- 3. ACCOUNT (username/password để đăng nhập — khớp với AccountDAO)
-- =============================================================================
INSERT INTO ACCOUNT (user_id, username, password_hash, status, created_at, updated_at, is_deleted)
SELECT user_id, 'admin', 'admin123', 'ACTIVE', SYSDATE, SYSDATE, 0
FROM USERS WHERE email = 'admin@flexinvest.vn';

INSERT INTO ACCOUNT (user_id, username, password_hash, status, created_at, updated_at, is_deleted)
SELECT user_id, 'staff', 'staff123', 'ACTIVE', SYSDATE, SYSDATE, 0
FROM USERS WHERE email = 'staff@flexinvest.vn';

INSERT INTO ACCOUNT (user_id, username, password_hash, status, created_at, updated_at, is_deleted)
SELECT user_id, 'customer', 'cus123', 'ACTIVE', SYSDATE, SYSDATE, 0
FROM USERS WHERE email = 'customer@flexinvest.vn';

-- =============================================================================
-- 4. WALLET (số dư ban đầu — khớp với WalletDAO.insertWithConnection)
-- =============================================================================
INSERT INTO WALLET (user_id, available_balance, locked_balance, status, is_deleted)
SELECT user_id, 50000000, 0, 'ACTIVE', 0
FROM USERS WHERE email = 'admin@flexinvest.vn';

INSERT INTO WALLET (user_id, available_balance, locked_balance, status, is_deleted)
SELECT user_id, 10000000, 0, 'ACTIVE', 0
FROM USERS WHERE email = 'staff@flexinvest.vn';

INSERT INTO WALLET (user_id, available_balance, locked_balance, status, is_deleted)
SELECT user_id, 20000000, 0, 'ACTIVE', 0
FROM USERS WHERE email = 'customer@flexinvest.vn';

-- =============================================================================
-- 5. SYS_FUNCTION
-- =============================================================================
INSERT INTO SYS_FUNCTION (NAME_FUNCTION) VALUES ('Quản lý người dùng');
INSERT INTO SYS_FUNCTION (NAME_FUNCTION) VALUES ('Quản lý giao dịch');
INSERT INTO SYS_FUNCTION (NAME_FUNCTION) VALUES ('Quản lý gói đầu tư');
INSERT INTO SYS_FUNCTION (NAME_FUNCTION) VALUES ('Quản lý token & nhiệm vụ');
INSERT INTO SYS_FUNCTION (NAME_FUNCTION) VALUES ('Báo cáo & thống kê');
COMMIT;

-- =============================================================================
-- 6. SYS_ROLE (5 vai trò, mỗi vai trò map 1 function đại diện)
-- =============================================================================
-- Admin: toàn quyền
INSERT INTO SYS_ROLE (FUNCTION_ID, ADD_PERM, EDIT_PERM, DELETE_PERM, DOWNLOAD_PERM, VIEW_PERM)
VALUES (1, 1, 1, 1, 1, 1);

-- Manager: quản lý gói đầu tư + báo cáo
INSERT INTO SYS_ROLE (FUNCTION_ID, ADD_PERM, EDIT_PERM, DELETE_PERM, DOWNLOAD_PERM, VIEW_PERM)
VALUES (3, 1, 1, 0, 1, 1);

-- Staff: quản lý người dùng, chỉ xem + sửa
INSERT INTO SYS_ROLE (FUNCTION_ID, ADD_PERM, EDIT_PERM, DELETE_PERM, DOWNLOAD_PERM, VIEW_PERM)
VALUES (1, 0, 1, 0, 0, 1);

-- Accountant: quản lý giao dịch, xem + tải
INSERT INTO SYS_ROLE (FUNCTION_ID, ADD_PERM, EDIT_PERM, DELETE_PERM, DOWNLOAD_PERM, VIEW_PERM)
VALUES (2, 0, 0, 0, 1, 1);

-- User (Member): chỉ xem thông tin của mình
INSERT INTO SYS_ROLE (FUNCTION_ID, ADD_PERM, EDIT_PERM, DELETE_PERM, DOWNLOAD_PERM, VIEW_PERM)
VALUES (1, 0, 0, 0, 0, 1);

COMMIT;

-- =============================================================================
-- 7. SAVINGS_PRODUCT
-- =============================================================================
-- Gói không kỳ hạn (term = 0)
INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-Safe', 0.0130, 0, 50000, 50000000, 0, 0, 0, 'ACTIVE', DATE '2024-01-01', NULL, 0);

INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, currency, status, start_date, end_date, is_deleted)
VALUES ('Flex-Safe-VIP', 0.0150, 0, 50000, 20000000, 0, 0, 0, 'FLEXTOKEN', 'ACTIVE', DATE '2024-01-01', NULL, 0);

-- Gói có kỳ hạn (term tính theo ngày)
-- interest_rate và fallback_interest_rate lưu dạng thập phân (0.0600 = 6%/năm)
INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-1', 0.0600, 30, 50000, 10000000, 0, 0.0050, 1, 'ACTIVE', DATE '2024-01-01', NULL, 0);

INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-3', 0.0650, 90, 50000, 10000000, 0, 0.0050, 1, 'ACTIVE', DATE '2024-01-01', NULL, 0);

INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-6', 0.0690, 180, 50000, 20000000, 0, 0.0050, 1, 'ACTIVE', DATE '2024-01-01', NULL, 0);

INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-9', 0.0730, 270, 50000, 20000000, 0, 0.0050, 1, 'ACTIVE', DATE '2024-01-01', NULL, 0);

INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-12', 0.0780, 365, 50000, 20000000, 0, 0.0050, 1, 'ACTIVE', DATE '2024-01-01', NULL, 0);

-- Gói VIP (chỉ dùng FlexToken)
INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, currency, status, start_date, end_date, is_deleted)
VALUES ('Flex-1-VIP', 0.0650, 30, 50000, 20000000, 0, 0.0050, 1, 'FLEXTOKEN', 'ACTIVE', DATE '2024-01-01', NULL, 0);

INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, currency, status, start_date, end_date, is_deleted)
VALUES ('Flex-6-VIP', 0.0750, 180, 50000, 20000000, 0, 0.0050, 1, 'FLEXTOKEN', 'ACTIVE', DATE '2024-01-01', NULL, 0);

-- Gói đặc biệt (mở theo ngày cụ thể)
INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-Sale', 0.0777, 30, 50000, 5000000, 0, 0.0050, 1, 'INACTIVE', NULL, NULL, 0);

INSERT INTO SAVINGS_PRODUCT (product_name, interest_rate, term, min_investment_amount, max_investment_amount, penalty_rate, fallback_interest_rate, min_holding_days, status, start_date, end_date, is_deleted)
VALUES ('Flex-Holiday', 0.0888, 30, 50000, 2500000, 0, 0.0050, 1, 'INACTIVE', NULL, NULL, 0);

COMMIT;

-- =============================================================================
-- 8. INVESTMENT (tham chiếu product_name theo data.sql)
-- =============================================================================

-- [INV-1] ACTIVE, Flex-Safe, mua 10 ngày trước
INSERT INTO INVESTMENT
  (user_id, product_id, invested_amount, applied_interest_rate,
   start_date, maturity_date, status, payout_method, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT product_id FROM SAVINGS_PRODUCT WHERE product_name = 'Flex-Safe'),
  5000000, 0.0130,
  SYSDATE - 10, NULL, 'ACTIVE', 'PT3', 0
FROM DUAL;

-- [INV-2] ACTIVE, Flex-1, sắp đáo hạn 2 ngày nữa
INSERT INTO INVESTMENT
  (user_id, product_id, invested_amount, applied_interest_rate,
   start_date, maturity_date, status, payout_method, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT product_id FROM SAVINGS_PRODUCT WHERE product_name = 'Flex-1'),
  10000000, 0.0600,
  SYSDATE - 28, SYSDATE + 2, 'ACTIVE', 'PT3', 0
FROM DUAL;

-- [INV-3] ACTIVE, Flex-3, mới mua hôm qua
INSERT INTO INVESTMENT
  (user_id, product_id, invested_amount, applied_interest_rate,
   start_date, maturity_date, status, payout_method, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT product_id FROM SAVINGS_PRODUCT WHERE product_name = 'Flex-3'),
  3000000, 0.0650,
  SYSDATE - 1, SYSDATE + 89, 'ACTIVE', 'PT1', 0
FROM DUAL;

-- [INV-4] ACTIVE, Flex-3, mua 35 ngày trước
INSERT INTO INVESTMENT
  (user_id, product_id, invested_amount, applied_interest_rate,
   start_date, maturity_date, status, payout_method, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT product_id FROM SAVINGS_PRODUCT WHERE product_name = 'Flex-3'),
  8000000, 0.0650,
  SYSDATE - 35, SYSDATE + 55, 'ACTIVE', 'PT2', 0
FROM DUAL;

-- [INV-5] COMPLETED, đã tất toán — demo lịch sử
INSERT INTO INVESTMENT
  (user_id, product_id, invested_amount, applied_interest_rate,
   start_date, maturity_date, status, payout_method, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT product_id FROM SAVINGS_PRODUCT WHERE product_name = 'Flex-1'),
  2000000, 0.0600,
  SYSDATE - 60, SYSDATE - 30, 'COMPLETED', 'PT3', 0
FROM DUAL;

-- =============================================================================
-- 9. eKYC — 1 hồ sơ PENDING để Staff duyệt
-- =============================================================================
INSERT INTO EKYC
  (user_id, id_number, full_name, date_of_birth, gender,
   place_of_origin, place_of_residence,
   issue_date, expiry_date, issue_place,
   front_image_url, back_image_url, face_image_url,
   verified_status, created_at, updated_at, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  '079204012345', 'Nguyễn Văn Demo', DATE '1998-06-15', 'Nam',
  'Hà Nội', 'TP. Hồ Chí Minh',
  DATE '2020-01-10', DATE '2030-01-10', 'Cục Cảnh sát QLHC về TTXH',
  '/demo/front.jpg', '/demo/back.jpg', '/demo/selfie.jpg',
  'PENDING', SYSDATE, SYSDATE, 0
FROM DUAL;

-- =============================================================================
-- 10. BANK_ACCOUNT
-- =============================================================================
INSERT INTO BANK_ACCOUNT (user_id, bank_name, account_number, is_linked, is_deleted)
SELECT user_id, 'Vietcombank', '1234567890', 1, 0
FROM USERS WHERE email = 'customer@flexinvest.vn';

INSERT INTO BANK_ACCOUNT (user_id, bank_name, account_number, is_linked, is_deleted)
SELECT user_id, 'Techcombank', '0987654321', 1, 0
FROM USERS WHERE email = 'staff@flexinvest.vn';

-- =============================================================================
-- 11. TRANSACTION + DEPOSIT PENDING
--     Dùng "TRANSACTION" (có ngoặc kép) vì là reserved word Oracle
-- =============================================================================

-- Lệnh nạp PENDING #1 — 5 triệu
INSERT INTO "TRANSACTION" (wallet_id, type_code, amount, status, created_at, is_deleted)
SELECT w.wallet_id, 'DEPOSIT', 5000000, 'PENDING', SYSDATE - 0.1, 0
FROM WALLET w
JOIN USERS u ON w.user_id = u.user_id
WHERE u.email = 'customer@flexinvest.vn';

INSERT INTO DEPOSIT (transaction_id, request_code, payment_gateway, receiving_account, is_deleted)
SELECT t.transaction_id,
       'DEP' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '001',
       'BANKING', '9704000000000018', 0
FROM "TRANSACTION" t
JOIN WALLET w ON t.wallet_id = w.wallet_id
JOIN USERS u  ON w.user_id = u.user_id
WHERE u.email = 'customer@flexinvest.vn'
  AND t.type_code = 'DEPOSIT'
  AND t.status = 'PENDING'
  AND ROWNUM = 1
ORDER BY t.created_at DESC;

-- Lệnh nạp PENDING #2 — 2 triệu
INSERT INTO "TRANSACTION" (wallet_id, type_code, amount, status, created_at, is_deleted)
SELECT w.wallet_id, 'DEPOSIT', 2000000, 'PENDING', SYSDATE - 0.05, 0
FROM WALLET w
JOIN USERS u ON w.user_id = u.user_id
WHERE u.email = 'customer@flexinvest.vn';

INSERT INTO DEPOSIT (transaction_id, request_code, payment_gateway, receiving_account, is_deleted)
SELECT t.transaction_id,
       'DEP' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '002',
       'MOMO', 'momo@flexinvest', 0
FROM (
  SELECT t.transaction_id, t.created_at
  FROM "TRANSACTION" t
  JOIN WALLET w ON t.wallet_id = w.wallet_id
  JOIN USERS u  ON w.user_id = u.user_id
  WHERE u.email = 'customer@flexinvest.vn'
    AND t.type_code = 'DEPOSIT'
    AND t.status = 'PENDING'
  ORDER BY t.created_at DESC
) t WHERE ROWNUM = 1;

-- =============================================================================
-- 12. MISSIONS
-- =============================================================================
-- Điểm danh (DAILY) — 1 mission duy nhất theo đúng MissionDAO.getCheckInMission()
-- Code dùng ROWNUM=1 + action_type='CHECKIN' (không có dấu gạch ngang)
-- target_value=7 → streak 7 ngày; reward_token=5 được cộng mỗi ngày điểm danh
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Chuỗi điểm danh 7 ngày', 'Điểm danh liên tiếp 7 ngày để hoàn thành chuỗi và nhận thưởng', 'DAILY', 'CHECKIN', 7, 5, 1, 0, SYSDATE, 0);

-- Hàng tuần (WEEKLY)
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Chia sẻ ứng dụng', 'Chia sẻ FlexInvest lên mạng xã hội',                            'WEEKLY', 'SHARE_APP', 1,      50,   1, 1, SYSDATE, 0);
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Nạp 100.000 VNĐ',  'Nạp ít nhất 100.000 VNĐ vào gói tích lũy trong tuần',           'WEEKLY', 'DEPOSIT',   100000, 100,  1, 2, SYSDATE, 0);
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Nạp 500.000 VNĐ',  'Nạp ít nhất 500.000 VNĐ vào gói tích lũy trong tuần',           'WEEKLY', 'DEPOSIT',   500000, 750,  1, 3, SYSDATE, 0);

-- Hàng tháng (MONTHLY)
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Nạp tiền 10 ngày',      'Thực hiện nạp tiền ít nhất 10 ngày trong tháng',                      'MONTHLY', 'DEPOSIT_DAYS',    10,      1000, 1, 1, SYSDATE, 0);
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Duy trì số dư 1 triệu', 'Duy trì số dư tích lũy cả tháng trên 1.000.000 VNĐ',                 'MONTHLY', 'MAINTAIN_BALANCE', 1000000, 1500, 1, 2, SYSDATE, 0);
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Mời bạn mới',           'Mời thành công 1 bạn đã KYC và nạp ít nhất 50.000 VNĐ',              'MONTHLY', 'REFERRAL',         1,       2000, 1, 3, SYSDATE, 0);
INSERT INTO MISSIONS (title, description, mission_type, action_type, target_value, reward_token, is_active, sort_order, created_at, is_deleted)
VALUES ('Top 20 nạp tiền',       'Nằm trong top 20 người nạp tiền nhiều nhất trong tháng',              'MONTHLY', 'TOP_DEPOSIT',       20,      5000, 1, 4, SYSDATE, 0);

-- =============================================================================
-- 13. TOKEN WALLET (cần có để MissionPanel hiển thị số dư FlexToken)
-- =============================================================================
INSERT INTO TOKEN (user_id, balance, total_earned, status, is_deleted)
SELECT user_id, 120, 120, 'ACTIVE', 0 FROM USERS WHERE email = 'admin@flexinvest.vn';

INSERT INTO TOKEN (user_id, balance, total_earned, status, is_deleted)
SELECT user_id, 75, 75, 'ACTIVE', 0 FROM USERS WHERE email = 'staff@flexinvest.vn';

INSERT INTO TOKEN (user_id, balance, total_earned, status, is_deleted)
SELECT user_id, 200, 200, 'ACTIVE', 0 FROM USERS WHERE email = 'customer@flexinvest.vn';

-- =============================================================================
-- 14. USER_MISSION — gán nhiệm vụ tuần/tháng cho customer để tab không trống
-- =============================================================================
-- Nhiệm vụ tuần: Chia sẻ ứng dụng (IN_PROGRESS)
INSERT INTO USER_MISSION (user_id, mission_id, status, progress, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT mission_id FROM MISSIONS WHERE action_type = 'SHARE_APP' AND is_deleted = 0 AND ROWNUM = 1),
  'IN_PROGRESS', 0, 0
FROM DUAL;

-- Nhiệm vụ tuần: Nạp 100.000 VNĐ (đã hoàn thành, chưa nhận)
INSERT INTO USER_MISSION (user_id, mission_id, status, progress, completed_at, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT mission_id FROM MISSIONS WHERE action_type = 'DEPOSIT' AND mission_type = 'WEEKLY' AND target_value = 100000 AND is_deleted = 0 AND ROWNUM = 1),
  'COMPLETED', 100000, SYSDATE - 1, 0
FROM DUAL;

-- Nhiệm vụ tuần: Nạp 500.000 VNĐ (đang tiến hành 60%)
INSERT INTO USER_MISSION (user_id, mission_id, status, progress, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT mission_id FROM MISSIONS WHERE action_type = 'DEPOSIT' AND mission_type = 'WEEKLY' AND target_value = 500000 AND is_deleted = 0 AND ROWNUM = 1),
  'IN_PROGRESS', 300000, 0
FROM DUAL;

-- Nhiệm vụ tháng: Nạp tiền 10 ngày (IN_PROGRESS — 3/10 ngày)
INSERT INTO USER_MISSION (user_id, mission_id, status, progress, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT mission_id FROM MISSIONS WHERE action_type = 'DEPOSIT_DAYS' AND is_deleted = 0 AND ROWNUM = 1),
  'IN_PROGRESS', 3, 0
FROM DUAL;

-- Nhiệm vụ tháng: Duy trì số dư 1 triệu (CLAIMED — đã nhận tháng trước)
INSERT INTO USER_MISSION (user_id, mission_id, status, progress, completed_at, claimed_at, is_deleted)
SELECT
  (SELECT user_id FROM USERS WHERE email = 'customer@flexinvest.vn'),
  (SELECT mission_id FROM MISSIONS WHERE action_type = 'MAINTAIN_BALANCE' AND is_deleted = 0 AND ROWNUM = 1),
  'CLAIMED', 1000000, SYSDATE - 5, SYSDATE - 3, 0
FROM DUAL;

-- =============================================================================
-- COMMIT tất cả
-- =============================================================================
COMMIT;

-- =============================================================================
-- KIỂM TRA KẾT QUẢ
-- =============================================================================
SELECT 'USERS'            AS tbl, COUNT(*) AS cnt FROM USERS           WHERE is_deleted = 0
UNION ALL SELECT 'ACCOUNT',             COUNT(*) FROM ACCOUNT          WHERE is_deleted = 0
UNION ALL SELECT 'WALLET',              COUNT(*) FROM WALLET           WHERE is_deleted = 0
UNION ALL SELECT 'SYS_FUNCTION',        COUNT(*) FROM SYS_FUNCTION
UNION ALL SELECT 'SYS_ROLE',            COUNT(*) FROM SYS_ROLE
UNION ALL SELECT 'SAVINGS_PRODUCT',     COUNT(*) FROM SAVINGS_PRODUCT  WHERE is_deleted = 0
UNION ALL SELECT 'INVESTMENT',          COUNT(*) FROM INVESTMENT        WHERE is_deleted = 0
UNION ALL SELECT 'EKYC',                COUNT(*) FROM EKYC             WHERE is_deleted = 0
UNION ALL SELECT 'BANK_ACCOUNT',        COUNT(*) FROM BANK_ACCOUNT     WHERE is_deleted = 0
UNION ALL SELECT 'DEPOSIT_PENDING',     COUNT(*) FROM DEPOSIT d
          JOIN "TRANSACTION" t ON d.transaction_id = t.transaction_id
          WHERE t.status = 'PENDING' AND d.is_deleted = 0
UNION ALL SELECT 'MISSIONS',            COUNT(*) FROM MISSIONS         WHERE is_deleted = 0
UNION ALL SELECT 'TOKEN',               COUNT(*) FROM TOKEN            WHERE is_deleted = 0
UNION ALL SELECT 'USER_MISSION',        COUNT(*) FROM USER_MISSION     WHERE is_deleted = 0;
