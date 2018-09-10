import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Email email = new Email();
        email.getTo().add("123456@gmail.com");

        email.setSubject("Test Mail from Java Jazzle");
        email.setText("Hi, This is a test email from Java Jazzle");

        Future<?> future = emailService.send(email);
        try {
            future.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }


}
