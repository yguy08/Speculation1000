package com.speculation1000.specvault.vault;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import com.speculation1000.specdb.market.MarketStatusContent;
import com.speculation1000.specdb.start.MarketStatus;
import com.speculation1000.specvault.listview.MarketStatusContentCellFactory;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;

public class VaultMainControl extends BorderPane implements Initializable {
	
	@FXML private ListView<MarketStatusContent> listViewDisplay;
	
	@FXML private Button showAll;
	
	@FXML private Button showHighs;
	
	@FXML private Button showLows;
	
	@FXML private Button autoBuy;
	
	@FXML private Button autoTrade;

	private ObservableList<MarketStatusContent> mainObsList = FXCollections.observableArrayList();
    
	public VaultMainControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("VaultMainView.fxml"));
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
	    List<MarketStatusContent> marketList = MarketStatus.getMarketStatusList();
	    for(MarketStatusContent msc : marketList){
	    	msc.setToStr(msc.getSymbol() + " @" + msc.getCurrentPrice());
	    }
		listViewDisplay.setCellFactory(new MarketStatusContentCellFactory());
		mainObsList.setAll(marketList);
		listViewDisplay.setItems(mainObsList);
    	listViewDisplay.scrollTo(0);
	}
	
	@FXML
	public void showAll(){
		
	}
	
	@FXML
	public void showHighs(){
		mainObsList.clear();
		loadAnimationStart();
		Task<List<MarketStatusContent>> task = new Task<List<MarketStatusContent>>() {
            @Override
            protected List<MarketStatusContent> call() throws Exception {
        	    List<MarketStatusContent> marketList = MarketStatus.getMarketStatusList();
	            return marketList;
            }
        };        
        new Thread(task).start();
        
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>(){
			@Override
			public void handle(WorkerStateEvent t){
				List<MarketStatusContent> marketList = task.getValue();
            	List<MarketStatusContent> highList = new ArrayList<>();
        	    for(MarketStatusContent m : marketList){
        	    	if(m.getDayHighLowMap().firstEntry().getValue() >= 25){
        	    		m.setToStr((m.getSymbol() + " @" + m.getCurrentPrice()));
        	    		highList.add(m);
        	    	}
        	    }
				Platform.runLater(new Runnable() {
		            @Override
					public void run() {
		            	loadAnimationEnd();
		            	mainObsList.setAll(highList);
			    		listViewDisplay.setItems(mainObsList);
			        	listViewDisplay.scrollTo(0);
		            }
		        });
			}
		});

	}
	
	@FXML
	public void showLows(){

	}
	
	@FXML
	public void buy(){

	}
	
	@FXML
	public void sell(){

	}
	
	@FXML
	public void auto(){

	}
	
	private void loadAnimationStart(){
		setCenter(new ProgressIndicator());
		showAll.setVisible(false);
	}
	
	private void loadAnimationEnd(){
		setCenter(listViewDisplay);
		showAll.setVisible(true);
	}
	
}
