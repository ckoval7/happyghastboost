# Happy Ghast Stamina Mod - Implementation Plan

## Overview
A NeoForge mod that adds a stamina system to Happy Ghasts, allowing players to increase flight speed by feeding them items.

## Core Mechanics
- **Stamina Range**: 0-100 points
- **Speed Scaling**: 1x (0.05) at 0 stamina → 2.5x (0.125) at 100 stamina
- **Decay**: Stamina decreases while player is riding (configurable rate)
- **Persistence**: Stamina stored as NBT data on ghast entity
- **Food Item**: Glowstone Dust (initial implementation)

## Project Structure
```
src/main/java/com/yourname/ghaststamina/
├── GhastStaminaMod.java              # Main mod class
├── config/
│   └── StaminaConfig.java            # Configuration (decay rate, feeding amounts)
├── data/
│   └── GhastStaminaData.java         # NBT data handler for stamina storage
├── event/
│   ├── GhastSpeedHandler.java        # Adjusts ghast speed based on stamina
│   ├── StaminaDecayHandler.java      # Handles stamina decay while riding
│   └── FeedingHandler.java           # Handles player feeding ghast
├── client/
│   └── gui/
│       └── StaminaBarOverlay.java    # Renders stamina bar on HUD
└── network/
    └── StaminaSyncPacket.java        # Syncs stamina server→client

src/main/resources/
├── META-INF/
│   └── neoforge.mods.toml            # Mod metadata
└── assets/ghaststamina/
    └── textures/
        └── gui/
            └── stamina_bar.png       # Stamina bar texture
```

## Implementation Phases

### Phase 1: Core Data & Config
**Files**: `GhastStaminaMod.java`, `StaminaConfig.java`, `GhastStaminaData.java`

1. Set up mod main class with NeoForge registration
2. Create config file with:
    - Decay rate (default: 0.5 points/second)
    - Food restoration amount (default: 20 points per glowstone dust)
    - Max stamina (default: 100)
3. Implement NBT data attachment to Happy Ghasts:
    - Store/retrieve stamina value
    - Initialize new ghasts with 50 stamina (mid-range)

### Phase 2: Stamina Mechanics
**Files**: `StaminaDecayHandler.java`, `GhastSpeedHandler.java`

1. **Decay System**:
    - Hook into server tick event
    - Check if player is riding a Happy Ghast
    - Decrease stamina at configured rate
    - Clamp to 0-100 range

2. **Speed Adjustment**:
    - Calculate speed using exponential curve (Option C - Inverted Decay):
      ```java
      float normalizedStamina = stamina / 100.0f;
      float curve = 1.0f - (float)Math.exp(-curveStrength * normalizedStamina);
      float speed = baseSpeed + curve * (maxSpeed - baseSpeed);
      ```
    - Default: baseSpeed=0.05, maxSpeed=0.125, curveStrength=4.0
    - Apply to ghast's movement attributes while riding
    - Reset to default when player dismounts

### Phase 3: Feeding System
**Files**: `FeedingHandler.java`

1. Listen for player interact event on Happy Ghast
2. Check if holding Glowstone Dust
3. Increase stamina by configured amount
4. Consume item and play particle effect
5. Sync stamina to client

### Phase 4: GUI (Client-Side)
**Files**: `StaminaBarOverlay.java`, `stamina_bar.png`

1. Create 9x9 texture for stamina icons (similar to hunger shanks)
2. Register GUI overlay renderer
3. Only render when:
    - Player is riding a Happy Ghast
    - Get stamina from synced data
4. Position opposite the ghast health bar
5. Render filled/empty icons based on stamina percentage

### Phase 5: Network Sync
**Files**: `StaminaSyncPacket.java`

1. Create packet to send stamina value server→client
2. Send when:
    - Player mounts ghast
    - Stamina changes (decay, feeding)
3. Client receives and updates local cache for GUI

## Key Technical Considerations

### Happy Ghast Detection
```java
// Check if entity is a Happy Ghast
if (entity instanceof TamableAnimal tamable && 
    entity.getType() == EntityType.GHAST && 
    tamable.isTame()) {
    // It's a Happy Ghast
}
```

### Speed Calculation
```java
// Option C: Inverted Exponential Decay (Capacitor-like curve)
float normalizedStamina = stamina / 100.0f; // 0.0 to 1.0
float curveStrength = StaminaConfig.CURVE_STRENGTH.get().floatValue(); // default: 4.0
float curve = 1.0f - (float)Math.exp(-curveStrength * normalizedStamina);

float baseSpeed = StaminaConfig.BASE_SPEED.get().floatValue(); // 0.05
float maxSpeed = StaminaConfig.MAX_SPEED.get().floatValue(); // 0.125
float speed = baseSpeed + curve * (maxSpeed - baseSpeed);

// Speed characteristics:
// 100 stamina: 0.125 (2.5x) | 50 stamina: 0.109 (2.18x)
// 40 stamina: 0.102 (2.04x) | 20 stamina: 0.078 (1.57x)
// Holds speed well at high stamina, drops rapidly below 30
```

### NBT Storage
```java
// NeoForge 1.21+ uses Attachment system instead of legacy NBT
// Store: entity.setData(GhastStaminaData.STAMINA, new StaminaData(value));
// Retrieve: float stamina = entity.getData(GhastStaminaData.STAMINA).stamina;
// Helper methods in GhastStaminaData class handle initialization and clamping
```

## Testing Plan
1. Spawn Happy Ghast in creative mode
2. Mount and verify default speed (0.05)
3. Feed Glowstone Dust and verify:
    - Stamina increases
    - Speed increases proportionally
    - GUI updates
4. Ride and verify stamina decays
5. Dismount/remount and verify stamina persists
6. Restart world and verify stamina persists

## Future Enhancements (Post-MVP)
- Custom food item with crafting recipe
- Sound effects for feeding
- Particle trail based on speed tier
- Multiple food types with different restoration amounts
- Stamina regeneration when ghast is resting (not moving)

## Questions Before Starting
1. ✅ Mod ID: "happyghastboost"
2. ✅ Package name: "com.happyghastboost"
3. ✅ Speed curve: Option C (Inverted Exponential Decay) - capacitor-like
4. ✅ Config-driven: curveStrength parameter allows easy tweaking without code changes

## Phase 1 Status: COMPLETE ✅
- `HappyGhastBoost.java` - Main mod class
- `StaminaConfig.java` - Configuration with curve strength parameter
- `GhastStaminaData.java` - Attachment-based stamina persistence