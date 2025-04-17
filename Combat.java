import javax.swing.*; // Imports tools for creating buttons, panels, etc. (Swing GUI library)
import java.awt.*; // Imports tools for colors, fonts, and layouts (AWT library)
import javax.sound.sampled.*; // Imports tools for playing sound files (like attack sounds)
import java.io.File; // Imports File class to work with sound files
import java.util.Random;

/**
 * Manages turn-based combat with class-specific attacks for Cooking with Carby.
 * This class handles the battle system, showing attack buttons, calculating damage,
 * playing sounds, and updating the game window (via GameWindow).
 */
public class Combat 
{
    // Instance variables (data the class needs to keep track of)
    private Player player; // The player’s character (stats, HP, etc.)
    private Enemy enemy; // The enemy being fought (HP, attack power, etc.)
    private JPanel visualPanel; // Panel for showing attack buttons (from GameWindow)
    private JTextArea textArea; // Text box for battle messages (from GameWindow)
    private GameWindow gameWindow; // Reference to the main game window
    private JButton[] attackButtons = new JButton[4]; // Array to hold up to 4 attack buttons
    private int enemyInitialHP; // Stores enemy’s starting HP for XP calculation

    /**
     * Constructor: Sets up a combat instance with player, enemy, and UI components.
     * @param player The player fighting in the battle.
     * @param enemy The enemy being fought.
     * @param visualPanel The panel for displaying attack buttons.
     * @param textArea The text area for battle messages.
     * @param gameWindow The main game window to update after battle.
     */
    public Combat(Player player, Enemy enemy, JPanel visualPanel, JTextArea textArea, GameWindow gameWindow) 
    {
        this.player = player; // Assigns the player object
        this.enemy = enemy; // Assigns the enemy object
        this.visualPanel = visualPanel; // Assigns the panel for buttons
        this.textArea = textArea; // Assigns the text area for messages
        this.gameWindow = gameWindow; // Assigns the game window reference
        this.enemyInitialHP = enemy.getHP(); // Saves enemy’s initial HP
    }

    /**
     * Starts the battle, setting up attack buttons and resetting player HP.
     */
    public void startBattle() 
    {
        resetPlayerHP(); // Heals player to full HP before battle
        visualPanel.removeAll(); // Clears any old buttons from the panel
  
        // Load and display the enemy's sprite
        JLabel enemySprite = new JLabel(); 
        try // Tries to load Enemy image
        { 
            ImageIcon enemyIcon = new ImageIcon(enemy.getSpritePath());
            if (enemyIcon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) // Checks if image loaded
            {
                throw new Exception("Image failed to load"); // Throws error if it fails
            }
            enemySprite.setIcon(enemyIcon); // Sets image to label
        } 
        catch (Exception e) // Catches error if image fails
        { 
            System.err.println("Enemy image not found at src/resources/enemy.png."); // Prints error
            enemySprite.setText("Enemy Image Missing"); // Shows text instead
        }

        enemySprite.setBounds(550, 150, 256, 256); 
        visualPanel.add(enemySprite);
        
        textArea.setText("Battle Begins: " + player.getName() + " vs. " + enemy.getName() + "\n"); // Shows battle start message
        textArea.append("Your HP: " + player.getHP() + " | Enemy HP: " + enemy.getHP() + "\n"); // Shows HP stats

        // Fetch class-specific attacks (e.g., "Chop" for Sous Chef)
        String[] attacks = getClassAttacks(); // Gets attack names based on player class
        int playerLevel = player.getLevel(); // Gets player’s level to unlock attacks

        // Set up base attacks (available from level 1)
        attackButtons[0] = createStyledButton(attacks[0]); // Creates button for first attack (e.g., "Chop")
        attackButtons[1] = createStyledButton(attacks[1]); // Creates button for second attack (e.g., "Sauté")
        attackButtons[0].addActionListener(e -> performAttackAsync(1, attacks[0])); // Runs attack 1 when clicked
        attackButtons[1].addActionListener(e -> performAttackAsync(2, attacks[1])); // Runs attack 2 when clicked
        attackButtons[0].setBounds(20, 361, 150, 40); // Positions first button
        attackButtons[1].setBounds(170, 361, 150, 40); // Positions second button
        visualPanel.add(attackButtons[0]); // Adds first button to panel
        visualPanel.add(attackButtons[1]); // Adds second button to panel

        // Unlock third attack at level 4 (e.g., "Dice" or "Fold")
        if (playerLevel >= 4) // Checks if player is level 4 or higher
        { 
            attackButtons[2] = createStyledButton(attacks[2]); // Creates third attack button
            attackButtons[2].addActionListener(e -> performAttackAsync(3, attacks[2])); // Runs attack 3 when clicked
            attackButtons[2].setBounds(550, 50, 150, 40); // Positions third button
            visualPanel.add(attackButtons[2]); // Adds third button to panel
        }

        // Unlock fourth attack at level 7 (e.g., "Simmer" or "Bake")
        if (playerLevel >= 7) // Checks if player is level 7 or higher
        {
            attackButtons[3] = createStyledButton(attacks[3]); // Creates fourth attack button
            attackButtons[3].addActionListener(e -> performAttackAsync(4, attacks[3])); // Runs attack 4 when clicked
            attackButtons[3].setBounds(250, 100, 150, 40); // Positions fourth button
            visualPanel.add(attackButtons[3]); // Adds fourth button to panel
        }
        
        if (gameWindow.battleCount == 0)
        {
        	textArea.append("Oh look, here's one now!\n");
        }

        visualPanel.revalidate(); // Updates panel layout after adding buttons
        visualPanel.repaint(); // Redraws panel to show buttons
    }

