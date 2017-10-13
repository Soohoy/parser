import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by user on 07.10.2017.
 */
public class ParserOrg{

    private static final String URL = "https://bankrot.fedresurs.ru/ArbitrManagersList.aspx?SroID=36";
    private Document doc;

    public List<ArbitrManager> getBankrupts() {
        List<ArbitrManager> bankrupts = new ArrayList<ArbitrManager>();

        for(int i = 1; i <=10; i++) {
            doc = getDoc(i);
            Element tableElement = doc.getElementsByAttributeValue("class", "bank").first();
            Element tbodyElement = tableElement.child(0);
            Elements elements = tbodyElement.tagName("tr").children();

            for (Element element : elements) {
                List<String> values = getValue(element);
                    bankrupts.add(new ArbitrManager(values.get(0)));

            }
        }
        return bankrupts;
    }

    protected List<String> getValue(Element element) {
        List<String> values = new ArrayList<String>();
        if (element.hasClass("pager")) {
            return values;
        }
        if (element.child(0).hasAttr("scope")) {
            return values;
        }

        for (Element e : element.children()) {
            values.add(e.text());
        }
        return values;

    }

    protected Document getDoc(int p){
        String page = "Page$%d";
        Document doc = null;
        try {
            /*if(p == 1){
                return Jsoup.connect(URL).get();
            } */
            Connection.Response response = Jsoup.connect(URL)
                    .data("ctl00$PrivateOffice1$ctl00", "ctl00$cphBody$ArbitrManagerList1$upArmList|ctl00$cphBody$ArbitrManagerList1$gvArbitrManagers")
                    .data("ctl00$cphBody$ArbitrManagerList1$msSro$tbSelectedText", "СРО ААУ \"Евросиб\" - Ассоциация Евросибирская саморегулируемая организация арбитражных управляющих")
                    .data("ctl00$cphBody$ArbitrManagerList1$msSro$hfSelectedValue", "36")
                    .data("ctl00$cphBody$ArbitrManagerList1$msSro$hfSelectedType", "Sro")

                    .data("__ASYNCPOST", "true")
                    .data("__EVENTARGUMENT", String.format(page, p))
                    .data("__EVENTTARGET", "ctl00$cphBody$ArbitrManagerList1$gvArbitrManagers")
                    .data("__EVENTVALIDATION", "/wEdABMJZFTx2EIDNYIZ4s7BSUDJZ14MMFU7gNQ8QUAGYKRBeJtQ54lj9CCT1aza1tIIkkJBD9CKWz3HWBDtnUmUeGMcv5jjekmf706DqVpQTEG8n4ncwwrT2Z9kjniSTEjthJogWwsHWl6hMjqgTOeiI2EuCcWzL3Kq7hgateXq9l9wg" +
                            "/UI0T6r7uMLOyD9jV+ddH4ZH7cqCNrVHTKRVSaktl/vS6kpNSZ9tkkS3Q0MD3X5YsP6EZeI8Rlu1p5S7tl4hGo5W9ny331Y5tuDUT" +
                            "+53jLOcWgmf5A3O67osIMHPohQK5wsaFg4Mjpm58+VZI7/oaVUDK4CU6aZtaipwN+8YWMkU1BaahNs01nvqGVLD6VyQSlgmwFkiGk0eST6cPy42oEwksY" +
                            "/G+YJ/o+DFqRKT/x2")
                    .data("__PREVIOUSPAGE", "NCv8brBNl2yT7iFo_pWHgZ__EtKHfsbmAZq5OtQOPCi3wMDQrNp78ssRvVKZlSbumA3-T5Dda_AH5MKrEqchBCh5zeSbDZ8jpJ3hAQww7tU9emGR0")
                    .data("__EVENTVALIDATION","/wEdAB8MFppB+kZ3yRaqJpBqAUbqidzDCtPZn2SOeJJMSO2EmiBbCwdaXqEyOqBM56IjYS6JoWlxKcoAaFKmIYkF1PmSCatj1Dut8yYLigE4QM+QkuZ7d0ZW4Pr44wn3aJYoP1qwaO508tZgktkXXh5umbbJQCM4jY0dFGCZKMyE/43FeNwJ1QJaGHjJklhKSMEjdzkR+w+NGhGwScmoN7lrM5TBOuNG8W1Bi17oacoS80fcRUOqom/59AnS3/Bm3eSYWZKUjXz0FyQk/hzNiFrWPjMYwUoIkVh5t4P3bjq5fUhc2GdeDDBVO4DUPEFABmCkQXibUOeJY/Qgk9Ws2tbSCJJCQQ/Qils9x1gQ7Z1JlHhjHL+Y43pJn+9Og6laUExBvJ8JxbMvcqruGBq15er2X3CD9QjRPqvu4ws7IP2NX510fhkftyoI2tUdMpFVJqS2X++yS1cNbtndQEGX/uhE74QvMpiEIzc8lz/HFw+zhT6FQcoRXLgKVQ5p8akj3Bqbulx4QZsrbIqFb53cOVPbB7NekzyMuFl45sbSe7PAQg6BeNUWoYc5zc8+kQsxXprXxfP1UxJJAE44rXRQLt0MPvENKYVA3yEocCmhSRvcVqrbivJ5e9PsEoNGqtUpVU0Oh/di7gTPrgRM0KIEX/DgC0jJ")
                    .data("__VIEWSTATE", "/wEPDwUJMjk5ODMxNTUzZBgCBR5fX0NvbnRyb2xzUmVxdWlyZVBvc3RCYWNrS2V5X18WCQUWY3RsMDAkcmFkV2luZG93TWFuYWdlcgUpY3RsMDAkUHJpdmF0ZU9mZmljZTEkaWJQcml2YXRlT2ZmaWNlRW50ZXIFIWN0bDAwJFByaXZhdGVPZmZpY2UxJGNiUmVtZW1iZXJNZQUgY3RsMDAkUHJpdmF0ZU9mZmljZTEkUmFkVG9vbFRpcDEFH2N0bDAwJFByaXZhdGVPZmZpY2UxJGlidFJlc3RvcmUFImN0bDAwJERlYnRvclNlYXJjaDEkaWJEZWJ0b3JTZWFyY2gFOmN0bDAwJGNwaEJvZHkkQXJiaXRyTWFuYWdlckxpc3QxJGNoYldpdGhQdWJsaWNhdGVkTWVzc2FnZXMFLGN0bDAwJGNwaEJvZHkkQXJiaXRyTWFuYWdlckxpc3QxJGliQXJtU2VhcmNoBStjdGwwMCRjcGhCb2R5JEFyYml0ck1hbmFnZXJMaXN0MSRpYkFybUNsZWFyBTFjdGwwMCRjcGhCb2R5JEFyYml0ck1hbmFnZXJMaXN0MSRndkFyYml0ck1hbmFnZXJzDzwrAAwBCAIhZA==")
                    .method(Connection.Method.POST)
                    .execute();
            doc = response.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}
