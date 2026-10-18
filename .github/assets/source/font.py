"""Hand-drawn pixel fonts. Each glyph: (top_row, rows). Baseline is row 6 (small) / row 9 (title)."""

SMALL = {
    'M': (0, ["X...X", "XX.XX", "X.X.X", "X...X", "X...X", "X...X", "X...X"]),
    'X': (0, ["X...X", "X...X", ".X.X.", "..X..", ".X.X.", "X...X", "X...X"]),
    'P': (0, ["XXXX.", "X...X", "X...X", "XXXX.", "X....", "X....", "X...."]),
    '&': (0, [".XX..", "X..X.", ".XX..", ".XX.X", "X..X.", "X..X.", ".XX.X"]),
    'd': (0, ["....X", "....X", ".XX.X", "X..XX", "X...X", "X...X", ".XXXX"]),
    'l': (0, ["X.", "X.", "X.", "X.", "X.", "X.", ".X"]),
    'i': (0, ["X", ".", "X", "X", "X", "X", "X"]),
    't': (1, [".X.", "XXX", ".X.", ".X.", ".X.", "..X"]),
    'u': (2, ["X...X", "X...X", "X...X", "X...X", ".XXXX"]),
    'r': (2, ["X.XX.", "XX..X", "X....", "X....", "X...."]),
    'o': (2, [".XXX.", "X...X", "X...X", "X...X", ".XXX."]),
    's': (2, [".XXXX", "X....", ".XXX.", "....X", "XXXX."]),
    'm': (2, ["XX.X.", "X.X.X", "X.X.X", "X...X", "X...X"]),
    'e': (2, [".XXX.", "X...X", "XXXXX", "X....", ".XXXX"]),
    'n': (2, ["X.XX.", "XX..X", "X...X", "X...X", "X...X"]),
    'p': (2, ["X.XX.", "XX..X", "X...X", "X...X", "XXXX.", "X....", "X...."]),
    'y': (2, ["X...X", "X...X", "X...X", "X...X", ".XXXX", "....X", "XXXX."]),
    'g': (2, [".XXXX", "X...X", "X...X", "X...X", ".XXXX", "....X", "XXXX."]),
    ',': (5, [".X", ".X", "X."]),
    ' ': (0, ["..."]),
}

TITLE = {
    'x': (3, ["XX....XX", "XXX..XXX", ".XXXXXX.", "..XXXX..", ".XXXXXX.", "XXX..XXX", "XX....XX"]),
    'C': (0, ["..XXXXX.", ".XXXXXXX", "XXX...XX", "XX......", "XX......",
              "XX......", "XX......", "XXX...XX", ".XXXXXXX", "..XXXXX."]),
    'r': (3, ["XX.XXX.", "XXXXXXX", "XXX..XX", "XX.....", "XX.....", "XX.....", "XX....."]),
    'a': (3, [".XXXXX.", ".....XX", ".XXXXXX", "XXXXXXX", "XX...XX", "XXXXXXX", ".XXXXXX"]),
    'f': (0, ["...XXX", "..XXXX", "..XX..", "XXXXXX", "XXXXXX", "..XX..", "..XX..", "..XX..", "..XX..", "..XX.."]),
    't': (1, ["..XX..", "..XX..", "XXXXXX", "XXXXXX", "..XX..", "..XX..", "..XX..", "..XXXX", "...XXX"]),
}


def layout(text, font, gap=1):
    """Return list of (x, top, rows, ch) and total width."""
    x = 0
    out = []
    for ch in text:
        top, rows = font[ch]
        out.append((x, top, rows, ch))
        x += len(rows[0]) + gap
    return out, x - gap
