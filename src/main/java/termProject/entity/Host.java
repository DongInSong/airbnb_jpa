package termProject.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import termProject.embeddable.Address;

import javax.persistence.*;

//@org.hibernate.annotations.DynamicUpdate
@Table(name = "host")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Host extends BaseEntity{
    @OneToOne(mappedBy = "host")
    @JoinColumn(name = "house_id")
    private House house;

    private Host(String name, Address address) {
        super(name, address);
    }

    public static Host create(String name, Address address) {
        return new Host(name, address);
    }

    @Override
    public String toString() {
        return "호스트 아이디: " + getId() + "\n" +
                "호스트 이름: " + getName() + "\n";
    }
}
