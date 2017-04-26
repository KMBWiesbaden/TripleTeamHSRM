import java.io.FileInputStream;

public class WordExtractor {
	public static void main(String[] args) throws Exception {

		try {

			XWPFDocument docx = new XWPFDocument(new FileInputStream("test.docx"));
		} catch (Exception e) {

			System.out.println(e);
		}
	}
}
