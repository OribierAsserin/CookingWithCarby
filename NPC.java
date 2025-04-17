import java.util.Random;

/**
 * Non-Player Character (NPC) class for "Cooking with Carby."
 * Represents characters the player can interact with in the game world.
 * Each NPC has an affection level that changes based on player interactions,
 * and provides buffs or debuffs depending on their relationship with the player.
 */
public class NPC 
{
    private String name;          // The NPC's display name (e.g., "Sous Chef Lila")
    private int affectionLevel;   // Tracks relationship status (-10 to 10)

    /**
     * Constructor creates a new NPC with neutral starting affection
     * @param name The display name for this NPC
     */
    public NPC(String name) 
    {
        this.name = name;
        this.affectionLevel = 0; // Starts neutral (0)
    }

    /**
     * Changes the NPC's affection based on player interaction
     * @param choice The interaction choice (positive = friendly, negative = hostile)
     */
    public void interact(int choice) 
    {
        affectionLevel = affectionLevel + choice; // Modify affection
    }

    /**
     * Checks current relationship status and returns corresponding effect
     * @return A buff string for positive relationships, debuff for negative
     */
    public String getBuff() 
    {
        if (affectionLevel > 5) return "Increased Creativity"; // Strong positive
        else if (affectionLevel < -5) return "Sabotaged Dish"; // Strong negative
        return "No Effect"; // Neutral relationship
    }
    
    /**
     * Gets the NPC's name
     * @return The name string
     */
    public String getName() { return name; }
}

/**
 * Factory class that creates randomized NPC instances.
 * Contains pools of possible names, buffs and debuffs to generate unique NPCs.
 * Uses the Factory design pattern to centralize NPC creation logic.
 */
class NPCFactory 
{
    private static Random rand = new Random(); // Randomizer for selections
    
    // Possible name components for generated NPCs
    private static String[] names = { "Pip Hamstein", "Liza Sharuum", "Ghislain Worcestershire",  "Aspar Gios" };
    
    // Possible positive effects from friendly NPCs
    private static String[] buffs = { "Stamina Boost", "Creative Spark", "Speed Rush", "Flavor Boost" };
    
    // Possible negative effects from hostile NPCs
    private static String[] debuffs = { "Burned Fingers", "Overwhelmed", "Soggy Dish",  "Critic’s Curse" };

    /**
     * Creates a new randomized NPC instance
     * @return A new NPC with random name and neutral affection
     */
    public static NPC createNPC() 
    {
        String name = names[rand.nextInt(names.length)]; // Random name selection
        return new NPC(name);
    }

    /**
     * Enhanced NPC class with capped affection levels and randomized effects.
     * This nested class extends the base NPC functionality.
     */
    static class NPC 
    {
        private String name;
        private int affectionLevel;

        /**
         * Creates an enhanced NPC instance
         * @param name The display name
         */
        public NPC(String name) 
        {
            this.name = name;
            this.affectionLevel = 0; // Neutral start
        }

        /**
         * Modifies affection with upper/lower bounds (-10 to 10)
         * @param choice Interaction value (positive/negative)
         */
        public void interact(int choice) 
        {
            affectionLevel += choice;
            if (affectionLevel > 10) affectionLevel = 10; // Maximum friendship
            if (affectionLevel < -10) affectionLevel = -10; // Maximum hostility
        }

        /**
         * Gets a random effect based on current affection level
         * @return Random buff (high affection), debuff (low affection), or neutral
         */
        public String getEffect() 
        {
            if (affectionLevel >= 7) 
                return buffs[rand.nextInt(buffs.length)]; // Random buff
            else if (affectionLevel <= -7) 
                return debuffs[rand.nextInt(debuffs.length)]; // Random debuff
            return "No Effect"; // Neutral
        }

        // Accessor methods
        public String getName() { return name; }
        public int getAffectionLevel() { return affectionLevel; }
    }
}