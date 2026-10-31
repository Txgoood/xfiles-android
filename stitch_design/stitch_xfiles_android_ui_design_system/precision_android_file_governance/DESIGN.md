---
name: Precision Android File Governance
colors:
  surface: '#101319'
  surface-dim: '#101319'
  surface-bright: '#36393f'
  surface-container-lowest: '#0b0e13'
  surface-container-low: '#191c21'
  surface-container: '#1d2025'
  surface-container-high: '#272a2f'
  surface-container-highest: '#32353a'
  on-surface: '#e1e2e9'
  on-surface-variant: '#c3c6d7'
  inverse-surface: '#e1e2e9'
  inverse-on-surface: '#2e3036'
  outline: '#8d90a0'
  outline-variant: '#434655'
  surface-tint: '#b4c5ff'
  primary: '#b4c5ff'
  on-primary: '#002a78'
  primary-container: '#2563eb'
  on-primary-container: '#eeefff'
  inverse-primary: '#0053db'
  secondary: '#4edea3'
  on-secondary: '#003824'
  secondary-container: '#00a572'
  on-secondary-container: '#00311f'
  tertiary: '#d0bcff'
  on-tertiary: '#3c0091'
  tertiary-container: '#7d4ce7'
  on-tertiary-container: '#f6edff'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#dbe1ff'
  primary-fixed-dim: '#b4c5ff'
  on-primary-fixed: '#00174b'
  on-primary-fixed-variant: '#003ea8'
  secondary-fixed: '#6ffbbe'
  secondary-fixed-dim: '#4edea3'
  on-secondary-fixed: '#002113'
  on-secondary-fixed-variant: '#005236'
  tertiary-fixed: '#e9ddff'
  tertiary-fixed-dim: '#d0bcff'
  on-tertiary-fixed: '#23005c'
  on-tertiary-fixed-variant: '#5516be'
  background: '#101319'
  on-background: '#e1e2e9'
  surface-variant: '#32353a'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 26px
    fontWeight: '700'
    lineHeight: 34px
  headline-md:
    fontFamily: Inter
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
  headline-sm:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
  title-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
  label-md:
    fontFamily: Inter
    fontSize: 13px
    fontWeight: '500'
    lineHeight: 18px
  meta-mono:
    fontFamily: JetBrains Mono
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
  meta-mono-sm:
    fontFamily: JetBrains Mono
    fontSize: 10px
    fontWeight: '500'
    lineHeight: 14px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 0.75rem
  gutter-tablet: 1rem
  margin: 1rem
  margin-tablet: 1.5rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.75rem
  space-lg: 1rem
  space-xl: 1.5rem
---

## Brand & Style
This design system defines an authoritative, reliable, and high-performance file management environment for Android, rooted in Material 3 principles with an ultra-clean professional utility aesthetic. Built for power users, developers, and mobile professionals, the interface deliberately eliminates bloated visual metaphors, fake cleaners, and predatory promotional elements in favor of absolute transparency, density balance, and tactile responsiveness.

The visual style employs **Modern Systematic Utility**:
- **Structured Precision**: Every pixel communicates state, metadata, or navigation pathways without ambiguity.
- **Surface Elevation Hierarchy**: Uses tonal layering and subtle ambient boundaries rather than heavy skeuomorphic drops or high-contrast borders.
- **Functional Semantics**: Colors directly represent MIME types and actionable file operations (documents, archives, system alerts, audio, video).
- **Engineered Ergonomics**: Strict 48dp minimum touch targets enforce frictionless one-handed accessibility on mobile touchscreens.

## Colors
The color palette uses high-visibility functional assignments designed for immediate data parsing and critical state clarity across both dark and light modes. Dark mode serves as the primary canvas for battery-efficient OLED performance and reduced visual fatigue in technical workflows.

### Semantic File-Type & Status System
- **Action & Primary Selection (`#2563EB`)**: Drives system controls, multi-selection checkboxes, focal buttons, and primary breadcrumb highlights.
- **Documents & Verified Operations (`#10B981`)**: PDF, DOCX, TXT formats, and completed background transfer states.
- **Archives & Storage Warnings (`#F59E0B`)**: ZIP, TAR, RAR files, low disk space warnings (85%+ capacity), and non-destructive alerts.
- **Destructive Action & Critical Systems (`#EF4444`)**: Batch delete actions, file path errors, permission revocations, and drive-full warnings.
- **Visual & Cinema Media (`#8B5CF6`)**: MP4, MKV, RAW photography, and stream container previews.
- **Acoustic Media (`#06B6D4`)**: FLAC, MP3, WAV files, and audio recording buffers.

