/**
 * Handles player data & actions.
 * This class stores and manages the player’s stats (like HP, precision, etc.),
 * class type (e.g., Sous Chef), and actions like leveling up or taking damage.
 */
public class Player 
{
    // Instance variables (data the player object keeps track of)
    private String name; // Player’s name (e.g., "Alex")
    private int precision, stamina, creativity, flavorSense, speed, hp, maxHP; // Player’s stats
    private int level, experience; // Player’s level and experience points (XP)
    private String classType; // Player’s class (e.g., "Sous Chef", "Pastry Artist")

    /**
     * Constructor: Creates a new player with a name and class type.
     * Sets up initial stats based on the chosen class.
     * @param name The player’s name (e.g., "Alex").
     * @param classType The player’s class (e.g., "Sous Chef").
     */
    public Player(String name, String classType) 
    {
        this.name = name; // Sets the player’s name
        this.classType = classType; // Sets the player’s class
        this.level = 1; // Starts player at level 1
        this.experience = 0; // Starts with 0 XP
        setStatsByClass(classType); // Sets initial stats based on class
        this.maxHP = stamina * 10; // Calculates max HP (stamina * 10)
        this.hp = maxHP; // Sets current HP to max HP
    }

    /**
     * Sets initial stats based on the player’s class type.
     * Each class has unique starting stats for balance.
     * @param classType The player’s class (e.g., "Sous Chef").
     */
    private void setStatsByClass(String classType) 
    {
        switch (classType) 
        { // Checks the class type
            case "Sous Chef": // If player is a Sous Chef
                this.precision = 5; // Sets precision to 5 (average accuracy)
                this.stamina = 5; // Sets stamina to 5 (average endurance)
                this.creativity = 5; // Sets creativity to 5 (average imagination)
                this.flavorSense = 5; // Sets flavor sense to 5 (average taste skill)
                this.speed = 5; // Sets speed to 5 (average quickness)
                break; // Ends this case
            case "Pastry Artist": // If player is a Pastry Artist
                this.precision = 7; // Higher precision (good at details)
                this.stamina = 3; // Lower stamina (less endurance)
                this.creativity = 8; // High creativity (very imaginative)
                this.flavorSense = 5; // Average flavor sense
                this.speed = 7; // Slightly below average speed
                break;
            case "Grill Master": // If player is a Grill Master
                this.precision = 4; // Lower precision (less accurate)
                this.stamina = 8; // High stamina (tough and enduring)
                this.creativity = 5; // Average creativity
                this.flavorSense = 3; // Lower flavor sense (less taste focus)
                this.speed = 4; // High speed (quick on the grill)
                break;
            default: // If class type doesn’t match (fallback)
                this.precision = 5; // Sets all stats to average
                this.stamina = 5;
                this.creativity = 5;
                this.flavorSense = 5;
                this.speed = 5;
        }
    }

    /**
     * Increases the player’s level and stats when they have enough XP.
     * Caps at level 10 and fully heals the player.
     */
    public void levelUp() 
    {
        if (level < 10) // Checks if level is below the cap (10)
        { 
            level++; // Increases level by 1
            precision++; // Increases precision by 1
            stamina++; // Increases stamina by 1
            creativity++; // Increases creativity by 1
            flavorSense++; // Increases flavor sense by 1
            speed++; // Increases speed by 1
            this.maxHP = stamina * 10; // Recalculates max HP based on new stamina
            this.hp = maxHP; // Fully heals player to new max HP
            experience = 0; // Resets XP to 0 after leveling up
        }
    }

    /**
     * Adds experience points and triggers a level-up if enough XP is gained.
     * @param exp The amount of experience points to add.
     */
    public void gainExperience(int exp) 
    {
        this.experience += exp; // Adds XP to current experience
        if (this.experience >= getExpToLevel() && level < 10) 
        { // Checks if XP meets level-up requirement and level < 10
            levelUp(); // Calls levelUp if conditions are met
        }
    }

    /**
     * Reduces the player’s HP by a given amount of damage.
     * @param damage The amount of damage to take.
     */
    public void takeDamage(int damage) 
    {
        hp -= damage; // Subtracts damage from current HP
        if (hp < 0) hp = 0; // Ensures HP doesn’t go below 0
    }

    /**
     * Restores the player’s HP by a given amount, up to max HP.
     * @param amount The amount of HP to restore.
     */
    public void restoreHealth(int amount) 
    {
        hp += amount; // Adds amount to current HP
        if (hp > maxHP) hp = maxHP; // Caps HP at max HP
    }

    public int getCritChance() { return precision * 2; }
    public int getDodgeChance() { return speed * 3; }

    // Getters
    public String getName() { return name; }
    public String getClassType() { return classType; } // New getter for class
    public int getPrecision() { return precision; }
    public int getStamina() { return stamina; }
    public int getCreativity() { return creativity; }
    public int getFlavorSense() { return flavorSense; }
    public int getSpeed() { return speed; }
    public int getHP() { return hp; }
    public int getMaxHP() { return maxHP; }
    public int getLevel() { return level; }
    public int getExperience() { return experience; }
    public int getExpToLevel() { return level * 100; }
    public boolean isDefeated() { return hp <= 0; }

    // Setters for buffs
    public void setPrecision(int precision) { this.precision = precision; }
    public void setStamina(int stamina) { this.stamina = stamina; this.maxHP = stamina * 10; }
    public void setCreativity(int creativity) { this.creativity = creativity; }
    public void setFlavorSense(int flavorSense) { this.flavorSense = flavorSense; }
    public void setSpeed(int speed) { this.speed = speed; }
}