package finalLiang;

import java.io.File;
import java.util.Random;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

public class GameLogic extends Application{
	
	// Game UI
	BorderPane root;     // the whole window
	Scene scene;   // the whole scene
	VBox score_pane; // the score pane
	Label score_label; // the score label
	GridPane world_pane;  // pane that contains all the cells
	
	// Game Object
	Random randy;  // random number generator 
	Cell[][] worlds;
    final String BGM = "src/finalLiang/bgm.wav";
    final String MUTE = "src/finalLiang/mute.png";
    final String UNMUTE = "src/finalLiang/unmute.png";
	
	
	// Game Varibale
	final int SIZE = 4; // size of the board
	enum Type {EMPTY, NUM_2, NUM_4, NUM_8, NUM_16, NUM_32, NUM_64,
		       NUM_128, NUM_256, NUM_512, NUM_1024, NUM_2048};
    final int UNIT_TIME = 100; // milliseconds
    int score = 0;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void start(Stage stage) {
		root = new BorderPane();
		scene = new Scene(root, 500,600);
		stage.setTitle("2048");
		//stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest( e->{Platform.exit(); System.exit(0);} );
		
		randy = new Random(System.currentTimeMillis());
		initUI();
	}
	
	public void initUI() {
		// 1. Set the background of UI
		root.setBackground(new Background(new BackgroundFill(Color.web("#faf8ef"), CornerRadii.EMPTY, Insets.EMPTY)));
		
		
		// 2. Setup the upper board
		
		Pane upper = new Pane();
		
		// Big Title
		Label big_title = new Label("2048");
		big_title.setFont(new Font("Lucida Sans Unicode", 60));
		big_title.setTextFill(Color.web("#776e65"));
		big_title.setLayoutX(55);
		big_title.setLayoutY(20);
		// Sub Title
		Label sub_title = new Label("Join the tiles, get to 2048!");
		sub_title.setFont(new Font("Lucida Sans Unicode", 20));
		sub_title.setTextFill(Color.web("#776e65"));
		sub_title.setLayoutX(55);
		sub_title.setLayoutY(100);
		
		// Score Pane and Label
		score_pane = new VBox();
		score_pane.setBackground( new Background(new BackgroundFill(Color.web("#bbada0"), CornerRadii.EMPTY, Insets.EMPTY)));
		score_pane.setPrefSize(100, 60);
		score_pane.setLayoutX(345);
		score_pane.setLayoutY(20);
		
		Label score_text_label = new Label("SCORE");
		score_text_label.setFont(new Font("Lucida Sans Unicode", 15));
		score_text_label.setTextFill(Color.WHITE);
		
		score = 0;
		score_label = new Label(""+score);
		score_label.setFont(new Font("Lucida Sans Unicode", 20));
		score_label.setTextFill(Color.WHITE);
		
		score_pane.setAlignment(Pos.CENTER);
		score_pane.getChildren().add(score_text_label);
		score_pane.getChildren().add(score_label);
		
		// New Game Button
		Button new_game_btn = new Button("New Game");
		new_game_btn.setOnAction(e->{restart();});
		new_game_btn.setPrefSize(100, 30);
		new_game_btn.setLayoutX(345);
		new_game_btn.setLayoutY(100);
		new_game_btn.setFocusTraversable(false);
		new_game_btn.setStyle("-fx-background-color:#8f7a66; -fx-font-size:14; -fx-text-fill: #FFFFFF");
		
		// Total Upper Board Setup
		//upper.setBackground( new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
		upper.setPrefHeight(170);
		upper.getChildren().add(big_title);
		upper.getChildren().add(sub_title);
		upper.getChildren().add(score_pane);
		upper.getChildren().add(new_game_btn);
		
		root.setTop(upper);
		
		
		// Lower Board
		world_pane = new GridPane();
		worlds = new Cell[SIZE][SIZE];
		for(int i=0; i<SIZE; ++i) {     // i: row_index
			for(int j=0; j<SIZE; ++j) {  // j: col_index
				worlds[i][j] = new Cell(i, j, Type.EMPTY);
				world_pane.add(worlds[i][j], j, i);
			}
		}
		world_pane.setPadding(new Insets(10, 0, 0, 10)); 
		world_pane.setHgap(10);
		world_pane.setVgap(10);
		world_pane.setPrefSize(400, 400);
		world_pane.setBackground(new Background(new BackgroundFill( Color.web("#bbad9f"), CornerRadii.EMPTY, Insets.EMPTY)));
		root.setCenter(world_pane);
		BorderPane.setAlignment(world_pane, Pos.CENTER);
		
		// Set the left/right/bottom side
		Pane left = new Pane(); left.setBackground(new Background(new BackgroundFill( Color.web("#faf8ef"), CornerRadii.EMPTY, Insets.EMPTY)));
		Pane right = new Pane();right.setBackground(new Background(new BackgroundFill( Color.web("#faf8ef"), CornerRadii.EMPTY, Insets.EMPTY)));
		Pane bottom = new Pane();bottom.setBackground(new Background(new BackgroundFill( Color.web("#faf8ef"), CornerRadii.EMPTY, Insets.EMPTY)));
		left.setPrefWidth(55);
		right.setPrefWidth(55);
		bottom.setPrefHeight(40);
		root.setLeft(left);
		root.setRight(right);
		root.setBottom(bottom);
		
		// Play the music
		File bgmFile = new File(BGM);
		String realAddress  = bgmFile.toURI().toString();
		//System.out.println("About to play: " + realAddress);
		Media bgm = new Media(realAddress);
		MediaPlayer bgm_player = new MediaPlayer(bgm);
		bgm_player.setVolume(0.9);
		bgm_player.setOnEndOfMedia(new Runnable() {
	       public void run() {
	    	   bgm_player.seek(Duration.ZERO);
	       }
		});
		
		File muteFile = new File(MUTE);
		File unmuteFile = new File(UNMUTE);
		String muteAddress  = muteFile.toURI().toString();
		String unmuteAddress  = unmuteFile.toURI().toString();
		
		ImageView mute = new ImageView(unmuteAddress);
		ImageView unmute = new ImageView(muteAddress);
		mute.setPreserveRatio(true); unmute.setPreserveRatio(true);
		mute.setFitHeight(30);unmute.setFitHeight(30);
		mute.setLayoutX(450);unmute.setLayoutX(450);
		// allows click on transparent areas
		mute.setPickOnBounds(true);unmute.setPickOnBounds(true);
	    
        mute.setOnMousePressed((MouseEvent e) -> {
            //System.out.println("clicked on mute!");
            bgm_player.pause();
            // Hide itself and let unmute button appears
            mute.setVisible(false);  mute.setDisable(true);
            unmute.setVisible(true);  unmute.setDisable(false);
        });
        unmute.setOnMousePressed((MouseEvent e) -> {
            //System.out.println("clicked on unmute!");
            bgm_player.play();
            // Hide itself and let mute button appears
            unmute.setVisible(false);  unmute.setDisable(true);
            mute.setVisible(true);  mute.setDisable(false);
        });
        // Add the two buttons to the pane, but at this time, only display unmute
        bottom.getChildren().add(mute);
        bottom.getChildren().add(unmute);
	    // Hide mute and let unmute button appears
	    mute.setVisible(false);  mute.setDisable(true);
        unmute.setVisible(true);  unmute.setDisable(false);

		// Start the Game
		gameStart();
		addEventHandler();
	}
	
