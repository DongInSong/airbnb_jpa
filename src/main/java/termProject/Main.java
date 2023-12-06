package termProject;
import com.querydsl.jpa.impl.JPAQueryFactory;

import javax.persistence.*;

public class Main {

    static EntityManagerFactory emf = Persistence.createEntityManagerFactory("termProject");
    public static void main(String[] args) {
        Controller controller = new Controller(emf);
        controller.run();
    }
}