import java.util.Random; // Imports Random class for generating random numbers

/**
 * Abstract base class for all enemies in the game. Defines common properties and behaviors,
 * allowing specific enemy types to inherit and customize their stats and attacks.
 */
abstract class Enemy 
{
    // Instance variables (data all enemies share)
    protected String name; // Enemy’s name (e.g., "Spicy Chili Demon")
    protected int hp, attackPower, speed; // Stats: health points, attack strength, speed
    protected String[] attackTypes; // Array of attack names unique to each enemy
    protected Random rand = new Random(); // Random object to pick attacks randomly
    protected String spritePath; // Path to the enemy's sprite image

    /**
     * Constructor: Initializes an enemy with a name and base stats.
     * @param name The enemy’s name (e.g., "Sushi Samurai").
     * @param hp Base health points (how much damage they can take).
     * @param attackPower Base attack strength (how much damage they deal).
     * @param speed Base speed (affects turn order in combat).
     */
    public Enemy(String name, int hp, int attackPower, int speed, String spritePath) 
    {
        this.name = name; // Sets the enemy’s name
        this.hp = hp; // Sets initial health points
        this.attackPower = attackPower; // Sets initial attack power
        this.speed = speed; // Sets initial speed
        this.spritePath = spritePath;
        
    }
    
    public String getSpritePath()
    {
    	return spritePath;
    }

    /**
     * Randomly selects one of the enemy’s attack types.
     * @return A string representing the chosen attack (e.g., "Flambé Blast").
     */
    public String chooseAttack() 
    {
        int roll = rand.nextInt(attackTypes.length); // Picks a random index (0 to length-1)
        return attackTypes[roll]; // Returns the attack at that index
    }

    /**
     * Reduces the enemy’s HP by the specified damage amount.
     * @param damage The amount of damage to take.
     */
    public void takeDamage(int damage) 
    {
        this.hp = this.hp - damage; // Subtracts damage from HP (can go negative)
    }

    /**
     * Abstract method that subclasses must implement to scale stats based on player level.
     * This ensures enemies get stronger as the player levels up.
     * @param playerLevel The current level of the player, used to increase enemy difficulty.
     */
    abstract void scaleStats(int playerLevel); // No body here—subclasses must define this
    
    
    // Getters (methods to access private/protected variables)
    public String getName() { return name; } // Returns the name
    public int getHP() { return hp; } // Returns current HP
    public int getAttackPower() { return attackPower; } // Returns attack power
    public int getSpeed() { return speed; } // Returns speed
}

/**
 * Subclass representing the "Spicy Chili Demon" enemy with fiery, high-damage attacks.
 */
class SpicyChiliDemon extends Enemy 
{
    /**
     * Constructor: Creates a Spicy Chili Demon with base stats scaled by player level.
     * @param playerLevel The player’s level to adjust difficulty.
     */
    public SpicyChiliDemon(int playerLevel) 
    {
        super("Spicy Chili Demon", 70, 15, 5, "src/resources/SpicyChiliDemon.png"); // Calls Enemy constructor with base stats
        this.attackTypes = new String[] { "Flambé Blast", "Spicy Toss" }; // Sets attack options
        scaleStats(playerLevel); // Scales stats based on player level
    }

    /**
     * Scales the Spicy Chili Demon’s stats based on the player’s level.
     * @param playerLevel The player’s level to increase difficulty.
     */
    @Override
    void scaleStats(int playerLevel) 
    {
        this.hp += playerLevel * 5; // Increases HP by 5 per player level
        this.attackPower += playerLevel * 2; // Increases attack by 2 per level
        this.speed += playerLevel; // Increases speed by 1 per level
    }
}

/**
 * Subclass representing the "Sushi Samurai" enemy, a fast and precise fighter.
 */
class SushiSamurai extends Enemy 
{
    /**
     * Constructor: Creates a Sushi Samurai with base stats scaled by player level.
     * @param playerLevel The player’s level to adjust difficulty.
     */
    public SushiSamurai(int playerLevel) 
    {
        super("Sushi Samurai", 60, 10, 10, "src/resources/SushiSamurai.png"); // Calls Enemy constructor with base stats
        this.attackTypes = new String[] {"Knife Slice", "Sushi Roll"}; // Sets attack options
        scaleStats(playerLevel); // Scales stats based on player level
    }

    /**
     * Scales the Sushi Samurai’s stats based on the player’s level.
     * @param playerLevel The player’s level to increase difficulty.
     */
    @Override
    void scaleStats(int playerLevel) 
    {
        this.hp += playerLevel * 4; // Increases HP by 4 per player level
        this.attackPower += playerLevel * 1; // Increases attack by 1 per level
        this.speed += playerLevel * 2; // Increases speed by 2 per level
    }
}

/**
 * Subclass representing the "Pretentious Gourmet Critic" enemy, with strong attacks.
 */
class PretentiousGourmetCritic extends Enemy 
{
    /**
     * Constructor: Creates a Pretentious Gourmet Critic with base stats scaled by player level.
     * @param playerLevel The player’s level to adjust difficulty.
     */
    public PretentiousGourmetCritic(int playerLevel)
    {
        super("Pretentious Gourmet Critic", 50, 12, 7, "src/resources/PretentiousGourmetCritic.png"); // Calls Enemy constructor with base stats
        this.attackTypes = new String[] {"Harsh Critique", "Pretentious Glare"}; // Sets attack options
        scaleStats(playerLevel); // Scales stats based on player level
    }

    /**
     * Scales the Pretentious Gourmet Critic’s stats based on the player’s level.
     * @param playerLevel The player’s level to increase difficulty.
     */
    @Override
    void scaleStats(int playerLevel) 
    {
        this.hp += playerLevel * 3; // Increases HP by 3 per player level
        this.attackPower += playerLevel * 3; // Increases attack by 3 per level
        this.speed += playerLevel; // Increases speed by 1 per level
    }
}

/**
 * Factory class responsible for creating random enemy instances based on the player’s level.
 * This class uses a switch statement to select and instantiate one of several enemy types.
 */
class EnemyFactory 
{
    private static Random rand = new Random(); // Static Random object shared by all calls

    /**
     * Creates a random enemy based on the player’s current level.
     * @param playerLevel The player’s current level, used to scale the enemy’s stats.
     * @return An instance of a randomly selected Enemy subclass.
     */
    public static Enemy createEnemy(int playerLevel)
    {
        int type = rand.nextInt(3); // Generates a random number (0, 1, or 2) for enemy type

        // Switch statement: picks an enemy based on the random number
        switch (type) 
        {
            case 0: // If type is 0
                return new SpicyChiliDemon(playerLevel); // Creates and returns a Spicy Chili Demon
            case 1: // If type is 1
                return new SushiSamurai(playerLevel); // Creates and returns a Sushi Samurai
            case 2: // If type is 2
                return new PretentiousGourmetCritic(playerLevel); // Creates and returns a Critic
            default: // Fallback if something goes wrong (shouldn’t happen with nextInt(3))
                return new SpicyChiliDemon(playerLevel); // Returns a Spicy Chili Demon
        }
    }
}