	// Happens when game start
	public void gameStart() {
		// generate 2 new tiles
		for(int i=0; i<2; i++) {
			randomGenerateNew();
		}
		
	}
	
	public void addEventHandler() {
		scene.setOnKeyPressed( m -> {
		    if (m.getCode() == KeyCode.LEFT || m.getCode() == KeyCode.A ) {
		           //System.out.println("left");
		    	   handleLEFT();
				   //randomGenerateNew();
		    }
		    else if (m.getCode() == KeyCode.RIGHT || m.getCode() == KeyCode.D ) {
		           //System.out.println("right");
		    	   handleRIGHT();
				   //randomGenerateNew();
		    }
		    else if (m.getCode() == KeyCode.UP || m.getCode() == KeyCode.W ) {
		           //System.out.println("up");
		    	   handleUP();
		    	   //randomGenerateNew();
		    }
		    else if (m.getCode() == KeyCode.DOWN || m.getCode() == KeyCode.S ) {
		           //System.out.println("down");
				   handleDOWN();
				   //randomGenerateNew();
		    }
		});
	}
	
	// Helper Function for restart the game
	public void regenerateGamePane(){
		
		// Clean the score
		score = 0;
		score_label.setText(""+score);
		
		// Renerate World Pane
		world_pane = new GridPane();
		worlds = new Cell[SIZE][SIZE];
		for(int i=0; i<SIZE; ++i) {     // i: row_index
			for(int j=0; j<SIZE; ++j) {  // j: col_index
				worlds[i][j] = new Cell(i, j, Type.EMPTY);
				world_pane.add(worlds[i][j], j, i);
			}
		}
		world_pane.setPadding(new Insets(10, 0, 0, 10)); 
		world_pane.setHgap(10);
		world_pane.setVgap(10);
		world_pane.setPrefSize(400, 400);
		world_pane.setBackground(new Background(new BackgroundFill( Color.web("#bbad9f"), CornerRadii.EMPTY, Insets.EMPTY)));
		root.setCenter(world_pane);
		BorderPane.setAlignment(world_pane, Pos.CENTER);
		
		// Start the game again
		gameStart();
		addEventHandler();
	}
	
