package termProject.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import termProject.embeddable.Address;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "guest")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Guest extends BaseEntity{
    @OneToMany(mappedBy = "guest")
    private List<Reservation> reservationList = new ArrayList<Reservation>();
    @OneToMany(mappedBy = "guest")
    private List<Review> reviewList = new ArrayList<Review>();
    private Guest(String name, Address address) {
        super(name, address);
    }

    public static Guest create(String name, Address address) {
        return new Guest(name, address);
    }

    @Override
    public String toString() {
        return "게스트 아이디: " + getId() + "\n" +
                "게스트 이름: " + getName() + "\n";
    }
}
