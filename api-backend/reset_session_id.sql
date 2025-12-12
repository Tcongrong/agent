-- 重置chat_session表的自增ID为1
ALTER TABLE chat_session AUTO_INCREMENT = 1;

-- 如果需要清空表数据并重置ID（谨慎使用）
-- TRUNCATE TABLE chat_session;