	// Handler Functions
	// 1. Handle DOWN
	public void handleDOWN(){
		
		// Total Transition
		ParallelTransition total = new ParallelTransition();
		
		for(int j=0; j< SIZE; ++j) { // col number: 0 to 3
			
			// Prepare for the parallel transition
			ParallelTransition pt = new ParallelTransition();
			
			for(int i=SIZE-1; i>=0; --i) { // row number: 3 to 0
				// if this cell is empty, we do nothing
				 if(worlds[i][j].isEmpty()) continue;
				 
				 // If it contains numbers
				 // First, we check whether it can move downward
				 // Note that we only consider merge after the tile can not move anymore
				 int row_number = i;
				 Type type_copy = worlds[i][j].getType();
				 while(row_number < 3 && worlds[row_number+1][j].isEmpty()) {
					 // If there is an empty cell below it
					 // We move the current cell downward
					 Type current_type = worlds[row_number][j].getType();
					 worlds[row_number][j].becomeEmpty();
					 worlds[row_number+1][j].partial_become(current_type);
					 row_number++;
				 }
				 // At this time, this tile can not move downward anymore
				 // We check whether it can merge with the tile below
				 
				 // Check merge condition
				 // Here we should use row_number because it is the current position
				 if(row_number < 3 
				    && !worlds[row_number+1][j].isEmpty() 
				    && worlds[row_number][j].getType() == worlds[row_number+1][j].getType() ) {
					// We should merge these two tiles 
					 worlds[row_number][j].becomeEmpty();
					 worlds[row_number+1][j].partial_double_up();
					 row_number++;
				 }
				 
				 // At this point, the row_number is final position
				 // If the cell is not empty and moves(including merges),
				 // Then we create an animation
				 if(row_number > i) {
					 int move_length = row_number - i;
					 // Create the movement
					 Cell copy = new Cell(-1,-1, type_copy);
					 world_pane.add(copy, j, i);
					 
					 // Tranlate Transition
					 TranslateTransition tt = new TranslateTransition();
					 tt.setNode(copy);
					 tt.setToY(95*move_length);
					 tt.setDuration( new Duration(UNIT_TIME*move_length));
				     tt.setCycleCount(1);
				     int row_num_copy = row_number;
				     int j_copy = j;
				     tt.setOnFinished(e->{
					    	world_pane.getChildren().remove(copy);
					    	worlds[row_num_copy][j_copy].update();
					 });
				     // Fade Transition
				     FadeTransition ft = new FadeTransition();
					 ft.setNode(copy);
				     ft.setDuration(new Duration(UNIT_TIME*move_length));
				     ft.setFromValue(1.0);
				     ft.setToValue(0.0);
				     ft.setAutoReverse(false);
				     ft.setCycleCount(1);
				     // Add this TranslateTransition to the ParallelTransition
				     pt.getChildren().add(ft);
				     pt.getChildren().add(tt);
				 }
				 
			}
			// At this point, we finish all the tiles in a single column
			// Now we want to fire the parallel transition
			//pt.playFromStart();
			total.getChildren().add(pt);
		}
		total.setOnFinished(e->{
			randomGenerateNew();
		});
		total.playFromStart();
	}
	