    /**
     * Returns an array of attack names based on the player’s class.
     * @return A String array of 4 attack names.
     */
    private String[] getClassAttacks() 
    {
        switch (player.getClassType()) // Checks player’s class type
        { 
            case "Sous Chef": // If Sous Chef
                return new String[] {"Chop", "Sauté", "Dice", "Simmer"}; // Returns Sous Chef attacks
            case "Pastry Artist": // If Pastry Artist
                return new String[] {"Whisk", "Frost", "Fold", "Bake"}; // Returns Pastry Artist attacks
            case "Grill Master": // If Grill Master
                return new String[] {"Grill", "Sear", "Baste", "Smoke"}; // Returns Grill Master attacks
            default: // If no class matches (fallback)
                return new String[] {"Slice", "Flambé", "Cut", "Roast"}; // Returns default attacks
        }
    }

    /**
     * Starts a player attack in a new thread to avoid freezing the UI.
     * @param attackType The type of attack (1-4) to determine damage.
     * @param attackName The name of the attack (e.g., "Chop").
     */
    private void performAttackAsync(int attackType, String attackName)
    {
        disableButtons(); // Disables attack buttons during attack
        new Thread(() -> performPlayerAttack(attackType, attackName)).start(); // Runs attack in new thread
    }

    /**
     * Performs the player’s attack and enemy’s counterattack (if alive).
     * @param attackType The type of attack (1-4) to calculate damage.
     * @param attackName The name of the attack to display.
     */
    private void performPlayerAttack(int attackType, String attackName) {
        disableButtons(); // Disable buttons during attack
        
        // Calculate base damage
        int baseDamage;
        switch (attackType) 
        { 
            case 1: baseDamage = player.getPrecision() * 2; break;
            case 2: baseDamage = player.getCreativity() * 3; break;
            case 3: baseDamage = player.getSpeed() * 2 + 5; break;
            case 4: baseDamage = player.getStamina() * 4; break;
            default: baseDamage = 10;
        }
        
        Random rand = new Random(); // Random object to pick attacks randomly

        // Check for critical hit (precision * 2% chance)
        boolean isCritical = rand.nextInt(100) < player.getCritChance();
        int finalDamage = isCritical ? baseDamage * 2 : baseDamage;

        // Display attack message with critical hit if applicable
        String attackMessage = player.getName() + " uses " + attackName;
        if (isCritical) 
        {
            attackMessage += " (CRITICAL HIT!)";
            playSound("src/resources/critical_sound.wav"); // Special critical sound
        }
        appendTextWithSound(attackMessage, "src/resources/player_attack.wav");
        
        // Apply damage
        appendTextWithSound(player.getName() + " deals " + finalDamage + " damage to " + enemy.getName() + "!", null);
        enemy.takeDamage(finalDamage);
        pause(500);

        // Enemy counterattack if alive
        if (enemy.getHP() > 0)
        {
            // Check for dodge (speed * 3% chance)
            boolean didDodge = rand.nextInt(100) < player.getDodgeChance();
            
            if (didDodge)
            {
                appendTextWithSound(player.getName() + " swiftly dodges the counterattack!", "src/resources/dodge_sound.wav");
            } else {
                String enemyAttack = enemy.chooseAttack();
                int enemyDamage = enemy.getAttackPower();
                appendTextWithSound(enemy.getName() + " uses " + enemyAttack + "!", "src/resources/enemy_attack.wav");
                player.takeDamage(enemyDamage);
                appendTextWithSound(enemy.getName() + " hits you for " + enemyDamage + " damage!", null);
            }
            pause(500);
        }

        SwingUtilities.invokeLater(() -> {
            checkBattleEnd();
            enableButtons();
        });
    }


