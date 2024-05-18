/** Game board field state (type) */
const FieldState = Object.freeze({
  NONE: "none",
  SHIP: "ship",
  HIT: "hit",
  MISS: "miss",
  INVALIDATED: "invalidated",
});

export default FieldState;
