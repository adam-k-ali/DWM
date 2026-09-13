"""CustomTkinter GUI for editing palette seeds and previewing recolour modes.

Offline tooling only — imported by generate_docs --gui.

Modes: Stone, Ore, Gem, Crystal, Ingot, Pickaxe, Sword.
"""

from __future__ import annotations

import json
import tkinter as tk
from pathlib import Path
from tkinter import colorchooser, filedialog, messagebox
from typing import Any, Literal

import customtkinter as ctk
from PIL import Image

from dwm_palette.palette import palette_hexes, save_palette_json
from dwm_palette.profiles import expand_ramp
from dwm_palette.recolor import (
    HEX_RE,
    Rgb,
    apply_host_palette,
    apply_mineral_item_palette,
    apply_ore_palettes,
    apply_tool_item_palette,
    load_palette,
    load_rgb_image,
)

PREVIEW_SCALE = 24  # 16×16 → 384×384
PreviewMode = Literal[
    "Stone", "Ore", "Gem", "Crystal", "Ingot", "Pickaxe", "Sword"
]
MINERAL_ONLY_MODES = frozenset({"Gem", "Crystal", "Ingot"})
TOOL_MODES = frozenset({"Pickaxe", "Sword"})
ALL_MODES = ("Stone", "Ore", "Gem", "Crystal", "Ingot", "Pickaxe", "Sword")


