package termProject.entity;

import lombok.*;
import termProject.embeddable.Address;

import javax.persistence.*;

@Getter
@Setter
@ToString
@MappedSuperclass
@NoArgsConstructor
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    @Embedded
    private Address address;

    public BaseEntity(String name, Address address) {
        this.name = name;
        this.address = address;
    }
}