	// 2. Handle UP
	public void handleUP(){
		
		// Total Transition
	    ParallelTransition total = new ParallelTransition();
	    
		for(int j=0; j< SIZE; ++j) { // col number: 0 to 3
			
			// Prepare for the parallel transition
			ParallelTransition pt = new ParallelTransition();
			
			for(int i=0; i<SIZE; ++i) { // row number: 0 to 3
				// if this cell is empty, we do nothing
				 if(worlds[i][j].isEmpty()) continue;
				 
				 // If it contains numbers
				 // First, we check whether it can move upward
				 // Note that we only consider merge after the tile can not move anymore
				 int row_number = i;
				 Type type_copy = worlds[i][j].getType();
				 while(row_number > 0 && worlds[row_number-1][j].isEmpty()) {
					 // If there is an empty cell above it
					 // We move the current cell upward
					 Type current_type = worlds[row_number][j].getType();
					 worlds[row_number][j].becomeEmpty();
					 worlds[row_number-1][j].partial_become(current_type);
					 row_number--;
				 }
				 // At this time, this tile can not move upward anymore
				 // We check whether it can merge with the tile above
				 
				 // Check merge condition
				 // Here we should use row_number because it is the current position
				 if(row_number > 0
				    && !worlds[row_number-1][j].isEmpty() 
				    && worlds[row_number][j].getType() == worlds[row_number-1][j].getType() ) {
					// We should merge these two tiles 
					 worlds[row_number][j].becomeEmpty();
					 worlds[row_number-1][j].partial_double_up();
					 row_number--;
				 }
				 
				 // At this point, the row_number is final position
				 // If the cell is not empty and moves(including merges),
				 // Then we create an animation
				 if(row_number < i) {
					 int move_length = i - row_number;
					 // Create the movement
					 Cell copy = new Cell(-1,-1, type_copy);
					 world_pane.add(copy, j, i);
					 
					 // Translate Transition
					 TranslateTransition tt = new TranslateTransition();
					 tt.setNode(copy);
					 tt.setToY(-95*move_length);
					 tt.setDuration( new Duration(UNIT_TIME*move_length));
				     tt.setCycleCount(1);
				     int row_num_copy = row_number;
				     int j_copy = j;
				     tt.setOnFinished(e->{
					    	world_pane.getChildren().remove(copy);
					    	worlds[row_num_copy][j_copy].update();
					 });
				     // Fade Transition  
				     FadeTransition ft = new FadeTransition();
					 ft.setNode(copy);
				     ft.setDuration(new Duration(UNIT_TIME*move_length));
				     ft.setFromValue(1.0);
				     ft.setToValue(0.0);
				     ft.setAutoReverse(false);
				     ft.setCycleCount(1);
				     // Add this TranslateTransition to the ParallelTransition
				     pt.getChildren().add(ft);
				     pt.getChildren().add(tt);
				 }
				 
			}
			// At this point, we finish all the tiles in a single column
			// Now we want to fire the parallel transition
			// pt.playFromStart();
			total.getChildren().add(pt);
		}
		total.setOnFinished(e->{
			randomGenerateNew();
		});
		total.playFromStart();
	}
	// 3. Handle LEFT
	public void handleLEFT(){
		
		// Total Transition
	    ParallelTransition total = new ParallelTransition();
		
		for(int i=0; i< SIZE; ++i) { // row number: 0 to 3
			
			// Prepare for the parallel transition
			ParallelTransition pt = new ParallelTransition();
			
			for(int j=0; j<SIZE; ++j) { // col number: 0 to 3
				// if this cell is empty, we do nothing
				 if(worlds[i][j].isEmpty()) continue;
				 
				 // If it contains numbers
				 // First, we check whether it can move leftward
				 // Note that we only consider merge after the tile can not move anymore
				 int col_number = j;
				 Type type_copy = worlds[i][j].getType();
				 while(col_number > 0 && worlds[i][col_number-1].isEmpty()) {
					 // If there is an empty cell on the LHS
					 // We move the current cell leftward
					 Type current_type = worlds[i][col_number].getType();
					 worlds[i][col_number].becomeEmpty();
					 worlds[i][col_number-1].partial_become(current_type);
					 col_number--;
				 }
				 // At this time, this tile can not move leftward anymore
				 // We check whether it can merge with the tile on LHS
				 
				 // Check merge condition
				 // Here we should use row_number because it is the current position
				 if(col_number > 0
				    && !worlds[i][col_number-1].isEmpty() 
				    && worlds[i][col_number].getType() == worlds[i][col_number-1].getType() ) {
					// We should merge these two tiles 
					 worlds[i][col_number].becomeEmpty();
					 worlds[i][col_number-1].partial_double_up();
					 col_number--;
				 }
				 
				 // At this point, the row_number is final position
				 // If the cell is not empty and moves(including merges),
				 // Then we create an animation
				 if(col_number < j) {
					 int move_length = j - col_number;
					 // Create the movement
					 Cell copy = new Cell(-1,-1, type_copy);
					 world_pane.add(copy, j, i);
					 
					 // Translate Transition
					 TranslateTransition tt = new TranslateTransition();
					 tt.setNode(copy);
					 tt.setToX(-95*move_length);
					 tt.setDuration( new Duration(UNIT_TIME*move_length));
				     tt.setCycleCount(1);
				     int col_num_copy = col_number;
				     int i_copy = i;
				     tt.setOnFinished(e->{
					    	world_pane.getChildren().remove(copy);
					    	worlds[i_copy][col_num_copy].update();
					 });
				     // Fade Transition
				     FadeTransition ft = new FadeTransition();
					 ft.setNode(copy);
				     ft.setDuration(new Duration(UNIT_TIME*move_length));
				     ft.setFromValue(1.0);
				     ft.setToValue(0.0);
				     ft.setAutoReverse(false);
				     ft.setCycleCount(1);
				     // Add this TranslateTransition to the ParallelTransition
				     pt.getChildren().add(ft);
				     pt.getChildren().add(tt);
				 }
				 
			}
			// At this point, we finish all the tiles in a single column
			// Now we want to fire the parallel transition
			// pt.playFromStart();
			total.getChildren().add(pt);
		}
		total.setOnFinished(e->{
			randomGenerateNew();
		});
		total.playFromStart();
	}
	// 4. Handle RIGHT
	public void handleRIGHT(){
		
		// Total Transition
	    ParallelTransition total = new ParallelTransition();
		
		for(int i=0; i< SIZE; ++i) { // row number: 0 to 3
			
			// Prepare for the parallel transition
			ParallelTransition pt = new ParallelTransition();
			
			for(int j=SIZE-1; j>=0; --j) { // col number: 3 to 0
				// if this cell is empty, we do nothing
				 if(worlds[i][j].isEmpty()) continue;
				 
				 // If it contains numbers
				 // First, we check whether it can move rightward
				 // Note that we only consider merge after the tile can not move anymore
				 int col_number = j;
				 Type type_copy = worlds[i][j].getType();
				 while(col_number < 3 && worlds[i][col_number+1].isEmpty()) {
					 // If there is an empty cell on the RHS
					 // We move the current cell rightward
					 Type current_type = worlds[i][col_number].getType();
					 worlds[i][col_number].becomeEmpty();
					 worlds[i][col_number+1].partial_become(current_type);
					 col_number++;
				 }
				 // At this time, this tile can not move rightward anymore
				 // We check whether it can merge with the tile on RHS
				 
				 // Check merge condition
				 // Here we should use row_number because it is the current position
				 if(col_number < 3
				    && !worlds[i][col_number+1].isEmpty() 
				    && worlds[i][col_number].getType() == worlds[i][col_number+1].getType() ) {
					// We should merge these two tiles 
					 worlds[i][col_number].becomeEmpty();
					 worlds[i][col_number+1].partial_double_up();
					 col_number++;
				 }
				 
				 // At this point, the row_number is final position
				 // If the cell is not empty and moves(including merges),
				 // Then we create an animation
				 if(col_number > j) {
					 int move_length = col_number - j;
					 // Create the movement
					 Cell copy = new Cell(-1,-1, type_copy);
					 world_pane.add(copy, j, i);
					 
					 // Translate Transition
					 TranslateTransition tt = new TranslateTransition();
					 tt.setNode(copy);
					 tt.setToX(95*move_length);
					 tt.setDuration( new Duration(UNIT_TIME*move_length));
				     tt.setCycleCount(1);
				     int col_num_copy = col_number;
				     int i_copy = i;
				     tt.setOnFinished(e->{
					    	world_pane.getChildren().remove(copy);
					    	worlds[i_copy][col_num_copy].update();
					 });
				     // Fade Transition
				     FadeTransition ft = new FadeTransition();
					 ft.setNode(copy);
				     ft.setDuration(new Duration(UNIT_TIME*move_length));
				     ft.setFromValue(1.0);
				     ft.setToValue(0.0);
				     ft.setAutoReverse(false);
				     ft.setCycleCount(1);
				     // Add this TranslateTransition to the ParallelTransition
				     pt.getChildren().add(ft);
				     pt.getChildren().add(tt);
				 }
				 
			}
			// At this point, we finish all the tiles in a single column
			// Now we want to fire the parallel transition
			// pt.playFromStart();
			total.getChildren().add(pt);
		}
		total.setOnFinished(e->{
			randomGenerateNew();
		});
		total.playFromStart();
	}
	
	
	// Helper Function 1
	public void restart() {
		//System.out.println("Start a new game!");
		
		// First, make all cells become empty
		for(int i=0; i<SIZE; ++i) {     // i: row_index
			for(int j=0; j<SIZE; ++j) {  // j: col_index
				worlds[i][j].becomeEmpty();
				worlds[i][j].update();
			}
		}
		// Then, create two 2s
		for(int i=0; i<2; i++) {
			randomGenerateNew();
		}
		// Finally, clean the score
		score = 0;
		score_label.setText(""+score);
	}
	
	
	// Helper Function 2
	// To generate a new 2 in an empty cell
	public void randomGenerateNew() {
		int numberOfEmpty = 0;
		for(int i=0; i<SIZE; ++i) {     // i: row_index
			for(int j=0; j<SIZE; ++j) {  // j: col_index
				if(worlds[i][j].isEmpty()) ++numberOfEmpty;
			}
		}
		if(numberOfEmpty == 0) {
			checkGameOver();
			return;
		}
		int r_int = randy.nextInt(numberOfEmpty);
		for(int i=0; i<SIZE; ++i) {     // i: row_index
			for(int j=0; j<SIZE; ++j) {  // j: col_index
				if(worlds[i][j].isEmpty()) {
					if(r_int == 0) {
						worlds[i][j].generateNew2();
					}
					r_int--;
				}
			}
		}
	}
	
