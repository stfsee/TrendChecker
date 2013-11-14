import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import java.util.Locale


public class Util{
	static String formattedDate(Date date)
	{   SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
		return dateFormat.format(date); 
	}
}
