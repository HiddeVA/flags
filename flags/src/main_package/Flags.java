package main_package;

import additional_controls.*;
import flags.*;
import java.io.File;
import java.sql.Connection;
import java.util.Arrays;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

////////////////////////////////////
// Table of Contents
////////////////////////////////////
// Initialisers / settings variables
// Application start
// Application helper functions
// Submenus
////////////////////////////////////

public class Flags extends Application
{
	static Flag flag;
	static FlagManager flagManager;
	ColorPicker backgroundColorpicker, symbolColorpicker;
	Settings settings = new Settings("");
	
	//read-only settings variables
	private static boolean snapToPosition = false;
	private static boolean editable = true;
	
	public static boolean getEditableSetting()
	{
		return editable;
	}
	
	public static boolean getSnapSetting()
	{
		return snapToPosition;
	}
	
	public static void main(String[] args)
	{
		if (args.length > 0)
		{
			flagManager = new FlagManager(DBConnection.getInstance());
			flag = flagManager.getFlagByCountryName(args[0]);
			System.out.println(Exporter.generateSVG(flag, args[0]));
		}
		else
		{
			launch(args);
		}
	}

	@Override public void start(Stage stage) 
	{
		flagManager = new FlagManager(DBConnection.getInstance());
		ComboBox<String> cbbFlags = new ComboBox<String>();
		
		BorderPane root = new BorderPane();
		BorderPane viewer = new BorderPane();
		BorderPane editor = new BorderPane();
		
		Menu menuFile = new Menu("File");
		Menu menuFlag = new Menu("Flag");
		Menu menuExport = new Menu("Export");
		Menu menuSettings = new Menu("Settings");
		MenuBar menuBar = new MenuBar(menuFile, menuFlag, menuExport, menuSettings);
		MenuItem editFlag = new MenuItem("Edit Flag");
		editFlag.setOnAction(e->{
			root.setCenter(editor);
			menuFlag.setDisable(false);
			editable = true;
			});
		MenuItem viewFlag = new MenuItem("View Flags");
		viewFlag.setOnAction(e->{
			root.setCenter(viewer);
			menuFlag.setDisable(true);
			editable = false;
			cbbFlags.getItems().addAll(flagManager.getAllFlagsByCountry());
			});
		menuFile.getItems().addAll(editFlag, viewFlag);
		
		MenuItem loadFlag = new MenuItem("Load Flag");
		MenuItem saveFlag = new MenuItem("Save Flag");
		MenuItem newFlag = new MenuItem("New Flag");
		MenuItem importFlag = new MenuItem("Import");
		menuFlag.getItems().addAll(newFlag, loadFlag, saveFlag, importFlag);
		newFlag.setOnAction(e->openCreateNewFlagMenu(stage, editor));
		loadFlag.setOnAction(e->openLoadFlagMenu(stage, editor));
		saveFlag.setOnAction(e->openSaveFlagMenu(stage));
		importFlag.setOnAction(e->openImportFlagMenu(stage, editor));
		
		MenuItem miConnect = new MenuItem("Connection Info");
		miConnect.setOnAction(e->openConnectMenu(stage));
		MenuItem miEditSettings = new MenuItem("Edit Settings");
		miEditSettings.setOnAction(e->openEditSettingsMenu(stage));
		menuSettings.getItems().addAll(miConnect, miEditSettings);
		
		MenuItem miExportSVG = new MenuItem("Export as SVG");
		miExportSVG.setOnAction(e->{
			if (flag == null) return;
			Exporter.exportAsSVGFile(flag, "Something");
		});
		MenuItem miExportSettings = new MenuItem("Export Settings");
		miExportSettings.setOnAction(e->openExportMenu(stage));
		menuExport.getItems().addAll(miExportSVG, miExportSettings);
		
		cbbFlags.getItems().addAll(flagManager.getAllFlagsByCountry());
		cbbFlags.setEditable(true);
		cbbFlags.setVisibleRowCount(10);
		cbbFlags.setOnAction(e->placeFlag(viewer, flagManager.getFlagByCountryName(cbbFlags.getValue())));
		Button btnRandomFlag = new Button("Show random flag");
		btnRandomFlag.setOnAction(e->placeFlag(viewer, flagManager.getRandomFlag()));
		HBox hboxSelector = new HBox(new Label("Select Flag: "), cbbFlags, btnRandomFlag);
		hboxSelector.setSpacing(8);
		viewer.setTop(hboxSelector);

		backgroundColorpicker = new ColorPicker();
		backgroundColorpicker.setOnAction(e->{
			if (flag == null)
			{
				flag = new Flag(backgroundColorpicker.getValue());
				placeFlag(editor, flag);
				editable = true;
			}
			else 
			{
				flag.setBackGround(backgroundColorpicker.getValue());
				flag.draw();
			}
		});
		VBox vboxBackground = new VBox(new Label("Background Colour:"), backgroundColorpicker);
		
		symbolColorpicker = new ColorPicker();
		VBox vboxSymbol = new VBox(new Label("Object Colour:"), symbolColorpicker);
		HBox toolbar = new HBox(vboxBackground, vboxSymbol);
		toolbar.setSpacing(5);
		
		Button addSymbol = new Button("Add");
		Button addBlock = new Button("Blck");
		Button rotate = new Button("Rot");
		Button sizeUp = new Button("+");
		Button sizeDown = new Button("-");
		Button undo = new Button("Undo");
		Button delete = new Button("Del");
		addSymbol.setOnAction(e->openAddSymbolMenu(stage));
		addBlock.setOnAction(e->{if (flag instanceof BlockFlag) openAddBlockMenu(stage);});		
		rotate.setOnAction(e->flag.rotateSymbol());
		sizeUp.setOnAction(e->flag.resizeSymbol(10));
		sizeDown.setOnAction(e->flag.resizeSymbol(-10));
		undo.setOnAction(e->flag.undoLastChange());
		delete.setOnAction(e->flag.removeSymbol());

		VBox sidebar = new VBox(addSymbol, addBlock, rotate, sizeUp, sizeDown, undo, delete);		
		editor.setTop(toolbar);
		editor.setLeft(sidebar);
		sidebar.setVisible(false);
		
		HBox statusBar = new HBox();
		
		root.setCenter(editor);
		root.setTop(menuBar);
		root.setBottom(statusBar);
		
		stage.setScene(new Scene(root, 400, 400));
		stage.setTitle("Flag Editor");
		stage.show();
		stage.setOnCloseRequest(e->DBConnection.closeConnection());
	}
	
