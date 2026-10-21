# Builds the Vita3kAI icons from the Mocha-recoloured artwork
import sys, os
import numpy as np
from PIL import Image, ImageDraw

art = np.asarray(Image.open(sys.argv[1]).convert("RGB"))
res = sys.argv[2]
PAD = 200
# mirror the wall on the left so the adaptive layer has room around the hat
art = np.pad(art, ((0, 0), (PAD, 0), (0, 0)), mode="reflect")
img = Image.fromarray(art)

# what people see: the head, braid and book (crop b, shifted by the padding)
vis = (20 + PAD, 250, 560 + PAD, 790)
cx, cy, size = (vis[0] + vis[2]) / 2, (vis[1] + vis[3]) / 2, vis[2] - vis[0]

# app art: the visible square with rounded corners
square = img.crop(vis).resize((512, 512), Image.LANCZOS)
mask = Image.new("L", (512 * 4, 512 * 4), 0)
ImageDraw.Draw(mask).rounded_rectangle((0, 0, 512 * 4 - 1, 512 * 4 - 1), radius=112 * 4, fill=255)
square.putalpha(mask.resize((512, 512), Image.LANCZOS))
os.makedirs(f"{res}/drawable-nodpi", exist_ok=True)
square.save(f"{res}/drawable-nodpi/vita3kai_art.png", optimize=True)

# adaptive foreground: a 108dp layer whose middle 72dp is the visible square
layer = size * 108 / 72
box = (int(cx - layer / 2), int(cy - layer / 2), int(cx + layer / 2), int(cy + layer / 2))
assert box[0] >= 0 and box[1] >= 0 and box[2] <= img.width and box[3] <= img.height, box
full = img.crop(box)
for density, px in {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}.items():
    os.makedirs(f"{res}/mipmap-{density}", exist_ok=True)
    full.resize((px, px), Image.LANCZOS).save(f"{res}/mipmap-{density}/ic_launcher_vita3kai_foreground.png", optimize=True)

# preview of how a circular launcher mask shows it
prev = full.resize((432, 432), Image.LANCZOS)
m = Image.new("L", (432, 432), 0)
ImageDraw.Draw(m).ellipse((72, 72, 360, 360), fill=255)
bg = Image.new("RGB", (432, 432), (30, 30, 46))
bg.paste(prev, (0, 0), m)
bg.crop((60, 60, 372, 372)).save(sys.argv[3])
