package speculator;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import market.MarketsEnum;
import vault.Config;
import vault.main.VaultMainControl;

public class SpeculatorControl extends GridPane implements Initializable {
	
	@FXML private TextField balanceTextField;
	
	@FXML private TextField riskTextField;
	
	@FXML private TextField maxUnitsTextField;
	
	@FXML private TextField stopLengthTextField;
	
	@FXML private TextField timeFrameDaysTextField;
	
	@FXML private TextField entryTextField;
	
	@FXML private TextField exitTextField;
	
	@FXML private CheckBox longOnlyCheckBox;
	
	@FXML private CheckBox sortVol;
	
	@FXML private ToggleButton bitcoinMarket;
	
	@FXML private ToggleButton dollarMarket;
	
	@FXML private ToggleButton ethereumMarket;
	
	@FXML private Button saveSettings;
	
	@FXML private ToggleGroup marketToggleGroup;
	
	private Speculator speculator;
	
    public SpeculatorControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SpeculatorView.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        
        try {
            fxmlLoader.load();            
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }
    
    @Override
	public void initialize(URL location, ResourceBundle resources) {
		setDefaultSettings();
		marketToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>(){
		    public void changed(ObservableValue<? extends Toggle> ov,
		        Toggle toggle, Toggle new_toggle) {
		            if (new_toggle == bitcoinMarket){
		            	setNewMarket(MarketsEnum.BITCOIN);
		            }else if(new_toggle == dollarMarket){
		            	setNewMarket(MarketsEnum.DOLLAR);
		            }else{
		            	setNewMarket(MarketsEnum.ETHEREUM);
		            }
		         }
		});
	}
    
    //set text field text to current settings
    private void setDefaultSettings(){    	
    	balanceTextField.setText(Config.getAccountBalance().toPlainString());
    	riskTextField.setText(Config.getRisk().toPlainString());
    	maxUnitsTextField.setText(Integer.toString(Config.getMaxUnits()));
    	stopLengthTextField.setText(Config.getStopLength().toPlainString());
    	timeFrameDaysTextField.setText(Integer.toString(Config.getTimeFrameDays()));
    	entryTextField.setText(Integer.toString(Config.getEntrySignalDays()));
    	exitTextField.setText(Integer.toString(Config.getSellSignalDays()));
    	longOnlyCheckBox.setSelected(Config.isLongOnly());
    	sortVol.setSelected(Config.isSortVol());
    }
    
    private Speculator setSpeculator(){
    	//set market
    	speculator = new DigitalSpeculator(Integer.parseInt(balanceTextField.getText().trim()),
    			Integer.parseInt(riskTextField.getText().trim()),
    			Integer.parseInt(maxUnitsTextField.getText().trim()), 
    			Integer.parseInt(stopLengthTextField.getText().trim()), 
    			Integer.parseInt(timeFrameDaysTextField.getText().trim()),
    			Integer.parseInt(entryTextField.getText().trim()),
    			Integer.parseInt(exitTextField.getText().trim()),
    			longOnlyCheckBox.isSelected(),
    			sortVol.isSelected());
    	return speculator;
    }
    
    @FXML
	public void saveSettings(){
    	setSpeculator();
    	VaultMainControl.getVaultMainControl().saveSettings();
	}

	public Speculator getSpeculator() {
		return setSpeculator();
	}
	
	public void setNewMarket(MarketsEnum marketEnum){
		VaultMainControl.getVaultMainControl().disableAllBtns();
		saveSettings.setDisable(true);
		VaultMainControl.getVaultMainControl().setBottom(new ProgressBar());
		Task<Void> task = new Task<Void>() {
    	    @Override public Void call() {
    	    	System.out.println(marketEnum.getMarketName() + " market");
    	    	Config.setMarket(marketEnum);
    	        return null;
    	    }
    	};
    	new Thread(task).start();    	
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
			@Override
			public void handle(WorkerStateEvent t){
				Platform.runLater(new Runnable() {
		            public void run() {
		            	VaultMainControl.getVaultMainControl().enableAllBtns();
		            	saveSettings.setDisable(false);
		            	VaultMainControl.getVaultMainControl().setStatus("Loaded " + marketEnum.getMarketName() + "!");
		            }
		        });
			}
		});
	}

}
