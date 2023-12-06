package termProject.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import termProject.type.DiscountType;
import termProject.type.HouseType;
import termProject.embeddable.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Table(name = "house")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class House extends BaseEntity{
    @Column(name = "description")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "housetype")
    private HouseType houseType;
    @Embedded
    private AcceptanceInfo acceptanceInfo;
    @Embedded
    private DiscountPolicy discountPolicy;
    @Embedded
    private Facility facility;
    @Embedded
    private Cost cost;

    @OneToOne
    @JoinColumn(name = "host_id")
    private Host host;
    @OneToMany(mappedBy = "house")
    private List<Review> reviewList = new ArrayList<Review>();
    @OneToMany(mappedBy = "house")
    private List<Reservation> reservationList = new ArrayList<Reservation>();

    private House(String name,
                  Address address,
                  Host host,
                  String description,
                  HouseType houseType,
                  AcceptanceInfo acceptanceInfo,
                  DiscountPolicy discountPolicy,
                  Facility facility,
                  Cost cost) {
        super(name, address);
        this.host = host;
        this.description = description;
        this.houseType = houseType;
        this.acceptanceInfo = acceptanceInfo;
        this.discountPolicy = discountPolicy;
        this.facility =facility;
        this.cost = cost;
    }

    @Override
    public String toString() {
        String discountInfo;
        String weekdaysPrice = this.getCost().getWeekdays() + " -> " + this.getCost().getWeekdays_discount();
        String weekendPrice = this.getCost().getWeekdend() + " -> " + this.getCost().getWeekend_discount();
        if(this.getDiscountPolicy().getDiscountType().equals(DiscountType.VARIABLE)) {
            discountInfo = "정량 할인 " + this.getDiscountPolicy().getVariableRate() + "원";
        } else if(this.getDiscountPolicy().getDiscountType().equals(DiscountType.FIXED)) {
            discountInfo = "정률 할인 " + this.getDiscountPolicy().getFixedRate() + "%";
        } else {
            discountInfo = "할인 없음";
            weekdaysPrice = this.getCost().getWeekdays() + "";
            weekendPrice = this.getCost().getWeekdend() + "";
        }

        return "----------------------------------------\n" +
                "숙소명: " + this.getName() + "\n" +
                "유형: " + this.getHouseType() + "\n" +
                "침실 수: " + this.getAcceptanceInfo().getRoom() + "명" + "\n" +
                "최대 인원: " + this.getAcceptanceInfo().getPeople() + "명" + "\n" +
                "할인: " + discountInfo + "\n" +
                "평일 가격: " + weekdaysPrice + "\n" +
                "주말 가격: " + weekendPrice + "\n" +
                "평균 리뷰: " + String.format("%.1f",getAverageReview()) + "\n" +
                "----------------------------------------\n";
    }

    public double getAverageReview() {
        double total = 0;
        for (Review review : this.reviewList) {
            total += review.getStar();
        }
        return total/this.reviewList.size();
    }

    public String printCost() {
        String discountInfo;
        String weekdaysPrice = this.getCost().getWeekdays() + " -> " + this.getCost().getWeekdays_discount();
        String weekendPrice = this.getCost().getWeekdend() + " -> " + this.getCost().getWeekend_discount();
        if(this.getDiscountPolicy().getDiscountType().equals(DiscountType.VARIABLE)) {
            discountInfo = "정량 할인 " + this.getDiscountPolicy().getVariableRate() + "원";
        } else if(this.getDiscountPolicy().getDiscountType().equals(DiscountType.FIXED)) {
            discountInfo = "정률 할인 " + this.getDiscountPolicy().getFixedRate() + "%";
        } else {
            discountInfo = "할인 없음";
            weekdaysPrice = this.getCost().getWeekdays() + "";
            weekendPrice = this.getCost().getWeekdend() + "";
        }
        return "\n----------------------------------------\n" +
                "숙소명: " + this.getName() + "\n" +
                "할인: " + discountInfo + "\n" +
                "평일 가격: " + weekdaysPrice + "\n" +
                "주말 가격: " + weekendPrice + "\n" +
                "----------------------------------------\n";
    }

    public static House create(String name,
                               Address address,
                               Host host,
                               String description,
                               HouseType houseType,
                               AcceptanceInfo acceptanceInfo,
                               DiscountPolicy discountPolicy,
                               Facility facility,
                               Cost cost) {
        return new House(name, address, host, description, houseType, acceptanceInfo, discountPolicy, facility, cost);
    }

    public void applyDiscountPolicy(DiscountType discountType, Integer value) {
        if(discountType.equals(DiscountType.FIXED) && value < 100) {
            Integer weekdays = this.getCost().getWeekdays();
            Integer weekend = this.getCost().getWeekdend();
            this.cost.setWeekdays_discount(weekdays - (weekdays * value / 100));
            this.cost.setWeekend_discount(weekend - (weekend * value / 100));
            this.discountPolicy = DiscountPolicy.create(DiscountType.FIXED, value, 0);
        }
        else if(discountType.equals(DiscountType.VARIABLE)) {
            Integer weekdays = this.getCost().getWeekdays();
            Integer weekend = this.getCost().getWeekdend();
            this.cost.setWeekdays_discount(weekdays - value);
            this.cost.setWeekend_discount(weekend - value);
            this.discountPolicy = DiscountPolicy.create(DiscountType.VARIABLE, 0, value);
        }
        else if(discountType.equals(DiscountType.NONE)) {
            this.cost.setWeekdays_discount(this.getCost().getWeekdays());
            this.cost.setWeekend_discount(this.getCost().getWeekdend());
            this.discountPolicy = DiscountPolicy.create(DiscountType.NONE, 0, 0);
        }
    }
}
