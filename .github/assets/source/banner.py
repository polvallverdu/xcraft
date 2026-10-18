import math, os, sys
from pix import Canvas, hexc
from font import SMALL, TITLE, layout
import icon

P = icon.P
W, H, SCALE = 320, 100, 4
OUT = sys.argv[1] if len(sys.argv) > 1 else 'out'

C_OUT = hexc('061416')
TEXT_BANDS = {  # per-group colours: (top band, mid band, bottom band, extrusion)
    'x': (hexc('ffd76a'), hexc('ffa22a'), hexc('f07a14'), hexc('8a3208')),
    'C': (hexc('ffffff'), hexc('b8fbf4'), hexc('5fe3e0'), hexc('17607a')),
}


def panel(c, r=6):
    for y in range(H):
        if y < r:
            d = r - y - 0.5
        elif y >= H - r:
            d = y - (H - r) + 0.5
        else:
            d = 0
        inset = int(round(r - math.sqrt(max(r * r - d * d, 0)))) if d else 0
        for x in range(inset, W - inset):
            # dithered vertical gradient
            t = y / H
            if t < 0.30 or (t < 0.36 and (x + y) % 2 == 0):
                col = P['bg2']
            elif t < 0.68 or (t < 0.74 and (x + y) % 2 == 0):
                col = P['bg1']
            else:
                col = P['bg0']
            c.put(x, y, col)
    # outline + bevel
    edge = [(x, y) for y in range(H) for x in range(W) if c.get(x, y) is not None and
            any(c.get(x + dx, y + dy) is None for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            or (c.get(x, y) is not None and (x in (0, W - 1) or y in (0, H - 1)))]
    for x, y in edge:
        c.put(x, y, P['bgO'])
    for x, y in edge:
        if y < H // 2 and c.get(x, y + 1) not in (None, P['bgO']):
            c.put(x, y + 1, P['bgH'])
        if y > H // 2 and c.get(x, y - 1) not in (None, P['bgO']):
            c.put(x, y - 1, P['bgS'])


def pattern(c):
    """Faint scattered gem silhouettes in the background."""
    faint = hexc('1d6463')
    faint2 = hexc('14504f')
    gem = [".x.", "xxx", ".x."]
    seed = 7
    pts = []
    for gy in range(4, H - 6, 9):
        for gx in range(150, W - 6, 11):
            seed = (seed * 1103515245 + 12345) & 0x7fffffff
            if seed % 3 == 0:
                continue
            ox = (seed >> 4) % 5
            oy = (seed >> 8) % 4
            pts.append((gx + ox + (gy // 9 % 2) * 5, gy + oy, faint if (seed >> 12) % 2 else faint2))
    for x, y, col in pts:
        for j, row in enumerate(gem):
            for i, ch in enumerate(row):
                if ch == 'x' and c.get(x + i, y + j) not in (None, P['bgO'], P['bgH'], P['bgS']):
                    c.put(x + i, y + j, col)


TONES = {  # base-top, base-bottom, highlight, shade, extrusion
    'x': (hexc('ffab2e'), hexc('f28a18'), hexc('ffe08a'), hexc('c95a0c'), hexc('7a2c07')),
    'C': (hexc('bff9f3'), hexc('7debe6'), hexc('ffffff'), hexc('3cc3c9'), hexc('145a73')),
}


def title(text='xCraft', k=4, depth=4):
    glyphs, tw = layout(text, TITLE, gap=1)
    Wt, Ht = tw * k + 2, 10 * k + depth + 3
    mask = Canvas(Wt, Ht)      # which group owns each pixel
    for gx, top, rows, ch in glyphs:
        grp = 'x' if ch == 'x' else 'C'
        for j, row in enumerate(rows):
            for i, v in enumerate(row):
                if v == 'X':
                    for b in range(k):
                        for a in range(k):
                            mask.put(1 + (gx + i) * k + a, 1 + (top + j) * k + b, grp)
    t = Canvas(Wt, Ht)
    # extrusion first
    for y in range(Ht):
        for x in range(Wt):
            g = mask.get(x, y)
            if g:
                for d in range(1, depth + 1):
                    if mask.get(x, y + d) is None:
                        t.put(x, y + d, TONES[g][4])
    for y in range(Ht):
        for x in range(Wt):
            g = mask.get(x, y)
            if not g:
                continue
            top_c, bot_c, hi, sh, _ = TONES[g]
            col = top_c if y < 1 + 6 * k else bot_c
            if mask.get(x, y - 1) is None or mask.get(x - 1, y) is None:
                col = hi
            elif mask.get(x, y + 1) is None or mask.get(x + 1, y) is None:
                col = sh
            t.put(x, y, col)
    t.outline(C_OUT)
    return t


def small_text(c, x, y, text, color, shadow):
    glyphs, tw = layout(text, SMALL, gap=1)
    for gx, top, rows, ch in glyphs:
        for j, row in enumerate(rows):
            for i, v in enumerate(row):
                if v == 'X':
                    c.put(x + gx + i, y + top + j + 1, shadow)
    for gx, top, rows, ch in glyphs:
        col = color(ch, gx) if callable(color) else color
        for j, row in enumerate(rows):
            for i, v in enumerate(row):
                if v == 'X':
                    c.put(x + gx + i, y + top + j, col)
    return tw


def build():
    c = Canvas(W, H)
    panel(c)
    pattern(c)
    ic = icon.build()
    # soft drop shadow for the icon tile
    sh = Canvas(64, 64)
    sh.paste(ic, 0, 0, 2)
    t = title()
    _, tagw = layout("Multiply drops, XP & smelting speed", SMALL)
    group_w = 64 + 16 + max(t.w, tagw)
    ix = (W - group_w) // 2
    iy = (H - 64) // 2 - 1
    for y in range(64):
        for x in range(64):
            if sh.get(x, y) is not None:
                c.put(ix + x + 2, iy + y + 2, hexc('061416'))
    c.paste(ic, ix, iy, 2)
    tx = ix + 64 + 16
    ty = iy - 1
    c.paste(t, tx - 1, ty)
    tag_y = ty + t.h + 6
    gold = hexc('ffcf5a')
    soft = hexc('bfe9e3')
    def col(ch, gx):
        return gold if gx < 40 else soft
    small_text(c, tx, tag_y, "Multiply drops, XP & smelting speed", col, hexc('061416'))
    print('title w', t.w, 'tag w', tagw, 'right edge', tx + max(t.w, tagw))
    return c


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    c = build()
    c.save_png(f'{OUT}/banner.png', SCALE)
    c.save_png(f'{OUT}/banner-1x.png', 1)
