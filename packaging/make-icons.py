"""Renders the application icon (packaging/iorg.png and packaging/iorg.ico). Needs Pillow."""
from PIL import Image, ImageDraw, ImageFont

S = 1024  # drawn large, scaled down for smooth edges


def card(size, frame, sky, hill1, hill2, sun):
    w, h = size
    img = Image.new("RGBA", size, (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([0, 0, w - 1, h - 1], radius=int(w * 0.07), fill=frame)
    m = int(w * 0.08)
    inner = [m, m, w - m, h - m]
    pic = Image.new("RGBA", size, (0, 0, 0, 0))
    p = ImageDraw.Draw(pic)
    p.rectangle(inner, fill=sky)
    p.ellipse([w * 0.62, h * 0.18, w * 0.80, h * 0.18 + w * 0.18], fill=sun)
    p.polygon([(m, h - m), (w * 0.38, h * 0.40), (w * 0.70, h - m)], fill=hill1)
    p.polygon([(w * 0.40, h - m), (w * 0.66, h * 0.52), (w - m, h * 0.78), (w - m, h - m)], fill=hill2)
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle(inner, radius=int(w * 0.03), fill=255)
    img.paste(pic, (0, 0), mask)
    return img


def shadow(img, offset, blur_alpha=90):
    sh = Image.new("RGBA", img.size, (0, 0, 0, 0))
    alpha = img.split()[3].point(lambda a: blur_alpha if a else 0)
    sh.putalpha(alpha)
    return sh


base = Image.new("RGBA", (S, S), (0, 0, 0, 0))
bg = Image.new("RGBA", (S, S))
top, bottom = (44, 88, 160), (24, 44, 92)
for y in range(S):
    t = y / S
    ImageDraw.Draw(bg).line([(0, y), (S, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
mask = Image.new("L", (S, S), 0)
ImageDraw.Draw(mask).rounded_rectangle([40, 40, S - 40, S - 40], radius=200, fill=255)
base.paste(bg, (0, 0), mask)

back = card((520, 420), (235, 240, 248, 255), (150, 190, 230, 255), (90, 140, 110, 255), (60, 110, 85, 255), (250, 250, 250, 255))
back = back.rotate(12, expand=True, resample=Image.BICUBIC)
front = card((560, 450), (255, 255, 255, 255), (120, 185, 245, 255), (70, 165, 95, 255), (40, 125, 70, 255), (255, 214, 64, 255))
front = front.rotate(-6, expand=True, resample=Image.BICUBIC)

for layer, pos in ((back, (150, 170)), (front, (300, 360))):
    base.alpha_composite(shadow(layer, 0), (pos[0] + 14, pos[1] + 18))
    base.alpha_composite(layer, pos)

# gold placement badge
d = ImageDraw.Draw(base)
cx, cy, r = 745, 300, 150
d.ellipse([cx - r + 10, cy - r + 14, cx + r + 10, cy + r + 14], fill=(0, 0, 0, 70))
d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=(255, 196, 37, 255), outline=(214, 150, 10, 255), width=14)
try:
    font = ImageFont.truetype("DejaVuSans-Bold.ttf", 200)
except OSError:
    font = ImageFont.load_default()
d.text((cx, cy + 6), "1", font=font, fill=(150, 90, 0, 255), anchor="mm")

icon = base.resize((256, 256), Image.LANCZOS)
icon.save("iorg.png")
base.resize((512, 512), Image.LANCZOS).save("iorg-512.png")
icon.save("iorg.ico", sizes=[(16, 16), (24, 24), (32, 32), (48, 48), (64, 64), (128, 128), (256, 256)])