	private void placeFlag(BorderPane b, Node node)
	{
		if (node == null)
		{
			b.setCenter(new Label("Unable to draw flag"));
		}
		else
		{
			b.setCenter(node);
		}
		if (editable)
		{
			VBox toolbar = (VBox)b.getLeft();
			if (node instanceof Flag)
			{
				flag = (Flag) node;
				flag.draw();
				flag.setColorPicker(symbolColorpicker); //we hand over this instance so that a selected colour can change on click
				flag.setOnMousePressed(mse->flag.checkForSymbol(mse.getX(), mse.getY()));
				flag.setOnMouseDragged(mse->flag.moveSymbolTo(mse.getX(), mse.getY()));
				flag.setOnMouseReleased(mse->flag.disallowdragging());
				toolbar.setVisible(true);
			}
			else 
			{
				toolbar.setVisible(false);
			}
		}
		else
		{
			if (node instanceof Flag)
			{
				flag = (Flag) node;
				flag.draw();
			}
		}		
	}
	
	private Stage createSubMenu(Stage mainStage)
	{
		Stage subMenu = new Stage();
		subMenu.initModality(Modality.APPLICATION_MODAL);
		subMenu.initOwner(mainStage);
		
		return subMenu;
	}
	
	private void openAddBlockMenu(Stage onStage)
	{
		Stage addBlockMenu = createSubMenu(onStage);
		
		boolean useSettings = settings.getId().equals("addblock");
		
		NumberTextField txtWidth = new NumberTextField(useSettings ? settings.getNumeric() : flag.getWidth() / 2);
		NumberTextField txtHeight = new NumberTextField(useSettings ? settings.getNumeric() : flag.getHeight() / 2);
		CheckBox chkPercentage = new CheckBox();
		chkPercentage.setSelected(useSettings ? settings.getOption() : false);
		
		ChoiceBox<Pos> chbPosition = new ChoiceBox<Pos>();
		chbPosition.getItems().addAll(Pos.TOP_LEFT, Pos.TOP_RIGHT, Pos.BOTTOM_LEFT, Pos.BOTTOM_RIGHT, Pos.CENTER);
		chbPosition.setValue(Pos.TOP_LEFT);
		
		RadioGroup rgBlockShape = new RadioGroup(new RadioButton("Rectangle"), new RadioButton("Triangle"));
		
		Button btnAddBlock = new Button("Add Block");
		btnAddBlock.setOnAction(e->{
			double width = txtWidth.getNumericText();
			double height = txtHeight.getNumericText();
			width = chkPercentage.isSelected() ? flag.getWidth() * width / 100 : Double.min(flag.getWidth(), width);
			height = chkPercentage.isSelected() ? flag.getHeight() * height / 100 : Double.min(flag.getHeight(), height);
			
			switch (rgBlockShape.getSelectedToggle())
			{
			case "Rectangle": default:
				((BlockFlag)flag).addBlock(width, height, chbPosition.getValue(), symbolColorpicker.getValue());
				break;
			case "Triangle":
				((TriangleFlag)flag).addTriangle(width, height, chbPosition.getValue(), symbolColorpicker.getValue());
				break;
			}
			
			flag.draw();
			flag.setColorPicker(symbolColorpicker);
			settings = new Settings("addblock");
			settings.add(txtHeight.getNumericText(), txtWidth.getNumericText());
			settings.add(chkPercentage.isSelected());
			addBlockMenu.close();
		});
				
		VBox options = new VBox(
				new Label("Add a Rectangular Section"),
				new HBox(new Label("Width: "), txtWidth),
				new HBox(new Label("Height: "), txtHeight),
				new HBox(new Label("Percentage? "), chkPercentage),
				new HBox(new Label("Position: "), chbPosition),
				flag instanceof TriangleFlag ? new HBox(new Label("Type of Block: "), rgBlockShape) : new HBox(),
				btnAddBlock);
		options.setSpacing(10);
		options.setAlignment(Pos.CENTER);
		
		addBlockMenu.setScene(new Scene(options, 200, 300));
		addBlockMenu.show();
	}
	
