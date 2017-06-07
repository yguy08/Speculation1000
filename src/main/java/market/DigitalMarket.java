package market;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.poloniex.PoloniexExchange;

import asset.Asset;
import asset.AssetFactory;

public class DigitalMarket implements Market {
	
	//poloniex exchange from xchange library
	public Exchange exchange;
	
	//market name
	private String marketName;
		
	//list of assets
	private List<Asset> assetList = new ArrayList<>();
	
	//static factory method to create online digital market
	public static DigitalMarket createOnlineDigitalMarket(){
		DigitalMarket digitalMarket = new DigitalMarket();
		digitalMarket.setExchange();
		digitalMarket.setAssetList();
		return digitalMarket;
	}
	
	//static factory method to create offline digital market
	public static DigitalMarket createOfflineDigitalMarket(){
		DigitalMarket digitalMarket = new DigitalMarket();
		digitalMarket.setOfflineAssetList();
		return digitalMarket;
	}
	
	@Override
	public String getMarketName() {
		return marketName;
	}

	@Override
	public void setMarketName(String marketName) {
		this.marketName = marketName;		
	}

	@Override
	public void setAssetList() {
		Asset asset = null;
		List<CurrencyPair> currencyPairs = exchange.getExchangeSymbols();
		for(CurrencyPair currencyPair : currencyPairs){
			if(currencyPair.toString().endsWith("BTC")){
				asset = AssetFactory.createAsset(this, currencyPair.toString());
				assetList.add(asset);
			}
		}
	}
	
	@Override
	public void setOfflineAssetList(){
		Asset asset = null;
		List<String> currencyPairs;
		URL resourceUrl = getClass().getResource("MarketList.csv");
		try {
			currencyPairs = Files.readAllLines(Paths.get(resourceUrl.toURI()));
			for(String currencyPair : currencyPairs){
				if(currencyPair.endsWith("BTC")){
					asset = AssetFactory.createAsset(this, currencyPair);
					assetList.add(asset);
				}
			}
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public List<Asset> getAssetList() {
		return assetList;
	}

	@Override
	public void setExchange() {
		//support different exchanges?
		exchange = ExchangeFactory.INSTANCE.createExchange(PoloniexExchange.class.getName());
	}

	@Override
	public Exchange getExchange() {
		return exchange;
	}
	
	@Override
	public String toString(){
		return marketName;
	}
	

}
