class OutputInfo implements Comparable{
	int period
	Stock stock
	String formattedStartDate
	String formattedLastValue
	String errorMessage
	double trendDiff
	double currentPrice
	String candleInfo = ""
	String candleTitle = ""
	ArrayList<StockValue> values
	StockValue last
  String indicators = "#indicatorsBelowChart=ADX&useFixAverage=true&fixAverage1=200&fixAverage0=38&e&"

	OutputInfo(Stock stock, int period, String date, String value, double trendDiff, double currentPrice, ArrayList<StockValue> values) {
		this.stock = stock
		this.formattedStartDate = date
		this.formattedLastValue = value
		this.period = period
		this.trendDiff = trendDiff
		this.currentPrice = currentPrice
		this.values = values
		this.last = values.get(values.size-1)
	}
	
	int compareTo(Object o){
		if (((OutputInfo)o).trendDiff > trendDiff)
			return -1
		else
			return 1
	}
	
	OutputInfo(Stock stock, int period, String errorMessage){
		this.stock = stock
		this.period = period
		this.errorMessage = errorMessage
	}
	
	OutputInfo(Stock stock, int period, double currentPrice)
	{
		this.period = period
		this.stock = stock
		this.currentPrice = currentPrice
	}
	
	boolean hasHammerShape()
	{
		double bodyTopPercentage
		double bodyToShadow
		println "HAS HAMMER SHAPE? LAST CLOSE: $last.close"
		if (last.close >= last.open)
		{
			bodyTopPercentage = (last.close - last.low)/(last.high-last.low)
			println "close >= open"
			println "bodyTopPercentage = $bodyTopPercentage"
			
			bodyToShadow = (last.close - last.open)/(last.open - last.low)
			println "bodyToShadow = $bodyToShadow"
			
		}
		else
		{
			bodyTopPercentage = (last.open - last.close)/(last.high-last.low)
			println "close < open"
			println "bodyTopPercentage = $bodyTopPercentage"
			
			bodyToShadow = (last.open - last.close)/(last.close - last.low)
			println "bodyToShadow = $bodyToShadow"
			
		}
		if (bodyTopPercentage >= 0.9 && bodyToShadow <= 0.5)
		{
			return true
		}

		return false
	}
	
	boolean isUpTrend()
	{
		return (last.close > (values.get(values.size-5)).close)
	}
	
	boolean isDownTrend()
	{
		return !isUpTrend()
	}
	
	boolean isBigBlack(StockValue value)
	{
		if (value.close > value.open)
		{
			return false
		}

		double percentageDiff = (value.open-value.close)/value.close
		
		if (percentageDiff > 0.01)
		{
			return true
		}
		return false
	}
	
	boolean isBigWhite(StockValue value)
	{
		if (value.close < value.open)
		{
			return false
		}

		double percentageDiff = (value.close-value.open)/value.open
		if (percentageDiff > 0.01)
		{
			return true
		}
		return false
	}
	
	boolean isWhite(StockValue value)
	{
		return (value.close > value.open)
	}
	
	void checkHammer()
	{
		if (hasHammerShape() && isDownTrend())
		{
			this.candleTitle = candleTitle+"Hammer: Untere Umkehr, kurzer K�rper, der oben sitzt. "
			this.candleInfo = candleInfo+" Hammer "
		}
		if (hasHammerShape() && isUpTrend())
		{
			this.candleTitle = candleTitle+"Hanging Man: \nObere Umkehr, kurzer K�rper, der oben sitzt."
			this.candleInfo = candleInfo+" Hanging Man "
		}
	}
	
