
public class Bankrupt{
    private String date;
    private String typeOfMessage;
    private String debtor;
    private String address;
    private String published;
    private String link;

    public Bankrupt(String date, String typeOfMessage, String debtor, String address, String published, String link) {
        this.date = date;
        this.typeOfMessage = typeOfMessage;
        this.debtor = debtor;
        this.address = address;
        this.published = published;
        this.link = link;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTypeOfMessage() {
        return typeOfMessage;
    }

    public void setTypeOfMessage(String typeOfMessage) {
        this.typeOfMessage = typeOfMessage;
    }

    public String getDebtor() {
        return debtor;
    }

    public void setDebtor(String debtor) {
        this.debtor = debtor;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPublished() {
        return published;
    }

    public void setPublished(String published) {
        this.published = published;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Дата: " + date + '\'' +
                ", Тип сообщения: '" + typeOfMessage + '\'' +
                ", Должник: '" + debtor + '\'' +
                ", Адрес: '" + address + '\'' +
                ", Кем опубликовано: '" + published + '\'' +
                ", --- '" + link + '\'' +
                '}';
    }
}
