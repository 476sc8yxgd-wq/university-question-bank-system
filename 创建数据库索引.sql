-- 创建数据库索引以提升性能
-- 请在 Supabase SQL Editor 中执行此脚本

-- 1. 为 questions 表创建全文搜索索引（最重要）
-- 使用 GIN 索引，可以大幅提升 LIKE 和全文搜索的性能
CREATE INDEX IF NOT EXISTS idx_questions_content_gin
ON questions
USING GIN (to_tsvector('simple', question_content));

-- 2. 为常用查询字段创建普通索引
CREATE INDEX IF NOT EXISTS idx_questions_category_id
ON questions(category_id);

CREATE INDEX IF NOT EXISTS idx_questions_difficulty_id
ON questions(difficulty_id);

CREATE INDEX IF NOT EXISTS idx_questions_creator_id
ON questions(creator_id);

CREATE INDEX IF NOT EXISTS idx_questions_created_at
ON questions(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_questions_question_type
ON questions(question_type);

-- 3. 为 users 表创建索引
CREATE INDEX IF NOT EXISTS idx_users_username
ON users(username);

CREATE INDEX IF NOT EXISTS idx_users_role_id
ON users(role_id);

-- 4. 为 question_categories 表创建索引
CREATE INDEX IF NOT EXISTS idx_categories_name
ON question_categories(category_name);

-- 5. 为 question_difficulties 表创建索引
CREATE INDEX IF NOT EXISTS idx_difficulties_level
ON question_difficulties(difficulty_level);

-- 验证索引创建成功
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('questions', 'users', 'question_categories', 'question_difficulties')
ORDER BY tablename, indexname;

-- 显示索引创建结果
SELECT
    '索引创建完成！' AS status;
