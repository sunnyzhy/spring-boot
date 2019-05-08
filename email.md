# pom.xml
```xml
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-context-support</artifactId>
        </dependency>
        <dependency>
            <groupId >com.sun.mail</groupId >
            <artifactId >javax.mail</artifactId>
        </dependency>
```

# Email类
```java
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * @author zhy
 * @date 2019/5/6 15:53
 **/
@Configuration
public class Email {
    private String encoding = "UTF-8";

    public JavaMailSenderImpl createMailSender(String host, Integer port, String username, String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding(encoding);
        return sender;
    }

    public JavaMailSenderImpl createAuthMailSender(String host, Integer port, String username, String password) {
        JavaMailSenderImpl sender = createMailSender(host, port, username, password);
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "true");
        sender.setJavaMailProperties(props);
        Session session = Session.getInstance(props, new Authenticator() {
            // 设置认证账户信息
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        // 设置为debug模式, 可以打印详细的邮件发送日志
        session.setDebug(true);
        sender.setSession(session);
        return sender;
    }

    public MimeMessageHelper createMimeMessageHelper(MimeMessage mimeMessage, String from, String[] to, String subject, String text) throws Exception {
        MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, encoding);
        messageHelper.setFrom(from); // 发件人
        messageHelper.setTo(to); // 收件人
        messageHelper.setSubject(subject); // 主题
        messageHelper.setText(text); // 内容
        return messageHelper;
    }

    public MimeMessageHelper createMimeMessageHelper(MimeMessage mimeMessage, String from, String[] to, String[] cc, String subject, String text) throws Exception {
        MimeMessageHelper messageHelper = createMimeMessageHelper(mimeMessage, from, to, subject, text);
        messageHelper.setCc(cc); // 抄送
        return messageHelper;
    }
}
```

# 测试类
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class StAlarmNotifyApplicationTests {

    @Autowired
    private Email email;

    @Test
    public void sendEmail() throws Exception {
        JavaMailSenderImpl mailSender = email.createMailSender("smtp.exmail.qq.com", -1, "xxx@foxmail.com", "password");
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String to = "yyy@foxmail.com";
        String cc = "zzz@163.com";
        MimeMessageHelper messageHelper = email.createMimeMessageHelper(mimeMessage,"xxx@foxmail.com", to.split(","), cc.split(","), "a", "b");
        String bcc = "ccc@163.com";
        messageHelper.setBcc(bcc.split(",")); // 秘密抄送
        mailSender.send(mimeMessage);
    }

    @Test
    public void sendAuthEmail() throws Exception {
        JavaMailSenderImpl mailSender = email.createAuthMailSender("smtp.qq.com", -1, "xxx@qq.com", "auth code");
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        String to = "yyy@foxmail.com";
        String cc = "zzz@163.com";
        MimeMessageHelper messageHelper = email.createMimeMessageHelper(mimeMessage,"xxx@qq.com", to.split(","), cc.split(","), "a1", "b1");
        String bcc = "ccc@163.com";
        messageHelper.setBcc(bcc.split(",")); // 秘密抄送
        mailSender.send(mimeMessage);
    }
}
```