	// Helper Function 3 -- Game Over Function
	public void gameOver(boolean is_win) {
		FadeTransition ft = new FadeTransition();
	    ft.setNode(world_pane);
	    ft.setDuration(new Duration(1000));
	    ft.setFromValue(1.0);
	    ft.setToValue(0.0);
	    ft.setAutoReverse(false);
	    ft.setCycleCount(1);
	    ft.setOnFinished(e->{
	    	world_pane.setVisible(false);
	    	//root.getChildren().remove(world_pane);
			VBox lose_pane = new VBox();
			Label lose_text;
			if(is_win) {
				lose_text = new Label("Congratulations! You Win!");
				lose_text.setFont(new Font("Lucida Sans Unicode", 30));
			}
			else {
				lose_text = new Label("Game Over!");
				lose_text.setFont(new Font("Lucida Sans Unicode", 50));
			}
			lose_text.setTextFill(Color.web("#776f66"));
			lose_text.setAlignment(Pos.CENTER);
		    Button restart_btn = new Button("Try Again");
		    restart_btn.setPrefSize(130, 50);
		    restart_btn.setFocusTraversable(false);
		    restart_btn.setStyle("-fx-background-color:#8f7b65; -fx-font-size:20; -fx-text-fill: #FFFFFF");
		    restart_btn.setOnAction(ee->{
		    	root.getChildren().remove(lose_pane);
		    	regenerateGamePane();
		    });
		    lose_pane.setSpacing(40);
		    lose_pane.setAlignment(Pos.CENTER);
		    lose_pane.getChildren().add(lose_text);
		    lose_pane.getChildren().add(restart_btn);
		    root.setCenter(lose_pane);
		});
	    
	    ft.playFromStart();
	}
	
