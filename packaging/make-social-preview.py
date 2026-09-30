"""Renders docs/social-preview.png (1280x640) for the GitHub repository settings. Needs Pillow."""
from PIL import Image, ImageDraw, ImageFilter, ImageFont

W, H = 1280, 640
img = Image.new("RGB", (W, H))
d = ImageDraw.Draw(img)
top, bottom = (44, 88, 160), (20, 36, 78)
for y in range(H):
    t = y / H
    d.line([(0, y), (W, y)], fill=tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))


def font(size, bold=False):
    try:
        return ImageFont.truetype("DejaVuSans-Bold.ttf" if bold else "DejaVuSans.ttf", size)
    except OSError:
        return ImageFont.load_default()


icon = Image.open("packaging/iorg-512.png").resize((150, 150), Image.LANCZOS)
img.paste(icon, (70, 80), icon)
d.text((70, 260), "Image Organizr", font=font(54, True), fill="white")
d.text((72, 345), "Sort, rank and rate your photos -", font=font(26), fill=(210, 222, 245))
d.text((72, 380), "one quick decision at a time.", font=font(26), fill=(210, 222, 245))
d.text((72, 480), "Order  ·  Knockout  ·  Rate  ·  Categorize", font=font(20, True), fill=(255, 200, 60))
d.text((72, 525), "Windows & Linux  ·  open source (MIT)", font=font(20), fill=(170, 190, 225))

shot = Image.open("docs/screenshots/order.jpg").convert("RGB")
shot = shot.resize((560, int(shot.height * 560 / shot.width)), Image.LANCZOS)
x, y = 660, (H - shot.height) // 2
shadow = Image.new("RGBA", (shot.width + 60, shot.height + 60), (0, 0, 0, 0))
ImageDraw.Draw(shadow).rectangle([30, 30, shot.width + 30, shot.height + 30], fill=(0, 0, 0, 140))
shadow = shadow.filter(ImageFilter.GaussianBlur(14))
img.paste(shadow, (x - 22, y - 16), shadow)
img.paste(shot, (x, y))
img.save("docs/social-preview.png", optimize=True)