class FamilyPaletteGui(ctk.CTk):
    def __init__(
        self,
        host_palette_path: Path,
        stone_template_path: Path,
        mineral_palette_path: Path,
        ore_rgb: Any,
        ore_host_colours: frozenset[Rgb],
        ore_template_label: str,
        ore_save_dir: Path,
        gem_rgba: Any,
        gem_template_label: str,
        crystal_rgba: Any,
        crystal_template_label: str,
        ingot_rgba: Any,
        ingot_template_label: str,
        pickaxe_rgba: Any,
        pickaxe_template_label: str,
        sword_rgba: Any,
        sword_template_label: str,
        tool_handle_colours: frozenset[Rgb],
        item_save_dir: Path,
        handle_palette_path: Path | None = None,
    ) -> None:
        super().__init__()
        self.title("Family palette recolour")
        self.geometry("1100x620")
        self.minsize(920, 540)

        ctk.set_appearance_mode("System")
        ctk.set_default_color_theme("blue")

        self._host_path = host_palette_path
        self._stone_host_path = host_palette_path
        self._handle_path = handle_palette_path
        self._mineral_path = mineral_palette_path
        self._stone_template_path = stone_template_path
        self._ore_template_label = ore_template_label
        self._ore_save_dir = ore_save_dir
        self._gem_template_label = gem_template_label
        self._crystal_template_label = crystal_template_label
        self._ingot_template_label = ingot_template_label
        self._pickaxe_template_label = pickaxe_template_label
        self._sword_template_label = sword_template_label
        self._item_save_dir = item_save_dir

        self._stone_rgb = load_rgb_image(stone_template_path)
        self._ore_rgb = ore_rgb
        self._ore_host_colours = ore_host_colours
        self._gem_rgba = gem_rgba
        self._crystal_rgba = crystal_rgba
        self._ingot_rgba = ingot_rgba
        self._pickaxe_rgba = pickaxe_rgba
        self._sword_rgba = sword_rgba
        self._tool_handle_colours = tool_handle_colours
        self._host: dict[str, Any] = load_palette(host_palette_path)
        self._mineral: dict[str, Any] = load_palette(mineral_palette_path)

        self._mode: PreviewMode = "Stone"
        self._host_seed_var = tk.StringVar(value=self._host["seed"])
        self._mineral_seed_var = tk.StringVar(value=self._mineral["seed"])
        self._host_step_labels: list[ctk.CTkLabel] = []
        self._mineral_step_labels: list[ctk.CTkLabel] = []
        self._preview_image: ctk.CTkImage | None = None
        self._last_preview_rgb = None

        self._build_layout()
        self._reload_host_ui()
        self._reload_mineral_ui()
        self._refresh_preview()

    def _build_layout(self) -> None:
        self.grid_columnconfigure(0, weight=1, minsize=360)
        self.grid_columnconfigure(1, weight=1, minsize=480)
        self.grid_rowconfigure(0, weight=1)

        left = ctk.CTkFrame(self)
        left.grid(row=0, column=0, sticky="nsew", padx=(12, 6), pady=12)
        left.grid_columnconfigure(0, weight=1)
        left.grid_rowconfigure(2, weight=1)
        left.grid_rowconfigure(4, weight=1)

        self._host_section = ctk.CTkFrame(left, fg_color="transparent")
        self._host_section.grid(row=0, column=0, rowspan=4, sticky="nsew", padx=0, pady=0)
        self._host_section.grid_columnconfigure(0, weight=1)
        self._host_section.grid_rowconfigure(2, weight=1)

        self._host_title = ctk.CTkLabel(
            self._host_section, text="", font=ctk.CTkFont(size=16, weight="bold"), anchor="w"
        )
        self._host_title.grid(row=0, column=0, sticky="ew", padx=12, pady=(12, 2))

        self._host_meta = ctk.CTkLabel(
            self._host_section, text="", font=ctk.CTkFont(size=12), anchor="w", text_color="gray70"
        )
        self._host_meta.grid(row=1, column=0, sticky="ew", padx=12, pady=(0, 8))

        self._host_frame = ctk.CTkScrollableFrame(
            self._host_section, label_text="Host palette (mid + derived)"
        )
        self._host_frame.grid(row=2, column=0, sticky="nsew", padx=12, pady=4)
        self._host_frame.grid_columnconfigure(1, weight=1)

        host_btns = ctk.CTkFrame(self._host_section, fg_color="transparent")
        host_btns.grid(row=3, column=0, sticky="ew", padx=12, pady=(8, 4))
        host_btns.grid_columnconfigure(0, weight=1)
        host_btns.grid_columnconfigure(1, weight=1)
        self._load_host_btn = ctk.CTkButton(
            host_btns, text="Load host…", command=self._on_load_host
        )
        self._load_host_btn.grid(row=0, column=0, sticky="ew", padx=(0, 4))
        self._save_host_btn = ctk.CTkButton(
            host_btns, text="Save host JSON…", command=self._on_save_host
        )
        self._save_host_btn.grid(row=0, column=1, sticky="ew", padx=(4, 0))

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
        self._mineral_meta.grid(row=0, column=0, sticky="ew", padx=12, pady=(28, 0))

        self._mineral_frame = ctk.CTkScrollableFrame(
            self._mineral_section, label_text="Mineral palette (mid + derived)"
        )
        self._mineral_frame.grid(row=1, column=0, sticky="nsew", padx=12, pady=4)
        self._mineral_frame.grid_columnconfigure(1, weight=1)

        mineral_btns = ctk.CTkFrame(self._mineral_section, fg_color="transparent")
        mineral_btns.grid(row=2, column=0, sticky="ew", padx=12, pady=(4, 12))
        mineral_btns.grid_columnconfigure(0, weight=1)
        mineral_btns.grid_columnconfigure(1, weight=1)
        self._load_mineral_btn = ctk.CTkButton(
            mineral_btns, text="Load mineral…", command=self._on_load_mineral
        )
        self._load_mineral_btn.grid(row=0, column=0, sticky="ew", padx=(0, 4))
        self._save_mineral_btn = ctk.CTkButton(
            mineral_btns, text="Save mineral JSON…", command=self._on_save_mineral
        )
        self._save_mineral_btn.grid(row=0, column=1, sticky="ew", padx=(4, 0))

        right = ctk.CTkFrame(self)
        right.grid(row=0, column=1, sticky="nsew", padx=(6, 12), pady=12)
        right.grid_columnconfigure(0, weight=1)
        right.grid_rowconfigure(2, weight=1)

        self._mode_var = ctk.StringVar(value="Stone")
        self._mode_seg = ctk.CTkSegmentedButton(
            right,
            values=list(ALL_MODES),
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
        if self._mode == "Stone":
            self._host_section.grid()
            self._mineral_section.grid_remove()
            self._preview_title.configure(text="Stone preview")
            self._set_host_slot_labels(handle=False)
            self._set_mineral_slot_labels(metal=False)
        elif self._mode == "Ore":
            self._host_section.grid()
            self._mineral_section.grid()
            self._preview_title.configure(text="Ore preview")
            self._set_host_slot_labels(handle=False)
            self._set_mineral_slot_labels(metal=False)
        elif self._mode in TOOL_MODES:
            self._host_section.grid()
            self._mineral_section.grid()
            self._preview_title.configure(text=f"{self._mode} preview")
            self._set_host_slot_labels(handle=True)
            self._set_mineral_slot_labels(metal=True)
        else:
            # Gem / Crystal / Ingot — mineral only
            self._host_section.grid_remove()
            self._mineral_section.grid()
            self._preview_title.configure(text=f"{self._mode} preview")
            self._set_mineral_slot_labels(metal=False)

    def _set_host_slot_labels(self, *, handle: bool) -> None:
        if handle:
            self._host_frame.configure(label_text="Handle palette (mid + derived)")
            self._load_host_btn.configure(text="Load handle…")
            self._save_host_btn.configure(text="Save handle JSON…")
        else:
            self._host_frame.configure(label_text="Host palette (mid + derived)")
            self._load_host_btn.configure(text="Load host…")
            self._save_host_btn.configure(text="Save host JSON…")

    def _set_mineral_slot_labels(self, *, metal: bool) -> None:
        if metal:
            self._mineral_frame.configure(label_text="Metal palette (mid + derived)")
            self._load_mineral_btn.configure(text="Load metal…")
            self._save_mineral_btn.configure(text="Save metal JSON…")
        else:
            self._mineral_frame.configure(label_text="Mineral palette (mid + derived)")
            self._load_mineral_btn.configure(text="Load mineral…")
            self._save_mineral_btn.configure(text="Save mineral JSON…")

    def _on_mode_changed(self, value: str) -> None:
        prev = self._mode
        if value in ALL_MODES:
            self._mode = value  # type: ignore[assignment]
        else:
            self._mode = "Stone"
        self._sync_slots_for_mode(prev)
        self._apply_mode_visibility()
        self._refresh_preview()

    def _sync_slots_for_mode(self, prev: PreviewMode) -> None:
        """Swap host↔handle (and remember paths) when entering/leaving tool modes."""
        entering_tool = self._mode in TOOL_MODES and prev not in TOOL_MODES
        leaving_tool = prev in TOOL_MODES and self._mode not in TOOL_MODES
        if entering_tool:
            # Remember stone/ore host; load handle palette if available.
            self._stone_host_path = self._host_path
            if self._handle_path is not None and self._handle_path.is_file():
                try:
                    palette = load_palette(self._handle_path)
                    palette_hexes(palette)
                except (OSError, ValueError, json.JSONDecodeError):
                    pass
                else:
                    self._host_path = self._handle_path
                    self._host = palette
                    self._reload_host_ui()
        elif leaving_tool:
            # Remember handle path from the slot; restore stone/ore host.
            self._handle_path = self._host_path
            if self._stone_host_path.is_file() and self._stone_host_path != self._host_path:
                try:
                    palette = load_palette(self._stone_host_path)
                    palette_hexes(palette)
                except (OSError, ValueError, json.JSONDecodeError):
                    pass
                else:
                    self._host_path = self._stone_host_path
                    self._host = palette
                    self._reload_host_ui()

    def _reexpand(self, palette: dict[str, Any], seed: str) -> bool:
        if not HEX_RE.match(seed):
            return False
        seed_u = seed.upper()
        palette["seed"] = seed_u
        palette["roles"] = expand_ramp(seed_u, palette["profile"])
        return True

    def _fill_palette_editor(
        self,
        frame: ctk.CTkScrollableFrame,
        palette: dict[str, Any],
        seed_var: tk.StringVar,
        on_seed_changed,
        on_pick_mid,
        step_label_list: list[ctk.CTkLabel],
    ) -> None:
        for child in frame.winfo_children():
            child.destroy()
        step_label_list.clear()

        ctk.CTkLabel(frame, text="mid", anchor="w").grid(
            row=0, column=0, sticky="w", padx=(4, 8), pady=4
        )
        mid_swatch = ctk.CTkButton(
            frame,
            text="",
            width=36,
            height=28,
            fg_color=palette["seed"],
            hover_color=palette["seed"],
            border_width=1,
            border_color="#555555",
            command=on_pick_mid,
        )
        mid_swatch.grid(row=0, column=1, padx=4, pady=4)
        seed_var.set(palette["seed"])
        seed_var.trace_add("write", lambda *_a: on_seed_changed())
        entry = ctk.CTkEntry(
            frame,
            textvariable=seed_var,
            width=100,
            font=ctk.CTkFont(family="Menlo", size=12),
        )
        entry.grid(row=0, column=2, sticky="ew", padx=4, pady=4)

        ctk.CTkLabel(
            frame,
            text=f"profile: {palette['profile']} (derived)",
            font=ctk.CTkFont(size=11),
            text_color="gray70",
            anchor="w",
        ).grid(row=1, column=0, columnspan=3, sticky="ew", padx=4, pady=(8, 4))

        for i, role in enumerate(palette["roles"]):
            row = i + 2
            ctk.CTkLabel(frame, text=role["role"], anchor="w").grid(
                row=row, column=0, sticky="w", padx=(4, 8), pady=2
            )
            swatch = ctk.CTkLabel(
                frame,
                text="",
                width=36,
                height=22,
                fg_color=role["hex"],
                corner_radius=4,
            )
            swatch.grid(row=row, column=1, padx=4, pady=2)
            hex_label = ctk.CTkLabel(
                frame,
                text=role["hex"],
                font=ctk.CTkFont(family="Menlo", size=11),
                anchor="w",
            )
            hex_label.grid(row=row, column=2, sticky="w", padx=4, pady=2)
            step_label_list.append(swatch)
            step_label_list.append(hex_label)

        # Stash mid swatch on the frame for refresh.
        frame._mid_swatch = mid_swatch  # type: ignore[attr-defined]

    def _refresh_derived_swatches(
        self,
        frame: ctk.CTkScrollableFrame,
        palette: dict[str, Any],
        step_label_list: list[ctk.CTkLabel],
    ) -> None:
        mid_swatch = getattr(frame, "_mid_swatch", None)
        if mid_swatch is not None:
            mid_swatch.configure(fg_color=palette["seed"], hover_color=palette["seed"])
        # Labels alternate swatch, hex for each role.
        for i, role in enumerate(palette["roles"]):
            swatch_i = i * 2
            hex_i = i * 2 + 1
            if swatch_i < len(step_label_list):
                step_label_list[swatch_i].configure(fg_color=role["hex"])
            if hex_i < len(step_label_list):
                step_label_list[hex_i].configure(text=role["hex"])

    def _reload_host_ui(self) -> None:
        self._host_title.configure(text=self._host["display_name"])
        self._host_meta.configure(
            text=(
                f"family_id: {self._host['family_id']}  ·  "
                f"{self._host_path.name}  ·  profile: {self._host['profile']}"
            )
        )
        # Rebuild StringVar to avoid stacking traces.
        self._host_seed_var = tk.StringVar(value=self._host["seed"])
        self._fill_palette_editor(
            self._host_frame,
            self._host,
            self._host_seed_var,
            self._on_host_seed_changed,
            self._on_pick_host_mid,
            self._host_step_labels,
        )

    def _reload_mineral_ui(self) -> None:
        self._mineral_title.configure(text=self._mineral["display_name"])
        self._mineral_meta.configure(
            text=(
                f"family_id: {self._mineral['family_id']}  ·  "
                f"{self._mineral_path.name}  ·  profile: {self._mineral['profile']}"
            )
        )
        self._mineral_seed_var = tk.StringVar(value=self._mineral["seed"])
        self._fill_palette_editor(
            self._mineral_frame,
            self._mineral,
            self._mineral_seed_var,
            self._on_mineral_seed_changed,
            self._on_pick_mineral_mid,
            self._mineral_step_labels,
        )

    def _on_host_seed_changed(self) -> None:
        value = self._host_seed_var.get().strip()
        if self._reexpand(self._host, value):
            self._refresh_derived_swatches(
                self._host_frame, self._host, self._host_step_labels
            )
            self._refresh_preview()

    def _on_mineral_seed_changed(self) -> None:
        value = self._mineral_seed_var.get().strip()
        if self._reexpand(self._mineral, value):
            self._refresh_derived_swatches(
                self._mineral_frame, self._mineral, self._mineral_step_labels
            )
            self._refresh_preview()

    def _on_pick_host_mid(self) -> None:
        picked = colorchooser.askcolor(
            color=self._host["seed"], title="Pick host/handle mid colour"
        )
        if not picked or not picked[1]:
            return
        self._host_seed_var.set(picked[1].upper())

    def _on_pick_mineral_mid(self) -> None:
        picked = colorchooser.askcolor(
            color=self._mineral["seed"], title="Pick mineral/metal mid colour"
        )
        if not picked or not picked[1]:
            return
        self._mineral_seed_var.set(picked[1].upper())

    def _on_load_host(self) -> None:
        slot = "handle" if self._mode in TOOL_MODES else "host"
        path_str = filedialog.askopenfilename(
            title=f"Load {slot} palette JSON",
            filetypes=[("Palette JSON", "*.json"), ("All files", "*.*")],
            initialdir=str(self._host_path.parent),
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            palette = load_palette(path)
            palette_hexes(palette)
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            messagebox.showerror(f"Load {slot} palette failed", str(exc))
            return
        self._host_path = path
        self._host = palette
        self._reload_host_ui()
        self._refresh_preview()

    def _on_load_mineral(self) -> None:
        slot = "metal" if self._mode in TOOL_MODES else "mineral"
        path_str = filedialog.askopenfilename(
            title=f"Load {slot} palette JSON",
            filetypes=[("Palette JSON", "*.json"), ("All files", "*.*")],
            initialdir=str(self._mineral_path.parent),
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            palette = load_palette(path)
            palette_hexes(palette)
        except (OSError, ValueError, json.JSONDecodeError) as exc:
            messagebox.showerror(f"Load {slot} palette failed", str(exc))
            return
        self._mineral_path = path
        self._mineral = palette
        self._reload_mineral_ui()
        self._refresh_preview()

    def _on_save_host(self) -> None:
        slot = "handle" if self._mode in TOOL_MODES else "host"
        if not self._reexpand(self._host, self._host_seed_var.get().strip()):
            messagebox.showerror("Save failed", f"{slot.capitalize()} mid hex is invalid.")
            return
        path_str = filedialog.asksaveasfilename(
            title=f"Save {slot} palette JSON",
            defaultextension=".json",
            filetypes=[("Palette JSON", "*.json"), ("All files", "*.*")],
            initialdir=str(self._host_path.parent),
            initialfile=self._host_path.name,
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            save_palette_json(self._host, path)
        except OSError as exc:
            messagebox.showerror("Save failed", str(exc))
            return
        self._host_path = path
        messagebox.showinfo("Saved", f"Wrote {path}")

    def _on_save_mineral(self) -> None:
        slot = "metal" if self._mode in TOOL_MODES else "mineral"
        if not self._reexpand(self._mineral, self._mineral_seed_var.get().strip()):
            messagebox.showerror("Save failed", f"{slot.capitalize()} mid hex is invalid.")
            return
        path_str = filedialog.asksaveasfilename(
            title=f"Save {slot} palette JSON",
            defaultextension=".json",
            filetypes=[("Palette JSON", "*.json"), ("All files", "*.*")],
            initialdir=str(self._mineral_path.parent),
            initialfile=self._mineral_path.name,
        )
        if not path_str:
            return
        path = Path(path_str)
        try:
            save_palette_json(self._mineral, path)
        except OSError as exc:
            messagebox.showerror("Save failed", str(exc))
            return
        self._mineral_path = path
        messagebox.showinfo("Saved", f"Wrote {path}")

    def _item_template_for_mode(self) -> tuple[Any, str]:
        if self._mode == "Gem":
            return self._gem_rgba, self._gem_template_label
        if self._mode == "Crystal":
            return self._crystal_rgba, self._crystal_template_label
        if self._mode == "Ingot":
            return self._ingot_rgba, self._ingot_template_label
        if self._mode == "Pickaxe":
            return self._pickaxe_rgba, self._pickaxe_template_label
        if self._mode == "Sword":
            return self._sword_rgba, self._sword_template_label
        raise ValueError(f"not an item mode: {self._mode}")

    def _compute_preview_rgb(self):
        if self._mode == "Stone":
            if not self._reexpand(self._host, self._host_seed_var.get().strip()):
                return None
            return apply_host_palette(self._stone_rgb, palette_hexes(self._host))

        if not self._reexpand(self._mineral, self._mineral_seed_var.get().strip()):
            return None
        mineral_hexes = palette_hexes(self._mineral)

        if self._mode == "Ore":
            if not self._reexpand(self._host, self._host_seed_var.get().strip()):
                return None
            return apply_ore_palettes(
                self._ore_rgb,
                palette_hexes(self._host),
                mineral_hexes,
                self._ore_host_colours,
            )

        if self._mode in TOOL_MODES:
            if not self._reexpand(self._host, self._host_seed_var.get().strip()):
                return None
            template, _label = self._item_template_for_mode()
            return apply_tool_item_palette(
                template,
                palette_hexes(self._host),
                mineral_hexes,
                self._tool_handle_colours,
            )

        template, _label = self._item_template_for_mode()
        return apply_mineral_item_palette(template, mineral_hexes)

    def _refresh_preview(self) -> None:
        remapped = self._compute_preview_rgb()
        if remapped is None:
            return
        self._last_preview_rgb = remapped
        mode = "RGBA" if remapped.shape[2] == 4 else "RGB"
        pil = Image.fromarray(remapped, mode=mode)
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
                    f"Template: {self._stone_template_path.name}  ·  "
                    f"host: {self._host_path.name}"
                )
            )
        elif self._mode == "Ore":
            self._preview_meta.configure(
                text=(
                    f"Ore template: {self._ore_template_label}  ·  "
                    f"host: {self._host_path.name}  ·  "
                    f"mineral: {self._mineral_path.name}"
                )
            )
        elif self._mode in TOOL_MODES:
            _template, label = self._item_template_for_mode()
            self._preview_meta.configure(
                text=(
                    f"{self._mode} template: {label}  ·  "
                    f"handle: {self._host_path.name}  ·  "
                    f"metal: {self._mineral_path.name}"
                )
            )
        else:
            _template, label = self._item_template_for_mode()
            self._preview_meta.configure(
                text=(
                    f"{self._mode} template: {label}  ·  "
                    f"mineral: {self._mineral_path.name}"
                )
            )

    def _on_save_png(self) -> None:
        remapped = self._compute_preview_rgb()
        if remapped is None:
            messagebox.showerror(
                "Save PNG failed",
                "Cannot save: host/handle or mineral/metal mid hex is incomplete/invalid.",
            )
            return

        if self._mode == "Ore":
            suggested = f"{self._mineral['family_id']}_ore.png"
            initial_dir = str(self._ore_save_dir)
        elif self._mode in MINERAL_ONLY_MODES | TOOL_MODES:
            if self._mode == "Crystal":
                suggested = f"{self._mineral['family_id']}_crystals.png"
            elif self._mode == "Ingot":
                suggested = f"{self._mineral['family_id']}_ingot.png"
            elif self._mode == "Pickaxe":
                suggested = f"{self._mineral['family_id']}_pickaxe.png"
            elif self._mode == "Sword":
                suggested = f"{self._mineral['family_id']}_sword.png"
            else:
                suggested = f"{self._mineral['family_id']}.png"
            initial_dir = str(self._item_save_dir)
        else:
            suggested = f"{self._host['family_id']}.png"
            initial_dir = str(self._stone_template_path.parent)

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
            img_mode = "RGBA" if remapped.shape[2] == 4 else "RGB"
            Image.fromarray(remapped, mode=img_mode).save(path)
        except OSError as exc:
            messagebox.showerror("Save PNG failed", str(exc))
            return
        messagebox.showinfo("Saved", f"Wrote {path}")


def run_gui(
    palette_path: Path,
    template_path: Path,
    mineral_palette_path: Path,
    ore_rgb: Any,
    ore_host_colours: frozenset[Rgb],
    ore_template_label: str,
    ore_save_dir: Path,
    gem_rgba: Any,
    gem_template_label: str,
    crystal_rgba: Any,
    crystal_template_label: str,
    ingot_rgba: Any,
    ingot_template_label: str,
    pickaxe_rgba: Any,
    pickaxe_template_label: str,
    sword_rgba: Any,
    sword_template_label: str,
    tool_handle_colours: frozenset[Rgb],
    item_save_dir: Path,
    handle_palette_path: Path | None = None,
) -> None:
    if not palette_path.is_file():
        raise FileNotFoundError(f"host palette not found: {palette_path}")
    if not template_path.is_file():
        raise FileNotFoundError(f"stone template not found: {template_path}")
    if not mineral_palette_path.is_file():
        raise FileNotFoundError(f"mineral palette not found: {mineral_palette_path}")
    if ore_rgb is None:
        raise ValueError("ore_rgb is required")
    if not ore_host_colours:
        raise ValueError("ore_host_colours must be a non-empty frozenset")
    if gem_rgba is None:
        raise ValueError("gem_rgba is required")
    if crystal_rgba is None:
        raise ValueError("crystal_rgba is required")
    if ingot_rgba is None:
        raise ValueError("ingot_rgba is required")
    if pickaxe_rgba is None:
        raise ValueError("pickaxe_rgba is required")
    if sword_rgba is None:
        raise ValueError("sword_rgba is required")
    if not tool_handle_colours:
        raise ValueError("tool_handle_colours must be a non-empty frozenset")

    load_palette(palette_path)
    load_rgb_image(template_path)
    load_palette(mineral_palette_path)

    app = FamilyPaletteGui(
        host_palette_path=palette_path,
        stone_template_path=template_path,
        mineral_palette_path=mineral_palette_path,
        ore_rgb=ore_rgb,
        ore_host_colours=ore_host_colours,
        ore_template_label=ore_template_label,
        ore_save_dir=ore_save_dir,
        gem_rgba=gem_rgba,
        gem_template_label=gem_template_label,
        crystal_rgba=crystal_rgba,
        crystal_template_label=crystal_template_label,
        ingot_rgba=ingot_rgba,
        ingot_template_label=ingot_template_label,
        pickaxe_rgba=pickaxe_rgba,
        pickaxe_template_label=pickaxe_template_label,
        sword_rgba=sword_rgba,
        sword_template_label=sword_template_label,
        tool_handle_colours=tool_handle_colours,
        item_save_dir=item_save_dir,
        handle_palette_path=handle_palette_path,
    )
    app.mainloop()
