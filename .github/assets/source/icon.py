from pix import Canvas, hexc

P = {
    # background
    'bgO': hexc('071b1d'), 'bg0': hexc('0d3436'), 'bg1': hexc('124547'), 'bg2': hexc('18595a'),
    'bgH': hexc('2a7d78'), 'bgS': hexc('0a2a2c'),
    # diamond
    'o': hexc('0a2f3c'), 'W': hexc('ffffff'), 'L': hexc('9ff7f0'), 'M': hexc('3fdcdc'),
    'D': hexc('1b97ad'), 'E': hexc('136a85'),
    # stone
    'k': hexc('1c1d22'), 'l': hexc('a3a3ad'), 'm': hexc('7f7f8a'), 'd': hexc('5c5c67'),
    # badge
    'b': hexc('3d1405'), 'B1': hexc('ffcf5a'), 'B2': hexc('ff9a1f'), 'B3': hexc('e0620f'), 'B4': hexc('a8400a'),
    'g': hexc('fffaf0'), 'gs': hexc('6e2508'),
    # sparkle
    'y': hexc('ffe066'), 'Y': hexc('fff6c2'),
}

GEM_BIG = [
    "...ooo...",
    "..oWWLo..",
    ".oWWLLMo.",
    "oWLLLLMDo",
    "oLLLLMMDo",
    ".oMLMMDo.",
    "..oMDEo..",
    "...oEo...",
    "....o....",
]
GEM_SMALL = [
    "..ooo..",
    ".oWLMo.",
    "oWLLMDo",
    "oLMMMDo",
    ".oMDEo.",
    "..oEo..",
    "...o...",
]

ORE = [
    "kkkkkkkkkkkkkk",
    "kllllllllllllk",
    "klmmmmdmmmmmdk",
    "klmWLmmmmmmmdk",
    "klmLMDdmmWLmdk",
    "klmdDmmmmLMDdk",
    "klmmmmdmmdEmdk",
    "kldmmmmmmmmmdk",
    "klmmWLmmmmdmdk",
    "klmLMDmmWLmmdk",
    "klmmEdmmMDdmdk",
    "klmmmmmmmEmmdk",
    "kldddddddddddk",
    "kkkkkkkkkkkkkk",
]

TIMES = [  # bold multiplication sign 5x5
    "g...g",
    "gg.gg",
    ".ggg.",
    "gg.gg",
    "g...g",
]
FOUR = [
    "gg.gg",
    "gg.gg",
    "gg.gg",
    "ggggg",
    "...gg",
    "...gg",
    "...gg",
]


def pal(**kw):
    d = {'o': P['o'], 'W': P['W'], 'L': P['L'], 'M': P['M'], 'D': P['D'], 'E': P['E'],
         'k': P['k'], 'l': P['l'], 'm': P['m'], 'd': P['d'], 'g': P['g']}
    d.update(kw)
    return d


def background(c):
    N = c.w
    r = [3, 2, 1]  # inset per row for rounded corners (rows 0..2)
    for y in range(N):
        inset = r[y] if y < 3 else (r[N - 1 - y] if y >= N - 3 else 0)
        for x in range(inset, N - inset):
            c.put(x, y, P['bg1'])
    # vertical gradient bands
    for y in range(N):
        for x in range(N):
            if c.get(x, y) is None:
                continue
            if y < 11 or (y == 11 and (x + y) % 2 == 0):
                c.put(x, y, P['bg2'])
            elif y > 22 or (y == 22 and (x + y) % 2 == 0):
                c.put(x, y, P['bg0'])
    # outline: any bg pixel touching outside -> outline
    edge = []
    for y in range(N):
        for x in range(N):
            if c.get(x, y) is None:
                continue
            if any(c.get(x + dx, y + dy) is None for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))) \
                    or x in (0, N - 1) or y in (0, N - 1):
                edge.append((x, y))
    for x, y in edge:
        c.put(x, y, P['bgO'])
    # inner bevel: top highlight, bottom shade
    for x, y in edge:
        if y <= 2 and c.get(x, y + 1) not in (None, P['bgO']):
            c.put(x, y + 1, P['bgH'])
        if y >= N - 3 and c.get(x, y - 1) not in (None, P['bgO']):
            c.put(x, y - 1, P['bgS'])


def glow(c, cx, cy, radii_cols):
    for r, col in radii_cols:
        for y in range(c.h):
            for x in range(c.w):
                cur = c.get(x, y)
                if cur in (None, P['bgO'], P['bgH'], P['bgS']):
                    continue
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                    c.put(x, y, col)


def badge(c, x, y):
    w, h = 15, 11
    # body with chamfered corners
    for j in range(h):
        for i in range(w):
            corner = (i in (0, w - 1)) and (j in (0, h - 1))
            if corner:
                continue
            edge = i in (0, w - 1) or j in (0, h - 1) or \
                ((i in (1, w - 2)) and (j in (1, h - 2)) and False)
            c.put(x + i, y + j, P['b'] if edge else P['B2'])
    # shading inside
    for i in range(1, w - 1):
        c.put(x + i, y + 1, P['B1'])
        c.put(x + i, y + h - 2, P['B3'])
    for j in range(1, h - 1):
        c.put(x + 1, y + j, P['B1'] if j < h - 2 else P['B3'])
        c.put(x + w - 2, y + j, P['B3'])
    c.put(x + 1, y + 1, P['B1'])
    # glyphs with hard drop shadow
    tx, ty = x + 2, y + 2
    ink = pal(g=P['b'])
    c.sprite(TIMES, ink, tx, ty + 2)
    c.sprite(FOUR, ink, tx + 6, ty)


def sparkle(c, x, y, big=False):
    c.put(x, y, P['Y'])
    for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        c.put(x + dx, y + dy, P['y'])
    if big:
        for dx, dy in ((2, 0), (-2, 0), (0, 2), (0, -2)):
            c.put(x + dx, y + dy, P['y'])


def trail(c, pts):
    for x, y in pts:
        c.put(x, y, P['bgH'])


def build():
    c = Canvas(32, 32)
    background(c)
    # motion trails from block to gems
    # little 'pop' sparks at the block's top corners
    for x, y in [(3, 13), (4, 12), (20, 12), (21, 11)]:
        c.put(x, y, P['y'])
    # ore block
    c.sprite(ORE, pal(), 5, 14)
    # gems flying out
    c.sprite(GEM_SMALL, pal(), 1, 6)
    c.sprite(GEM_BIG, pal(), 8, 2)
    c.sprite(GEM_SMALL, pal(), 18, 4)
    c.sprite(GEM_SMALL, pal(), 23, 10)
    sparkle(c, 27, 4)
    sparkle(c, 4, 15 - 13)
    # multiplier badge, overlapping block corner like a sticker
    badge(c, 16, 19)
    return c


if __name__ == '__main__':
    import sys, os
    out = sys.argv[1] if len(sys.argv) > 1 else 'out'
    os.makedirs(out, exist_ok=True)
    c = build()
    c.save_png(f'{out}/icon-32.png', 1)
    c.save_png(f'{out}/icon-512.png', 16)
    c.save_png(f'{out}/icon-128.png', 4)
    c.save_svg(f'{out}/icon.svg', 16)
