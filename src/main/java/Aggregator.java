import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class Aggregator {
    public static void main(String[] args) throws IOException {
        ParserMessage parser = new ParserMessage();
        Sort sort = new Sort();

        System.out.println("Введите дату в формате: ДД.ММ.ГГГГ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String date = reader.readLine();
        Pattern p = Pattern.compile("\\d\\d.\\d\\d.\\d\\d\\d\\d");
        while (!p.matcher(date).matches()){
            System.out.println("Не правильный формат даты. Введите дату:");
            date = reader.readLine();
        }
        System.out.println("Ожидайте...");

        new File("C:/messages").mkdirs();

        List<Bankrupt> list = sort.getSortList(parser.getBankrupts(date), date);

        System.out.println("Отсортированно " + list.size() + " сообщений");
    }

}
