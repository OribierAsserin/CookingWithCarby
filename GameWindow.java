import javax.swing.*; // Imports tools for creating windows, buttons, text fields (Swing GUI library)
import java.awt.*; // Imports tools for layouts, colors, and drawing (AWT library)
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.sound.sampled.*; // Imports tools for playing audio files (like music or sound effects)
import java.io.File; // Imports File class to work with files (e.g., sound or image files)
import java.util.ArrayList; // Imports ArrayList, a resizable list for storing objects
import java.util.List; // Imports List interface for managing collections (like NPCs)


/**
 * Main game window for "Cooking with Carby."
 * This class creates the game’s main window using JFrame (a basic window in Java),
 * sets up the layout with a top panel for buttons/images and a bottom text area,
 * and handles all screens (title, tutorial, menu, combat, etc.)
 */
public class GameWindow extends JFrame // Extends JFrame to make a window
{ 
    // Instance variables (data the class needs to keep track of)
    private JPanel visualPanel; // Panel for buttons, images (top half of window)
    private JTextArea textArea; // Text box for game messages (bottom half)
    private Player player; // Player object (stores name, stats, etc.)
    public int battleCount = 0; // Counts how many battles the player has fought
    private Clip normalMusicClip; // Stores normal background music
    private Clip combatMusicClip; // Stores combat music
    private List<NPCFactory.NPC> encounteredNPCs = new ArrayList<>(); // List of NPCs player has met
    private String selectedClass = null; // Tracks the previewed class
    private Clip currentMusicClip; // To track which music clip is playing
    private JPanel overlayPanel; // The main overlay container
    private JButton returnButton; // The permanent return button
    private JButton settingsButton; // The permanent settings button
    private float musicVolume = 0.6f; // Default volume (60%)
    private float soundVolume = 1.0f; // Default sound effects volume
    private boolean tutorial = false;



    /**
     * Constructor: Sets up the game window when the program starts.
     * This runs when you create a new GameWindow object (like in main()).
     */
    public GameWindow() 
    {
        setTitle("Cooking with Carby"); // Sets the window title at the top
        setSize(800, 600); // Sets window size to 800 pixels wide, 600 tall
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Closes program when you click X
        getContentPane().setBackground(new Color(139, 69, 19)); // Sets window background to brown (RGB)
        
        // Create layered pane for proper z-ordering
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(800, 600));
        setContentPane(layeredPane);

        // Create the visual panel (top section for buttons/images)
        visualPanel = new JPanel() // JPanel is a container for GUI elements
        { 
            @Override
            protected void paintComponent(Graphics g) // Overrides how the panel is drawn
            { 
                super.paintComponent(g); // Calls default drawing first
                try // Try block: Attempts code that might fail (like loading an image)
                { 
                    ImageIcon backgroundIcon = new ImageIcon("src/resources/background.png"); // Loads image
                    if (backgroundIcon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) // Checks if image loaded
                    { 
                        throw new Exception("Image didn’t load"); // Throws an error if image fails
                    }
                    g.drawImage(backgroundIcon.getImage(), 0, 0, 800, 580, this); // Draws image across panel
                } 
                catch (Exception e) // Catch block: Handles errors from try block
                { 
                    System.err.println("No background image found. Using color instead."); // Prints error
                    g.setColor(new Color(160, 82, 45)); // Sets fallback brown color
                    g.fillRect(0, 0, 800, 400); // Fills panel with this color
                }
            }
        };
        
        visualPanel.setBounds(0, 0, 800, 400); // Sets panel size to 800x400
        visualPanel.setOpaque(false); // Makes panel transparent (shows window background)
        visualPanel.setLayout(null); // Uses absolute positioning (setBounds) for buttons
        layeredPane.add(visualPanel, JLayeredPane.DEFAULT_LAYER); // Adds panel

