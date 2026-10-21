# Vita3kAI icon

`artwork_mocha.png` is the source illustration recoloured into Catppuccin Mocha.

Regenerate from an original illustration:

```sh
python3 recolor.py original.jpg artwork_mocha.png 9 2.0   # palette blend width, chroma boost
python3 make_icons.py artwork_mocha.png ../../android/app/src/main/res preview.png
```

`make_icons.py` writes the rounded in-app art (`drawable-nodpi/vita3kai_art.png`) and the
adaptive launcher foreground at every density; `preview.png` shows it under a round mask.