	// Helper Function 4 -- Check whether the game is over
	// This is only called when there is no empty cell
	// Because only this case will make game lose
	public void checkGameOver() {
		boolean gameOver = true;
		// We need to check whether there is no merge possibility
		for(int i=0; i<SIZE; ++i) {     // i: row_index
			for(int j=0; j<SIZE; ++j) { 
				// Check whether worlds[i][j] has same type with its below and right neignbor
				if(i < 3 && worlds[i][j].getType() == worlds[i+1][j].getType()) {
					gameOver = false;
				}
				if(j < 3 && worlds[i][j].getType() == worlds[i][j+1].getType()) {
					gameOver = false;
				}
			}
			if(gameOver) gameOver(false);
		}
	}
	
	
	// Inner Class - Cell
	public class Cell extends Pane{
		private Label number; // label that dispaly the number
		private Type type;    // type of the cell 
		private int row_num;  // row index
		private int col_num;  // col index
		private boolean is_empty; // whether the cell is empty
		private int num;   // number of this cell
		
		// Constructor: Create a tile with numbers/empty
		Cell(int row, int col, Type input_type){
			
			// Basic Setup
			super();
			setPrefSize(85,85);
			
			// Update variable
			row_num = row;
			col_num = col;
			type = input_type;
			
			if(type == Type.EMPTY) 
				is_empty = true;
			else is_empty = false; 
			
			num = typeToInt(type);
			
			// Set the background color
			setBackground( new Background(new BackgroundFill( typeToColor(type), CornerRadii.EMPTY, Insets.EMPTY)));
			
			FadeTransition ft = new FadeTransition();
		    ft.setNode(this);
		    ft.setDuration(new Duration(300));
		    ft.setFromValue(0.0);
		    ft.setToValue(1.0);
		    ft.setAutoReverse(false);
		    ft.setCycleCount(1);
		    
		    ft.setOnFinished(e->{
		    	
				// Set up the label correctly
				if(num == 0) {
					number = new Label("");
				}
				else {
					number = new Label(""+num);
				}
				number.setLayoutY(22);
				number.setFont(new Font("Lucida Sans Unicode", 27));
				number.setTextFill(Color.WHITE);
				number.setAlignment(Pos.CENTER);
				
				number.setPrefSize(80,40);
				getChildren().add(number);
				
			});
		    
		    ft.playFromStart();
		}
		
