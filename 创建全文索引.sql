-- 为 questions 表创建全文索引
-- 这可以大幅提升搜索性能

-- 1. 创建 GIN 索引用于全文搜索
CREATE INDEX IF NOT EXISTS idx_questions_content_fts
ON questions
USING GIN (to_tsvector('simple', question_content));

-- 2. 创建普通索引用于快速查询
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

-- 验证索引创建成功
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'questions'
ORDER BY indexname;
