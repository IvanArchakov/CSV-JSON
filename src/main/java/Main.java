import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;



import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, ParseException {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileName = "data.csv";
        String fileNameJson = "data.json";

        //CSV-JSON
        List<Employee> list = parseCSV(columnMapping, fileName);
        String json = listToJson(list);
        writeString(json, fileNameJson);

        //XML-JSON
        List<Employee> list2 = parseXML("data.xml");
        String json2 = listToJson(list2);
        writeString(json2, "data2.json");

        //JSON парсер
        String json3 = readString("data.json");
        List<Employee> list3 = jsonToList(json3);
        list3.forEach(System.out::println);
    }

    private static List<Employee> jsonToList(String json) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(json);
        Gson gson = new GsonBuilder().create();
        List<Employee> employeeList = new ArrayList<>();
        for(Object object : jsonArray){
            Employee employee = gson.fromJson(object.toString(), Employee.class);
            employeeList.add(employee);
        }
        return employeeList;
    }

    private static String readString(String s) {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(s))) {
            return bufferedReader.lines()
                    .map(x -> x.replace(" ", ""))
                    .reduce(String::concat)
                    .orElse("");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<Employee> parseXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        List<Employee> employeeList = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xml);

        Element element = document.getDocumentElement();
        NodeList nodeList = element.getElementsByTagName("employee");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element item = (Element) nodeList.item(i);
            employeeList.add(new Employee(
                    Long.parseLong(item.getElementsByTagName("id").item(0).getTextContent()),
                    item.getElementsByTagName("firstName").item(0).getTextContent(),
                    item.getElementsByTagName("lastName").item(0).getTextContent(),
                    item.getElementsByTagName("country").item(0).getTextContent(),
                    Integer.parseInt(item.getElementsByTagName("age").item(0).getTextContent())
            ));
        }
        return employeeList;
    }


    public static List<Employee> parseCSV(String[] cM, String fN) {
        try (CSVReader csvReader = new CSVReader(new FileReader(fN))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(cM);

            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();

            List<Employee> staff = csv.parse();
            return staff;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String listToJson(List<Employee> staff) {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson.toJson(staff);
        return json;
    }

    private static void writeString(String json, String fNJ) {
        try (FileWriter file = new FileWriter(fNJ)) {
            file.write(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
