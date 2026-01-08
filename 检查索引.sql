-- 检查 questions 表的索引
SELECT
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename = 'questions'
ORDER BY indexname;

-- 检查是否有全文索引
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE indexname LIKE '%tsvector%'
   OR indexdef LIKE '%to_tsvector%';
