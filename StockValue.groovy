import java.text.SimpleDateFormat

public class StockValue{
	
	static final int CLOSE = 0
	static final int LOW = 1
	
	def Date date;
	int index;
	def double open;
	def double high;
	def double low;
	def double close;
	def long volume;
	def double adjClose;
	def double relevant;
	
	String toString(){
		"Datum: $date, Open: $open, High: $high, Low: $low, Close: $close, Relevant: $relevant"
	}
	
	StockValue() {
		this.date = new Date()
		this.open = 0
		this.high = 0
		this.low = 0
		this.close = 0
		this.relevant = 0
	}
	
	StockValue(line) {
		String[] lineValues = line.tokenize(",")
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd")
		this.date = dateFormat.parse(lineValues[0])
		this.open = lineValues[1].toDouble()
		this.high = lineValues[2].toDouble()
		this.low = lineValues[3].toDouble()
		this.close = lineValues[4].toDouble()
		this.relevant = this.close
		//println this.toString()
	}
	
	StockValue(line, int relevant) {
		String[] lineValues = line.tokenize(",")
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd")
		this.date = dateFormat.parse(lineValues[0])
		this.open = lineValues[1].toDouble()
		this.high = lineValues[2].toDouble()
		this.low = lineValues[3].toDouble()
		this.close = lineValues[4].toDouble()
		
		if (relevant == CLOSE)
			this.relevant = this.close
		if (relevant == LOW)
			this.relevant = this.low
	}
}