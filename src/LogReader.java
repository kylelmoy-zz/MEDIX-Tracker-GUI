import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;


public class LogReader {
	public static void main(String[] args) throws Exception {
		DataInputStream is = new DataInputStream(new FileInputStream(new File("R:/test2/log.dat")));
		while (is.available() > 0) {
			int f = is.readInt();
			long time = is.readLong();
			int x = is.readInt();
			int y = is.readInt();
			int m = is.readInt();
			//String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (time));
			System.out.println(time + "\t" + f + "\t" + x + "\t" + y + "\t" + m);
		}
		is.close();
	}
}
