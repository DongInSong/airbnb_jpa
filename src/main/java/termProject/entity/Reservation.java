package termProject.entity;

import com.sun.istack.Nullable;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Table(name = "reservation")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "checkin")
    private Date checkin;
    @Column(name = "checkout")
    private Date checkout;
    @Column(name = "numOfPeople")
    private Integer numOfPeople;
    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;
    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;
    @OneToMany(mappedBy = "reservation")
    private List<Review> reviewList = new ArrayList<Review>();

    private Reservation(Date checkin, Date checkout, Integer numOfPeople) {
        this.checkin = checkin;
        this.checkout = checkout;
        this.numOfPeople = numOfPeople;
    }

    public static Reservation create(Date checkin, Date checkout, Integer numOfPeople) {return new Reservation(checkin, checkout, numOfPeople);}

    public void setReservation(Guest guest, House house){
        if(this.guest != null) {
            this.guest.getReservationList().remove(this);
        }
        if(this.house != null) {
            this.house.getReservationList().remove(this);
        }
        this.guest = guest;
        this.house = house;
        guest.getReservationList().add(this);
        house.getReservationList().add(this);
    }

    @Override
    public String toString() {
        getCheckin().setYear(2023 - 1900);
        getCheckout().setYear(2023 - 1900);
        return "\n----------------------------------------\n" +
                "예약 번호: " + getId() + "\n" +
                "게스트 이름: " + getGuest().getName() + "\n" +
                "숙소명: " + getHouse().getName() + "\n" +
                "예약 인원: " + getNumOfPeople() + "명" + "\n" +
                "체크인: " + getCheckin() + "\n" +
                "체크아웃: " + getCheckout() + "\n" +
                "----------------------------------------\n";
    }
}
