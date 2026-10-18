"""Tiny stdlib pixel-art toolkit: canvas, sprite blitting, PNG + SVG output."""
import struct, zlib


def hexc(h, a=255):
    h = h.lstrip('#')
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), a)


class Canvas:
    def __init__(self, w, h):
        self.w, self.h = w, h
        self.px = [[None] * w for _ in range(h)]

    def put(self, x, y, c):
        if 0 <= x < self.w and 0 <= y < self.h and c is not None:
            self.px[y][x] = c

    def get(self, x, y):
        if 0 <= x < self.w and 0 <= y < self.h:
            return self.px[y][x]
        return None

    def rect(self, x, y, w, h, c):
        for j in range(y, y + h):
            for i in range(x, x + w):
                self.put(i, j, c)

    def sprite(self, rows, pal, x, y, flip=False):
        for j, row in enumerate(rows):
            if flip:
                row = row[::-1]
            for i, ch in enumerate(row):
                if ch in ('.', ' '):
                    continue
                self.put(x + i, y + j, pal[ch])

    def paste(self, other, x, y, scale=1):
        for j in range(other.h):
            for i in range(other.w):
                c = other.px[j][i]
                if c is not None:
                    for b in range(scale):
                        for a in range(scale):
                            self.put(x + i * scale + a, y + j * scale + b, c)

    def outline(self, c, diag=False):
        """Add 1px outline around all opaque pixels (into transparent ones)."""
        add = []
        nb = [(1, 0), (-1, 0), (0, 1), (0, -1)]
        if diag:
            nb += [(1, 1), (-1, -1), (1, -1), (-1, 1)]
        for y in range(self.h):
            for x in range(self.w):
                if self.px[y][x] is None:
                    if any(self.get(x + dx, y + dy) is not None for dx, dy in nb):
                        add.append((x, y))
        for x, y in add:
            self.px[y][x] = c

    def shadow(self, c, dx=1, dy=1):
        """Drop shadow below existing pixels (only into transparent ones)."""
        add = []
        for y in range(self.h):
            for x in range(self.w):
                if self.px[y][x] is None and self.get(x - dx, y - dy) is not None:
                    add.append((x, y))
        for x, y in add:
            self.px[y][x] = c

    # ---- output -------------------------------------------------------
    def save_png(self, path, scale=1):
        W, H = self.w * scale, self.h * scale
        raw = bytearray()
        for y in range(H):
            raw.append(0)
            row = self.px[y // scale]
            for x in range(W):
                c = row[x // scale] or (0, 0, 0, 0)
                raw.extend(c)
        def chunk(t, d):
            return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
        png = b'\x89PNG\r\n\x1a\n'
        png += chunk(b'IHDR', struct.pack('>IIBBBBB', W, H, 8, 6, 0, 0, 0))
        png += chunk(b'IDAT', zlib.compress(bytes(raw), 9))
        png += chunk(b'IEND', b'')
        with open(path, 'wb') as f:
            f.write(png)

    def save_svg(self, path, scale=16, title='xCraft'):
        out = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.w*scale}" height="{self.h*scale}" '
               f'viewBox="0 0 {self.w*scale} {self.h*scale}" shape-rendering="crispEdges">',
               f'<title>{title}</title>']
        for y in range(self.h):
            x = 0
            while x < self.w:
                c = self.px[y][x]
                if c is None:
                    x += 1
                    continue
                n = 1
                while x + n < self.w and self.px[y][x + n] == c:
                    n += 1
                fill = '#%02x%02x%02x' % c[:3]
                op = '' if c[3] == 255 else f' fill-opacity="{c[3]/255:.3f}"'
                out.append(f'<rect x="{x*scale}" y="{y*scale}" width="{n*scale}" height="{scale}" fill="{fill}"{op}/>')
                x += n
        out.append('</svg>')
        with open(path, 'w') as f:
            f.write('\n'.join(out) + '\n')
