-- Добавление поддержки вакансий в таблицу active_chats
ALTER TABLE active_chats ADD COLUMN IF NOT EXISTS vacancies BOOLEAN DEFAULT FALSE;
ALTER TABLE active_chats ADD COLUMN IF NOT EXISTS vacancy_areas TEXT;

