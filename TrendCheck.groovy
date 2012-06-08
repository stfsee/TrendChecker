import java.util.Iterator;

import java.text.NumberFormat

import java.text.NumberFormat

import groovy.util.XmlSlurper;

import java.text.NumberFormat;
import java.text.SimpleDateFormat
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale


import org.cyberneko.html.parsers.SAXParser;


public class TrendCheck {

	final int IGNORE_LAST_DAYS = 5
	final int DAYS_TO_NEXT_MIN = 2
	double NEAR = 0.015
	int MONTH = 6
	int MIN_DISTANCE = 90
	int SIGNIFICANT_DISTANCE = (MONTH*30.5)/6
	int INCREMENTS = 300

	int relevant = StockValue.CLOSE

	ArrayList<OutputInfo> nears = new ArrayList<OutputInfo>()
	ArrayList<OutputInfo> notNears = new ArrayList<OutputInfo>()
	ArrayList<OutputInfo> noTrends = new ArrayList<OutputInfo>()
	ArrayList<OutputInfo> problems = new ArrayList<OutputInfo>()
	ArrayList<OutputInfo> downTrends = new ArrayList<OutputInfo>()

	StockValue importLine(line, lineCount, relevant) {
		if (lineCount == 0) {
			return
		}
		StockValue value = new StockValue(line, relevant)
		return value
	}

	double getCurrentPrice(String symbol) {
		def html = ""
		def currentPriceUrl =""
		println "getting current price..."
//		try {
//		currentPriceUrl = "http://de.finance.yahoo.com/q?s="+symbol.toUpperCase()+"&ql=0"
//		html = new XmlSlurper(new SAXParser()).parse(currentPriceUrl)
//		}catch(java.io.IOException ex)
//		{
//			
//		}
		def price = ""
			currentPriceUrl = "http://de.finance.yahoo.com/q/hp?s="+symbol.toUpperCase()
			html = new XmlSlurper(new SAXParser()).parse(currentPriceUrl)
			println "parsed..."
			price = html.'**'.findAll {
				it.@id.text()=="yfs_l84_"+symbol.toLowerCase()
			}[0].toString()
			println "Got current value from 1st URL"
		
		if (price == null || price.equals("null")) {
		try { price = html.'**'.findAll {
				it.@id.text()=="yfs_l10_"+symbol.toLowerCase()
			}[0].toString()
			println "Current value from yfs_110"
		}
		catch(Exception ex)
		{
			println "Seltsames HTML: $html"
		}

		}
		NumberFormat nf = NumberFormat.getInstance(Locale.GERMAN)
		println "Price: $price"
		println "Trying: $symbol"
    try{
    		return nf.parse(price)
    }
    catch(java.text.ParseException ex)
    { 
      return 0
    }
        
	}

	String createUrl(String symbol, int days){
		// M d Y
		// a=02 b=01 c=2010 d=11 e=14 f=2010
		// von 01.03.2010
		// bis 14.12.2010
		Date today = new Date()
		Date halfYearAgo = today.minus(days)

		int a = halfYearAgo.getAt(Calendar.MONTH)
		int b = halfYearAgo.getAt(Calendar.DAY_OF_MONTH)
		int c = halfYearAgo.getAt(Calendar.YEAR)

		int d = today.getAt(Calendar.MONTH)
		int e = today.getAt(Calendar.DAY_OF_MONTH)
		int f = today.getAt(Calendar.YEAR)

		String url = "http://ichart.finance.yahoo.com/table.csv?s="+symbol.toUpperCase()+"&a=$a&b=$b&c=$c&d=$d&e=$e&f=$f&g=d&ignore=.csv"
		return url
	}

	int findFirstMin(ArrayList<StockValue> values) {
		def minStockValue = new StockValue()
		minStockValue.relevant = 100000
		int half = values.size() / 2
		int minIndex = 0
		for (int i = 0; i < half; i++) {
			if (values.get(i).relevant < minStockValue.relevant) {
				minStockValue = values.get(i)
				minIndex = i
			}
		}
		return minIndex
	}

	int findNextMin(ArrayList<StockValue> values, start) {
		def minStockValue = new StockValue()
		minStockValue.relevant = 100000
		int half = values.size()
		int minIndex
		for (int i = start+DAYS_TO_NEXT_MIN ; i < half; i++) {
			if (values.get(i).relevant < minStockValue.relevant) {
				minStockValue = values.get(i)
				minIndex = i
			}
		}
		return minIndex
	}