	void checkEngulfing()
	{
		StockValue secondToLast = values.get(values.size-2)
		if (isDownTrend())
		{
			if (last.open < secondToLast.close && last.close > secondToLast.open && last.close > last.open && secondToLast.open > secondToLast.close)
			{
				this.candleTitle = candleTitle+"Bullish Engulfing: \nUntere Umkehr, zweiter K�rper wei�, erster schwarz, zweiter umh�llt ersten. $last.close"
				this.candleInfo = candleInfo+" Bullish Engulfing "
			}
		}
		else
		{
			if (last.open > secondToLast.close && last.close < secondToLast.open && last.close < last.open && secondToLast.open < secondToLast.close)
			{
				this.candleTitle = candleTitle+"Bearish Engulfing: \nObere Umkehr, Zweiter K�rper schwarz, erster wei�, zweiter umh�llt ersten. $last.close"
				this.candleInfo = candleInfo+" Bearish Engulfing "
			}
		}
	}
	
	void checkDarkCloudCover()
	{
		StockValue secondToLast = values.get(values.size-2)
		if (isBigWhite(secondToLast) && (this.last.open < this.last.close) && (this.last.open > secondToLast.high) && (this.last.close < secondToLast.close))
		{
			this.candleTitle = candleTitle+" Dark Cloud Cover: \nObere Umkehr, zweite Kerze tief in vorletzter"
			this.candleInfo = candleInfo+" Dark Cloud Cover "
		}
	}

	void checkMorningStar()
	{
		StockValue thirdToLast = values.get(values.size-3)
		StockValue secondToLast = values.get(values.size-2)
		if (isDownTrend() && isBigBlack(thirdToLast))
		{
			if (!isBigBlack(secondToLast) && !isBigWhite(secondToLast))
			{
				if ((isWhite(secondToLast) && secondToLast.close < thirdToLast.close) || !(isWhite(secondToLast) && secondToLast.open < thirdToLast.close))
				{
					if (isWhite(this.last) && this.last.close > thirdToLast.close)
					{
						this.candleTitle = candleTitle+" Morning Star: \nUntere Umkehr mit 3 Kerzen, mittlere unter den anderen"
						this.candleInfo = candleInfo + " Morning Star "
					}
				}
			}
		}
	}
	
	void checkCandles()
	{
		checkHammer()
		checkEngulfing()
		checkDarkCloudCover()
		checkMorningStar()
	}
	
	String getOutputLinesWithTrends() {
		String trendDiffFormatted = sprintf("%.2f", trendDiff*100)
		String currentPriceFormatted = sprintf("%.2f", currentPrice)
		checkCandles()
		StringBuffer outLine = new StringBuffer('<a href="http://www.comdirect.de/inf/aktien/detail/chart.html?ID_NOTATION='+stock.comdNotationId+'&timeSpan='+period+'M'+indicators+'" target="_blank">'+stock.name+'</a> '+currentPriceFormatted)
		outLine.append "<div title=\""
		outLine.append candleTitle
		outLine.append "\">"
		outLine.append "Trendstart: "+formattedStartDate+", letzter Wert = "+formattedLastValue
		//outLine.append(", Letzter Close: ")
		//outLine.append last.close + " "
		if (candleInfo.size() > 1)
		{
			outLine.append ("<font color=\"#FF0000\">")
			outLine.append candleInfo
			outLine.append ("</font>")
		}
		outLine.append ' Abstand zum Trend: '+trendDiffFormatted+'%'
		outLine.append("</div>") 
		//outLine.append('</br>\n')
		return outLine
	}
	
	String getOutputLinesWithoutTrends() {
println "cmdn"+stock.comdNotationId
println "period: "+period
println "name: "+stock.name
		String currentPriceFormatted = sprintf("%.2f", currentPrice)
		return '<a href="http://www.comdirect.de/inf/aktien/detail/chart.html?ID_NOTATION='+stock.comdNotationId+'&timeSpan='+period+'M'+indicators+'" target="_blank">'+stock.name+'</a> '+currentPriceFormatted+'</br>' 
	}
	
	String getOutputLinesForProblems() {
		return '<a href="http://www.comdirect.de/inf/aktien/detail/chart.html?ID_NOTATION='+stock.comdNotationId+'&timeSpan='+period+'M'+indicators+'" target="_blank">'+stock.name+'</a></br>' + //
		'PROBLEM: $errorMessage'
	}
}