		// Make this cell become a newly-generated 2
		public void generateNew2() {
			// Update all the variables
			type = Type.NUM_2;
			is_empty = false; 
			num = 2;
			
			// Set the background color
			setBackground( new Background(new BackgroundFill( typeToColor(type), CornerRadii.EMPTY, Insets.EMPTY)));
			
			// Set the fade transition
			FadeTransition ft = new FadeTransition();
		    ft.setNode(this);
		    ft.setDuration(new Duration(300));
		    ft.setFromValue(0.8);
		    ft.setToValue(1.0);
		    ft.setAutoReverse(false);
		    ft.setCycleCount(1);
		    ft.setOnFinished(e->{
		    	if(num == 0) {
		    		number.setText("");
		    	}
		    	else {
		    		number.setText(""+num);
		    	}
		    });
		    ft.playFromStart();
		}	
		
		public void becomeEmpty() {
			// Update all the variables
			type = Type.EMPTY;
			is_empty = true; 
			num = 0;
			
			// Set the background color
			setBackground( new Background(new BackgroundFill( typeToColor(type), CornerRadii.EMPTY, Insets.EMPTY)));
			// Set the label
			number.setText("");
		}
		
		
		// Partially become another type
		// This is to wait the animation to happen
		public void partial_become(Type i_type) {
			// Update all the variables
			type = i_type;
			if(type == Type.EMPTY) 
				is_empty = true;
			else is_empty = false; 	
			num = typeToInt(type);
		}
		
		
		// Partially double_up
		// This is to wait the animation to happen
		public void partial_double_up() {
			// Update all the variables
			num = 2 * num;
			type = intToType(num);
			if(type == Type.EMPTY) 
				is_empty = true;
			else is_empty = false; 
			
			// Update the score
			// Each time two tile merges, score will increase by the sum
			score += num;
			score_label.setText(""+score);
			
			// If the num is 2048, meaning that we win this game
			if(num == 2048) gameOver(true);
		}
		
		
		// Update function
		// This will only be called when all the variable has already been set correctly
		public void update() {
			// Set the background color
			setBackground( new Background(new BackgroundFill( typeToColor(type), CornerRadii.EMPTY, Insets.EMPTY)));
		    // Set the label
			if(num == 0) {
				number.setText("");
			}
			else {
				number.setText(""+num);
			}
		}
		
		
		public boolean isEmpty() {return is_empty; }
		public Type getType() {return type; }
		
		
	}
	// END OF CLASS
	
	
	// Helper Function 
	public int typeToInt(Type type) {
		int return_int;
		 switch (type) {
         case EMPTY:  return_int = 0;
                  break;
         case NUM_2:  return_int = 2;
                  break;
         case NUM_4:  return_int = 4;
                  break;
         case NUM_8:  return_int = 8;
                  break;
         case NUM_16: return_int = 16; 
                  break;
         case NUM_32: return_int = 32;
                  break;
         case NUM_64: return_int = 64;
                  break;
         case NUM_128: return_int = 128;
                  break;
         case NUM_256: return_int = 256;
                  break;
         case NUM_512: return_int = 512;
                  break;
         case NUM_1024: return_int = 1024;
                  break;
         case NUM_2048: return_int = 2048;
                  break;
         default: return_int = -1;
                  break;
     }
	 return return_int;
	}
	
