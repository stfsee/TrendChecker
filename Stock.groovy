class Stock {
	String name;
	
	String symbol;
	
	int comdNotationId;
	
	Stock(String[] stock) {
		assert stock.length == 3
		this.name = stock[0]
		this.symbol = stock[1]
		this.comdNotationId = Integer.parseInt(stock[2].trim())
	}
}
