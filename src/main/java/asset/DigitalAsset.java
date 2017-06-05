package asset;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.poloniex.dto.marketdata.PoloniexChartData;
import org.knowm.xchange.poloniex.service.PoloniexChartDataPeriodType;
import org.knowm.xchange.poloniex.service.PoloniexMarketDataServiceRaw;
import org.knowm.xchange.service.marketdata.MarketDataService;

import entry.Entry;
import exit.Exit;
import market.Market;
import price.PoloniexPriceList;
import speculator.Speculator;
import util.DateUtils;
import util.SaveToFile;
import util.StringFormatter;

public class DigitalAsset implements Asset {

	private MarketDataService dataService;
	
	private String assetName;
	
	private List<PoloniexChartData> priceList = new ArrayList<>();
	
	private List<PoloniexChartData> priceSubList;
	
	//static factory method to create online asset
	public static DigitalAsset createOnlineDigitalAsset(Market market, String assetName){
		DigitalAsset digitalAsset = new DigitalAsset();
		digitalAsset.setAssetName(assetName);
		digitalAsset.setMarketDataService(market);
		digitalAsset.setPriceList();
		return digitalAsset;
	}
	
	//static factory method to create offline asset
	public static DigitalAsset createOfflineDigitalAsset(Market market, String assetName){
		DigitalAsset digitalAsset = new DigitalAsset();
		digitalAsset.setAssetName(assetName);
		digitalAsset.setOfflinePriceList();
		return digitalAsset;
	}

	@Override
	public void setAssetName(String assetName) {
		this.assetName = assetName;
	}

	@Override
	public void setPriceList() {
		//consider updating date range to something more configurable
		long date = new Date().getTime() / 1000;
		CurrencyPair currencyPair = new CurrencyPair(assetName);
		try {
			priceList = Arrays
					.asList(((PoloniexMarketDataServiceRaw) dataService)
					.getPoloniexChartData(currencyPair, date - 365 * Speculator.getPriceHistoryYears() * 24 * 60 * 60,
					date, PoloniexChartDataPeriodType.PERIOD_86400));
			PoloniexPriceList pl;
			List<String> plist = new ArrayList<>();
			for(PoloniexChartData p : priceList){
				pl = new PoloniexPriceList(p.getDate(), p.getHigh(), p.getLow(), p.getOpen(), p.getClose(), p.getVolume(), p.getQuoteVolume(), p.getWeightedAverage());
				plist.add(pl.toString());
			}
			SaveToFile.writeAssetPriceListToFile(this, plist);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void setOfflinePriceList() {
		List<String> pcd = new ArrayList<>();
		String fileName = getAssetName().replace("/", "") + ".txt";
		URL resourceUrl = getClass().getResource(fileName);
		PoloniexChartData pl;
		try {
			pcd = Files.readAllLines(Paths.get(resourceUrl.toURI()));
			for(String p : pcd){
				String[] arr = p.split(",");
				pl = PoloniexPriceList.createPoloOfflinePriceList(arr);
				priceList.add(pl);
			}
		} catch (IOException | URISyntaxException | ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<PoloniexChartData> getPriceList() {
		return this.priceList;
	}
	
	@Override
	public void setPriceSubList(int start, int end) {
		this.priceSubList = this.priceList.subList(start, end);
	}
	
	@Override
	public List<PoloniexChartData> getPriceSubList() {
		return this.priceSubList;
	}

	@Override
	public String getAssetName() {
		return this.assetName;
	}

	@Override
	public void setMarketDataService(Market market) {
		 dataService = market.getExchange().getMarketDataService();
	}
	
	@Override
	public int getIndexOfCurrentRecord() {
		return priceList.indexOf(priceSubList.get(priceSubList.size()-1));
	}

	@Override
	public List<BigDecimal> getClosePriceListFromSubList() {
		List<BigDecimal> closePriceList = new ArrayList<>();
		for(int i = 0; i < priceSubList.size(); i++){
			closePriceList.add(priceSubList.get(i).getClose());
		}
		return closePriceList;
	}

	@Override
	public BigDecimal getClosePriceFromIndex(int index) {
		return priceList.get(index).getClose();
	}
	
	@Override
	public BigDecimal getHighPriceFromIndex(int index) {
		return priceList.get(index).getHigh();
	}

	@Override
	public BigDecimal getLowPriceFromIndex(int index) {
		return priceList.get(index).getLow();
	}

	@Override
	public String getDateStringFromIndex(int index) {
		return DateUtils.dateToSimpleDateFormat(priceList.get(index).getDate());
	}
	
	@Override
	public Date getDateTimeFromIndex(int index) {
		String date = getDateStringFromIndex(index);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date dateTime;
		try {
			dateTime = df.parse(date);
			return DateUtils.dateToUTCMidnight(dateTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	@Override
	public BigDecimal getVolumeFromIndex(int index) {
		return priceList.get(index).getVolume();
	}
	
	@Override
	public String toString(){
		return "$" + getAssetName() + " " + StringFormatter.bigDecimalToEightString(getPriceList().get(getPriceList().size()-1).getClose());   
	}

	@Override
	public List<Entry> getEntryList(Speculator speculator) {
		Entry entry;
		List<Entry> entryList = new ArrayList<>();
		
		int i = getPriceList().size();
		if(i < speculator.getTimeFrameDays()*2){
			i = speculator.getEntrySignalDays();
		}else{
			i = getPriceList().size() - (speculator.getTimeFrameDays()*2);
		} 
		
		for(int x = i;x < getPriceList().size();x++){
			setPriceSubList(x - speculator.getEntrySignalDays(), x + 1);
			entry = new Entry(this, speculator);
			int days = DateUtils.getNumDaysFromDateToToday(entry.getDateTime());
			if(entry.isEntry() && days < speculator.getTimeFrameDays()){
				entryList.add(entry);
				System.out.println(entry.toString());
			}				
		}
		
		return entryList;
	}

	@Override
	public List<Entry> getExitList(Speculator speculator) {
		List<Entry> entryList = getEntryList(speculator);
		List<Entry> exitList = new ArrayList<>();
		Exit exit;
		for(Entry e : entryList){
			for(int i = e.getEntryIndex();i < priceList.size();i++){
				setPriceSubList(i - speculator.getSellSignalDays(), i + 1);
				exit = new Exit(e, speculator);
				if(exit.isExit()){
					exitList.add(e);
					System.out.println(e.toString());
					break;
				}
			}
		}
		return exitList;
	}
	
	@Override
	public List<Entry> getOpenList(Speculator speculator) {
		List<Entry> entryList = getEntryList(speculator);
		List<Entry> exitList = new ArrayList<>();
		Exit exit;
		for(Entry e : entryList){
			for(int i = e.getEntryIndex();i < priceList.size();i++){
				setPriceSubList(i - speculator.getSellSignalDays(), i + 1);
				exit = new Exit(e, speculator);
				if(exit.isOpen()){
					exitList.add(e);
					System.out.println(e.toString());
					break;
				}
			}
		}
		return exitList;
	}

}
