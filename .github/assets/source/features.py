import math, os, sys
from pix import Canvas, hexc
import icon

P = icon.P
OUT = sys.argv[1] if len(sys.argv) > 1 else 'out'
K = hexc('0a1a1c')  # universal outline


def obj(w=16, h=16):
    return Canvas(w, h)


def finish(c):
    c.outline(K)
    return c


def ore_block():
    rows = [
        "lllllllllll",
        "lmmmmdmmmmd",
        "lmWLmmmmdmd",
        "lmLMDmmmmmd",
        "lmdEmmWLmmd",
        "lmmmmmLMDmd",
        "ldmmmmdEmmd",
        "lmWLmmmmmmd",
        "lmLMDmmdmmd",
        "lmmEmmmmmmd",
        "ddddddddddd",
    ]
    c = obj()
    c.sprite(rows, icon.pal(), 1, 4)
    return finish(c)


def pickaxe():
    c = obj()
    bx, by = 1.5, 14.5
    head = {}
    for y in range(16):
        for x in range(16):
            dx, dy = x + .5 - bx, y + .5 - by
            d = math.hypot(dx, dy)
            p = (dx + dy) / math.sqrt(2)
            if 10.6 <= d <= 12.6 and abs(p) <= 6.2:
                head[(x, y)] = d
    for (x, y), d in head.items():
        c.put(x, y, P['L'] if d > 11.9 else P['M'])
    # handle (two-pixel staircase)
    for i in range(10):
        x, y = 2 + i, 13 - i
        if (x, y) not in head:
            c.put(x, y, hexc('c89a5a'))
        if (x + 1, y) not in head:
            c.put(x + 1, y, hexc('7a5530'))
    # head shading: darker on the inner edge
    for (x, y), d in head.items():
        if d < 11.2:
            c.put(x, y, P['D'])
    return finish(c)


def ores():
    c = obj()
    b = ore_block()
    c.paste(b, 0, 0)
    p = pickaxe()
    c.paste(p, 1, -1)
    return c


def zombie_head():
    rows = [
        "hhhhhhhhhhh",
        "hhhhhhhhhhh",
        "hbhhbhhhbhh",
        "bbcbbbbbcbb",
        "bbbbbbbbbbb",
        "beebbbbbeeb",
        "beebbbbbeeb",
        "bbbbbnbbbbb",
        "bcbbbnbbbcb",
        "bbmmmmmmmbb",
        "abbbbbbbbba",
    ]
    pal = {'h': hexc('24521f'), 'a': hexc('3a7d31'), 'b': hexc('55a043'), 'c': hexc('77c75c'),
           'e': hexc('0b140c'), 'n': hexc('3a7d31'), 'm': hexc('2a5c24')}
    c = obj()
    c.sprite(rows, pal, 1, 1)
    return finish(c)


def bone():
    c = obj()
    lite, mid, shade = hexc('fbf7e6'), hexc('ddd6b8'), hexc('a59d7e')
    # shaft from (7,14) to (14,7)
    for i in range(7):
        x, y = 7 + i, 13 - i
        c.put(x, y, lite)
        c.put(x + 1, y, shade)
    # knobs
    for (x, y) in [(5, 13), (6, 14), (6, 13), (6, 15), (5, 14)]:
        c.put(x, y, mid)
    c.put(6, 13, lite)
    for (x, y) in [(14, 5), (15, 6), (14, 6), (13, 5), (15, 7)]:
        c.put(x, y, mid)
    c.put(14, 6, lite)
    c.put(15, 7, shade)
    c.put(6, 15, shade)
    return finish(c)


def mobs():
    c = obj()
    c.paste(zombie_head(), 0, 0)
    c.paste(bone(), -1, 0)
    return c


def orb(r, cx, cy):
    c = obj()
    dark, mid, lite, hi = hexc('3f9a12'), hexc('8ee62c'), hexc('d6ff5e'), hexc('ffffd8')
    for y in range(16):
        for x in range(16):
            dx, dy = x + .5 - cx, y + .5 - cy
            d = math.hypot(dx, dy)
            if d <= r:
                # light from top-left
                l = math.hypot(dx + r * .35, dy + r * .35)
                col = lite if l < r * .55 else (mid if l < r * 1.05 else dark)
                c.put(x, y, col)
    hx, hy = int(cx - r * .45), int(cy - r * .45)
    c.put(hx, hy, hi)
    if r > 3:
        c.put(hx + 1, hy, hi)
        c.put(hx, hy + 1, hi)
    return finish(c)


def xp():
    c = obj()
    c.paste(orb(2.3, 3.0, 3.0), 0, 0)
    c.paste(orb(2.3, 13.0, 3.5), 0, 0)
    c.paste(orb(5.2, 7.5, 10.5), 0, 0)
    for x, y in [(14, 11), (8, 1)]:
        c.put(x, y, P['Y'])
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            c.put(x + dx, y + dy, P['y'])
    return c


def furnace():
    rows = [
        "llllllllllllll",
        "lmmmmmmmmmmmmd",
        "lmddddddddddmd",
        "lmmmmmmmmmmmmd",
        "lmmdmmmmmmdmmd",
        "lmmmmmmmmmmmmd",
        "lmOOOOOOOOOOmd",
        "lmOfYfffYffOmd",
        "lmOYwYfYwYfOmd",
        "lmOYwwYwwwYOmd",
        "lmOrrrrrrrrOmd",
        "lmOOOOOOOOOOmd",
        "lmmmmmmmmmmmmd",
        "dddddddddddddd",
    ]
    pal = {'l': P['l'], 'm': P['m'], 'd': P['d'], 'O': hexc('202027'),
           'f': hexc('ff7a1a'), 'Y': hexc('ffc93a'), 'w': hexc('fff3b0'), 'r': hexc('d4351c')}
    c = obj()
    c.sprite(rows, pal, 1, 1)
    return finish(c)


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for name, fn in [('ores', ores), ('mobs', mobs), ('xp', xp), ('furnace', furnace)]:
        fn().save_png(f'{OUT}/feature-{name}.png', 4)
