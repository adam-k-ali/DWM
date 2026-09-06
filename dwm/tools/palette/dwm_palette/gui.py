"""CustomTkinter GUI for editing family palettes and previewing stone/ore recolour.

Offline tooling only — imported by generate_docs --gui.
"""

from __future__ import annotations

import json
import tkinter as tk
from pathlib import Path
from tkinter import colorchooser, filedialog, messagebox
from typing import Any, Literal

import customtkinter as ctk
from PIL import Image

from dwm_palette.recolor import (
    HEX_RE,
    apply_host_palette,
    apply_ore_palettes,
    host_colour_set,
    host_hexes,
    load_palette,
    load_rgb_image,
    split_ore_colours,
    vein_hexes,
)

PREVIEW_SCALE = 24  # 16×16 → 384×384
PreviewMode = Literal["Stone", "Ore"]


class FamilyPaletteGui(ctk.CTk):
    def __init__(
        self,
        palette_path: Path,
        template_path: Path,
        mineral_palette_path: Path,
        ore_template_path: Path,
    ) -> None:
        super().__init__()
        self.title("Family palette recolour")
        self.geometry("1000x560")
        self.minsize(860, 480)

        ctk.set_appearance_mode("System")
        ctk.set_default_color_theme("blue")

        self._palette_path = palette_path
        self._template_path = template_path
        self._mineral_palette_path = mineral_palette_path
        self._ore_template_path = ore_template_path

        self._template_rgb = load_rgb_image(template_path)
        self._host_colours = host_colour_set(self._template_rgb)
        self._ore_rgb = load_rgb_image(ore_template_path)
        self._palette: dict[str, Any] = load_palette(palette_path)
        self._mineral_palette: dict[str, Any] = load_palette(mineral_palette_path)

        self._mode: PreviewMode = "Stone"
        self._hex_vars: list[tk.StringVar] = []
        self._swatch_buttons: list[ctk.CTkButton] = []
        self._mineral_hex_vars: list[tk.StringVar] = []
        self._mineral_swatch_buttons: list[ctk.CTkButton] = []
        self._mineral_role_indices: list[int] = []
        self._preview_image: ctk.CTkImage | None = None
        self._last_preview_rgb = None

        self._build_layout()
        self._reload_palette_ui()
        self._reload_mineral_ui()
        self._refresh_preview()

    def _build_layout(self) -> None:
        self.grid_columnconfigure(0, weight=1, minsize=360)
        self.grid_columnconfigure(1, weight=1, minsize=420)
        self.grid_rowconfigure(0, weight=1)

        left = ctk.CTkFrame(self)
        left.grid(row=0, column=0, sticky="nsew", padx=(12, 6), pady=12)
        left.grid_columnconfigure(0, weight=1)
        left.grid_rowconfigure(2, weight=1)
        left.grid_rowconfigure(4, weight=1)

        self._title_label = ctk.CTkLabel(
            left, text="", font=ctk.CTkFont(size=16, weight="bold"), anchor="w"
        )
        self._title_label.grid(row=0, column=0, sticky="ew", padx=12, pady=(12, 2))

        self._meta_label = ctk.CTkLabel(
            left, text="", font=ctk.CTkFont(size=12), anchor="w", text_color="gray70"
        )
        self._meta_label.grid(row=1, column=0, sticky="ew", padx=12, pady=(0, 8))

        self._roles_frame = ctk.CTkScrollableFrame(left, label_text="Host roles")
        self._roles_frame.grid(row=2, column=0, sticky="nsew", padx=12, pady=4)
        self._roles_frame.grid_columnconfigure(2, weight=1)

        load_host_btn = ctk.CTkButton(
            left, text="Load host palette…", command=self._on_load_palette
        )
        load_host_btn.grid(row=3, column=0, sticky="ew", padx=12, pady=(8, 4))

        self._mineral_section = ctk.CTkFrame(left, fg_color="transparent")
        self._mineral_section.grid(row=4, column=0, sticky="nsew", padx=0, pady=0)
        self._mineral_section.grid_columnconfigure(0, weight=1)
        self._mineral_section.grid_rowconfigure(1, weight=1)

        self._mineral_title = ctk.CTkLabel(
            self._mineral_section,
            text="",
            font=ctk.CTkFont(size=14, weight="bold"),
            anchor="w",
        )
        self._mineral_title.grid(row=0, column=0, sticky="ew", padx=12, pady=(8, 2))

        self._mineral_meta = ctk.CTkLabel(
            self._mineral_section,
            text="",
            font=ctk.CTkFont(size=11),
            anchor="w",
            text_color="gray70",
        )
        self._mineral_meta.grid(row=1, column=0, sticky="ew", padx=12, pady=(0, 4))

        self._mineral_roles_frame = ctk.CTkScrollableFrame(
            self._mineral_section, label_text="Mineral vein roles"
        )
        self._mineral_roles_frame.grid(row=2, column=0, sticky="nsew", padx=12, pady=4)
        self._mineral_roles_frame.grid_columnconfigure(2, weight=1)
        self._mineral_section.grid_rowconfigure(2, weight=1)

        self._load_mineral_btn = ctk.CTkButton(
            self._mineral_section,
            text="Load mineral palette…",
            command=self._on_load_mineral_palette,
        )
        self._load_mineral_btn.grid(row=3, column=0, sticky="ew", padx=12, pady=(4, 4))

        self._load_ore_btn = ctk.CTkButton(
            self._mineral_section,
            text="Load ore template…",
            command=self._on_load_ore_template,
        )
        self._load_ore_btn.grid(row=4, column=0, sticky="ew", padx=12, pady=(0, 12))

        right = ctk.CTkFrame(self)
        right.grid(row=0, column=1, sticky="nsew", padx=(6, 12), pady=12)
        right.grid_columnconfigure(0, weight=1)
        right.grid_rowconfigure(2, weight=1)

        self._mode_var = ctk.StringVar(value="Stone")
        self._mode_seg = ctk.CTkSegmentedButton(
            right,
            values=["Stone", "Ore"],
            variable=self._mode_var,
            command=self._on_mode_changed,
        )
        self._mode_seg.grid(row=0, column=0, sticky="ew", padx=12, pady=(12, 8))

        self._preview_title = ctk.CTkLabel(
            right,
            text="Stone preview",
            font=ctk.CTkFont(size=16, weight="bold"),
            anchor="w",
        )
        self._preview_title.grid(row=1, column=0, sticky="ew", padx=12, pady=(0, 8))

        self._preview_label = ctk.CTkLabel(right, text="")
        self._preview_label.grid(row=2, column=0, sticky="", padx=12, pady=12)

        self._preview_meta = ctk.CTkLabel(
            right,
            text="",
            font=ctk.CTkFont(size=11),
            text_color="gray70",
            anchor="w",
        )
        self._preview_meta.grid(row=3, column=0, sticky="ew", padx=12, pady=(0, 4))

        save_btn = ctk.CTkButton(right, text="Save PNG…", command=self._on_save_png)
        save_btn.grid(row=4, column=0, sticky="ew", padx=12, pady=(4, 12))

        self._apply_mode_visibility()

    def _apply_mode_visibility(self) -> None:
        if self._mode == "Ore":
            self._mineral_section.grid()
            self._preview_title.configure(text="Ore preview")
        else:
            self._mineral_section.grid_remove()
            self._preview_title.configure(text="Stone preview")

    def _on_mode_changed(self, value: str) -> None:
        self._mode = "Ore" if value == "Ore" else "Stone"
        self._apply_mode_visibility()
        self._refresh_preview()

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

    def _reload_mineral_ui(self) -> None:
        for child in self._mineral_roles_frame.winfo_children():
            child.destroy()
        self._mineral_hex_vars.clear()
        self._mineral_swatch_buttons.clear()
        self._mineral_role_indices.clear()

        self._mineral_title.configure(text=self._mineral_palette["display_name"])
        self._mineral_meta.configure(
            text=(
                f"family_id: {self._mineral_palette['family_id']}  ·  "
                f"{self._mineral_palette_path.name}"
            )
        )

        header_font = ctk.CTkFont(size=11, weight="bold")
        ctk.CTkLabel(self._mineral_roles_frame, text="Role", font=header_font).grid(
            row=0, column=0, sticky="w", padx=(4, 8), pady=(0, 4)
        )
        ctk.CTkLabel(self._mineral_roles_frame, text="", font=header_font).grid(
            row=0, column=1, padx=4, pady=(0, 4)
        )
        ctk.CTkLabel(self._mineral_roles_frame, text="Hex", font=header_font).grid(
            row=0, column=2, sticky="w", padx=4, pady=(0, 4)
        )

        ui_row = 1
        for i, entry in enumerate(self._mineral_palette["roles"]):
            if not entry["role"].startswith("vein_"):
                continue
            self._mineral_role_indices.append(i)

            ctk.CTkLabel(
                self._mineral_roles_frame, text=entry["role"], anchor="w"
            ).grid(row=ui_row, column=0, sticky="w", padx=(4, 8), pady=4)

            swatch = ctk.CTkButton(
                self._mineral_roles_frame,
                text="",
                width=36,
                height=28,
                fg_color=entry["hex"],
                hover_color=entry["hex"],
                border_width=1,
                border_color="#555555",
                command=lambda idx=len(self._mineral_role_indices) - 1: self._on_pick_mineral_colour(
                    idx
                ),
            )
            swatch.grid(row=ui_row, column=1, padx=4, pady=4)
            self._mineral_swatch_buttons.append(swatch)

            var = tk.StringVar(value=entry["hex"])
            var.trace_add(
                "write",
                lambda *_args, idx=len(self._mineral_hex_vars): self._on_mineral_hex_changed(
                    idx
                ),
            )
            entry_box = ctk.CTkEntry(
                self._mineral_roles_frame,
                textvariable=var,
                width=100,
                font=ctk.CTkFont(family="Menlo", size=12),
            )
            entry_box.grid(row=ui_row, column=2, sticky="ew", padx=4, pady=4)
            self._mineral_hex_vars.append(var)
            ui_row += 1

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

    def _sync_mineral_from_vars(self) -> bool:
        """Apply valid vein hex fields. True when all vein_* hexes are valid."""
        roles = self._mineral_palette["roles"]
        for ui_i, var in enumerate(self._mineral_hex_vars):
            value = var.get().strip()
            if not HEX_RE.match(value):
                continue
            role_i = self._mineral_role_indices[ui_i]
            roles[role_i]["hex"] = value.upper()
            self._mineral_swatch_buttons[ui_i].configure(
                fg_color=roles[role_i]["hex"], hover_color=roles[role_i]["hex"]
            )
        try:
            veins = vein_hexes(self._mineral_palette)
        except ValueError:
            return False
        return all(HEX_RE.match(h) for h in veins)

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

    def _on_mineral_hex_changed(self, index: int) -> None:
        if index >= len(self._mineral_hex_vars):
            return
        value = self._mineral_hex_vars[index].get().strip()
        if not HEX_RE.match(value):
            return
        role_i = self._mineral_role_indices[index]
        self._mineral_palette["roles"][role_i]["hex"] = value.upper()
        self._mineral_swatch_buttons[index].configure(
            fg_color=self._mineral_palette["roles"][role_i]["hex"],
            hover_color=self._mineral_palette["roles"][role_i]["hex"],
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

    def _on_pick_mineral_colour(self, index: int) -> None:
        role_i = self._mineral_role_indices[index]
        current = self._mineral_palette["roles"][role_i]["hex"]
        picked = colorchooser.askcolor(
            color=current,
            title=f"Pick colour for {self._mineral_palette['roles'][role_i]['role']}",
        )
        if not picked or not picked[1]:
            return
        hex_value = picked[1].upper()
        self._mineral_hex_vars[index].set(hex_value)

    def _on_load_palette(self) -> None:
        path_str = filedialog.askopenfilename(
            title="Load host palette JSON",
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
            messagebox.showerror("Load host palette failed", str(exc))
            return
        self._palette_path = path
        self._palette = palette
        self._reload_palette_ui()
        self._refresh_preview()

    def _on_load_mineral_palette(self) -> None:
        path_str = filedialog.askopenfilename(
            title="Load mineral palette JSON",
            filetypes=[("Palette JSON", "*.json"), ("All files", "*.*")],
            initialdir=str(self._mineral_palette_path.parent),
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            palette = load_palette(path)
            vein_hexes(palette)
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            messagebox.showerror("Load mineral palette failed", str(exc))
            return
        self._mineral_palette_path = path
        self._mineral_palette = palette
        self._reload_mineral_ui()
        self._refresh_preview()

    def _on_load_ore_template(self) -> None:
        path_str = filedialog.askopenfilename(
            title="Load ore template PNG",
            filetypes=[("PNG images", "*.png"), ("All files", "*.*")],
            initialdir=str(self._ore_template_path.parent),
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            ore_rgb = load_rgb_image(path)
            # Validate split against frozen stone host colours.
            split_ore_colours(ore_rgb, self._host_colours)
        except (OSError, ValueError) as exc:
            messagebox.showerror("Load ore template failed", str(exc))
            return
        self._ore_template_path = path
        self._ore_rgb = ore_rgb
        self._refresh_preview()

    def _compute_preview_rgb(self):
        if not self._sync_palette_from_vars():
            return None
        if self._mode == "Stone":
            return apply_host_palette(self._template_rgb, host_hexes(self._palette))
        if not self._sync_mineral_from_vars():
            return None
        return apply_ore_palettes(
            self._ore_rgb,
            host_hexes(self._palette),
            vein_hexes(self._mineral_palette),
            self._host_colours,
        )

    def _refresh_preview(self) -> None:
        remapped = self._compute_preview_rgb()
        if remapped is None:
            return
        self._last_preview_rgb = remapped
        pil = Image.fromarray(remapped, mode="RGB")
        scaled = pil.resize(
            (pil.width * PREVIEW_SCALE, pil.height * PREVIEW_SCALE),
            resample=Image.Resampling.NEAREST,
        )
        self._preview_image = ctk.CTkImage(
            light_image=scaled, dark_image=scaled, size=scaled.size
        )
        self._preview_label.configure(image=self._preview_image)

        if self._mode == "Stone":
            self._preview_meta.configure(
                text=(
                    f"Template: {self._template_path.name}  ·  "
                    "host_* roles drive the preview"
                )
            )
        else:
            self._preview_meta.configure(
                text=(
                    f"Ore template: {self._ore_template_path.name}  ·  "
                    f"host: {self._palette_path.name}  ·  "
                    f"mineral: {self._mineral_palette_path.name}"
                )
            )

    def _on_save_png(self) -> None:
        remapped = self._compute_preview_rgb()
        if remapped is None:
            messagebox.showerror(
                "Save PNG failed",
                "Cannot save: host or mineral palette hexes are incomplete/invalid.",
            )
            return

        if self._mode == "Ore":
            suggested = f"{self._mineral_palette['family_id']}_ore.png"
            initial_dir = str(self._ore_template_path.parent)
        else:
            suggested = f"{self._palette['family_id']}.png"
            initial_dir = str(self._template_path.parent)

        path_str = filedialog.asksaveasfilename(
            title="Save PNG",
            defaultextension=".png",
            filetypes=[("PNG images", "*.png"), ("All files", "*.*")],
            initialdir=initial_dir,
            initialfile=suggested,
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            Image.fromarray(remapped, mode="RGB").save(path)
        except OSError as exc:
            messagebox.showerror("Save PNG failed", str(exc))
            return
        messagebox.showinfo("Saved", f"Wrote {path}")


def run_gui(
    palette_path: Path,
    template_path: Path,
    mineral_palette_path: Path,
    ore_template_path: Path,
) -> None:
    if not palette_path.is_file():
        raise FileNotFoundError(f"palette not found: {palette_path}")
    if not template_path.is_file():
        raise FileNotFoundError(f"template not found: {template_path}")
    if not mineral_palette_path.is_file():
        raise FileNotFoundError(f"mineral palette not found: {mineral_palette_path}")
    if not ore_template_path.is_file():
        raise FileNotFoundError(f"ore template not found: {ore_template_path}")

    # Validate early so ImportError/ValueError surface before the window.
    load_palette(palette_path)
    load_rgb_image(template_path)
    mineral = load_palette(mineral_palette_path)
    vein_hexes(mineral)
    load_rgb_image(ore_template_path)

    app = FamilyPaletteGui(
        palette_path=palette_path,
        template_path=template_path,
        mineral_palette_path=mineral_palette_path,
        ore_template_path=ore_template_path,
    )
    app.mainloop()
