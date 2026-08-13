from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
IMAGES = ROOT / "assets" / "images"

for source in sorted(IMAGES.glob("*.png")):
    target = source.with_suffix(".webp")
    with Image.open(source) as image:
        if image.mode not in {"RGB", "RGBA"}:
            image = image.convert("RGBA" if "A" in image.getbands() else "RGB")
        image.save(
            target,
            format="WEBP",
            quality=88,
            method=6,
            lossless=False,
        )
    print(f"{source.name} -> {target.name}")
