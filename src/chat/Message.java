package chat;

/**
 * Created by Dongfang on 2015/12/17.
 */
public class Message {

    private String subject;
    private String content;
    private String sender;
    private String time;

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

}