### Background and Surface Tokens
- **Canvas Tiers (Dark)**: Background (`#101318`) holds base pages; Surface (`#1B2028`) structures cards and bottom bars; Surface Variant (`#242B35`) provides clear separation for breadcrumb segments, active search inputs, and chips.
- **Canvas Tiers (Light)**: Background (`#F6F8FC`), Surface (`#FFFFFF`), Surface Variant (`#EEF2F6`).
- **Divider & Border Rules**: Apply strictly with `#2D3748` (dark) and `#E2E8F0` (light) at 1px thickness without added inner shadow.

## Typography
Typographic scale prioritizes scannability, vertical rhythm, and numeric precision. Primary content relies on **Inter** for clean readability across various device densities, while file technical properties rely on **JetBrains Mono**.

### Roles and Treatment
- **Headlines & Primary Titles**: Rendered in Inter with solid weight (`600`/`700`). Titles must truncate cleanly with an ellipsis (`...`) at middle or end depending on filename conventions (e.g., `system_log_2024...tar.gz`).
- **Technical Metadata (`meta-mono` / `meta-mono-sm`)**: Byte sizes (e.g., `48.2 MB`, `1.24 GB`), hash checksums, UNIX permission strings (`-rw-r--r--`), and file extensions are fixed to JetBrains Mono to avoid tabular jitter during list sorting and live I/O transfer reporting.
- **Secondary Body Text**: Kept strictly to 12px–14px with high-contrast muted tones (`#94A3B8` on dark, `#64748B` on light) ensuring low noise alongside dense file hierarchies.

## Layout & Spacing
The layout follows an adaptive fluid grid structured around dynamic multi-pane capabilities for tablets and compact vertical flows for mobile devices.

### Grid & Breakpoints
- **Mobile Handset (`< 600dp`)**: Single-column vertical stream. Screen margin is strictly `1rem` (16dp). Content spans full width with `0.75rem` (12dp) gutters for grid mode.
- **Tablet / Foldable Expanded (`600dp - 1024dp`)**: Dual-pane split mode (Directory tree pinned left at 280dp; file content stream right). Margin adjusts to `1.5rem` (24dp) with `1rem` (16dp) gutters.
- **Desktop / DeX Mode (`> 1024dp`)**: Triple-tier layout (Persistent side drawer, main file browser, and inspector metadata sidebar on the right).

### Ergonomics & Spacing Rhythm
- **Touch Targets**: All actionable icons, list item hitboxes, checkboxes, and breadcrumb segments require a hard floor of `48px x 48px` (`min-h-[48px] min-w-[48px]`), even when visual glyphs are constrained to 20px or 24px.
- **Horizontal Breadcrumb Spacing**: Horizontal scroll track aligns with the page margin (`1rem`), with each path element isolated by `0.5rem` (`space-sm`) gaps to prevent accidental miss-clicks.

## Elevation & Depth
This design system avoids blurry drop-shadows and skeuomorphic gradients in favor of **Tonal Surface Stacking** coupled with **Subtle Structural Outlines**.

### Elevation Hierarchy (Material 3 Tonal Levels)
- **Level 0 (Canvas Base)**: Application root background (`#101318` dark / `#F6F8FC` light).
- **Level 1 (In-Page Groupings & Inactive Cards)**: Base surfaces (`#1B2028` dark / `#FFFFFF` light) surrounded by a 1px border (`#2D3748` dark / `#E2E8F0` light).
- **Level 2 (Active Sheets, Search Containers, Sticky Toolbars)**: Elevated panels (`#242B35` dark / `#EEF2F6` light). Used for bottom navigation, floating multi-select contextual bars, and breadcrumb chips.
- **Level 3 (Modal Bottom Sheets & Context Menus)**: Highest local level (`#242B35` dark / `#FFFFFF` light) backed by an ultra-diffused shadow: `0 8px 32px rgba(0, 0, 0, 0.45)` in dark mode and `0 8px 24px rgba(15, 23, 42, 0.08)` in light mode, over an ambient backdrop dimming layer (`rgba(0, 0, 0, 0.65)`).
- **Interactive State Indication**: Elements do not float upward on press; rather, they register an inner surface overlay fill with 8% primary tint (`#2563EB14`) or lighten the background state seamlessly.

