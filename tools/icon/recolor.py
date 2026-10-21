# Recolours an image into Catppuccin Mocha: each pixel becomes a weighted blend of the palette
# colours nearest to it in CIELAB, so shading survives instead of posterising.
import sys
import numpy as np
from PIL import Image

MOCHA = ["f5e0dc","f2cdcd","f5c2e7","cba6f7","f38ba8","eba0ac","fab387","f9e2af","a6e3a1","94e2d5",
         "89dceb","74c7ec","89b4fa","b4befe","cdd6f4","bac2de","a6adc8","9399b2","7f849c","6c7086",
         "585b70","45475a","313244","1e1e2e","181825","11111b"]

def srgb_to_lab(rgb):
    c = rgb / 255.0
    c = np.where(c > 0.04045, ((c + 0.055) / 1.055) ** 2.4, c / 12.92)
    m = np.array([[0.4124, 0.3576, 0.1805], [0.2126, 0.7152, 0.0722], [0.0193, 0.1192, 0.9505]])
    xyz = c @ m.T / np.array([0.95047, 1.0, 1.08883])
    f = np.where(xyz > 0.008856, np.cbrt(xyz), 7.787 * xyz + 16 / 116)
    return np.stack([116 * f[..., 1] - 16, 500 * (f[..., 0] - f[..., 1]), 200 * (f[..., 1] - f[..., 2])], -1)

src, dst = sys.argv[1], sys.argv[2]
sigma = float(sys.argv[3]) if len(sys.argv) > 3 else 9.0
chroma = float(sys.argv[4]) if len(sys.argv) > 4 else 1.0  # >1 pushes coloured areas onto the accents instead of the greys
img = np.asarray(Image.open(src).convert("RGB")).astype(np.float64)
pal = np.array([[int(h[i:i + 2], 16) for i in (0, 2, 4)] for h in MOCHA], dtype=np.float64)
pal_lab = srgb_to_lab(pal)
def blur(channel, radius):
    # separable gaussian, used on colour only so the line art stays sharp
    x = np.arange(-3 * radius, 3 * radius + 1)
    k = np.exp(-x ** 2 / (2 * radius ** 2)); k /= k.sum()
    pad = np.pad(channel, 3 * radius, mode="edge")
    rows = np.apply_along_axis(lambda r: np.convolve(r, k, "valid"), 1, pad)
    return np.apply_along_axis(lambda c: np.convolve(c, k, "valid"), 0, rows)

lab = srgb_to_lab(img)
for ch in (1, 2):
    lab[..., ch] = blur(lab[..., ch], 3) * chroma
lab = lab.reshape(-1, 3)
d2 = ((lab[:, None, :] - pal_lab[None, :, :]) ** 2).sum(-1)
d2 -= d2.min(1, keepdims=True)  # numerical stability: the nearest colour gets weight 1
w = np.exp(-d2 / (2 * sigma ** 2))
out = (w @ pal) / w.sum(1, keepdims=True)
Image.fromarray(out.reshape(img.shape).clip(0, 255).astype(np.uint8)).save(dst)
