package termProject.entity;

import com.sun.istack.Nullable;
import lombok.*;

import javax.persistence.*;

@Table(name = "review")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "star")
    private Integer star;
    @Column(name = "comment")
    private String comment;
    @ManyToOne
    @JoinColumn(name = "house_id")
    private House house;
    @ManyToOne
    @JoinColumn(name = "guest_id")
    private Guest guest;
    @ManyToOne
    @JoinColumn(name = "reserve_id")
    private Reservation reservation;

    private Review(Integer star, String comment){
        this.star = star;
        this.comment = comment;
    }

    public static Review create(Integer star, String comment) {
        return new Review(star, comment);
    }

    public void setReview(Guest guest, House house, Reservation reservation) {
        if(this.guest != null) {
            this.guest.getReviewList().remove(this);
        }
        if(this.house != null) {
            this.house.getReviewList().remove(this);
        }
        if(this.reservation != null) {
            this.reservation.getReviewList().remove(this);
        }
        this.guest = guest;
        this.house = house;
        this.reservation = reservation;

        guest.getReviewList().add(this);
        house.getReviewList().add(this);
        reservation.getReviewList().add(this);
    }

    @Override
    public String toString() {
        return "게스트 이름: " + getGuest().getName() + "\n" +
                "별점: " + getStar() + "\n" +
                "코멘트: " + getComment() + "\n";
    }
}
