package termProject.embeddable;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import java.util.Arrays;
import java.util.List;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Facility {
    @Column(name = "basic")
    private String basic;
    @Column(name = "preference")
    private String preference;
    @Column(name = "safety")
    private String safety;
    @Column(name = "accessibility")
    private String accessibility;
    @ElementCollection
    private static List<String> basics = Arrays.asList(
            "화장지",
            "손과 몸을 씻을 수 있는 비누",
            "게스트당 수건 1장",
            "침대당 침구 1세트",
            "게스트당 베개 1개",
            "청소용품");
    @ElementCollection
    private static List<String> preferences = Arrays.asList(
            "수영장",
            "와이파이",
            "주방",
            "무료 주차 공간",
            "자쿠지",
            "세탁기 또는 건조기",
            "에어컨 또는 난방",
            "셀프 체크인",
            "노트북 작업 공간",
            "반려동물 동반 가능");
    @ElementCollection
    private static List<String> safeties = Arrays.asList(
            "안전 편의시설",
            "일산화탄소 경보기",
            "화재 경보기",
            "소화기",
            "구급상자",
            "비상 대피 안내도 및 현지 응급 구조기관 번호"
);
    @ElementCollection
    private static List<String> accessibilities = Arrays.asList(
            "접근성 편의시설",
            "계단이나 단차가 없는 현관",
            "폭 32인치/81cm 이상의 넓은 출입구",
            "폭 36인치/91cm 이상의 넓은 복도",
            "휠체어 접근 가능 욕실"
    );

    public static Facility create(int basic, int preference, int safety, int accessibility) {
        return new Facility(basics.get(basic), preferences.get(preference), safeties.get(safety), accessibilities.get(accessibility));
    }
}
