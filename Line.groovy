class Line{
	int startX
	double startY
	double ascending
	int touchIndex
	
	Line(int startX, double startY, double ascending, int touchIndex) {
		this.startX = startX
		this.startY = startY
		this.ascending = ascending
		this.touchIndex = touchIndex
	}
	
	double getY(int x) {
		assert x >= startX
		double y = startY
		for (int i = startX; i <= x; i++) {
			y += ascending
		}
		return y
	}
	
	String toString() {
		"X: $startX, Y: $startY, Steigung: $ascending, Berührung: $touchIndex"
	}
}