        // Create the text area (bottom section for messages)
        textArea = new JTextArea(6, 40); // Creates text area (10 rows, 40 columns wide)
        textArea.setEditable(false); // Player can’t type in it
        textArea.setLineWrap(true); // Text wraps to next line if too long
        textArea.setWrapStyleWord(true); // Wraps at word boundaries, not mid-word
        textArea.setBackground(new Color(245, 222, 179)); // Sets beige background
        textArea.setForeground(new Color(51, 25, 0)); // Sets dark brown text color
        textArea.setFont(new Font("Matura MT Script Capitals", Font.PLAIN, 19)); // Uses Papyrus font, size 16
        textArea.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 2)); // Adds 2-pixel brown border
        JScrollPane scrollPane = new JScrollPane(textArea); // Adds scrollbar to text area
        scrollPane.setBounds(0, 400, 800, 160); // Sets scrollbar size to 800x200
        scrollPane.setOpaque(false); // Makes scrollbar background transparent
        scrollPane.getViewport().setOpaque(false); // Makes viewport (visible area) transparent
        scrollPane.setBorder(null); // Removes scrollbar border
        layeredPane.add(scrollPane, JLayeredPane.DEFAULT_LAYER); // Adds text area to center of window
        
        // Create the permanent return button
        returnButton = createStyledButton("Return");
        returnButton.setBounds(650, 515, 120, 40); // Position in bottom right
        returnButton.addActionListener(e -> {
            // Default action - can be overridden per screen
            showMainMenu();
        });
        layeredPane.add(returnButton, JLayeredPane.PALETTE_LAYER); // Higher layer
        
     // Create the settings button
        settingsButton = createStyledButton("⚙"); // Gear icon for settings
        settingsButton.setBounds(25, 25, 30, 30); // Position in top left
        settingsButton.addActionListener(e -> showSettingsDialog());
        settingsButton.setFont(new Font("Arial", Font.PLAIN, 20)); // Larger font for icon
        
        layeredPane.add(settingsButton, JLayeredPane.PALETTE_LAYER);        setReturnButtonVisible(false); // Hide return button on main menu since it's the root screen

        
        playBackgroundMusic("src/resources/normal_music.wav"); // Starts background music
        showTitleScreen(); // Shows title screen first
        setVisible(true); // Makes window visible on screen
        setResizable(false);
    }

    /**
     * Creates a styled button with consistent look and sound.
     * @param text The text to display on the button.
     * @return A styled JButton with Papyrus font and click sound.
     */
    private JButton createStyledButton(String text) 
    {
        JButton button = new JButton(text); // Creates button with given text
        button.setFont(new Font("Matura MT Script Capitals", Font.BOLD, 12)); // Sets bold Papyrus font, size 14
        button.setBackground(new Color(160, 82, 45)); // Sets brown background
        button.setForeground(Color.WHITE); // Sets white text color
        button.setBorder(BorderFactory.createRaisedBevelBorder()); // Adds 3D raised border
        button.setFocusPainted(false); // Removes focus outline when clicked
        button.setPreferredSize(new Dimension(150, 40)); // Sets button size to 150x40
        button.addActionListener(e -> playSound("src/resources/button_click.wav")); // Plays sound on click
        return button; // Returns the styled button
    }

    /**
     * Displays the title screen with "Start Game" and "Exit" options.
     */
    private void showTitleScreen() 
    {
        visualPanel.removeAll(); // Clears all buttons/images from panel
        textArea.setText("Welcome to Cooking with Carby!\n"); // Sets welcome message
        JButton startButton = createStyledButton("Start Game"); // Creates "Start Game" button
        JButton exitButton = createStyledButton("Exit"); // Creates "Exit" button

        startButton.addActionListener(e -> showClass()); // When clicked, shows tutorial
        exitButton.addActionListener(e -> System.exit(0)); // When clicked, closes program

        startButton.setBounds(170, 300, 150, 40); // Positions button at x=250, y=50
        exitButton.setBounds(470, 300, 150, 40); // Positions button at x=450, y=50
        visualPanel.add(startButton); // Adds start button to panel
        visualPanel.add(exitButton); // Adds exit button to panel
        textArea.append("Choose an option above:\n"); // Adds instruction

        visualPanel.revalidate(); // Updates panel layout
        visualPanel.repaint(); // Redraws panel to show changes
    }

    /**
     * Displays the tutorial screen for picking a class and name.
     */
    private void showClass() 
    {
    	setReturnButtonVisible(false); // Hide return button on main menu since it's the root screen
        visualPanel.removeAll(); // Clears panel
        textArea.setText("Pick a cooking style to get started.\n");
        textArea.append("Enter your name: "); // Prompts for name

        JTextField nameField = new JTextField(10); // Creates text box for name (10 chars wide)
        nameField.setFont(new Font("Matura MT Script Capitals", Font.PLAIN, 14)); // Sets Papyrus font
        nameField.setBackground(new Color(245, 222, 179)); // Sets beige background
        nameField.setBorder(BorderFactory.createLineBorder(new Color(139, 69, 19), 1)); // Adds thin brown border
        nameField.setBounds(20, 361, 150, 40); // Positions text box
        nameField.setText("Enter Name Here");
        nameField.addFocusListener(new FocusListener() // Removes "Enter Name Here" when clicked
        {
			@Override
			public void focusGained(FocusEvent e) 
			{
				if(nameField.getText().equals("Enter Name Here")) 
				{
                    nameField.setText("");
                }
			}

			@Override
			public void focusLost(FocusEvent e)
			{
				if(nameField.getText().equals(""))
				{
					nameField.setText("Enter Name Here");
				}
			}
		});
        visualPanel.add(nameField); // Adds text box to panel

        JButton sousChefButton = createStyledButton("Sous Chef"); // Button for Sous Chef class
        JButton pastryArtistButton = createStyledButton("Pastry Artist"); // Button for Pastry Artist
        JButton grillMasterButton = createStyledButton("Grill Master"); // Button for Grill Master
        JButton confirmButton = createStyledButton("Confirm");
        confirmButton.setEnabled(false); // Disabled until a class is previewed
        
        sousChefButton.setBounds(170, 361, 150, 40);
        pastryArtistButton.setBounds(320, 361, 150, 40);
        grillMasterButton.setBounds(470, 361, 150, 40);
        confirmButton.setBounds(620, 361, 150, 40); // Positioned after name field when enabled
        
        sousChefButton.addActionListener(e -> {
            selectedClass = "Sous Chef";
            textArea.setText("Sous Chef Preview:\n");
            textArea.append("Stats: Precision: 5, Stamina: 5, Creativity: 5, Flavor Sense: 5, Speed: 5\n");
            textArea.append("Benefits: Balanced stats for versatility in all cooking challenges.\n");
            textArea.append("A master of balance, the Sous Chef thrives in any kitchen, juggling tasks with ease.\n");
            textArea.append("Click Confirm to choose this class!\n");
            visualPanel.remove(confirmButton); // Remove old confirm button if present
            confirmButton.setEnabled(true);
            visualPanel.add(confirmButton);
            visualPanel.revalidate();
            visualPanel.repaint();
        });

        pastryArtistButton.addActionListener(e -> {
            selectedClass = "Pastry Artist";
            textArea.setText("Pastry Artist Preview:\n");
            textArea.append("Stats: Precision: 7, Stamina: 3, Creativity: 8, Flavor Sense: 5, Speed: 4\n");
            textArea.append("Benefits: High precision and creativity, but low health for crafting stunning dishes.\n");
            textArea.append("With a flair for the dramatic, the Pastry Artist turns sugar into art.\n");
            textArea.append("Click Confirm to choose this class!\n");
            visualPanel.remove(confirmButton);
            confirmButton.setEnabled(true);
            visualPanel.add(confirmButton);
            visualPanel.revalidate();
            visualPanel.repaint();
        });

        grillMasterButton.addActionListener(e -> {
            selectedClass = "Grill Master";
            textArea.setText("Grill Master Preview:\n");
            textArea.append("Stats: Precision: 4, Stamina: 8, Creativity: 5, Flavor Sense: 3, Speed: 7\n");
            textArea.append("Benefits: High stamina but low speed for enduring tough cooking battles.\n");
            textArea.append("Forged in fire, the Grill Master commands the flames with bold confidence.\n");
            textArea.append("Click Confirm to choose this class!\n");
            visualPanel.remove(confirmButton);
            confirmButton.setEnabled(true);
            visualPanel.add(confirmButton);
            visualPanel.revalidate();
            visualPanel.repaint();
        });

        confirmButton.addActionListener(e -> {
            if (selectedClass != null) 
            {
                String name = nameField.getText().isEmpty() ? "Chef" : nameField.getText();
                player = new Player(name, selectedClass);
                textArea.append("\nYou picked " + selectedClass + "! Welcome to the kitchen, " + name + "!\n");
                SwingUtilities.invokeLater(this::showTutorial); // Safely shows main menu on UI thread
            }
        });
        
        visualPanel.add(sousChefButton); // Adds buttons to panel
        visualPanel.add(pastryArtistButton);
        visualPanel.add(grillMasterButton);

        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }

    
    private void showTutorial() 
    {
    	visualPanel.removeAll(); // Clears panel
    	showCarby();
    	textArea.setText("Carby: ");
    	textArea.append("So, today's your first day on the job.\n");
    	textArea.append("Don't think I'm going to be going easy on you just because of that - a  job's a job.\n");
    	textArea.append("Have you ever worked in a food truck before?\n");
    	JButton noButton = createStyledButton("No"); // Combat button
    	JButton yesButton = createStyledButton("Yes"); // Combat button
    	JButton whatButton = createStyledButton("What's a 'food truck'?"); // Combat button

        noButton.addActionListener(e -> {
        textArea.append("Okay then, I guess I have a lot more to teach you than I previously thought.\n");
    	textArea.append("But that's okay. I've got all the time in the world\n");
    	showMainMenuAfterDelay();
        });
        
        yesButton.addActionListener(e -> {
        textArea.append("Doesn't matter; I'm going to be teaching you everything from scratch anyway.\n");
    	textArea.append("You'll see that this one is very different from the other ones you've worked at before.\n");
    	showMainMenuAfterDelay();
        });
        
        whatButton.addActionListener(e -> {
        textArea.append("...\n");
    	textArea.append(".....\n");
    	textArea.append("Anyway, yeah... I guess I'll show you the ropes.\n");
    	showMainMenuAfterDelay();
        });
        
    	noButton.setBounds(20, 361, 150, 40); // Positions buttons
    	yesButton.setBounds(170, 361, 150, 40); // Positions buttons
    	whatButton.setBounds(320, 361, 150, 40); // Positions buttons

        visualPanel.add(noButton); // Adds buttons to panel
        visualPanel.add(yesButton); // Adds buttons to panel
        visualPanel.add(whatButton); // Adds buttons to panel
        
        
        
        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }
    /**
     * Delays showing the main menu by 0.5 seconds for a smooth transition.
     */
    private void showMainMenuAfterDelay() 
    {
        new Thread(() -> // Creates a new thread (runs separately so UI doesn’t freeze)
        { 
            try // Try block: Attempts to pause (might fail if interrupted)
            { 
                Thread.sleep(2500); // Pauses for 1500 milliseconds (1.5 seconds)
                SwingUtilities.invokeLater(this::showMainMenu); // Safely shows main menu on UI thread
            } 
            catch (InterruptedException e) // Catches interruption error
            { 
                Thread.currentThread().interrupt(); // Restores interrupted status
            }
        }).start(); // Starts the thread
    }

    /**
     * Displays the main menu with game options.
     */
    private void showMainMenu() 
    {
        visualPanel.removeAll(); // Clears panel
        textArea.setText("Main Menu\nLevel: " + player.getLevel() + " | Exp: " + player.getExperience() + "/" + player.getExpToLevel() + "\n");
        setReturnButtonVisible(false); // Hide return button on main menu since it's the root screen
        textArea.append("HP: " + player.getHP() + "/" + player.getMaxHP() + " | Battles Fought: " + battleCount + "\n");


        JButton combatButton = createStyledButton("Cook"); // Combat button
        JButton talkButton = createStyledButton("Talk to Carby"); // Talk button
        JButton statsButton = createStyledButton("View Stats"); // Stats button
        JButton restButton = createStyledButton("Rest"); // Rest button
        JButton cityButton = createStyledButton("Enter City"); // Exit button

        combatButton.addActionListener(e -> startCombat()); // Starts combat
        talkButton.addActionListener(e -> talkToCarby()); // Talks to Carby
        statsButton.addActionListener(e -> showStats()); // Shows stats
        restButton.addActionListener(e -> // Restores HP
        { 
            player.restoreHealth(player.getMaxHP() - player.getHP());
            textArea.append("You rested and feel refreshed!\n");
            showMainMenuAfterDelay();
        });
        cityButton.addActionListener(e -> {
            if (battleCount >= 3) // Double check in case somehow enabled prematurely
            { 
                showCity();
            }
        });

        combatButton.setBounds(20, 361, 150, 40); // Positions buttons
        talkButton.setBounds(170, 361, 150, 40);
        statsButton.setBounds(320, 361, 150, 40);
        restButton.setBounds(470, 361, 150, 40);
        cityButton.setBounds(620, 361, 150, 40);
        
        visualPanel.add(combatButton); // Adds buttons to panel
        visualPanel.add(talkButton);
        visualPanel.add(statsButton);
        visualPanel.add(restButton);
        visualPanel.add(cityButton);
        
        if (player.getLevel() == 1 && player.getExperience() == 0)
        {
        	textArea.removeAll();
        	textArea.append("Here is the main part of the truck where you'll be managing things.\n");
        	textArea.append("Here, click on the 'COOK' button.\n");
        }
        
        
        talkButton.setEnabled(battleCount >= 0); // Only enable if 3+ battles fought
        statsButton.setEnabled(battleCount >= 1); // Only enable if 3+ battles fought
        restButton.setEnabled(battleCount >= 2); // Only enable if 3+ battles fought
        cityButton.setEnabled(battleCount >= 3); // Only enable if 3+ battles fought

        // Style the disabled button differently
        if (!statsButton.isEnabled()) 
        {
            statsButton.setBackground(new Color(100, 60, 30)); // Darker brown when disabled
            statsButton.setForeground(new Color(180, 180, 180)); // Grey text when disabled
            statsButton.setToolTipText("Follow the Tutorial to progress!"); // Add tooltip
        }
        if (!restButton.isEnabled()) 
        {
        	restButton.setBackground(new Color(100, 60, 30)); // Darker brown when disabled
        	restButton.setForeground(new Color(180, 180, 180)); // Grey text when disabled
        	restButton.setToolTipText("Follow the Tutorial to progress!"); // Add tooltip
        }
        if (!cityButton.isEnabled()) 
        {
            cityButton.setBackground(new Color(100, 60, 30)); // Darker brown when disabled
            cityButton.setForeground(new Color(180, 180, 180)); // Grey text when disabled
            statsButton.setToolTipText("Follow the Tutorial to progress!"); // Add tooltip
        }
        

        if (battleCount >= 3) 
        {
            textArea.append("\nThe city gates are now open to you!\n");
        } 
        else 
        {
            textArea.append("\nFight " + (3 - battleCount) + " more battles to unlock the city\n");
        }


        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }

    /**
     * Displays the player’s stats screen.
     */
    public void showStats() 
    {
        visualPanel.removeAll(); // Clears panel
        textArea.setText("Your Stats:\n"); // Shows player stats
        textArea.append("Name: " + player.getName() + "\n");
        textArea.append("Class: " + player.getClassType() + "\n");
        textArea.append("Level: " + player.getLevel() + " | Exp: " + player.getExperience() + "/" + player.getExpToLevel() + "\n");
        textArea.append("Vigor: " + player.getHP() + "/" + player.getMaxHP() + "\n");
        textArea.append("Precision: " + player.getPrecision() + " (Crit: " + player.getCritChance() + "%)\n");
        textArea.append("Stamina: " + player.getStamina() + "\n");
        textArea.append("Creativity: " + player.getCreativity() + "\n");
        textArea.append("Flavor Sense: " + player.getFlavorSense() + "\n");
        textArea.append("Speed: " + player.getSpeed() + " (Dodge: " + player.getDodgeChance() + "%)\n");

        JButton backButton = createStyledButton("Back"); // Back button
        backButton.addActionListener(e -> showMainMenu()); // Returns to main menu
        backButton.setBounds(20, 361, 150, 40); // Positions button
        visualPanel.add(backButton); // Adds button to panel

        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }

    /**
     * Starts a combat encounter with a random enemy.
     */
    private void startCombat() 
    {
        stopBackgroundMusic(); // Stops normal music
        playCombatMusic("src/resources/combat_music.wav"); // Plays combat music
        Enemy enemy = EnemyFactory.createEnemy(player.getLevel()); // Creates enemy based on player level
        Combat combat = new Combat(player, enemy, visualPanel, textArea, this); // Starts combat
        combat.startBattle(); // Runs the battle
    }

    /**
     * Displays a conversation with Carby, with a challenge option at level 10.
     */
    private void talkToCarby() 
    {
        visualPanel.removeAll(); // Clears panel
        textArea.setText("Carby: "); // Starts Carby’s dialogue
        if (player.getLevel() < 4) // Different messages based on level
        {
            textArea.append("Hey, keep practicing! You’re doing great so far.");
        } 
        else if (player.getLevel() < 10) 
        {
            textArea.append("You’re getting stronger every day. I’m proud of you!");
        } 
        else 
        {
            textArea.append("Wow, you’ve come so far! Ready to test your skills against me?");
        }

        showCarby();

        JButton backButton = createStyledButton("Back"); // Back button
        backButton.addActionListener(e -> showMainMenu()); // Returns to main menu
        backButton.setBounds(20, 361, 150, 40); // Positions button
        visualPanel.add(backButton); // Adds button to panel

        if (player.getLevel() >= 10) // Shows challenge button at level 10
        { 
            JButton challengeButton = createStyledButton("Challenge Carby");
            challengeButton.addActionListener(e -> fightCarby());
            challengeButton.setBounds(50, 100, 150, 40);
            visualPanel.add(challengeButton);
        }

        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }

    /**
     * Starts a combat encounter with Carby as the enemy.
     */
    private void fightCarby() 
    {
        Enemy carby = new Enemy("Chef Carby", 200, 20, 15, "Carby.png") // Creates Carby enemy with base stats
        { 
            @Override
            void scaleStats(int playerLevel) // Overrides to scale stats
            { 
                this.hp += playerLevel * 10; // Increases HP
                this.attackPower += playerLevel * 5; // Increases attack
                this.speed += playerLevel * 2; // Increases speed
            }
        };
        carby.scaleStats(player.getLevel()); // Scales Carby’s stats
        carby.attackTypes = new String[]{"Fiery Flambé", "Perfect Plating"}; // Sets Carby’s attacks

        stopBackgroundMusic(); // Stops normal music
        playCombatMusic("src/resources/combat_music.wav"); // Plays combat music
        Combat combat = new Combat(player, carby, visualPanel, textArea, this); // Starts combat
        combat.startBattle(); // Runs the battle
    }
    
    /**
     * Displays the city where npc interaction takes place.
     */
    private void showCity() 
    {
        visualPanel.removeAll(); // Clears panel
        textArea.setText("The City!/n");
        // Set return button to go back to main menu and make it visible
        setReturnAction(this::showMainMenu);
        setReturnButtonVisible(true);
        textArea.append("Vigor: " + player.getHP() + "/" + player.getMaxHP() + " | Battles Fought: " + battleCount + "\n");

        JButton vendorButton = createStyledButton("Vendor"); // Combat button
        JButton homecookButton = createStyledButton("Home Cook"); // Talk button
        JButton criticButton = createStyledButton("Critic"); // Stats button
        JButton rivalButton = createStyledButton("Rival"); // Rest button
        JButton customerButton = createStyledButton("Customer"); // Exit button
        
        vendorButton.addActionListener(e -> interactWithVendor()); // Starts interaction with Pip
        homecookButton.addActionListener(e -> interactWithHomecook()); // Talks to Carby
        criticButton.addActionListener(e -> interactWithCritic()); // Shows stats
        rivalButton.addActionListener(e -> interactWithRival()); // Shows stats
        customerButton.addActionListener(e -> interactWithCustomer()); // Returns to title
        
        vendorButton.setBounds(20, 361, 150, 40); // Positions buttons
        homecookButton.setBounds(170, 361, 150, 40);
        criticButton.setBounds(320, 361, 150, 40);
        rivalButton.setBounds(470, 361, 150, 40);
        customerButton.setBounds(620, 361, 150, 40);
        
        visualPanel.add(vendorButton); // Adds buttons to panel
        visualPanel.add(homecookButton);
        visualPanel.add(criticButton);
        visualPanel.add(rivalButton);
        visualPanel.add(customerButton);

        //if (battleCount % 3 == 0 && battleCount > 0 || player.getLevel() % 2 == 0 && player.getLevel() > 1)
        //{ 
        //    JButton npcButton = createStyledButton("Interact with NPC");
        //    npcButton.addActionListener(e -> interactWithNPC());
        //    npcButton.setBounds(550, 100, 150, 40);
        //    visualPanel.add(npcButton);
        //}
        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }
    
    private void interactWithVendor()
    {
    	visualPanel.removeAll(); // Clears panel
    	
    	if (player.getLevel() <= 2)
    	{
            textArea.setText("Pip Hamstein:\n"); // Shows player stats
            textArea.append("*GERBIL NOISES*!\n");
            textArea.append("*GERBIL NOISES*!\n");
            textArea.append("\n");
            textArea.append("He presents you... something?");
            applyNPCEffect("Flavor Boost");
    	}
    }
    
    private void interactWithHomecook()
    {
    	visualPanel.removeAll(); // Clears panel
    	
    	if (player.getLevel() <= 2)
    	{
            textArea.setText("Liza Sharuum:\n"); // Shows player stats
            textArea.append("Hello there, dear.\n");
            textArea.append("Sorry, I thought you were one of the mushroom people I've been seeing recently!!\n");
            textArea.append("Here's a mushroom, now go along, shoo!.\n");            
            applyNPCEffect("Creative Spark");
    	}
    	
        // Load and display the critic sprite
        JLabel homecookSprite = new JLabel(); 
        try // Tries to load Enemy image
        { 
            ImageIcon homecookIcon = new ImageIcon("src/resources/homecook.png");
            if (homecookIcon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) // Checks if image loaded
            {
                throw new Exception("Image failed to load"); // Throws error if it fails
            }
            homecookSprite.setIcon(homecookIcon); // Sets image to label
        } 
        catch (Exception e) // Catches error if image fails
        { 
            System.err.println("Enemy image not found at src/resources/homecook.png."); // Prints error
            homecookSprite.setText("Enemy Image Missing"); // Shows text instead
        }

        homecookSprite.setBounds(300, 120, 300, 300); 
        visualPanel.add(homecookSprite);

        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel

    }
    
    private void interactWithCritic()
    {
    	visualPanel.removeAll(); // Clears panel
    	
    	if (player.getLevel() <= 2)
    	{
            textArea.setText("Ghislain Worcestershire:\n"); // Shows player stats
            textArea.append("And who might you be... ah, I recognize you!\n");
            textArea.append("You're that new apprentice that the great Carby recruited!\n");
            textArea.append("Here... a gift.\n");            
            applyNPCEffect("Stamina Boost");
    	}
        // Load and display the critic sprite
        JLabel criticSprite = new JLabel(); 
        try // Tries to load Enemy image
        { 
            ImageIcon criticIcon = new ImageIcon("src/resources/critic.png");
            if (criticIcon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) // Checks if image loaded
            {
                throw new Exception("Image failed to load"); // Throws error if it fails
            }
            criticSprite.setIcon(criticIcon); // Sets image to label
        } 
        catch (Exception e) // Catches error if image fails
        { 
            System.err.println("Enemy image not found at src/resources/critic.png."); // Prints error
            criticSprite.setText("Enemy Image Missing"); // Shows text instead
        }

        criticSprite.setBounds(300, 120, 300, 300); 
        visualPanel.add(criticSprite);

        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }
    
    private void interactWithRival()
    {
    	visualPanel.removeAll(); // Clears panel
    	
    	if (player.getLevel() <= 2)
    	{
            textArea.setText("Aspar Gios:\n"); // Shows player stats
            textArea.append("HEY!\n");
            textArea.append("FRICK YOU, YOU TOOK MAH JOB!\n");
            textArea.append("YOU'RE A SECOND RATE CHEF FOR A THIRD RATE RESTAURANT, MONGREL!!!.\n");
            applyNPCEffect("Critic's Curse");
    	}

    }
    
    private void interactWithCustomer()
    {
    	visualPanel.removeAll(); // Clears panel
    	
    	if (player.getLevel() <= 2)
    	{
            textArea.setText("Lamce Tayk:\n"); // Shows player stats
            textArea.append("DOES ANYONE NOW WHERE I AM, WHY ARE THERE LIVING FOOD THINGS!\n");
            textArea.append("THIS ISN'T MY APARTMENT!\n");
            textArea.append("AHHHHH!!!\n");
            Combat.pause(500);
            applyNPCEffect("Overwhelmed");
    	}
    }


    /**
     * Handles NPC interaction with praise, chat, or insult options.
     */
    private void interactWithNPC() 
    {
        visualPanel.removeAll(); // Clears panel
        
        NPCFactory.NPC npc; // Declares NPC variable
        if (encounteredNPCs.isEmpty() || Math.random() < 0.5) // 50% chance of new NPC
        { 
            npc = NPCFactory.createNPC(); // Creates new NPC
            encounteredNPCs.add(npc); // Adds to list
            textArea.setText("A new face approaches: " + npc.getName() + "!\n");
        } 
        else // Picks existing NPC
        { 
            npc = encounteredNPCs.get((int)(Math.random() * encounteredNPCs.size()));
            textArea.setText("You run into " + npc.getName() + " again!\n");
        }
        textArea.append("Affection: " + npc.getAffectionLevel() + "\n"); // Shows affection level
        textArea.append("What do you say?\n"); // Prompts choice

        JButton praiseButton = createStyledButton("Offer Praise"); // Praise button
        JButton chatButton = createStyledButton("Chat Casually"); // Chat button
        JButton insultButton = createStyledButton("Insult Dish"); // Insult button


        praiseButton.addActionListener(e -> // When praise is clicked
        { 
            npc.interact(3); // Increases affection by 3
            String effect = npc.getEffect(); // Gets effect based on affection
            textArea.append(npc.getName() + " beams at your praise. Affection: " + npc.getAffectionLevel() + "\n");
            textArea.append("Effect: " + effect + "\n");
            applyNPCEffect(effect); // Applies effect
            showMainMenuAfterDelay(); // Returns to menu
        });

        chatButton.addActionListener(e -> // When chat is clicked
        { 
            npc.interact(1); // Increases affection by 1
            textArea.append(npc.getName() + " nods at your words. Affection: " + npc.getAffectionLevel() + "\n");
            textArea.append("Effect: No Effect\n");
            showMainMenuAfterDelay();
        });

        insultButton.addActionListener(e -> // When insult is clicked
        { 
            npc.interact(-3); // Decreases affection by 3
            String effect = npc.getEffect(); // Gets effect
            textArea.append(npc.getName() + " scowls at your insult. Affection: " + npc.getAffectionLevel() + "\n");
            textArea.append("Effect: " + effect + "\n");
            applyNPCEffect(effect); // Applies effect
            showMainMenuAfterDelay();
        });

        praiseButton.setBounds(20, 361, 150, 40); // Positions buttons
        chatButton.setBounds(170, 361, 150, 40);
        insultButton.setBounds(320, 361, 150, 40);
        visualPanel.add(praiseButton); // Adds buttons to panel
        visualPanel.add(chatButton);
        visualPanel.add(insultButton);

        visualPanel.revalidate(); // Updates layout
        visualPanel.repaint(); // Redraws panel
    }
    

    /**
     * Applies buffs or debuffs from NPC interactions to the player.
     * @param effect The effect string from NPC interaction.
     */
    private void applyNPCEffect(String effect) 
    {
        switch (effect) // Checks effect string and applies changes
        { 
            case "Stamina Boost":
                player.setStamina(player.getStamina() + 2); // Boosts stamina
                textArea.append("Your stamina surges! +2 Stamina\n");
                break;
            case "Creative Spark":
                player.setCreativity(player.getCreativity() + 3); // Boosts creativity
                textArea.append("A spark ignites your mind! +3 Creativity\n");
                break;
            case "Speed Rush":
                player.setSpeed(player.getSpeed() + 2); // Boosts speed
                textArea.append("You move like the wind! +2 Speed\n");
                break;
            case "Flavor Boost":
                player.setFlavorSense(player.getFlavorSense() + 2); // Boosts flavor sense
                textArea.append("Your palate sharpens! +2 Flavor Sense\n");
                break;
            case "Burned Fingers":
                player.takeDamage(15); // Deals 15 damage
                textArea.append("Ouch! Your fingers burn, losing 15 HP\n");
                break;
            case "Overwhelmed":
                player.setSpeed(player.getSpeed() - 1); // Reduces speed
                textArea.append("You feel overwhelmed! -1 Speed\n");
                break;
            case "Soggy Dish":
                player.setPrecision(player.getPrecision() - 1); // Reduces precision
                textArea.append("A soggy mess dulls your skill! -1 Precision\n");
                break;
            case "Critic’s Curse":
                player.setCreativity(player.getCreativity() - 2); // Reduces creativity
                textArea.append("Harsh words sap your spirit! -2 Creativity\n");
                break;
            default:
                textArea.append("Nothing happens...\n"); // No effect
        }
    }

    /**
     * Ends combat and returns to the main menu.
     */
    public void endCombat() 
    {
        stopCombatMusic(); // Stops combat music
        playBackgroundMusic("src/resources/normal_music.wav"); // Restarts normal music
        showMainMenu(); // Shows main menu
    }

    /**
     * Increases the battle count after a combat ends.
     */
    public void incrementBattleCount() 
    {
        battleCount++; // Adds 1 to battle count
    }

    /**
     * Plays a one-time sound effect (like a button click).
     * @param soundFile Path to the sound file (e.g., "src/resources/button_click.wav").
     */
    private void playSound(String soundFile) {
        try {
            File file = new File(soundFile);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            
            // Apply volume setting
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log(soundVolume) / Math.log(10.0) * 20.0);
                gainControl.setValue(dB);
            }
            
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + soundFile + " - " + e.getMessage());
        }
    }

    /**
     * Plays looping background music for normal scenes.
     * @param musicFile Path to the music file (e.g., "src/resources/normal_music.wav").
     */
    private void playBackgroundMusic(String musicFile) 
    {
        try // Tries to play music (might fail if file is missing)
        { 
            File file = new File(musicFile); // Creates File object for music
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file); // Prepares audio data
            normalMusicClip = AudioSystem.getClip(); // Assigns Clip for control
            normalMusicClip.open(audioIn); // Loads audio into Clip
            FloatControl gainControl = (FloatControl) normalMusicClip.getControl(FloatControl.Type.MASTER_GAIN); // Gets volume control
            float dB = (float) (Math.log(musicVolume) / Math.log(10.0) * 20.0); // Sets volume to 60% (math converts to decibels)
            gainControl.setValue(dB); // Applies volume
            normalMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Loops music forever
            normalMusicClip.start(); // Starts playing
        } 
        catch (Exception e) // Catches errors (e.g., file not found)
        { 
            System.err.println("Error playing background music: " + musicFile + " - " + e.getMessage()); // Prints error
        }
    }

    /**
     * Plays looping combat music during battles.
     * @param musicFile Path to the combat music file (e.g., "src/resources/combat_music.wav").
     */
    private void playCombatMusic(String musicFile) 
    {
        try // Tries to play combat music
        { 
            File file = new File(musicFile); // Creates File object for music
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file); // Prepares audio data
            combatMusicClip = AudioSystem.getClip(); // Assigns Clip for control
            combatMusicClip.open(audioIn); // Loads audio into Clip
            FloatControl gainControl = (FloatControl) combatMusicClip.getControl(FloatControl.Type.MASTER_GAIN); // Gets volume control
            float dB = (float) (Math.log(musicVolume) / Math.log(10.0) * 20.0); // Sets volume to 60%
            gainControl.setValue(dB); // Applies volume
            combatMusicClip.loop(Clip.LOOP_CONTINUOUSLY); // Loops music forever
            combatMusicClip.start(); // Starts playing
        } 
        catch (Exception e) // Catches errors
        { 
            System.err.println("Error playing combat music: " + musicFile + " - " + e.getMessage()); // Prints error
        }
    }

    /**
     * Stops the background music when switching scenes (e.g., to combat).
     */
    private void stopBackgroundMusic() 
    {
        if (normalMusicClip != null && normalMusicClip.isRunning()) // Checks if music is loaded and playing
        { 
            normalMusicClip.stop(); // Stops music
            normalMusicClip.close(); // Frees resources
        }
    }

    /**
     * Stops the combat music when the battle ends.
     */
    private void stopCombatMusic() 
    {
        if (combatMusicClip != null && combatMusicClip.isRunning()) // Checks if combat music is loaded and playing
        { 
            combatMusicClip.stop(); // Stops music
            combatMusicClip.close(); // Frees resources
        }
    }
    
    /**
     * Sets the action for the permanent return button
     * @param action The action to perform when clicked
     */
    public void setReturnAction(Runnable action) 
    {
        for (ActionListener al : returnButton.getActionListeners()) 
        {
            returnButton.removeActionListener(al);
        }
        returnButton.addActionListener(e -> action.run());
    }
    
    /**
     * Shows or hides the return button
     * @param visible Whether the button should be visible
     */
    public void setReturnButtonVisible(boolean visible) 
    {
        returnButton.setVisible(visible);
    }
    
    /**
     * Shows the settings dialog with volume controls.
     */
    private void showSettingsDialog() {
        // Create the dialog window
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setSize(400, 350);
        settingsDialog.setLayout(new BorderLayout());
        settingsDialog.setLocationRelativeTo(this); // Center on main window
        
        // Create panel for settings
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new GridLayout(4, 1, 10, 10));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        settingsPanel.setBackground(new Color(245, 222, 179));
        
        // Music volume slider
        JLabel musicLabel = new JLabel("Music Volume:");
        musicLabel.setFont(new Font("Matura MT Script Capitals", Font.PLAIN, 16));
        
        JSlider musicSlider = new JSlider(0, 100, (int)(musicVolume * 100));
        musicSlider.setMajorTickSpacing(25);
        musicSlider.setPaintTicks(true);
        musicSlider.setPaintLabels(true);
        musicSlider.addChangeListener(e -> {
            musicVolume = musicSlider.getValue() / 100f;
            updateMusicVolume();
        });
        
        // Sound effects volume slider
        JLabel soundLabel = new JLabel("Sound Effects Volume:");
        soundLabel.setFont(new Font("Matura MT Script Capitals", Font.PLAIN, 16));
        
        JSlider soundSlider = new JSlider(0, 100, (int)(soundVolume * 100));
        soundSlider.setMajorTickSpacing(25);
        soundSlider.setPaintTicks(true);
        soundSlider.setPaintLabels(true);
        soundSlider.addChangeListener(e -> {
            soundVolume = soundSlider.getValue() / 100f;
        });
        
        // Add components to panel
        settingsPanel.add(musicLabel);
        settingsPanel.add(musicSlider);
        settingsPanel.add(soundLabel);
        settingsPanel.add(soundSlider);
        
        // Close button
        JButton closeButton = createStyledButton("Close");
        closeButton.addActionListener(e -> settingsDialog.dispose());
        
        // Add to dialog
        settingsDialog.add(settingsPanel, BorderLayout.CENTER);
        settingsDialog.add(closeButton, BorderLayout.SOUTH);
        
        settingsDialog.setVisible(true);
    }
    
    /**
     * Updates the volume of currently playing music.
     */
    private void updateMusicVolume() {
        if (normalMusicClip != null && normalMusicClip.isRunning()) {
            FloatControl gainControl = (FloatControl) normalMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(musicVolume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
        if (combatMusicClip != null && combatMusicClip.isRunning()) {
            FloatControl gainControl = (FloatControl) combatMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = (float) (Math.log(musicVolume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }

    private void showCarby()
    {
        JLabel carbyLabel = new JLabel(); // Creates label for Carby’s image
        try // Tries to load Carby’s image
        { 
            ImageIcon carbyIcon = new ImageIcon("src/resources/carby.png");
            if (carbyIcon.getImageLoadStatus() != java.awt.MediaTracker.COMPLETE) // Checks if image loaded
            {
                throw new Exception("Image failed to load"); // Throws error if it fails
            }
            carbyLabel.setIcon(carbyIcon); // Sets image to label
        } 
        catch (Exception e) // Catches error if image fails
        { 
            System.err.println("Carby image not found at src/resources/carby.png."); // Prints error
            carbyLabel.setText("Carby Image Missing"); // Shows text instead
        }
        carbyLabel.setBounds(550, 150, 256, 256); // Positions image
        visualPanel.add(carbyLabel); // Adds image to panel

    }

    /**
     * Main method: Starting point of the program.
     * @param args Command-line arguments (not used here).
     */
    public static void main(String[] args) 
    {
        new GameWindow(); // Creates new GameWindow object, running constructor to start game
    }
} // Closes the GameWindow class