## Shapes
A balanced geometric corner profile matches modern mobile hardware bezels while maintaining clean internal density for complex directory browsing.

### Corner Radius System
- **Micro Radii (4px / 0.25rem)**: Storage progress indicators, metadata tag badges, file extension micro-chips.
- **Default Base (`8px / 0.5rem`)**: File list item interaction envelopes, grid thumbnails, context menu containers, input fields, and action buttons.
- **Large Container Radii (`16px / 1rem`)**: Storage overview summary cards, multi-selection contextual floating bars, and directory cards.
- **Sheet Radii (`24px / 1.5rem` on top corners)**: Draggable bottom action sheets and transfer progress trays.
- **Fully Rounded (Pill / 9999px)**: Active directory breadcrumb chips and floating quick-action triggers.

## Components

### Bottom Navigation Bar
- **Dimensions**: Anchored directly to screen base, total height 64dp plus system navigation inset bar.
- **Structure**: 4 static destinations: *Home*, *Files*, *Transfers*, *Settings*.
- **States**: Inactive icons render in secondary text color (`#94A3B8` dark); active state displays a pill indicator in primary surface tint (`#2563EB20`) paired with a sharp primary accent icon (`#2563EB`) and bold label. Minimum touch area per tab is `min-w-[64px] min-h-[48px]`.

### Horizontal Breadcrumb Track
- **Behavior**: Single-row horizontal scroll container with auto-scroll to the active trailing leaf.
- **Appearance**: Root and ancestor directories render as surface-variant chips (`#242B35` dark) with a trailing chevron icon. The active directory is highlighted with a semi-transparent primary fill and primary-colored text. Minimum touch target per folder crumb is `min-h-[48px]`.

### Storage Capacity Indicator Bars
- **Layout**: Two-tier linear breakdown. Base track background in dark surface variant (`#242B35`) with 4px height and rounded caps.
- **Fill Segments**: Dynamic multi-color breakdown displaying internal distribution (Apps: `#8B5CF6`, Documents: `#10B981`, Media: `#06B6D4`, Archives: `#F59E0B`, Free: `#2D3748`). 
- **Critical Threshold**: Turns entirely to `#EF4444` when used space exceeds 92%.

### File Cards & List Items
- **List View**: 56dp height per item (`min-h-[48px]` guaranteed interaction pad). Features 40dp MIME icon on the left, primary file label in `body-md` (Inter, 500 weight), and secondary metadata line combining size and timestamp formatted in `meta-mono-sm` (JetBrains Mono).
- **Grid View**: Aspect-ratio 1:1 container with a 72dp preview thumbnail or centered MIME icon, followed by a two-line metadata basement. Long-press triggers selection elevation with a 2px `#2563EB` border.

### Multi-Selection Contextual Top Bar
- **Trigger**: Activates upon long-pressing any file or clicking the selection mode toggle.
- **Layout**: Overlays the standard app bar with primary surface background (`#1B2028`), featuring an exit/cancel button on the left, active counter (e.g., `14 selected`), and right-aligned batch actions (*Share*, *Archive*, *Cut*, *Delete*).
- **Destructive Trigger**: The *Delete* icon on the top bar triggers a confirmation bottom sheet using warning hues (`#EF4444`).

### Action Bottom Sheets
- **Structure**: Slides from bottom screen edge with a 32dp width x 4dp height drag handle centered at the top.
- **Content**: Detailed file summary at top (full path in monospaced font, exact byte count, SHA256 checksum button), followed by a list of vertical options (*Open with*, *Compress*, *Rename*, *Permissions*, *Delete*), each adhering strictly to a 48dp minimum item height.

### Input Fields & Search Bars
- **Search Bar**: Sticky pill-shaped field with `#242B35` background, integrated back button, instant-clear action button, and search shortcut tags (e.g., `type:pdf`, `size:>100MB`).