	private void openAddSymbolMenu(Stage onStage)
	{
		Stage addSymbolMenu = createSubMenu(onStage);
		
		boolean useSettings = settings.getId().equals("addsymbol");
		
		ComboBox<SymbolType> cbxType = new ComboBox<>();
		cbxType.getItems().addAll(SymbolType.CIRCLE, SymbolType.STAR, SymbolType.CRESCENT);
		cbxType.setValue(SymbolType.CIRCLE);
		
		NumberTextField txtSize = new NumberTextField(useSettings ? settings.getNumeric() : 20);
		NumberTextField txtXPos = new NumberTextField(useSettings ? settings.getNumeric() : flag.getWidth() / 2);
		NumberTextField txtYPos = new NumberTextField(useSettings ? settings.getNumeric() : flag.getHeight() / 2);
		txtXPos.setPrefWidth(50);
		txtYPos.setPrefWidth(50);
		CheckBox chkPercentage = new CheckBox();
		
		Button btnConfirm = new Button("Add Symbol");
		btnConfirm.setOnAction(e->{
			//These lines perform various checks to stop the user from adding oversized objects
			double size = txtSize.getNumericText();
			double maxSize = Double.min(flag.getWidth(), flag.getHeight());
			size = Double.min(size, maxSize);
			
			double x = txtXPos.getNumericText();
			x = chkPercentage.isSelected() ? x * flag.getWidth() / 100 : x;
			double xMax = flag.getWidth() - size / 2;
			
			double y = txtYPos.getNumericText();
			y = chkPercentage.isSelected() ? y * flag.getHeight() / 100 : y;
			double yMax = flag.getHeight() - size / 2;
			
			y = Double.max(size / 2, Double.min(y, yMax)); //There are 4 checks that need to be made, 2 for x, 2 for y
			x = Double.max(size / 2, Double.min(x, xMax)); //Max for lower bounds, min for upper bounds
			
			flag.addSymbol(cbxType.getValue(), symbolColorpicker.getValue(), x, y, size, 0);
			flag.setColorPicker(symbolColorpicker);
			settings = new Settings("addsymbol");
			settings.add(y, x, size);
			settings.add(chkPercentage.isSelected());
			addSymbolMenu.close();
		});
		
		VBox options = new VBox(
				new Label("Add a symbol"), 
				new HBox(new Label("Type: "), cbxType), 
				new HBox(new Label("Size: "), txtSize),
				new HBox(new Label("Percentage? "), chkPercentage),
				new HBox(new Label("Horizontal :"), txtXPos),
				new HBox(new Label("Vertical :"), txtYPos),
				btnConfirm);
		options.setSpacing(10);
		options.setAlignment(Pos.CENTER);
		
		addSymbolMenu.setScene(new Scene(options, 200, 300));
		addSymbolMenu.show();
	}
	
