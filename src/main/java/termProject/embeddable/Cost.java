package termProject.embeddable;

import lombok.*;

import javax.persistence.*;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Cost {
    @Column(name = "weekdays")
    private Integer weekdays;
    @Column(name = "weekend")
    private Integer weekdend;
    @Column(name = "weekdays_discount")
    private Integer weekdays_discount;
    @Column(name = "weekend_discount")
    private Integer weekend_discount;

    public static Cost create(Integer weekdays, Integer weekdend) {
        return new Cost(weekdays, weekdend, weekdays, weekdend);
    }
}
