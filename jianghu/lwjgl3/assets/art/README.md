# Jianghu High-Resolution Art Pipeline

The desktop client loads `art/jianghu.atlas` when it exists. Until then, the Java procedural renderer remains the fallback.

## Hero animation atlas contract

Use transparent PNG source frames, packed into `jianghu.atlas` with matching `.png` page files. The current hero renderer recognizes these region prefixes:

- `hero_idle`
- `hero_walk`
- `hero_attack`
- `hero_block`
- `hero_hurt`
- `hero_death`
- `hero_train`

Name frames with numeric suffixes (`hero_walk_0`, `hero_walk_1`, ...); atlas regions must be ordered by frame index. Keep a consistent baseline at the feet and consistent character scale across every frame. The renderer mirrors the frame horizontally for left-facing movement.

## Art quality targets

- Hero source frame target: 256 x 384 px or larger, transparent background, clean silhouette and hand-painted material detail.
- Preserve separate source layers for body, face/hair, clothing, weapon, shadow and effects in the authoring files; pack flattened runtime frames into the atlas.
- Character frames should include cloth secondary motion, readable anticipation/contact/recovery poses, weapon trails and distinct hit/death silhouettes.
- Environment art should use modular ground, cliff, water, foliage, architecture, prop and lighting layers rather than baking the entire scene into one image.
- UI icons should use consistent 128 x 128 transparent source canvases with a common lighting direction and readable silhouettes at small display sizes.

## Runtime behavior

`ArtAssetCatalog` loads the atlas only when present and exposes per-state hero animation lookup. Missing animation regions safely fall back to the procedural Java sprite. Atlas textures are disposed with the desktop game.

This contract is an integration foundation, not a claim that finished 3A artwork is already present. The actual painted frames, environment sets, portraits, icon pack and effects still need to be authored and added.