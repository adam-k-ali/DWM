"""3x5 pixel font for the POLICE BOX sign lettering (only the glyphs the boxes need)."""
from __future__ import annotations

GLYPHS: dict[str, list[str]] = {
    "P": ["###", "#.#", "###", "#..", "#.."],
    "O": ["###", "#.#", "#.#", "#.#", "###"],
    "L": ["#..", "#..", "#..", "#..", "###"],
    "I": ["#", "#", "#", "#", "#"],
    "C": ["###", "#..", "#..", "#..", "###"],
    "E": ["###", "#..", "##.", "#..", "###"],
    "B": ["##.", "#.#", "##.", "#.#", "##."],
    "X": ["#.#", "#.#", ".#.", "#.#", "#.#"],
}


def text_width(text: str) -> int:
    return sum(len(GLYPHS[c][0]) for c in text) + max(0, len(text) - 1)


def text_pixels(text: str) -> list[tuple[int, int]]:
    """(x, y) offsets of the lit pixels for `text`, one column of spacing between glyphs."""
    out = []
    x = 0
    for c in text:
        rows = GLYPHS[c]
        for y, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch == "#":
                    out.append((x + dx, y))
        x += len(rows[0]) + 1
    return out