	private void openConnectMenu(Stage onStage)
	{
		Stage connectMenu = createSubMenu(onStage);
		
		Label lblConnectInfo = new Label();
		Button btnReconnect = new Button("Reconnect");
		VBox container = new VBox(lblConnectInfo, btnReconnect);
		if (DBConnection.isConnnected())
		{
			lblConnectInfo.setText("Currently connected to: " + DBConnection.getServerInfo()[0]);
			btnReconnect.setVisible(false);
		}
		else
		{
			lblConnectInfo.setText("Not connected to database");			
			btnReconnect.setOnAction(e->{
				Connection newConnection = DBConnection.getInstance();
				if (newConnection != null)
				{
					flagManager = new FlagManager(newConnection);
					Alert reconnectSuccess = new Alert(AlertType.INFORMATION);
					reconnectSuccess.setContentText("Reconnect successful");
					reconnectSuccess.showAndWait();
					lblConnectInfo.setText("Currently connected to: " + DBConnection.getServerInfo()[0]);
					btnReconnect.setVisible(false);
				}
				else
				{
					Alert reconnectFailed = new Alert(AlertType.ERROR);
					reconnectFailed.setContentText("Failed to reconnect");
					reconnectFailed.showAndWait();
				}
			});
		}
		connectMenu.setScene(new Scene(container, 250, 150));
		connectMenu.show();
	}
	
	private void openCreateNewFlagMenu(Stage onStage, BorderPane editor)
	{
		Stage createNewFlag = createSubMenu(onStage);
		
		ChoiceBox<String> chbFlagType = new ChoiceBox<String>();
		chbFlagType.getItems().addAll("Standard", "Horizontal", "Vertical", "Diagonal", "Advanced", "Extra Advanced");
		chbFlagType.setValue("Standard");
		
		Slider rowSlider = new Slider(2, 6, 3);
		rowSlider.setShowTickLabels(true);
		rowSlider.setMajorTickUnit(1);
		rowSlider.setBlockIncrement(1);
		rowSlider.setSnapToTicks(true);
		
		Button btnCreateFlag = new Button("Make New Flag");
		btnCreateFlag.setOnAction(f->{
			switch(chbFlagType.getValue())
			{
			case "Standard": default: 
				flag = new Flag(backgroundColorpicker.getValue());
				break;
			case "Horizontal":
				flag = new HorizontalFlag((int)rowSlider.getValue());
				break;
			case "Vertical":
				flag = new VerticalFlag((int)rowSlider.getValue());
				break;
			case "Diagonal":
				//flag = new DiagonalFlag((int)rowSlider.getValue(), backgroundColorpicker.getValue());
				DiagonalFlag dFlag = new DiagonalFlag(Arrays.asList(Color.BLACK, Color.GREEN, Color.BLACK));
				dFlag.setRowSizes(Arrays.asList(10.0, 20.0, 10.0), true);
				dFlag.setDiagonalOrientation(-1);
				flag = dFlag;
				break;
			case "Advanced":
				flag = new BlockFlag(backgroundColorpicker.getValue());
				break;
			case "Extra Advanced":
				flag = new TriangleFlag(backgroundColorpicker.getValue());
				break;
			}
			placeFlag(editor, flag);
			createNewFlag.close();
		});
		
		GridPane exampleFlagPane = new GridPane();
		Flag flag1 = new Flag(Color.RED); flag1.addSymbol(SymbolType.STAR, Color.WHITE, 50);
		
		VBox vboxCreateFlag = new VBox(
				new HBox(new Label("Rows: "), rowSlider), 
				chbFlagType, 
				btnCreateFlag,
				new Label("Example Flags:"),
				exampleFlagPane
				);
		vboxCreateFlag.setSpacing(10);
		createNewFlag.setScene(new Scene(vboxCreateFlag, 250, 250));
		createNewFlag.show();
	}
	
	private void openEditSettingsMenu(Stage onStage)
	{
		Stage editSettingsMenu = createSubMenu(onStage);
		
		CheckBox chkSnap = new CheckBox("Snap to Position");
		chkSnap.setSelected(snapToPosition);
		chkSnap.setOnAction(e->snapToPosition = !snapToPosition);
		
		editSettingsMenu.setScene(new Scene(new VBox(chkSnap), 150, 150));
		editSettingsMenu.show();
	}
	
