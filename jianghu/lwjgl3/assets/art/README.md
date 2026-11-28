# Jianghu High-Fidelity Art Pipeline

The desktop client loads `art/jianghu.atlas` when it exists. Until painted source assets are supplied, the Java procedural renderer remains the fallback. The target is a premium 2D/2.5D presentation inspired by modern AAA lighting and material work; this LibGDX renderer is not Unreal Engine 5 and does not provide UE5's full 3D Nanite/Lumen pipeline.

## Character atlas contract

Pack transparent PNG frames into `jianghu.atlas` with matching PNG page files. Every frame must share a stable feet baseline, consistent scale, and indexed suffixes (`_0`, `_1`, ...).

### Hero
- `hero_idle`, `hero_walk`, `hero_attack`, `hero_block`, `hero_hurt`, `hero_death`, `hero_train`
- Recommended source canvas: at least 512 x 768 px for the hero, with layered authoring files retained before flattening.
- Include anticipation, contact, follow-through and recovery; cloth, hair, tassels and weapon trails need secondary motion.

### NPC archetypes
- `npc_villager`, `npc_guard`, `npc_merchant`, `npc_scholar`, `npc_disciple`
- Each archetype supports the same state suffixes as the hero. Current scene integration selects a visual archetype from the NPC role and draws its idle loop when available.
- Distinguish age, body shape, clothing silhouette, profession props, palette and gait; avoid recoloring one generic sprite for every resident.

### Enemy archetypes
- `enemy_bandit`, `enemy_wolf`, `enemy_boss`, `enemy_martial_artist`
- Each archetype supports the same state suffixes. The active real-time encounter renders its matching enemy atlas actor beside the hero when the corresponding art is installed.
- Bosses need unique silhouettes, scale, armor materials, attack anticipation and hit/death poses.

## High-fidelity scene and material targets

- Character materials: separate albedo/detail authoring layers, cloth weave and fold highlights, leather roughness cues, metal specular accents, skin gradients and hair strand breakup.
- Environment: modular ground, cliffs, water, foliage, architecture, props, decals and atmospheric layers; use parallax depth planes rather than one flattened background.
- Lighting: normal-map aware 2D lights, warm/cool time-of-day key light, local lantern/torch lights, contact shadows, fog gradients and restrained bloom. Keep a readable unlit fallback for unsupported hardware.
- Effects: authored slash trails plus additive particles, sparks, dust, leaves, rain and water splashes; effects must be pooled and bounded.
- UI: 128 x 128 transparent icon source canvases, shared light direction, consistent material language, scalable nine-patch panels, typography hierarchy and clear interaction states.

## Runtime and verification

`ArtAssetCatalog` safely falls back when a region is absent and disposes atlas textures with the game. Atlas region names are sorted by numeric suffix by the asset pipeline; do not rely on filesystem order. Never add placeholder or blank PNGs merely to make an atlas appear populated.

An atlas contract or loader is not finished artwork. At present this repository contains the loader and Java fallback, not the final hand-painted hero/NPC/enemy frame packs, complete environment set, normal maps, portraits or UI icon pack. These need to be created, reviewed in-game and committed as actual assets.