class OutputInfo implements Comparable{
	int period
	Stock stock
	String formattedStartDate
	String formattedLastValue
	String errorMessage
	double trendDiff
	double currentPrice
	ArrayList<StockValue> values
  String indicators = "#indicatorsBelowChart=ADX&useFixAverage=true&fixAverage1=200&fixAverage0=38&e&"

	OutputInfo(Stock stock, int period, String date, String value, double trendDiff, double currentPrice, ArrayList<StockValue> values) {
		this.stock = stock
		this.formattedStartDate = date
		this.formattedLastValue = value
		this.period = period
		this.trendDiff = trendDiff
		this.currentPrice = currentPrice
		this.values = values
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

	String getOutputLinesWithTrends() {
		String trendDiffFormatted = sprintf("%.2f", trendDiff*100)
		String currentPriceFormatted = sprintf("%.2f", currentPrice)
		StringBuffer outLine = new StringBuffer('<a href="http://www.comdirect.de/inf/aktien/detail/chart.html?ID_NOTATION='+stock.comdNotationId+'&timeSpan='+period+'M'+indicators+'" target="_blank">'+stock.name+'</a> '+currentPriceFormatted)
		outLine.append('<div title="CandleStickComment">Trend startet am: '+formattedStartDate+', letzter Wert = '+formattedLastValue+' Abstand zum Trend: '+trendDiffFormatted+'%')
		StockValue last = values.get(values.size-1)
		outLine.append(" Hier steht das CandleStickErgebnis, Letzter Close: $last.close")
		outLine.append("</div>") 
		outLine.append('</br>\n')
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