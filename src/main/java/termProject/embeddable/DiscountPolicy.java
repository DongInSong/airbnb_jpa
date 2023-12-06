package termProject.embeddable;

import lombok.*;
import termProject.type.DiscountType;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class DiscountPolicy {
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DiscountType discountType;
    @Column(name = "fixed")
    private Integer FixedRate;
    @Column(name = "variable")
    private Integer variableRate;

    public static DiscountPolicy create(DiscountType discountType, Integer fixedRate, Integer variableRate) {
        return new DiscountPolicy(discountType, fixedRate, variableRate);
    }
}
