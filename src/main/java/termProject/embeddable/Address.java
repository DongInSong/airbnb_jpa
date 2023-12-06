package termProject.embeddable;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Address {
    @Column(name = "city")
    private String city;
    @Column(name = "street")
    private String street;
    @Column(name = "zipcode")
    private String zipcode;

    public static Address create(String city, String street, String zipcode) {
        return new Address(city, street, zipcode);
    }
}
