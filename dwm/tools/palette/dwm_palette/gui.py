"""CustomTkinter GUI for editing family palettes and previewing stone recolour.

Offline tooling only — imported by generate_docs --gui.
"""

from __future__ import annotations

import json
import tkinter as tk
from pathlib import Path
from tkinter import colorchooser, filedialog, messagebox
from typing import Any

import customtkinter as ctk
from PIL import Image

from dwm_palette.recolor import (
    HEX_RE,
    apply_host_palette,
    host_hexes,
    load_palette,
    load_rgb_image,
)

PREVIEW_SCALE = 24  # 16×16 → 384×384


class FamilyPaletteGui(ctk.CTk):
    def __init__(self, palette_path: Path, template_path: Path) -> None:
        super().__init__()
        self.title("Family palette recolour")
        self.geometry("920x520")
        self.minsize(780, 440)

        ctk.set_appearance_mode("System")
        ctk.set_default_color_theme("blue")

        self._palette_path = palette_path
        self._template_path = template_path
        self._template_rgb = load_rgb_image(template_path)
        self._palette: dict[str, Any] = load_palette(palette_path)
        self._hex_vars: list[tk.StringVar] = []
        self._swatch_buttons: list[ctk.CTkButton] = []
        self._preview_image: ctk.CTkImage | None = None

        self._build_layout()
        self._reload_palette_ui()
        self._refresh_preview()

    def _build_layout(self) -> None:
        self.grid_columnconfigure(0, weight=1, minsize=340)
        self.grid_columnconfigure(1, weight=1, minsize=400)
        self.grid_rowconfigure(0, weight=1)

        left = ctk.CTkFrame(self)
        left.grid(row=0, column=0, sticky="nsew", padx=(12, 6), pady=12)
        left.grid_columnconfigure(0, weight=1)
        left.grid_rowconfigure(2, weight=1)

        self._title_label = ctk.CTkLabel(
            left, text="", font=ctk.CTkFont(size=16, weight="bold"), anchor="w"
        )
        self._title_label.grid(row=0, column=0, sticky="ew", padx=12, pady=(12, 2))

        self._meta_label = ctk.CTkLabel(
            left, text="", font=ctk.CTkFont(size=12), anchor="w", text_color="gray70"
        )
        self._meta_label.grid(row=1, column=0, sticky="ew", padx=12, pady=(0, 8))

        self._roles_frame = ctk.CTkScrollableFrame(left, label_text="Roles")
        self._roles_frame.grid(row=2, column=0, sticky="nsew", padx=12, pady=4)
        self._roles_frame.grid_columnconfigure(2, weight=1)

        load_btn = ctk.CTkButton(left, text="Load palette…", command=self._on_load_palette)
        load_btn.grid(row=3, column=0, sticky="ew", padx=12, pady=(8, 12))

        right = ctk.CTkFrame(self)
        right.grid(row=0, column=1, sticky="nsew", padx=(6, 12), pady=12)
        right.grid_columnconfigure(0, weight=1)
        right.grid_rowconfigure(1, weight=1)

        ctk.CTkLabel(
            right,
            text="Stone preview",
            font=ctk.CTkFont(size=16, weight="bold"),
            anchor="w",
        ).grid(row=0, column=0, sticky="ew", padx=12, pady=(12, 8))

        self._preview_label = ctk.CTkLabel(right, text="")
        self._preview_label.grid(row=1, column=0, sticky="", padx=12, pady=12)

        self._preview_meta = ctk.CTkLabel(
            right,
            text="",
            font=ctk.CTkFont(size=11),
            text_color="gray70",
            anchor="w",
        )
        self._preview_meta.grid(row=2, column=0, sticky="ew", padx=12, pady=(0, 12))

    def _reload_palette_ui(self) -> None:
        for child in self._roles_frame.winfo_children():
            child.destroy()
        self._hex_vars.clear()
        self._swatch_buttons.clear()

        self._title_label.configure(text=self._palette["display_name"])
        self._meta_label.configure(
            text=f"family_id: {self._palette['family_id']}  ·  {self._palette_path.name}"
        )

        header_font = ctk.CTkFont(size=11, weight="bold")
        ctk.CTkLabel(self._roles_frame, text="Role", font=header_font).grid(
            row=0, column=0, sticky="w", padx=(4, 8), pady=(0, 4)
        )
        ctk.CTkLabel(self._roles_frame, text="", font=header_font).grid(
            row=0, column=1, padx=4, pady=(0, 4)
        )
        ctk.CTkLabel(self._roles_frame, text="Hex", font=header_font).grid(
            row=0, column=2, sticky="w", padx=4, pady=(0, 4)
        )

        for i, entry in enumerate(self._palette["roles"]):
            row = i + 1
            ctk.CTkLabel(self._roles_frame, text=entry["role"], anchor="w").grid(
                row=row, column=0, sticky="w", padx=(4, 8), pady=4
            )

            swatch = ctk.CTkButton(
                self._roles_frame,
                text="",
                width=36,
                height=28,
                fg_color=entry["hex"],
                hover_color=entry["hex"],
                border_width=1,
                border_color="#555555",
                command=lambda idx=i: self._on_pick_colour(idx),
            )
            swatch.grid(row=row, column=1, padx=4, pady=4)
            self._swatch_buttons.append(swatch)

            var = tk.StringVar(value=entry["hex"])
            var.trace_add("write", lambda *_args, idx=i: self._on_hex_changed(idx))
            entry_box = ctk.CTkEntry(
                self._roles_frame,
                textvariable=var,
                width=100,
                font=ctk.CTkFont(family="Menlo", size=12),
            )
            entry_box.grid(row=row, column=2, sticky="ew", padx=4, pady=4)
            self._hex_vars.append(var)

        self._preview_meta.configure(
            text=f"Template: {self._template_path.name}  ·  host_* roles drive the preview"
        )

    def _sync_palette_from_vars(self) -> bool:
        """Apply valid hex fields into self._palette. True when all host_* hexes are valid."""
        roles = self._palette["roles"]
        for i, var in enumerate(self._hex_vars):
            value = var.get().strip()
            if not HEX_RE.match(value):
                continue
            roles[i]["hex"] = value.upper()
            self._swatch_buttons[i].configure(
                fg_color=roles[i]["hex"], hover_color=roles[i]["hex"]
            )
        try:
            hosts = host_hexes(self._palette)
        except ValueError:
            return False
        return all(HEX_RE.match(h) for h in hosts)

    def _on_hex_changed(self, index: int) -> None:
        if index >= len(self._hex_vars):
            return
        value = self._hex_vars[index].get().strip()
        if not HEX_RE.match(value):
            return
        self._palette["roles"][index]["hex"] = value.upper()
        self._swatch_buttons[index].configure(
            fg_color=self._palette["roles"][index]["hex"],
            hover_color=self._palette["roles"][index]["hex"],
        )
        self._refresh_preview()

    def _on_pick_colour(self, index: int) -> None:
        current = self._palette["roles"][index]["hex"]
        picked = colorchooser.askcolor(
            color=current, title=f"Pick colour for {self._palette['roles'][index]['role']}"
        )
        if not picked or not picked[1]:
            return
        hex_value = picked[1].upper()
        self._hex_vars[index].set(hex_value)

    def _on_load_palette(self) -> None:
        path_str = filedialog.askopenfilename(
            title="Load palette JSON",
            filetypes=[("Palette JSON", "*.json"), ("All files", "*.*")],
            initialdir=str(self._palette_path.parent),
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            palette = load_palette(path)
            host_hexes(palette)
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            messagebox.showerror("Load palette failed", str(exc))
            return
        self._palette_path = path
        self._palette = palette
        self._reload_palette_ui()
        self._refresh_preview()

    def _refresh_preview(self) -> None:
        if not self._sync_palette_from_vars():
            return
        remapped = apply_host_palette(self._template_rgb, host_hexes(self._palette))
        pil = Image.fromarray(remapped, mode="RGB")
        scaled = pil.resize(
            (pil.width * PREVIEW_SCALE, pil.height * PREVIEW_SCALE),
            resample=Image.Resampling.NEAREST,
        )
        self._preview_image = ctk.CTkImage(
            light_image=scaled, dark_image=scaled, size=scaled.size
        )
        self._preview_label.configure(image=self._preview_image)


def run_gui(palette_path: Path, template_path: Path) -> None:
    if not palette_path.is_file():
        raise FileNotFoundError(f"palette not found: {palette_path}")
    if not template_path.is_file():
        raise FileNotFoundError(f"template not found: {template_path}")

    # Validate early so ImportError/ValueError surface before the window.
    load_palette(palette_path)
    load_rgb_image(template_path)

    app = FamilyPaletteGui(palette_path=palette_path, template_path=template_path)
    app.mainloop()