	double findMaxClose(ArrayList<StockValue> values) {
		StockValue maxStockValue = new StockValue()
		maxStockValue.relevant = 0
		int end = values.size()
		int half = values.size() / 2
		for (int i = half; i < end; i++) {
			if (values.get(i).relevant > maxStockValue.relevant) {
				maxStockValue = values.get(i)
			}
		}
		return maxStockValue.relevant
	}

	StockValue findMin(ArrayList<StockValue> values) {
		double min = 100000.0
		StockValue minValue = new StockValue()
		minValue.relevant = min
		for(StockValue value : values){
			if (value.relevant <= minValue.relevant)
				minValue = value
		}
		return minValue
	}

	double findMinClose(ArrayList<StockValue> values, int firstMin, int nextMin) {
		if (values.get(firstMin).relevant < values.get(nextMin).relevant)
			return values.get(firstMin).relevant
		else
			return values.get(nextMin).relevant
	}

	Line findUpTrendLine(ArrayList<StockValue> values, int minIndex, double inc){
		Date minDate = values.get(minIndex).date
		println "Trying to find line, start $minIndex = $minDate"
		double currentInc = 0
		int lastIndex = values.size()-1-IGNORE_LAST_DAYS
		int valuesCount = lastIndex - minIndex
		double tickInc = 0
		double minValue = values.get(minIndex).relevant
		for (int i = 0; i < INCREMENTS; i++) {
			currentInc += inc
			tickInc = currentInc / valuesCount
			//println "TickInc: $tickInc"
			for (int j = minIndex+MIN_DISTANCE; j <= lastIndex; j++) {
				if ((minValue + (j-minIndex)*tickInc) > values.get(j).relevant){
					println "Berührung bei $j"
					println values.get(j)
					return new Line(minIndex, minValue, tickInc,j)
				}
			}
		}
	}

	boolean isSecondMinTouched(Line line, nextMinIndex){
		return line.touchIndex == nextMinIndex
	}
	
	boolean isDownTrend(ArrayList<StockValue> values){
		return (values.get(0).getClose() > 1.05*values.get(values.size-1).getClose())
	}

	void importStocks(ArrayList<Stock> stocks, File stocksFile){
		stocksFile.eachLine{ line ->
			String[] lineValues = line.tokenize(',');
			Stock stock = new Stock(lineValues);
			stocks << stock;
		}
	}

	double trendDiff(double lastTrendValue, double currentPrice){
		double diff = (1-lastTrendValue / currentPrice)
		return diff
	}
	
	boolean isNear(double diff){
		return diff.abs() < NEAR
	}
	
	double getLastTrendValue(Line trend, int days){
		println trend
		return trend.getY(days)
	}

	ArrayList<StockValue> importStockValues(Stock stock, URL url){
		ArrayList<StockValue> values = new ArrayList<StockValue>()
		StockValue val

		int lineCount = 0

		try{
			url.eachLine(){
				val = importLine(it,lineCount++,relevant)
				if (val != null) values << val
			}
		}catch(FileNotFoundException ex) {
			String errorMessage = "FILENOTFOUND: "+ex
			println errorMessage
			problems << new OutputInfo(stock,MONTH,errorMessage)
			return
		}
		catch(java.net.ConnectException ex){
			String errorMessage = "ConnectException: "+ex
			println errorMessage
			problems << new OutputInfo(stock,MONTH,errorMessage)
			return
		}
		values = values.reverse()

		for(int i = 0;  i < values.size; i++){
			values.get(i).index = i
		}

		return values
	}