	public Type intToType(int i) {
		 switch (i) {
         case 0:  return Type.EMPTY;
         case 2:  return Type.NUM_2;
         case 4:  return Type.NUM_4;
         case 8:  return Type.NUM_8;
         case 16: return Type.NUM_16;
         case 32: return Type.NUM_32;
         case 64: return Type.NUM_64;
         case 128: return Type.NUM_128;
         case 256: return Type.NUM_256;
         case 512: return Type.NUM_512;
         case 1024: return Type.NUM_1024;
         case 2048: return Type.NUM_2048;
         default: 
        	 return Type.EMPTY;
     }
	}
	
	// Helper Function 
		public Color typeToColor(Type type) {
			Color return_color;
			 switch (type) {
	         case EMPTY:  return_color = Color.rgb(204, 192, 179);
	                  break;
	         case NUM_2:  return_color = Color.rgb(238, 228, 218);
	                  break;
	         case NUM_4:  return_color = Color.rgb(237, 224, 200);
	                  break;
	         case NUM_8:  return_color = Color.rgb(242, 177, 121);
	                  break;
	         case NUM_16: return_color = Color.rgb(245, 149, 99); 
	                  break;
	         case NUM_32: return_color = Color.rgb(246, 124, 95);
	                  break;
	         case NUM_64: return_color = Color.rgb(246, 94, 59);
	                  break;
	         case NUM_128: return_color = Color.rgb(237, 207, 114);
	                  break;
	         case NUM_256: return_color = Color.rgb(237, 204, 97);
	                  break;
	         case NUM_512: return_color = Color.rgb(237, 200, 80);
	                  break;
	         case NUM_1024: return_color = Color.rgb(237, 197, 63);
	                  break;
	         case NUM_2048: return_color = Color.rgb(237, 194, 46);
	                  break;
	         default: return_color = Color.WHITE;
	                  break;
	     }
		 return return_color;
		}
		
}
