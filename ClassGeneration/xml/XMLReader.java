import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

public class XMLReader {
   public static void main(String[] args){

      try {
         File inputFile = new File("Test.xml");
         System.out.println(inputFile.getAbsolutePath());
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(inputFile);
         doc.getDocumentElement().normalize();

         System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

         NodeList nList = doc.getElementsByTagName("employee");

         System.out.println("----------------------------");

         for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            System.out.println("\nCurrent Element :" + nNode.getNodeName());

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
               Element eElement = (Element) nNode;
               System.out.println("Employee id : " + eElement.getElementsByTagName("id").item(0).getTextContent());
               System.out.println("Employee name : " + eElement.getElementsByTagName("name").item(0).getTextContent());
               System.out.println("Employee age : " + eElement.getElementsByTagName("age").item(0).getTextContent());
               System.out.println("Employee department : " + eElement.getElementsByTagName("department").item(0).getTextContent());
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