	void check(Stock stock, int days) {

		String symbol = stock.symbol
		def lineCount = 0
		def url = createUrl(symbol, days).toURL()

		ArrayList<StockValue> values = importStockValues(stock, url)

		StockValue val

		if (values == null)
			return
		println "\n\n\nAnzahl Werte: $values.size\n\n\n"

		double currentPrice = getCurrentPrice(symbol)
		println "aktueller Preis: $currentPrice"

		StockValue minValue = findMin(values)
		println "Kleinster Wert Datum: $minValue.date Wert:$minValue.relevant"

		double maxClose = findMaxClose(values)
		println "Maximum: $maxClose"

		double inc = (maxClose - minValue.relevant) / INCREMENTS
		println "inc=$inc"

		isDownTrend(values)
		
		Line trend = findUpTrendLine(values, minValue.index, inc)

		Line lastTrend
		StockValue lastMinValue

		while (trend != null && trend.touchIndex-minValue.index < SIGNIFICANT_DISTANCE && trend.touchIndex < (values.size()-IGNORE_LAST_DAYS) ){
			lastTrend = trend
			lastMinValue = minValue
			minValue = values.get(trend.touchIndex)
			trend = findUpTrendLine(values, minValue.index, inc)
		}

		if (trend == null && lastTrend == null){
			println "COULD NOT FIND ANY TREND"
			noTrends << new OutputInfo(stock,MONTH,currentPrice)
			return
		}

		if (trend == null && lastTrend != null){
			println "COULD NOT FIND LONG TREND, TAKING LAST TREND"
			minValue = lastMinValue
			trend = lastTrend
		}


		StockValue trendEndValue = values.get(trend.touchIndex)

		println "-----------TREND--------------"
		println "von: $minValue.date $minValue.relevant"
		println "bis: $trendEndValue.date $trendEndValue.relevant"
		println "-----------TREND--------------"

		double lastTrendValue = getLastTrendValue(trend,values.size)
		String lastTrendValueFormatted = sprintf("%.2f", lastTrendValue)

		String start = formattedDate(values.get(trend.startX).date);
		double trendDiff = trendDiff(lastTrendValue, currentPrice)
		if (isNear(trendDiff) ) {
			nears << new OutputInfo(stock,MONTH,start,lastTrendValueFormatted,trendDiff,currentPrice,values)
		}
		else {
			notNears << new OutputInfo(stock,MONTH,start,lastTrendValueFormatted,trendDiff,currentPrice,values)
		}
		
	}
	
	String formattedDate(Date date)
	{   SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
		return dateFormat.format(date); 
	}
	
	String formattedDateTime(Date date)
	{   SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
		return dateTimeFormat.format(date); 
	}
	void writeOutput(File outputFile){
		outputFile.append("<br/>trendnah: $nears.size<br/>")
		Collections.sort(nears)
		for (OutputInfo nearOutputInfo : nears) {
			outputFile.append(nearOutputInfo.getOutputLinesWithTrends())
		}
		outputFile.append("<br/>trendfern: $notNears.size<br/>")
		Collections.sort(notNears)
		for (OutputInfo notNearOutputInfo : notNears) {
			outputFile.append(notNearOutputInfo.getOutputLinesWithTrends())
		}
		outputFile.append("<br/>ohne Trend: $noTrends.size<br/>")
		for (OutputInfo noTrendOutputInfo : noTrends) {
			outputFile.append(noTrendOutputInfo.getOutputLinesWithoutTrends())
		}
		outputFile.append("<br/>Probleme: $problems.size<br/>")
		for (OutputInfo problemOutputInfo : problems) {
			outputFile.append(problemOutputInfo.getOutputLinesForProblems())
		}
	}


	public static void main(String[] args){
		TrendCheck trendCheck = new TrendCheck()

		if (args.length == 1 && args[0].equals("help")){
			println "groovy TrendCheck.groovy [Monate low|close near]"
			println "Bsp: groovy TrendCheck.groovy 12 low 0.02"
			println "     12 Monate Tiefstkurse trendnah, wenn hÃ¶chstens 2% Abweichung"
			return
		}

		if (args.length == 0) {
			println "groovy TrendCheck.groovy [Monate low|close near"
			println "Monate = $trendCheck.MONTH close $trendCheck.NEAR"
		}

		if (args.length > 0) {
			trendCheck.MONTH = Integer.parseInt(args[0]);
		}

		if (args.length > 1) {
			if (args[1].equalsIgnoreCase("low")) {
				trendCheck.relevant = StockValue.LOW
			}
		}

		if (args.length > 2) {
			trendCheck.NEAR = Double.parseDouble(args[2])
		}

		int days = 30.5 * trendCheck.MONTH
		trendCheck.MIN_DISTANCE = days / 20
		trendCheck.SIGNIFICANT_DISTANCE = days / 6

		ArrayList<Stock> stocks = new ArrayList<Stock>()
		File stocksFile = new File("stocks.csv")
		trendCheck.importStocks(stocks, stocksFile)

		stocks.each { println it.symbol }

		File outputFile = new File('output.html')
		String today = trendCheck.formattedDateTime(new Date())

		for (int i = 0; i < stocks.size(); i++) {
			trendCheck.check(stocks.get(i),days)
		}
		if (trendCheck.relevant == StockValue.CLOSE)
			outputFile.write("<p><b>Schlusskurse $trendCheck.MONTH Monate $today</b></p>\n")
		else
			outputFile.write("<p><b>Tiefstkurse $trendCheck.MONTH Monate $today</b></p>\n")
		trendCheck.writeOutput(outputFile)
		println "fertig mit $stocks.size Werten"
	}
}
