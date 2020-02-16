package capstone.rt04.retailbackend.services;

import capstone.rt04.retailbackend.entities.Customer;
import capstone.rt04.retailbackend.entities.ShoppingCart;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class TimerService {

    private final CustomerService customerService;
    private final JavaMailSender javaMailSender;

    private final Integer unattendedTimeLimit = 4;

    public TimerService(CustomerService customerService, JavaMailSender javaMailSender) {
        this.customerService = customerService;
        this.javaMailSender = javaMailSender;
    }

    // in milliseconds
    @Scheduled(fixedRate = 60*60*1000, initialDelay = 10000)
    public void checkForUnattendedShoppingCarts() {
        System.out.println("*********** TIMER ************");
        List<Customer> allCustomers = customerService.retrieveAllCustomers();

        for (Customer customer : allCustomers){
            ShoppingCart onlineShoppingCart = customer.getOnlineShoppingCart();

            // only applied to non-empty shopping carts
            if (!onlineShoppingCart.getShoppingCartItems().isEmpty()){
                Timestamp lastUpdatedTime = onlineShoppingCart.getLastUpdated();
                Timestamp threshold = new Timestamp(lastUpdatedTime.getTime() + TimeUnit.HOURS.toMillis(unattendedTimeLimit));
                if (threshold.before(new Timestamp(System.currentTimeMillis()))){
                    //e.g. if last updated time + 4h is before now, means haven't updated in at least 4h
                    String email = customer.getEmail();
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setTo(email);
                    msg.setSubject("Your shopping cart is waiting!");
                    msg.setText("Hello! We noticed you have yet to checkout your shopping cart items! " +
                                    "Click here to continue shopping!");
                    javaMailSender.send(msg);
                }
            }
        }
    }
}
