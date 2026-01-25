# Message format

Each message is stored as a sequence of bytes:

```
id (i64)
is_deleted (bool) -- needed by FPLS
type (i8)
author_id (i64)
author_name (i64, string id in VLS)
author_handle (i64, string id in VLS)
text (i64, string id in VLS)
media (i64, vector<i64, file id> id in VLS)
has_reply (bool)
reply_to (i64, !has_reply => 0)
```
