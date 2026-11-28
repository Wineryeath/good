/** 一人一江湖：无 UI 依赖的规则内核。ES module，供浏览器与自动化测试复用。 */
export const SAVE_KEY = "jianghu-save-v2";
export const SAVE_VERSION = 2;
export const clamp = (n, min, max) => Math.min(max, Math.max(min, Number(n) || 0));
export const createPlayer = (name = "无名旅人") => ({
  id: crypto.randomUUID(), name, level: 1, exp: 0,
  attributes: { health:100, stamina:100, hunger:20, thirst:15, morale:70, reputation:0 },
  money: 2000, inventory: {}, equipment: {}, skills: {},
  position: { map:"wupeng-town", x:9, y:9 }, relationships: {}, flags: {},
  createdAt: new Date().toISOString()
});
export const createWorldState = () => ({
  day:1, minuteOfDay:360, seasonIndex:0, solarTermIndex:0, weather:"晴",
  mapStates:{}, npcStates:{}, questStates:{}, events:[], rngSeed:20261128
});
export function advanceTime(world, minutes) {
  if (!Number.isFinite(minutes) || minutes < 0) throw new RangeError("时间推进必须为非负分钟数");
  const total = world.minuteOfDay + Math.floor(minutes);
  world.day += Math.floor(total / 1440);
  world.minuteOfDay = total % 1440;
  return world;
}
export function getTimePhase(minute) {
  const h = Math.floor(minute / 60) % 24;
  if (h >= 5 && h < 8) return "dawn";
  if (h >= 8 && h < 17) return "day";
  if (h >= 17 && h < 20) return "dusk";
  return "night";
}
export function addItem(player, itemId, count, catalog) {
  if (!Number.isInteger(count) || count <= 0) throw new RangeError("物品数量必须为正整数");
  const item = catalog.find(x => x.id === itemId);
  if (!item) throw new Error("未知物品：" + itemId);
  const current = player.inventory[itemId] || 0;
  if (current + count > item.stack) throw new Error("超过该物品堆叠上限");
  player.inventory[itemId] = current + count;
  return player.inventory[itemId];
}
export function removeItem(player, itemId, count) {
  const current = player.inventory[itemId] || 0;
  if (!Number.isInteger(count) || count <= 0 || current < count) return false;
  const left = current - count;
  if (left) player.inventory[itemId] = left; else delete player.inventory[itemId];
  return true;
}
export function applyNeeds(player, effects) {
  for (const [key, delta] of Object.entries(effects)) {
    if (!(key in player.attributes)) continue;
    const bounds = key === "reputation" ? [-100,100] : [0,100];
    player.attributes[key] = clamp(player.attributes[key] + delta, ...bounds);
  }
}
export function transitionQuest(questState, event, questDefinition) {
  const stage = questDefinition.stages.find(s => s.id === questState.stage);
  if (!stage || questState.status === "completed" || questState.status === "failed") return {changed:false,reason:"terminal-or-invalid"};
  const objective = stage.objectives.find(o => o.event === event.type && (!o.target || o.target === event.target));
  if (!objective) return {changed:false,reason:"no-matching-objective"};
  const key = objective.id;
  questState.progress[key] = Math.min(objective.count, (questState.progress[key] || 0) + (event.count || 1));
  const complete = stage.objectives.every(o => (questState.progress[o.id] || 0) >= o.count);
  if (complete) questState.status = stage.next ? "ready-to-turn-in" : "completed";
  return {changed:true,stageComplete:complete,status:questState.status};
}
export function makeSave(player, world, extra = {}) {
  return {version:SAVE_VERSION,savedAt:new Date().toISOString(),player,world,...extra};
}
export function parseSave(raw) {
  const data = typeof raw === "string" ? JSON.parse(raw) : raw;
  if (!data || typeof data !== "object") throw new TypeError("存档格式无效");
  if (data.version !== SAVE_VERSION) throw new Error("存档版本不兼容，需要迁移或重新开始");
  if (!data.player?.position || !data.world) throw new Error("存档缺少角色或世界数据");
  return data;
}
