-- Добавление поля message_thread_id для поддержки топиков форума
ALTER TABLE active_chats ADD COLUMN message_thread_id INTEGER;