    /**
     * Adds text to the text area and plays a sound if provided.
     * @param text The message to display in the text area.
     * @param soundFile The sound file to play (or null for no sound).
     */
    private void appendTextWithSound(String text, String soundFile) 
    {
        SwingUtilities.invokeLater(() -> // Updates UI safely (Swing is picky about threads)
        { 
            textArea.append(text + "\n"); // Adds text with a new line
            visualPanel.revalidate(); // Updates panel layout
            visualPanel.repaint(); // Redraws panel
        });
        if (soundFile != null) // If there’s a sound file
        { 
            playSound(soundFile); // Plays the sound
            pause(500); // Pauses to let sound finish
        }
    }

    /**
     * Plays a sound file at maximum volume.
     * @param soundFile The path to the sound file (e.g., "src/resources/player_attack.wav").
     */
    private void playSound(String soundFile) 
    {
        try // Tries to play sound (might fail if file is missing)
        { 
            File file = new File(soundFile); // Creates File object for sound
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file); // Prepares audio data
            Clip clip = AudioSystem.getClip(); // Creates a Clip to play sound
            clip.open(audioIn); // Loads audio into Clip
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN); // Gets volume control
            float maxDB = gainControl.getMaximum(); // Gets max volume level
            gainControl.setValue(maxDB); // Sets volume to maximum
            clip.start(); // Starts playing sound
            while (!clip.isRunning()) Thread.sleep(10); // Waits until sound starts
            while (clip.isRunning()) Thread.sleep(10); // Waits until sound finishes
            clip.close(); // Frees resources after sound ends
        } 
        catch (Exception e) // Catches errors (e.g., file not found)
        { 
            System.err.println("Error playing sound: " + soundFile + " - " + e.getMessage()); // Prints error
        }
    }

    /**
     * Disables all attack buttons to prevent multiple clicks during an attack.
     */
    private void disableButtons() 
    {
        SwingUtilities.invokeLater(() -> // Updates UI safely
        { 
            for (JButton button : attackButtons) // Loops through attack buttons
            { 
                if (button != null) button.setEnabled(false); // Disables button if it exists
            }
        });
    }

    /**
     * Enables all attack buttons after an attack finishes.
     */
    private void enableButtons() 
    {
        SwingUtilities.invokeLater(() -> // Updates UI safely
        { 
            for (JButton button : attackButtons) // Loops through attack buttons
            { 
                if (button != null) button.setEnabled(true); // Enables button if it exists
            }
        });
    }

    /**
     * Pauses the thread for a given number of milliseconds.
     * @param milliseconds The time to pause in milliseconds (e.g., 500 = 0.5 seconds).
     */
    static void pause(int milliseconds) 
    {
        try // Tries to pause (might fail if interrupted)
        { 
            Thread.sleep(milliseconds); // Pauses current thread
        } 
        catch (InterruptedException e) // Catches interruption error
        { 
            Thread.currentThread().interrupt(); // Restores interrupted status
        }
    }

    /**
     * Checks if the battle has ended (player or enemy HP <= 0) and updates accordingly.
     */
    private void checkBattleEnd() 
    {
        textArea.append("Your HP: " + player.getHP() + " | Enemy HP: " + enemy.getHP() + "\n"); // Shows current HP
        if (player.getHP() <= 0) // If player is defeated
        {
            textArea.append("You were defeated by " + enemy.getName() + "!\n"); // Shows defeat message
            gameWindow.incrementBattleCount(); // Increases battle count in GameWindow
            endBattle("Back to Menu"); // Ends battle with "Back to Menu" button
        } 
        else if (enemy.getHP() <= 0) // If enemy is defeated
        {
            textArea.append("You defeated " + enemy.getName() + "!\n"); // Shows victory message
            int oldLevel = player.getLevel(); // Saves current level
            awardExperience(); // Gives player XP
            if (player.getLevel() > oldLevel) // If player leveled up
            { 
                textArea.append("Level Up! You reached Level " + player.getLevel() + "!\n"); // Shows level up
            }
            gameWindow.incrementBattleCount(); // Increases battle count
            endBattle("Back to Menu"); // Ends battle
        }
    }

    /**
     * Ends the battle, replacing attack buttons with a back button.
     * @param buttonText The text for the back button (e.g., "Back to Menu").
     */
    private void endBattle(String buttonText)
    {
        visualPanel.removeAll(); // Clears attack buttons from panel
        JButton backButton = createStyledButton(buttonText); // Creates back button
        backButton.addActionListener(e -> gameWindow.endCombat()); // Returns to main menu when clicked
        backButton.setBounds(350, 50, 150, 40); // Positions button
        visualPanel.add(backButton); // Adds button to panel
        visualPanel.revalidate(); // Updates panel layout
        visualPanel.repaint(); // Redraws panel
    }

    /**
     * Awards experience points to the player based on enemy HP and level.
     */
    private void awardExperience() 
    {
        int expGained = enemyInitialHP / 2 + player.getLevel() * 10; // Calculates XP (half enemy HP + level * 10)
        player.gainExperience(expGained); // Adds XP to player
        textArea.append("You gained " + expGained + " experience!\n"); // Shows XP gained
    }

    /**
     * Resets the player’s HP to full before the battle starts.
     */
    private void resetPlayerHP() 
    {
        player.restoreHealth(player.getMaxHP() - player.getHP()); // Heals player to max HP
    }

    /**
     * Creates a styled button for attacks or back navigation.
     * @param text The text to display on the button (e.g., "Chop").
     * @return A styled JButton with Papyrus font and brown color.
     */
    private JButton createStyledButton(String text) 
    {
        JButton button = new JButton(text); // Creates button with given text
        button.setFont(new Font("Matura MT Script Capitals", Font.BOLD, 14)); // Sets bold Papyrus font, size 14
        button.setBackground(new Color(160, 82, 45)); // Sets brown background
        button.setForeground(Color.WHITE); // Sets white text color
        button.setBorder(BorderFactory.createRaisedBevelBorder()); // Adds 3D raised border
        button.setFocusPainted(false); // Removes focus outline when clicked
        button.setPreferredSize(new Dimension(150, 40)); // Sets button size to 150x40
        return button; // Returns the styled button
    }
}