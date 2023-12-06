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
public class AcceptanceInfo {
    @Column(name = "room")
    private Integer room;
    @Column(name = "bath")
    private Integer bath;
    @Column(name = "people")
    private Integer people;

    public static AcceptanceInfo create(Integer room, Integer bath, Integer people) {
        return new AcceptanceInfo(room, bath, people);
    }
}
