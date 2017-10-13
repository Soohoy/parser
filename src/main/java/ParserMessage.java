import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class ParserMessage {
    private static final String URL = "https://bankrot.fedresurs.ru/Messages.aspx";
    private Document doc;

    public ParserMessage() throws IOException {
    }

    public List<Bankrupt> getBankrupts(String date) {
        List<Bankrupt> bankrupts = new ArrayList<>();

        for (int i = 1; i <= 20; i++) {
            doc = getDocInventory(i, date);
            Element tableElement = doc.getElementsByAttributeValue("class", "bank").first();
            Element tbodyElement = tableElement.child(0);
            if(tbodyElement.childNodeSize() < 3){
                break;
            }
            Elements elements = tbodyElement.tagName("tr").children();

            bankrupts.addAll(valueToBankrupt(elements));
        }

        for (int i = 1; i <= 20; i++) {
            doc = getDocReport(i, date);
            Element tableElement = doc.getElementsByAttributeValue("class", "bank").first();
            Element tbodyElement = tableElement.child(0);
            if(tbodyElement.childNodeSize() < 3){
                break;
            }
            Elements elements = tbodyElement.tagName("tr").children();

            bankrupts.addAll(valueToBankrupt(elements));
        }

        return bankrupts;
    }

    List<Bankrupt> valueToBankrupt(Elements elements){
        List<Bankrupt> bankrupts = new ArrayList<Bankrupt>();
        for (Element element : elements) {
            List<String> values = getValue(element);
            if (values.size() == 6) {
                String date = values.get(0);
                String type = values.get(1);
                String debtor = values.get(2);
                String address = values.get(3);
                String published = values.get(4);

                bankrupts.add(new Bankrupt(date, type, debtor, address, published, values.get(5)));

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
        if(element.children().size() > 4) {
            Element e = element.child(4);
            String s = e.child(0).attr("href");
            values.add("https://bankrot.fedresurs.ru" + s);
        }
        return values;

    }

    protected Document getDocInventory(int p, String date) {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        String currentDate = day + "." + month + "." + year;
        String page = "Page$%d";
        Document doc = null;
        try {
            Connection.Response response = Jsoup.connect(URL)
                    .data("ctl00$cphBody$mdsMessageType$tbSelectedText", "Сведения о результатах инвентаризации имущества должника")
                    .data("ctl00$cphBody$mdsMessageType$hfSelectedValue", "PropertyInventoryResult")
                    .data("ctl00$PrivateOffice1$ctl00", "ctl00$cphBody$upMessages|ctl00$cphBody$gvMessages")
                    .data("__ASYNCPOST", "true")
                    .data("ctl00$cphBody$cldrBeginDate$tbSelectedDate", date)
                    .data("ctl00$cphBody$cldrBeginDate$tbSelectedDateValue", date)
                    .data("ctl00$cphBody$cldrEndDate$tbSelectedDate", currentDate)
                    .data("ctl00$cphBody$cldrEndDate$tbSelectedDateValue", currentDate)
                    .data("__EVENTARGUMENT", String.format(page, p))
                    .data("__EVENTTARGET", "ctl00$cphBody$gvMessages")
                    .data("__VIEWSTATE", "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0LjQvdCw0L3RgdC+0LLQvtCz0L4g0L7Qt9C00L7RgNC+0LLQu9C10L3QuNGPyAHQviDQv9GA0LjQt9C90LDQvdC40Lgg0L7QsdC+0YHQvdC+0LLQsNC90L3Ri9C8INC30LDRj9Cy0LvQtdC90LjRjyDQviDQv9GA0LjQt9C90LDQvdC40Lgg0LPRgNCw0LbQtNCw0L3QuNC90LAg0LHQsNC90LrRgNC+0YLQvtC8INC4INCy0LLQtdC00LXQvdC40Lgg0YDQtdGB0YLRgNGD0LrRgtGD0YDQuNC30LDRhtC40Lgg0LXQs9C+INC00L7Qu9Cz0L7Qsn3QviDQv9GA0LjQt9C90LDQvdC40Lgg0LTQvtC70LbQvdC40LrQsCDQsdCw0L3QutGA0L7RgtC+0Lwg0Lgg0L7RgtC60YDRi9GC0LjQuCDQutC+0L3QutGD0YDRgdC90L7Qs9C+INC/0YDQvtC40LfQstC+0LTRgdGC0LLQsEvQvtCxINC+0YLQutCw0LfQtSDQsiDQv9GA0LjQt9C90LDQvdC40Lgg0LTQvtC70LbQvdC40LrQsCDQsdCw0L3QutGA0L7RgtC+0LyaAdC+INC/0YDQuNC80LXQvdC10L3QuNC4INC/0YDQuCDQsdCw0L3QutGA0L7RgtGB0YLQstC1INC00L7Qu9C20L3QuNC60LAg0L/RgNCw0LLQuNC7INC/0LDRgNCw0LPRgNCw0YTQsCDCq9CR0LDQvdC60YDQvtGC0YHRgtCy0L4g0LfQsNGB0YLRgNC+0LnRidC40LrQvtCywrtr0L4g0L/QtdGA0LXQtNCw0YfQtSDQtNC10LvQsCDQvdCwINGA0LDRgdGB0LzQvtGC0YDQtdC90LjQtSDQtNGA0YPQs9C+0LPQviDQsNGA0LHQuNGC0YDQsNC20L3QvtCz0L4g0YHRg9C00LBp0L7QsSDRg9GC0LLQtdGA0LbQtNC10L3QuNC4INC/0LvQsNC90LAg0YDQtdGB0YLRgNGD0LrRgtGD0YDQuNC30LDRhtC40Lgg0LTQvtC70LPQvtCyINCz0YDQsNC20LTQsNC90LjQvdCwWtC+INC30LDQstC10YDRiNC10L3QuNC4INGA0LXRgdGC0YDRg9C60YLRg9GA0LjQt9Cw0YbQuNC4INC00L7Qu9Cz0L7QsiDQs9GA0LDQttC00LDQvdC40L3QsI4B0L4g0L/RgNC40LfQvdCw0L3QuNC4INCz0YDQsNC20LTQsNC90LjQvdCwINCx0LDQvdC60YDQvtGC0L7QvCDQuCDQstCy0LXQtNC10L3QuNC4INGA0LXQsNC70LjQt9Cw0YbQuNC4INC40LzRg9GJ0LXRgdGC0LLQsCDQs9GA0LDQttC00LDQvdC40L3QsKYB0L4g0L3QtdC/0YDQuNC80LXQvdC10L3QuNC4INCyINC+0YLQvdC+0YjQtdC90LjQuCDQs9GA0LDQttC00LDQvdC40L3QsCDQv9GA0LDQstC40LvQsCDQvtCxINC+0YHQstC+0LHQvtC20LTQtdC90LjQuCDQvtGCINC40YHQv9C+0LvQvdC10L3QuNGPINC+0LHRj9C30LDRgtC10LvRjNGB0YLQslTQviDQt9Cw0LLQtdGA0YjQtdC90LjQuCDRgNC10LDQu9C40LfQsNGG0LjQuCDQuNC80YPRidC10YHRgtCy0LAg0LPRgNCw0LbQtNCw0L3QuNC90LBH0L4g0LfQsNCy0LXRgNGI0LXQvdC40Lgg0LrQvtC90LrRg9GA0YHQvdC+0LPQviDQv9GA0L7QuNC30LLQvtC00YHRgtCy0LBA0L4g0L/RgNC10LrRgNCw0YnQtdC90LjQuCDQv9GA0L7QuNC30LLQvtC00YHRgtCy0LAg0L/QviDQtNC10LvRg4MB0L4g0LLQvtC30L7QsdC90L7QstC70LXQvdC40Lgg0L/RgNC+0LjQt9Cy0L7QtNGB0YLQstCwINC/0L4g0LTQtdC70YMg0L4g0L3QtdGB0L7RgdGC0L7Rj9GC0LXQu9GM0L3QvtGB0YLQuCAo0LHQsNC90LrRgNC+0YLRgdGC0LLQtSlN0L7QsSDRg9GC0LLQtdGA0LbQtNC10L3QuNC4INCw0YDQsdC40YLRgNCw0LbQvdC+0LPQviDRg9C/0YDQsNCy0LvRj9GO0YnQtdCz0L5t0L7QsSDQvtGB0LLQvtCx0L7QttC00LXQvdC40Lgg0LjQu9C4INC+0YLRgdGC0YDQsNC90LXQvdC40Lgg0LDRgNCx0LjRgtGA0LDQttC90L7Qs9C+INGD0L/RgNCw0LLQu9GP0Y7RidC10LPQvogB0L4g0L/RgNC40LfQvdCw0L3QuNC4INC00LXQudGB0YLQstC40LkgKNCx0LXQt9C00LXQudGB0YLQstC40LkpINCw0YDQsdC40YLRgNCw0LbQvdC+0LPQviDRg9C/0YDQsNCy0LvRj9GO0YnQtdCz0L4g0L3QtdC30LDQutC+0L3QvdGL0LzQuNUB0L4g0LLQt9GL0YHQutCw0L3QuNC4INGBINCw0YDQsdC40YLRgNCw0LbQvdC+0LPQviDRg9C/0YDQsNCy0LvRj9GO0YnQtdCz0L4g0YPQsdGL0YLQutC+0LIg0LIg0YHQstGP0LfQuCDRgSDQvdC10LjRgdC/0L7Qu9C90LXQvdC40LXQvCDQuNC70Lgg0L3QtdC90LDQtNC70LXQttCw0YnQuNC8INC40YHQv9C+0LvQvdC10L3QuNC10Lwg0L7QsdGP0LfQsNC90L3QvtGB0YLQtdC5nQHQvtCxINGD0LTQvtCy0LvQtdGC0LLQvtGA0LXQvdC40Lgg0LfQsNGP0LLQu9C10L3QuNC5INGC0YDQtdGC0YzQuNGFINC70LjRhiDQviDQvdCw0LzQtdGA0LXQvdC40Lgg0L/QvtCz0LDRgdC40YLRjCDQvtCx0Y/Qt9Cw0YLQtdC70YzRgdGC0LLQsCDQtNC+0LvQttC90LjQutCwJtCU0YDRg9Cz0LjQtSDRgdGD0LTQtdCx0L3Ri9C1INCw0LrRgtGLR9C+0LEg0L7RgtC80LXQvdC1INC40LvQuCDQuNC30LzQtdC90LXQvdC40Lgg0YHRg9C00LXQsdC90YvRhSDQsNC60YLQvtCyJNCU0YDRg9Cz0LjQtcKg0L7Qv9GA0LXQtNC10LvQtdC90LjRjxUZAAIxMQExATkCMTgBNwIxMAIyNgIyNwIyMAIyMQIxOQIyNAIyNQIyOAE4ATMBNAE2AjIyAjIzAjE3AjEyATICMTYUKwMZZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2RkAgcPZBYGZg8PZBYCHwQFLE9wZW5Nb2RhbFdpbmRvd19jdGwwMF9jcGhCb2R5X21kc1B1Ymxpc2hlcigpZAIBDw9kFgIfBAUsT3Blbk1vZGFsV2luZG93X2N0bDAwX2NwaEJvZHlfbWRzUHVibGlzaGVyKClkAgIPD2QWAh8EBSJDbGVhcl9jdGwwMF9jcGhCb2R5X21kc1B1Ymxpc2hlcigpZAILD2QWAmYPEA8WAh8FZ2QQFVgAG9CQ0LvRgtCw0LnRgdC60LjQuSDQutGA0LDQuR/QkNC80YPRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCQ0YDRhdCw0L3Qs9C10LvRjNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJ9CQ0YHRgtGA0LDRhdCw0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQkdC10LvQs9C+0YDQvtC00YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywf0JHRgNGP0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQktC70LDQtNC40LzQuNGA0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywp0JLQvtC70LPQvtCz0YDQsNC00YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0JLQvtC70L7Qs9C+0LTRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCXQktC+0YDQvtC90LXQttGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMENCzLiDQnNC+0YHQutCy0LAh0LMuINCh0LDQvdC60YIt0J/QtdGC0LXRgNCx0YPRgNCzGtCzLiDQodC10LLQsNGB0YLQvtC/0L7Qu9GMNtCV0LLRgNC10LnRgdC60LDRjyDQsNCy0YLQvtC90L7QvNC90LDRjyDQvtCx0LvQsNGB0YLRjCPQl9Cw0LHQsNC50LrQsNC70YzRgdC60LjQuSDQutGA0LDQuSPQmNCy0LDQvdC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjEHQmNC90YvQtSDRgtC10YDRgNC40YLQvtGA0LjQuCwg0LLQutC70Y7Rh9Cw0Y8g0LMu0JHQsNC50LrQvtC90YPRgCHQmNGA0LrRg9GC0YHQutCw0Y8g0L7QsdC70LDRgdGC0Yw80JrQsNCx0LDRgNC00LjQvdC+LdCR0LDQu9C60LDRgNGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwLdCa0LDQu9C40L3QuNC90LPRgNCw0LTRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCHQmtCw0LvRg9C20YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywd0JrQsNC80YfQsNGC0YHQutC40Lkg0LrRgNCw0Lk80JrQsNGA0LDRh9Cw0LXQstC+LdCn0LXRgNC60LXRgdGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwJdCa0LXQvNC10YDQvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0JrQuNGA0L7QstGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJdCa0L7RgdGC0YDQvtC80YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywj0JrRgNCw0YHQvdC+0LTQsNGA0YHQutC40Lkg0LrRgNCw0Lkh0JrRgNCw0YHQvdC+0Y/RgNGB0LrQuNC5INC60YDQsNC5I9Ca0YPRgNCz0LDQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMHdCa0YPRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCb0LXQvdC40L3Qs9GA0LDQtNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMH9Cb0LjQv9C10YbQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0JzQsNCz0LDQtNCw0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCPQnNC+0YHQutC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCPQnNGD0YDQvNCw0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjDDQndC10L3QtdGG0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LMp0J3QuNC20LXQs9C+0YDQvtC00YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywn0J3QvtCy0LPQvtGA0L7QtNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCd0L7QstC+0YHQuNCx0LjRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMG9Ce0LzRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQntGA0LXQvdCx0YPRgNCz0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0J7RgNC70L7QstGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMI9Cf0LXQvdC30LXQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMGdCf0LXRgNC80YHQutC40Lkg0LrRgNCw0Lkd0J/RgNC40LzQvtGA0YHQutC40Lkg0LrRgNCw0Lkh0J/RgdC60L7QstGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMIdCg0LXRgdC/0YPQsdC70LjQutCwINCQ0LTRi9Cz0LXRjx/QoNC10YHQv9GD0LHQu9C40LrQsCDQkNC70YLQsNC5LdCg0LXRgdC/0YPQsdC70LjQutCwINCR0LDRiNC60L7RgNGC0L7RgdGC0LDQvSPQoNC10YHQv9GD0LHQu9C40LrQsCDQkdGD0YDRj9GC0LjRjyXQoNC10YHQv9GD0LHQu9C40LrQsCDQlNCw0LPQtdGB0YLQsNC9J9Cg0LXRgdC/0YPQsdC70LjQutCwINCY0L3Qs9GD0YjQtdGC0LjRjyXQoNC10YHQv9GD0LHQu9C40LrQsCDQmtCw0LvQvNGL0LrQuNGPI9Cg0LXRgdC/0YPQsdC70LjQutCwINCa0LDRgNC10LvQuNGPHdCg0LXRgdC/0YPQsdC70LjQutCwINCa0L7QvNC4HdCg0LXRgdC/0YPQsdC70LjQutCwINCa0YDRi9C8JNCg0LXRgdC/0YPQsdC70LjQutCwINCc0LDRgNC40Lkg0K3QuyXQoNC10YHQv9GD0LHQu9C40LrQsCDQnNC+0YDQtNC+0LLQuNGPLNCg0LXRgdC/0YPQsdC70LjQutCwINCh0LDRhdCwICjQr9C60YPRgtC40Y8pQdCg0LXRgdC/0YPQsdC70LjQutCwINCh0LXQstC10YDQvdCw0Y8g0J7RgdC10YLQuNGPIC0g0JDQu9Cw0L3QuNGPJ9Cg0LXRgdC/0YPQsdC70LjQutCwINCi0LDRgtCw0YDRgdGC0LDQvR3QoNC10YHQv9GD0LHQu9C40LrQsCDQotGL0LLQsCPQoNC10YHQv9GD0LHQu9C40LrQsCDQpdCw0LrQsNGB0LjRjyPQoNC+0YHRgtC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCHQoNGP0LfQsNC90YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0KHQsNC80LDRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJdCh0LDRgNCw0YLQvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0KHQsNGF0LDQu9C40L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQodCy0LXRgNC00LvQvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywj0KHQvNC+0LvQtdC90YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0KHRgtCw0LLRgNC+0L/QvtC70YzRgdC60LjQuSDQutGA0LDQuSPQotCw0LzQsdC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjB/QotCy0LXRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMHdCi0L7QvNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMH9Ci0YPQu9GM0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0KLRjtC80LXQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCj0LTQvNGD0YDRgtGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwJdCj0LvRjNGP0L3QvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywf0KXQsNCx0LDRgNC+0LLRgdC60LjQuSDQutGA0LDQuUrQpdCw0L3RgtGLLdCc0LDQvdGB0LjQudGB0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LMgLSDQrtCz0YDQsCXQp9C10LvRj9Cx0LjQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJ9Cn0LXRh9C10L3RgdC60LDRjyDQoNC10YHQv9GD0LHQu9C40LrQsCHQp9C40YLQuNC90YHQutCw0Y8g0L7QsdC70LDRgdGC0Yw40KfRg9Cy0LDRiNGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwIC0g0KfRg9Cy0LDRiNC40Y8y0KfRg9C60L7RgtGB0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LM70K/QvNCw0LvQvi3QndC10L3QtdGG0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LMl0K/RgNC+0YHQu9Cw0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjBVYAAExAjEwAjExAjEyAjE0AjE1AjE3AjE4AjE5AjIwAjQ1AjQwAzIwMQI5OQMxMDECMjQDMjAzAjI1AjgzAjI3AjI5AjMwAjkxAjMyAjMzAjM0ATMBNAIzNwIzOAI0MQI0MgI0NAI0NgI0NwMyMDACMjICNDkCNTACNTICNTMCNTQCNTYCNTcBNQI1OAI3OQI4NAI4MAI4MQI4MgIyNgI4NQI4NgI4NwMyMDICODgCODkCOTgDMTAyAjkyAjkzAjk1AjYwAjYxAjM2AjYzAjY0AjY1AjY2ATcCNjgCMjgCNjkCNzACNzECOTQCNzMBOAMxMDMCNzUCOTYCNzYCOTcCNzcDMTA0Ajc4FCsDWGdnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dkZAIND2QWBmYPD2QWAh8EBSlPcGVuTW9kYWxXaW5kb3dfY3RsMDBfY3BoQm9keV9tZHNEZWJ0b3IoKWQCAQ8PZBYCHwQFKU9wZW5Nb2RhbFdpbmRvd19jdGwwMF9jcGhCb2R5X21kc0RlYnRvcigpZAICDw9kFgIfBAUfQ2xlYXJfY3RsMDBfY3BoQm9keV9tZHNEZWJ0b3IoKWQCDw9kFggCAw8PZBYEHghvbmNoYW5nZQU2U2V0SGlkZGVuRmllbGRfY3RsMDBfY3BoQm9keV9jbGRyQmVnaW5EYXRlKHRoaXMudmFsdWUpHgpvbmtleXByZXNzBTZTZXRIaWRkZW5GaWVsZF9jdGwwMF9jcGhCb2R5X2NsZHJCZWdpbkRhdGUodGhpcy52YWx1ZSlkAgUPD2QWAh8EBSpTaG93Q2FsZW5kYXJfY3RsMDBfY3BoQm9keV9jbGRyQmVnaW5EYXRlKClkAgYPD2QWBB4FU3R5bGUFMGN1cnNvcjogcG9pbnRlcjsgdmlzaWJpbGl0eTpoaWRkZW47IGRpc3BsYXk6bm9uZR8EBShDbGVhcklucHV0X2N0bDAwX2NwaEJvZHlfY2xkckJlZ2luRGF0ZSgpZAIHDw8WAh4YQ2xpZW50VmFsaWRhdGlvbkZ1bmN0aW9uBSlWYWxpZGF0ZUlucHV0X2N0bDAwX2NwaEJvZHlfY2xkckJlZ2luRGF0ZWRkAhEPZBYIAgMPD2QWBB8GBTRTZXRIaWRkZW5GaWVsZF9jdGwwMF9jcGhCb2R5X2NsZHJFbmREYXRlKHRoaXMudmFsdWUpHwcFNFNldEhpZGRlbkZpZWxkX2N0bDAwX2NwaEJvZHlfY2xkckVuZERhdGUodGhpcy52YWx1ZSlkAgUPD2QWAh8EBShTaG93Q2FsZW5kYXJfY3RsMDBfY3BoQm9keV9jbGRyRW5kRGF0ZSgpZAIGDw9kFgQfCAUwY3Vyc29yOiBwb2ludGVyOyB2aXNpYmlsaXR5OmhpZGRlbjsgZGlzcGxheTpub25lHwQFJkNsZWFySW5wdXRfY3RsMDBfY3BoQm9keV9jbGRyRW5kRGF0ZSgpZAIHDw8WAh8JBSdWYWxpZGF0ZUlucHV0X2N0bDAwX2NwaEJvZHlfY2xkckVuZERhdGVkZAIbD2QWAmYPZBYCZg9kFgQCAQ9kFgJmD2QWBGYPZBYCAgMPDxYCHwBoZBYCAgEPZBYCAgIPDxYCHwBoZGQCAQ9kFgICAw8PFgIfAGhkFgICAQ9kFgICAg8PFgIfAGhkZAIDDxYCHgdWaXNpYmxlaGQCAw9kFgJmD2QWAgIHD2QWAmYPFgIeBXN0eWxlBSBwb3NpdGlvbjogcmVsYXRpdmU7IGJvdHRvbTogMjVweGQYAgUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhMFFmN0bDAwJHJhZFdpbmRvd01hbmFnZXIFKWN0bDAwJFByaXZhdGVPZmZpY2UxJGliUHJpdmF0ZU9mZmljZUVudGVyBSFjdGwwMCRQcml2YXRlT2ZmaWNlMSRjYlJlbWVtYmVyTWUFIGN0bDAwJFByaXZhdGVPZmZpY2UxJFJhZFRvb2xUaXAxBR9jdGwwMCRQcml2YXRlT2ZmaWNlMSRpYnRSZXN0b3JlBSJjdGwwMCREZWJ0b3JTZWFyY2gxJGliRGVidG9yU2VhcmNoBRZjdGwwMCRjcGhCb2R5JGNiV2l0aEF1BR1jdGwwMCRjcGhCb2R5JGNiV2l0aFZpb2xhdGlvbgUeY3RsMDAkY3BoQm9keSRpYk1lc3NhZ2VzU2VhcmNoBRZjdGwwMCRjcGhCb2R5JGltZ0NsZWFyBStjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyRyYWRUb29sVGlwSW1nBUNjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyR1Y1N1YnNjcmliZXJMb2dpbkltZyRpYkVudGVyU3Vic2NyaWJlBUJjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyR1Y1N1YnNjcmliZXJMb2dpbkltZyRQYXNzd29yZFRvb2xUaXAFSWN0bDAwJGNwaEJvZHkkdWNBZGRNb25pdG9yaW5nJHVjU3Vic2NyaWJlckxvZ2luSW1nJFJlbWVtYmVyUGFzc3dvcmRCdXR0b24FLGN0bDAwJGNwaEJvZHkkdWNBZGRNb25pdG9yaW5nJHJhZFRvb2xUaXBIcmVmBURjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyR1Y1N1YnNjcmliZXJMb2dpbkhyZWYkaWJFbnRlclN1YnNjcmliZQVDY3RsMDAkY3BoQm9keSR1Y0FkZE1vbml0b3JpbmckdWNTdWJzY3JpYmVyTG9naW5IcmVmJFBhc3N3b3JkVG9vbFRpcAVKY3RsMDAkY3BoQm9keSR1Y0FkZE1vbml0b3JpbmckdWNTdWJzY3JpYmVyTG9naW5IcmVmJFJlbWVtYmVyUGFzc3dvcmRCdXR0b24FG2N0bDAwJGNwaEJvZHkkaWJFeGNlbEV4cG9ydAUYY3RsMDAkY3BoQm9keSRndk1lc3NhZ2VzDzwrAAwBCAIUZA==")
                    .data("__PREVIOUSPAGE", "u0YJjgLPY8IcrrwtjyQQyAByUrDnIF2hgMnHcGz5GQsVuQzVrMYdXP131VSJH6EoLidaUV2RDa6yf_YOiLwgUzYJ6Qk1")
                    .method(Connection.Method.POST)
                    .execute();
            doc = response.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

    protected Document getDocReport(int p, String date) {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.valueOf(calendar.get(Calendar.MONTH)+1);
        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

        String currentDate = day + "." + month + "." + year;
        String page = "Page$%d";
        Document doc = null;
        try {

            Connection.Response response = Jsoup.connect(URL)
                    .data("ctl00$cphBody$mdsMessageType$tbSelectedText", "Отчет оценщика об оценке имущества должника")
                    .data("ctl00$cphBody$mdsMessageType$hfSelectedValue", "PropertyEvaluationReport")
                    .data("ctl00$PrivateOffice1$ctl00", "ctl00$cphBody$upMessages|ctl00$cphBody$gvMessages")
                    .data("__ASYNCPOST", "true")
                    .data("ctl00$cphBody$cldrBeginDate$tbSelectedDate", date)
                    .data("ctl00$cphBody$cldrBeginDate$tbSelectedDateValue", date)
                    .data("ctl00$cphBody$cldrEndDate$tbSelectedDate", currentDate)
                    .data("ctl00$cphBody$cldrEndDate$tbSelectedDateValue", currentDate)
                    .data("__EVENTARGUMENT", String.format(page, p))
                    .data("__EVENTTARGET", "ctl00$cphBody$gvMessages")
                    .data("__VIEWSTATE", "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0LjQvdCw0L3RgdC+0LLQvtCz0L4g0L7Qt9C00L7RgNC+0LLQu9C10L3QuNGPyAHQviDQv9GA0LjQt9C90LDQvdC40Lgg0L7QsdC+0YHQvdC+0LLQsNC90L3Ri9C8INC30LDRj9Cy0LvQtdC90LjRjyDQviDQv9GA0LjQt9C90LDQvdC40Lgg0LPRgNCw0LbQtNCw0L3QuNC90LAg0LHQsNC90LrRgNC+0YLQvtC8INC4INCy0LLQtdC00LXQvdC40Lgg0YDQtdGB0YLRgNGD0LrRgtGD0YDQuNC30LDRhtC40Lgg0LXQs9C+INC00L7Qu9Cz0L7Qsn3QviDQv9GA0LjQt9C90LDQvdC40Lgg0LTQvtC70LbQvdC40LrQsCDQsdCw0L3QutGA0L7RgtC+0Lwg0Lgg0L7RgtC60YDRi9GC0LjQuCDQutC+0L3QutGD0YDRgdC90L7Qs9C+INC/0YDQvtC40LfQstC+0LTRgdGC0LLQsEvQvtCxINC+0YLQutCw0LfQtSDQsiDQv9GA0LjQt9C90LDQvdC40Lgg0LTQvtC70LbQvdC40LrQsCDQsdCw0L3QutGA0L7RgtC+0LyaAdC+INC/0YDQuNC80LXQvdC10L3QuNC4INC/0YDQuCDQsdCw0L3QutGA0L7RgtGB0YLQstC1INC00L7Qu9C20L3QuNC60LAg0L/RgNCw0LLQuNC7INC/0LDRgNCw0LPRgNCw0YTQsCDCq9CR0LDQvdC60YDQvtGC0YHRgtCy0L4g0LfQsNGB0YLRgNC+0LnRidC40LrQvtCywrtr0L4g0L/QtdGA0LXQtNCw0YfQtSDQtNC10LvQsCDQvdCwINGA0LDRgdGB0LzQvtGC0YDQtdC90LjQtSDQtNGA0YPQs9C+0LPQviDQsNGA0LHQuNGC0YDQsNC20L3QvtCz0L4g0YHRg9C00LBp0L7QsSDRg9GC0LLQtdGA0LbQtNC10L3QuNC4INC/0LvQsNC90LAg0YDQtdGB0YLRgNGD0LrRgtGD0YDQuNC30LDRhtC40Lgg0LTQvtC70LPQvtCyINCz0YDQsNC20LTQsNC90LjQvdCwWtC+INC30LDQstC10YDRiNC10L3QuNC4INGA0LXRgdGC0YDRg9C60YLRg9GA0LjQt9Cw0YbQuNC4INC00L7Qu9Cz0L7QsiDQs9GA0LDQttC00LDQvdC40L3QsI4B0L4g0L/RgNC40LfQvdCw0L3QuNC4INCz0YDQsNC20LTQsNC90LjQvdCwINCx0LDQvdC60YDQvtGC0L7QvCDQuCDQstCy0LXQtNC10L3QuNC4INGA0LXQsNC70LjQt9Cw0YbQuNC4INC40LzRg9GJ0LXRgdGC0LLQsCDQs9GA0LDQttC00LDQvdC40L3QsKYB0L4g0L3QtdC/0YDQuNC80LXQvdC10L3QuNC4INCyINC+0YLQvdC+0YjQtdC90LjQuCDQs9GA0LDQttC00LDQvdC40L3QsCDQv9GA0LDQstC40LvQsCDQvtCxINC+0YHQstC+0LHQvtC20LTQtdC90LjQuCDQvtGCINC40YHQv9C+0LvQvdC10L3QuNGPINC+0LHRj9C30LDRgtC10LvRjNGB0YLQslTQviDQt9Cw0LLQtdGA0YjQtdC90LjQuCDRgNC10LDQu9C40LfQsNGG0LjQuCDQuNC80YPRidC10YHRgtCy0LAg0LPRgNCw0LbQtNCw0L3QuNC90LBH0L4g0LfQsNCy0LXRgNGI0LXQvdC40Lgg0LrQvtC90LrRg9GA0YHQvdC+0LPQviDQv9GA0L7QuNC30LLQvtC00YHRgtCy0LBA0L4g0L/RgNC10LrRgNCw0YnQtdC90LjQuCDQv9GA0L7QuNC30LLQvtC00YHRgtCy0LAg0L/QviDQtNC10LvRg4MB0L4g0LLQvtC30L7QsdC90L7QstC70LXQvdC40Lgg0L/RgNC+0LjQt9Cy0L7QtNGB0YLQstCwINC/0L4g0LTQtdC70YMg0L4g0L3QtdGB0L7RgdGC0L7Rj9GC0LXQu9GM0L3QvtGB0YLQuCAo0LHQsNC90LrRgNC+0YLRgdGC0LLQtSlN0L7QsSDRg9GC0LLQtdGA0LbQtNC10L3QuNC4INCw0YDQsdC40YLRgNCw0LbQvdC+0LPQviDRg9C/0YDQsNCy0LvRj9GO0YnQtdCz0L5t0L7QsSDQvtGB0LLQvtCx0L7QttC00LXQvdC40Lgg0LjQu9C4INC+0YLRgdGC0YDQsNC90LXQvdC40Lgg0LDRgNCx0LjRgtGA0LDQttC90L7Qs9C+INGD0L/RgNCw0LLQu9GP0Y7RidC10LPQvogB0L4g0L/RgNC40LfQvdCw0L3QuNC4INC00LXQudGB0YLQstC40LkgKNCx0LXQt9C00LXQudGB0YLQstC40LkpINCw0YDQsdC40YLRgNCw0LbQvdC+0LPQviDRg9C/0YDQsNCy0LvRj9GO0YnQtdCz0L4g0L3QtdC30LDQutC+0L3QvdGL0LzQuNUB0L4g0LLQt9GL0YHQutCw0L3QuNC4INGBINCw0YDQsdC40YLRgNCw0LbQvdC+0LPQviDRg9C/0YDQsNCy0LvRj9GO0YnQtdCz0L4g0YPQsdGL0YLQutC+0LIg0LIg0YHQstGP0LfQuCDRgSDQvdC10LjRgdC/0L7Qu9C90LXQvdC40LXQvCDQuNC70Lgg0L3QtdC90LDQtNC70LXQttCw0YnQuNC8INC40YHQv9C+0LvQvdC10L3QuNC10Lwg0L7QsdGP0LfQsNC90L3QvtGB0YLQtdC5nQHQvtCxINGD0LTQvtCy0LvQtdGC0LLQvtGA0LXQvdC40Lgg0LfQsNGP0LLQu9C10L3QuNC5INGC0YDQtdGC0YzQuNGFINC70LjRhiDQviDQvdCw0LzQtdGA0LXQvdC40Lgg0L/QvtCz0LDRgdC40YLRjCDQvtCx0Y/Qt9Cw0YLQtdC70YzRgdGC0LLQsCDQtNC+0LvQttC90LjQutCwJtCU0YDRg9Cz0LjQtSDRgdGD0LTQtdCx0L3Ri9C1INCw0LrRgtGLR9C+0LEg0L7RgtC80LXQvdC1INC40LvQuCDQuNC30LzQtdC90LXQvdC40Lgg0YHRg9C00LXQsdC90YvRhSDQsNC60YLQvtCyJNCU0YDRg9Cz0LjQtcKg0L7Qv9GA0LXQtNC10LvQtdC90LjRjxUZAAIxMQExATkCMTgBNwIxMAIyNgIyNwIyMAIyMQIxOQIyNAIyNQIyOAE4ATMBNAE2AjIyAjIzAjE3AjEyATICMTYUKwMZZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2RkAgcPZBYGZg8PZBYCHwQFLE9wZW5Nb2RhbFdpbmRvd19jdGwwMF9jcGhCb2R5X21kc1B1Ymxpc2hlcigpZAIBDw9kFgIfBAUsT3Blbk1vZGFsV2luZG93X2N0bDAwX2NwaEJvZHlfbWRzUHVibGlzaGVyKClkAgIPD2QWAh8EBSJDbGVhcl9jdGwwMF9jcGhCb2R5X21kc1B1Ymxpc2hlcigpZAILD2QWAmYPEA8WAh8FZ2QQFVgAG9CQ0LvRgtCw0LnRgdC60LjQuSDQutGA0LDQuR/QkNC80YPRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCQ0YDRhdCw0L3Qs9C10LvRjNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJ9CQ0YHRgtGA0LDRhdCw0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQkdC10LvQs9C+0YDQvtC00YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywf0JHRgNGP0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQktC70LDQtNC40LzQuNGA0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywp0JLQvtC70LPQvtCz0YDQsNC00YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0JLQvtC70L7Qs9C+0LTRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCXQktC+0YDQvtC90LXQttGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMENCzLiDQnNC+0YHQutCy0LAh0LMuINCh0LDQvdC60YIt0J/QtdGC0LXRgNCx0YPRgNCzGtCzLiDQodC10LLQsNGB0YLQvtC/0L7Qu9GMNtCV0LLRgNC10LnRgdC60LDRjyDQsNCy0YLQvtC90L7QvNC90LDRjyDQvtCx0LvQsNGB0YLRjCPQl9Cw0LHQsNC50LrQsNC70YzRgdC60LjQuSDQutGA0LDQuSPQmNCy0LDQvdC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjEHQmNC90YvQtSDRgtC10YDRgNC40YLQvtGA0LjQuCwg0LLQutC70Y7Rh9Cw0Y8g0LMu0JHQsNC50LrQvtC90YPRgCHQmNGA0LrRg9GC0YHQutCw0Y8g0L7QsdC70LDRgdGC0Yw80JrQsNCx0LDRgNC00LjQvdC+LdCR0LDQu9C60LDRgNGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwLdCa0LDQu9C40L3QuNC90LPRgNCw0LTRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCHQmtCw0LvRg9C20YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywd0JrQsNC80YfQsNGC0YHQutC40Lkg0LrRgNCw0Lk80JrQsNGA0LDRh9Cw0LXQstC+LdCn0LXRgNC60LXRgdGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwJdCa0LXQvNC10YDQvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0JrQuNGA0L7QstGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJdCa0L7RgdGC0YDQvtC80YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywj0JrRgNCw0YHQvdC+0LTQsNGA0YHQutC40Lkg0LrRgNCw0Lkh0JrRgNCw0YHQvdC+0Y/RgNGB0LrQuNC5INC60YDQsNC5I9Ca0YPRgNCz0LDQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMHdCa0YPRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCb0LXQvdC40L3Qs9GA0LDQtNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMH9Cb0LjQv9C10YbQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0JzQsNCz0LDQtNCw0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCPQnNC+0YHQutC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCPQnNGD0YDQvNCw0L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjDDQndC10L3QtdGG0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LMp0J3QuNC20LXQs9C+0YDQvtC00YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywn0J3QvtCy0LPQvtGA0L7QtNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCd0L7QstC+0YHQuNCx0LjRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMG9Ce0LzRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQntGA0LXQvdCx0YPRgNCz0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0J7RgNC70L7QstGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMI9Cf0LXQvdC30LXQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMGdCf0LXRgNC80YHQutC40Lkg0LrRgNCw0Lkd0J/RgNC40LzQvtGA0YHQutC40Lkg0LrRgNCw0Lkh0J/RgdC60L7QstGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMIdCg0LXRgdC/0YPQsdC70LjQutCwINCQ0LTRi9Cz0LXRjx/QoNC10YHQv9GD0LHQu9C40LrQsCDQkNC70YLQsNC5LdCg0LXRgdC/0YPQsdC70LjQutCwINCR0LDRiNC60L7RgNGC0L7RgdGC0LDQvSPQoNC10YHQv9GD0LHQu9C40LrQsCDQkdGD0YDRj9GC0LjRjyXQoNC10YHQv9GD0LHQu9C40LrQsCDQlNCw0LPQtdGB0YLQsNC9J9Cg0LXRgdC/0YPQsdC70LjQutCwINCY0L3Qs9GD0YjQtdGC0LjRjyXQoNC10YHQv9GD0LHQu9C40LrQsCDQmtCw0LvQvNGL0LrQuNGPI9Cg0LXRgdC/0YPQsdC70LjQutCwINCa0LDRgNC10LvQuNGPHdCg0LXRgdC/0YPQsdC70LjQutCwINCa0L7QvNC4HdCg0LXRgdC/0YPQsdC70LjQutCwINCa0YDRi9C8JNCg0LXRgdC/0YPQsdC70LjQutCwINCc0LDRgNC40Lkg0K3QuyXQoNC10YHQv9GD0LHQu9C40LrQsCDQnNC+0YDQtNC+0LLQuNGPLNCg0LXRgdC/0YPQsdC70LjQutCwINCh0LDRhdCwICjQr9C60YPRgtC40Y8pQdCg0LXRgdC/0YPQsdC70LjQutCwINCh0LXQstC10YDQvdCw0Y8g0J7RgdC10YLQuNGPIC0g0JDQu9Cw0L3QuNGPJ9Cg0LXRgdC/0YPQsdC70LjQutCwINCi0LDRgtCw0YDRgdGC0LDQvR3QoNC10YHQv9GD0LHQu9C40LrQsCDQotGL0LLQsCPQoNC10YHQv9GD0LHQu9C40LrQsCDQpdCw0LrQsNGB0LjRjyPQoNC+0YHRgtC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjCHQoNGP0LfQsNC90YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0KHQsNC80LDRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJdCh0LDRgNCw0YLQvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0KHQsNGF0LDQu9C40L3RgdC60LDRjyDQvtCx0LvQsNGB0YLRjCfQodCy0LXRgNC00LvQvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywj0KHQvNC+0LvQtdC90YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywl0KHRgtCw0LLRgNC+0L/QvtC70YzRgdC60LjQuSDQutGA0LDQuSPQotCw0LzQsdC+0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjB/QotCy0LXRgNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMHdCi0L7QvNGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMH9Ci0YPQu9GM0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywh0KLRjtC80LXQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMKdCj0LTQvNGD0YDRgtGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwJdCj0LvRjNGP0L3QvtCy0YHQutCw0Y8g0L7QsdC70LDRgdGC0Ywf0KXQsNCx0LDRgNC+0LLRgdC60LjQuSDQutGA0LDQuUrQpdCw0L3RgtGLLdCc0LDQvdGB0LjQudGB0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LMgLSDQrtCz0YDQsCXQp9C10LvRj9Cx0LjQvdGB0LrQsNGPINC+0LHQu9Cw0YHRgtGMJ9Cn0LXRh9C10L3RgdC60LDRjyDQoNC10YHQv9GD0LHQu9C40LrQsCHQp9C40YLQuNC90YHQutCw0Y8g0L7QsdC70LDRgdGC0Yw40KfRg9Cy0LDRiNGB0LrQsNGPINCg0LXRgdC/0YPQsdC70LjQutCwIC0g0KfRg9Cy0LDRiNC40Y8y0KfRg9C60L7RgtGB0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LM70K/QvNCw0LvQvi3QndC10L3QtdGG0LrQuNC5INCw0LLRgtC+0L3QvtC80L3Ri9C5INC+0LrRgNGD0LMl0K/RgNC+0YHQu9Cw0LLRgdC60LDRjyDQvtCx0LvQsNGB0YLRjBVYAAExAjEwAjExAjEyAjE0AjE1AjE3AjE4AjE5AjIwAjQ1AjQwAzIwMQI5OQMxMDECMjQDMjAzAjI1AjgzAjI3AjI5AjMwAjkxAjMyAjMzAjM0ATMBNAIzNwIzOAI0MQI0MgI0NAI0NgI0NwMyMDACMjICNDkCNTACNTICNTMCNTQCNTYCNTcBNQI1OAI3OQI4NAI4MAI4MQI4MgIyNgI4NQI4NgI4NwMyMDICODgCODkCOTgDMTAyAjkyAjkzAjk1AjYwAjYxAjM2AjYzAjY0AjY1AjY2ATcCNjgCMjgCNjkCNzACNzECOTQCNzMBOAMxMDMCNzUCOTYCNzYCOTcCNzcDMTA0Ajc4FCsDWGdnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dnZ2dkZAIND2QWBmYPD2QWAh8EBSlPcGVuTW9kYWxXaW5kb3dfY3RsMDBfY3BoQm9keV9tZHNEZWJ0b3IoKWQCAQ8PZBYCHwQFKU9wZW5Nb2RhbFdpbmRvd19jdGwwMF9jcGhCb2R5X21kc0RlYnRvcigpZAICDw9kFgIfBAUfQ2xlYXJfY3RsMDBfY3BoQm9keV9tZHNEZWJ0b3IoKWQCDw9kFggCAw8PZBYEHghvbmNoYW5nZQU2U2V0SGlkZGVuRmllbGRfY3RsMDBfY3BoQm9keV9jbGRyQmVnaW5EYXRlKHRoaXMudmFsdWUpHgpvbmtleXByZXNzBTZTZXRIaWRkZW5GaWVsZF9jdGwwMF9jcGhCb2R5X2NsZHJCZWdpbkRhdGUodGhpcy52YWx1ZSlkAgUPD2QWAh8EBSpTaG93Q2FsZW5kYXJfY3RsMDBfY3BoQm9keV9jbGRyQmVnaW5EYXRlKClkAgYPD2QWBB4FU3R5bGUFMGN1cnNvcjogcG9pbnRlcjsgdmlzaWJpbGl0eTpoaWRkZW47IGRpc3BsYXk6bm9uZR8EBShDbGVhcklucHV0X2N0bDAwX2NwaEJvZHlfY2xkckJlZ2luRGF0ZSgpZAIHDw8WAh4YQ2xpZW50VmFsaWRhdGlvbkZ1bmN0aW9uBSlWYWxpZGF0ZUlucHV0X2N0bDAwX2NwaEJvZHlfY2xkckJlZ2luRGF0ZWRkAhEPZBYIAgMPD2QWBB8GBTRTZXRIaWRkZW5GaWVsZF9jdGwwMF9jcGhCb2R5X2NsZHJFbmREYXRlKHRoaXMudmFsdWUpHwcFNFNldEhpZGRlbkZpZWxkX2N0bDAwX2NwaEJvZHlfY2xkckVuZERhdGUodGhpcy52YWx1ZSlkAgUPD2QWAh8EBShTaG93Q2FsZW5kYXJfY3RsMDBfY3BoQm9keV9jbGRyRW5kRGF0ZSgpZAIGDw9kFgQfCAUwY3Vyc29yOiBwb2ludGVyOyB2aXNpYmlsaXR5OmhpZGRlbjsgZGlzcGxheTpub25lHwQFJkNsZWFySW5wdXRfY3RsMDBfY3BoQm9keV9jbGRyRW5kRGF0ZSgpZAIHDw8WAh8JBSdWYWxpZGF0ZUlucHV0X2N0bDAwX2NwaEJvZHlfY2xkckVuZERhdGVkZAIbD2QWAmYPZBYCZg9kFgQCAQ9kFgJmD2QWBGYPZBYCAgMPDxYCHwBoZBYCAgEPZBYCAgIPDxYCHwBoZGQCAQ9kFgICAw8PFgIfAGhkFgICAQ9kFgICAg8PFgIfAGhkZAIDDxYCHgdWaXNpYmxlaGQCAw9kFgJmD2QWAgIHD2QWAmYPFgIeBXN0eWxlBSBwb3NpdGlvbjogcmVsYXRpdmU7IGJvdHRvbTogMjVweGQYAgUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFhMFFmN0bDAwJHJhZFdpbmRvd01hbmFnZXIFKWN0bDAwJFByaXZhdGVPZmZpY2UxJGliUHJpdmF0ZU9mZmljZUVudGVyBSFjdGwwMCRQcml2YXRlT2ZmaWNlMSRjYlJlbWVtYmVyTWUFIGN0bDAwJFByaXZhdGVPZmZpY2UxJFJhZFRvb2xUaXAxBR9jdGwwMCRQcml2YXRlT2ZmaWNlMSRpYnRSZXN0b3JlBSJjdGwwMCREZWJ0b3JTZWFyY2gxJGliRGVidG9yU2VhcmNoBRZjdGwwMCRjcGhCb2R5JGNiV2l0aEF1BR1jdGwwMCRjcGhCb2R5JGNiV2l0aFZpb2xhdGlvbgUeY3RsMDAkY3BoQm9keSRpYk1lc3NhZ2VzU2VhcmNoBRZjdGwwMCRjcGhCb2R5JGltZ0NsZWFyBStjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyRyYWRUb29sVGlwSW1nBUNjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyR1Y1N1YnNjcmliZXJMb2dpbkltZyRpYkVudGVyU3Vic2NyaWJlBUJjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyR1Y1N1YnNjcmliZXJMb2dpbkltZyRQYXNzd29yZFRvb2xUaXAFSWN0bDAwJGNwaEJvZHkkdWNBZGRNb25pdG9yaW5nJHVjU3Vic2NyaWJlckxvZ2luSW1nJFJlbWVtYmVyUGFzc3dvcmRCdXR0b24FLGN0bDAwJGNwaEJvZHkkdWNBZGRNb25pdG9yaW5nJHJhZFRvb2xUaXBIcmVmBURjdGwwMCRjcGhCb2R5JHVjQWRkTW9uaXRvcmluZyR1Y1N1YnNjcmliZXJMb2dpbkhyZWYkaWJFbnRlclN1YnNjcmliZQVDY3RsMDAkY3BoQm9keSR1Y0FkZE1vbml0b3JpbmckdWNTdWJzY3JpYmVyTG9naW5IcmVmJFBhc3N3b3JkVG9vbFRpcAVKY3RsMDAkY3BoQm9keSR1Y0FkZE1vbml0b3JpbmckdWNTdWJzY3JpYmVyTG9naW5IcmVmJFJlbWVtYmVyUGFzc3dvcmRCdXR0b24FG2N0bDAwJGNwaEJvZHkkaWJFeGNlbEV4cG9ydAUYY3RsMDAkY3BoQm9keSRndk1lc3NhZ2VzDzwrAAwBCAIUZA==")
                    .data("__PREVIOUSPAGE", "u0YJjgLPY8IcrrwtjyQQyAByUrDnIF2hgMnHcGz5GQsVuQzVrMYdXP131VSJH6EoLidaUV2RDa6yf_YOiLwgUzYJ6Qk1")
                    .method(Connection.Method.POST)
                    .execute();
            doc = response.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }
}