	private void openExportMenu(Stage onStage)
	{
		Stage exportMenu = createSubMenu(onStage);
		
		Button btnOpenDialog = new Button("Select Directory");		
		FileChooser exportFileChooser = new FileChooser();
		Label lblCurrentDirectory = new Label("Current Directory: " + Exporter.getDirectory());
		lblCurrentDirectory.setWrapText(true);
		btnOpenDialog.setOnAction(e->{
			File file = exportFileChooser.showOpenDialog(exportMenu);
			exportFileChooser.setInitialDirectory(new File(Exporter.getDirectory()));
			if (file != null)
			{
				Exporter.setExportDestination(file);
				lblCurrentDirectory.setText(Exporter.getDirectory());
			}
		});
		VBox container = new VBox(lblCurrentDirectory, btnOpenDialog);
		container.setSpacing(10);
		exportMenu.setScene(new Scene(container, 250, 150));		
		exportMenu.show();
	}
	
	private void openImportFlagMenu(Stage onStage, BorderPane b)
	{
		Stage importFlagMenu = createSubMenu(onStage);
		
		Label lblSelectedFile = new Label();
		Button btnImportFlag = new Button("Start Import");
		btnImportFlag.setDisable(true);
		Button btnOpenFile = new Button("Select File");
		
		btnOpenFile.setOnAction(e->{
			FileChooser fc = new FileChooser();
			fc.setInitialDirectory(new File("C:\\Users\\Public"));
			fc.getExtensionFilters().addAll(new ExtensionFilter("Vector Graphics (*.svg)", "*.svg"), new ExtensionFilter("XML files (*.xml)","*.xml"));
			File importFile = fc.showOpenDialog(importFlagMenu);
			if (importFile != null)
			{
				btnImportFlag.setDisable(false);
				lblSelectedFile.setText(importFile.getPath());
				Importer.setImportFile(importFile);
			}
		});
		btnImportFlag.setOnAction(e->{
			placeFlag(b, Importer.beginImport());
			importFlagMenu.close();
		});
		
		
		VBox container = new VBox(new Label("Select a File to Import"), btnOpenFile, lblSelectedFile, btnImportFlag);
		container.setSpacing(10);
		importFlagMenu.setScene(new Scene(container, 250, 250));
		importFlagMenu.show();
	}
	
	private void openLoadFlagMenu(Stage onStage, BorderPane editor)
	{
		Stage loadFlagMenu = createSubMenu(onStage);
		
		ComboBox<String> cbbLoader = new ComboBox<String>();
		cbbLoader.getItems().addAll(flagManager.getAllFlagsByCountry());
		
		Button btnLoadFlag = new Button("Load Flag");
		btnLoadFlag.setOnAction(f->{
			flag = flagManager.getFlagByCountryName(cbbLoader.getValue());
			placeFlag(editor, flag);
			backgroundColorpicker.setValue(flag == null? Color.WHITE : flag.getBackGround());
			loadFlagMenu.close();
		});
		
		loadFlagMenu.setScene(new Scene(new VBox(
				new Label("Select a Flag"), 
				cbbLoader, 
				btnLoadFlag
				), 200, 200));
		loadFlagMenu.setTitle("Load a Flag");
		loadFlagMenu.show();
	}
	
	private void openSaveFlagMenu(Stage onStage)
	{
		Stage saveFlagMenu = createSubMenu(onStage);
		
		TextField txtCountryName = new TextField();
		Button btnSaveFlag = new Button("Save Flag");
		VBox container = new VBox(new Label("Name of country"), txtCountryName, btnSaveFlag);
		VBox.setMargin(btnSaveFlag, new Insets(10));
		
		btnSaveFlag.setOnAction(e->{
			if (flagManager.checkForCountryName(txtCountryName.getText()))
			{
				Alert a = new Alert(AlertType.CONFIRMATION);
				a.setHeaderText("Duplicate flagname");
				a.setContentText("This flag already exists in the database. Do you wish to overwrite it?");
				a.showAndWait();
				if (a.getResult() == ButtonType.OK)
				{
					flagManager.saveFlagToDB(flag, txtCountryName.getText(), true);						
				}
				else return;
			}
			else
			{
				flagManager.saveFlagToDB(flag, txtCountryName.getText(), false);				
			}
			saveFlagMenu.close();
		});
		
		saveFlagMenu.setScene(new Scene(container, 200, 250));
		saveFlagMenu.show();
	}
}
