def adapt_string(text: str) -> str:
    chars = [c.encode("utf-8") for c in text]
    return "".join(filter(_is_valid_char_for_java, chars))

def _is_valid_char_for_java(c: bytes) -> bool:
    if c[0] <= 127:
        return True
    if 192 <= c[0] <= 223 and (len(c) < 2 or 128 <= c[1] <= 191):
        return False
    if 224 <= c[0] <= 239 and (len(c) < 2 or 128 <= c[1] <= 191):
        return False
    if c[0] >= 240 or c[0] <= 191:
        return False
    return True
